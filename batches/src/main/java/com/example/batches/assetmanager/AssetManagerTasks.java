package com.example.batches.assetmanager;

import com.example.batches.PlutoBatchUtils;
import com.example.pluto.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class AssetManagerTasks extends PlutoBatchUtils {

    public static Logger LOG = LoggerFactory.getLogger(AssetManagerTasks.class);

    private static final MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);

    public static void balance() {
        BigDecimal threshold = BigDecimal.valueOf(0.01);    // Desviaciones del 1%
        Map<String, BigDecimal> positions = getPositions();
        Map<String, BigDecimal> weights = getWeights();

        Set<String> currencies = new HashSet<>(2);
        currencies.addAll(weights.keySet());
        currencies.addAll(positions.keySet());

        Map<String, SpotEntity> spots = getSpots(currencies);
        BigDecimal btc = getBTCEquivalent(positions, spots);
        Map<String, BigDecimal> qBid = getUpperBounds(btc, spots, weights);
        Map<String, BigDecimal> qAsk = getLowerBounds(btc, spots, weights);
        List<TradeTO> trading = getSellingTrades(qBid, positions, threshold);
        trading.addAll(getBuyingTrades(qAsk, positions, threshold));
        callTrader(trading);
    }

    private static List<TradeTO> getSellingTrades(Map<String, BigDecimal> qBid, Map<String, BigDecimal> positions, BigDecimal threshold) {
        return getTrades(qBid, positions, threshold, 1);
    }

    private static List<TradeTO> getBuyingTrades(Map<String, BigDecimal> qAsk, Map<String, BigDecimal> positions, BigDecimal threshold) {
        return getTrades(qAsk, positions, threshold, -1);
    }

    private static List<TradeTO> getTrades(Map<String, BigDecimal> q, Map<String, BigDecimal> positions, BigDecimal threshold, int side) {
        List<TradeTO> selling = new LinkedList<>();
        positions.forEach((ccy, p) -> {
            if (q.get(ccy) != null) {
                BigDecimal diff = q.get(ccy).subtract(p);
                if (diff.compareTo(threshold) == side && !"BTC".equals(ccy)) {
                    selling.add(new TradeTO(ccy+"BTC", diff));
                }
            }
        });
        return selling;
    }

    private static Map<String, BigDecimal> getUpperBounds(BigDecimal btc, Map<String, SpotEntity> spots, Map<String, BigDecimal> weights) {
        Map<String, BigDecimal> bounds = new HashMap<>();
        weights.forEach((ccy, w) -> bounds.put(ccy, w.multiply(btc).divide(spots.get(ccy).getBid(), RoundingMode.HALF_UP)));
        return bounds;
    }

    private static Map<String, BigDecimal> getLowerBounds(BigDecimal btc, Map<String, SpotEntity> spots, Map<String, BigDecimal> weights) {
        Map<String, BigDecimal> bounds = new HashMap<>();
        weights.forEach((ccy, w) -> bounds.put(ccy, w.multiply(btc).divide(spots.get(ccy).getOffer(), RoundingMode.HALF_UP)));
        return bounds;
    }

    private static Map<String, BigDecimal> getWeights() {
        Optional<BasketTO> basket = getBaskets().stream().filter(b -> Long.compare(1L, b.getId()) == 0).findFirst();
        List<WeightTO> w = basket.isPresent() ? basket.get().getWeights() : new ArrayList<>();
        Map<String, BigDecimal> weights = new HashMap<>(w.size());
        w.forEach(i -> weights.put(i.getCurrency(), BigDecimal.valueOf(i.getWeight())));
        return weights;
    }

    /**
     * Builds needed trades to undo deviations
     * @param deviation Map<PositionTO, BigDecimal>
     * @param spots Map<String, SpotTO> spots
     * @return Map<Long, List<TradeTO>>
     */
    protected static Map<Long, List<TradeTO>> buildTrades(Map<PositionTO, BigDecimal> deviation, Map<String, SpotEntity> spots) {
        LOG.info("Building trades");
        Map<Long, List<TradeTO>> tradesByBaskets = new HashMap<>();
        for (PositionTO k : deviation.keySet()) {
            TradeTO trade = buildTrade(k, deviation.get(k), spots.get(k.getCurrency()));
            if (!tradesByBaskets.containsKey(k.getBasket().getId())) {
                tradesByBaskets.put(k.getBasket().getId(), new ArrayList<>());
            }
            if (!"BTCBTC".equals(trade.getPair())) {
                List<TradeTO> pairTrades = tradesByBaskets.get(k.getBasket().getId());
                pairTrades.add(trade);
            }
        }
        LOG.debug("Trades built: " + tradesByBaskets);
        return tradesByBaskets;
    }

    /**
     * Build one trade from parameters
     * @param deviation BigDecimal
     * @param spot SpotTO
     * @param k PositionTO
     * @return TradeTO
     */
    private static TradeTO buildTrade(PositionTO k, BigDecimal deviation, SpotEntity spot) {
        String pair = k.getCurrency() + "BTC";
        BigDecimal amount = deviation.negate();
        // Price da igual porque usaremos operaciones market, no limit
        Double priceAux = amount.doubleValue() < 0.0 ? spot.getBid().doubleValue() : spot.getOffer().doubleValue();
        BigDecimal price = BigDecimal.valueOf(priceAux);
        return new TradeTO(pair, price, amount);
    }

    /**
     *
     * @param tradesByBaskets
     */
    private static void submitTradesAndUpdatePositions(Map<Long, List<TradeTO>> tradesByBaskets) {
        tradesByBaskets.forEach((k, v) -> {
            List<TradeTO> sells = v.stream().filter(t -> BigDecimal.ZERO.compareTo(t.getAmount()) == 1).collect(Collectors.toList());
            List<TradeTO> buys = v.stream().filter(t -> BigDecimal.ZERO.compareTo(t.getAmount()) == -1).collect(Collectors.toList());
            List<TradeTO> filled = new ArrayList<>(v.size());
            filled.addAll(waitToBeFilled(callTrader(sells)));
            filled.addAll(waitToBeFilled(callTrader(buys)));
            updatePositions(k, filled);
        });
    }

    private static List<TradeTO> waitToBeFilled(List<TradeTO> placed) {
        List<TradeTO> filled = filterUnactive(placed);
        if (filled.isEmpty() && placed != null && !placed.isEmpty()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return waitToBeFilled(placed);
        } else {
            updateRecentTrades(filled);
            return filled;
        }
    }

    public static List<TradeTO> filterUnactive(List<TradeTO> placed) {
        List<TradeTO> filtered = new ArrayList<>();
        if (placed == null || placed.isEmpty()) {
            return filtered;
        }

        Map<String, List<TradeTO>> unactive = new HashMap<>();
        for (TradeTO p : placed) {
            String pair = p.getPair();
            if (!unactive.containsKey(pair)) {
                unactive.put(pair, getUnactiveOrders(pair));
            }
            unactive.get(pair).forEach(u -> {
                if (u.getExchangeId() != null && u.getExchangeId().equals(p.getExchangeId())) {
                    u.setId(p.getId());
                    u.setIssuedTimestamp(p.getIssuedTimestamp());
                    filtered.add(u);
                }
            });
        }
        return filtered;
    }

    /**
     * Calculate how much each position is in the base currency (BTC), in order to compare positions easily
     * @param positions list with each position (each currency in each basket)
     * @param spots map with the price of each currency over the base currency (BTC)
     * @return Map with values in base currency for each alt currency in each basket
     */
    public static BigDecimal getBTCEquivalent(Map<String, BigDecimal> positions, Map<String, SpotEntity> spots) {
        BigDecimal equivalent = BigDecimal.ZERO;
        for (String c : positions.keySet()) {
            BigDecimal p = positions.get(c);
            if (spots.get(c) != null && p != null) {
                equivalent = equivalent.add(p.multiply(spots.get(c).getBid()));
            }
        }
        return equivalent;
    }

    /**
     * Transforms a list of complete SpotTO objects into Map: name, spot
     * @param spots list to be transformed
     * @return Map with basic info from the list
     */
    public static Map<String, SpotEntity> spotsAsMap(List<SpotEntity> spots) {
        Map<String, SpotEntity> res = new HashMap<>();
        if (spots == null) {
            return res;
        }
        spots.forEach(s -> {
            if (s.getInstrument() != null && s.getInstrument().contains("BTC")){
                res.putIfAbsent(s.getInstrument().replace("BTC", ""), s);
            }
        });

        res.putIfAbsent("BTC", new SpotEntity("BTCBTC", BigDecimal.ONE));
        return res;
    }

}
