package com.example.batches.assetmanager;

import com.example.batches.PlutoBatchUtils;
import com.example.pluto.PlutoConstants;
import com.example.pluto.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssetManagerTasks extends PlutoBatchUtils {

    public static Logger LOG = LoggerFactory.getLogger(AssetManagerTasks.class);

    private static final MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);

    public static void balance() {
        List<BasketTO> baskets = getBaskets();
        Map<String, SpotTO> spots = getSpots();
        List<PositionTO> positions = getCurrentPositions();
        List<PositionTO> positionsByDesign = getPositionsByDesign(baskets, spots, getEquivalentSumByBasket(positions, spots));

        double threshold = 0.05;

        Map<PositionTO, BigDecimal> deviations = filterDeviations(getDeviations(positions, positionsByDesign), threshold, positions, spots);
        submitTradesAndUpdatePositions(buildTrades(deviations, spots));
    }

    /**
     * Builds needed trades to undo deviations
     * @param deviation Map<PositionTO, BigDecimal>
     * @param spots Map<String, SpotTO> spots
     * @return Map<Long, List<TradeTO>>
     */
    protected static Map<Long, List<TradeTO>> buildTrades(Map<PositionTO, BigDecimal> deviation, Map<String, SpotTO> spots) {
        LOG.info("Building trades");
        Map<Long, List<TradeTO>> tradesByBaskets = new HashMap<>();
        for (PositionTO k : deviation.keySet()) {
            TradeTO trade = buildTrade(k, deviation.get(k), spots.get(k.getCurrency()));
            if (!tradesByBaskets.containsKey(k.getBasket().getId())) {
                tradesByBaskets.put(k.getBasket().getId(), new ArrayList<>());
            }
            if (!"BTCBTC".equals(trade.getPair())) {
                List<TradeTO> pairTrades = tradesByBaskets.get(k.getBasket().getId());
                if (PlutoConstants.minAmounts.get(trade.getBase()) > Math.abs(trade.getAmount())) {
                    Double recalculated = PlutoConstants.minAmounts.get(trade.getBase()) * 2.0 + trade.getAmount();
                    trade.setAmount(recalculated);
                    TradeTO complementary = buildTrade(k, BigDecimal.valueOf(PlutoConstants.minAmounts.get(trade.getBase()) * 2.0), spots.get(k.getCurrency()));
                    pairTrades.add(complementary);
                }
                pairTrades.add(trade);
            }
        }
        return tradesByBaskets;

    }

    /**
     * Build one trade from parameters
     * @param deviation BigDecimal
     * @param spot SpotTO
     * @param k PositionTO
     * @return TradeTO
     */
    private static TradeTO buildTrade(PositionTO k, BigDecimal deviation, SpotTO spot) {
        String pair = k.getCurrency() + "BTC";
        Double amount = - deviation.doubleValue();
        Double priceAux = amount < 0.0 ? spot.getBid() : spot.getOffer();
        BigDecimal price = BigDecimal.valueOf(priceAux);
        return new TradeTO(pair, price, amount);
    }

    /**
     *
     * @param tradesByBaskets
     */
    private static void submitTradesAndUpdatePositions(Map<Long, List<TradeTO>> tradesByBaskets) {
        tradesByBaskets.forEach((k, v) -> {
            List<TradeTO> sells = v.stream().filter(t -> t.getAmount() < 0).collect(Collectors.toList());
            List<TradeTO> buys = v.stream().filter(t -> t.getAmount() > 0).collect(Collectors.toList());
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
                Thread.sleep(15000);
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
     * Filters deviations map to keep only the ones which must be corrected attending to the threshold
     * @param deviations deviation values for each position
     * @param threshold this limits which positions must be corrected and which not to (x per one, 100% = 1)
     * @param spots map with the price of each currency over the base currency (BTC)
     * @return deviations bigger than threshold
     */
    public static Map<PositionTO, BigDecimal> filterDeviations(Map<PositionTO, BigDecimal> deviations, double threshold, List<PositionTO> positions, Map<String, SpotTO> spots) {
        boolean proceed = false;
        Map<String, BigDecimal> basketEquivalentSums = getEquivalentSumByBasket(positions, spots);
        Map<PositionTO, BigDecimal> filtered = new HashMap<>();
        for (PositionTO p : deviations.keySet()) {
            BigDecimal price = BigDecimal.valueOf(spots.get(p.getCurrency()).getMid());
            BigDecimal deviationBtcEq = deviations.get(p).multiply(price);
            BigDecimal deviationOverSum = deviationBtcEq.divide(basketEquivalentSums.get(p.getBasket().getLabel()), mathContext);
            filtered.put(p, deviations.get(p));
            if (deviationOverSum.abs().doubleValue() > threshold) {
                proceed = true;
            }
        }
        return proceed ? filtered : new HashMap<>(0);
    }

    /**
     * Calculates the difference between what is and what should be for each position
     * @param positions actual positions
     * @param positionsByDesign positions that should be caring the basket design and current spots
     * @return the excess in each position
     */
    public static Map<PositionTO, BigDecimal> getDeviations(List<PositionTO> positions, List<PositionTO> positionsByDesign) {
        Map<PositionTO, BigDecimal> deviations = new HashMap<>();
        Map<String, Map<String, Double>> pos = positionsAsMap(positions);
        Map<String, Map<String, Double>> posByDesign = positionsAsMap(positionsByDesign);
        positions.forEach(p -> {
            BigDecimal minuendo = getPositionValue(pos, p);
            BigDecimal sustraendo = getPositionValue(posByDesign, p);
            BigDecimal diferencia = minuendo.add(sustraendo.negate(), mathContext).setScale(10, RoundingMode.HALF_UP);
            if (BigDecimal.ZERO.setScale(10, RoundingMode.HALF_UP).compareTo(diferencia) != 0) {
                deviations.put(p, diferencia);
            }
        });
        return deviations;
    }

    /**
     * Position on currencies that does not exist in our system are considered zero
     * @param positionsMap positions ordered by basket and currency
     * @param position position being analyzed
     * @return quantity of analyzed position
     */
    public static BigDecimal getPositionValue(Map<String, Map<String, Double>> positionsMap, PositionTO position) {
        String label = position.getBasket() != null ? position.getBasket().getLabel() : null;
        String ccy = position.getCurrency();

        Double value = 0.0;
        if (label != null && ccy != null && positionsMap.get(label) != null && positionsMap.get(label).get(ccy) != null) {
            value = positionsMap.get(label).get(ccy);
        } else {
            LOG.error("Void value for position: " + position);
        }
        return new BigDecimal(value, mathContext).setScale(10, RoundingMode.HALF_UP);
    }

    /**
     * Calculates what the positions should be to fit into our designed weights with current spots
     * @param baskets designed baskets with the weights
     * @param spots map with the price of each currency over the base currency (BTC)
     * @param sums addition in base currency (BTC) of all the positions in a basket
     * @return the positions that we should have
     */
    public static List<PositionTO> getPositionsByDesign(List<BasketTO> baskets, Map<String, SpotTO> spots, Map<String, BigDecimal> sums) {
        List<PositionTO> wished = new ArrayList<>();
        if (spots == null || spots.isEmpty()) {
            LOG.error("Could not get wished positions by design due to lack of spots");
            return wished;
        }

        for (BasketTO basket : baskets) {
            BigDecimal sum = sums.get(basket.getLabel());

            for (WeightTO weight : basket.getWeights()) {
                BigDecimal price = BigDecimal.valueOf(spots.get(weight.getCurrency()).getMid());
                BigDecimal w = new BigDecimal(weight.getWeight(), mathContext);
                LOG.info("[weight]: "+weight+"[sum]: "+sum+"[price]: "+price);
                BigDecimal q = w.multiply(sum).divide(price, mathContext).setScale(10, RoundingMode.HALF_UP);
                PositionTO position = new PositionTO(null, basket, weight.getCurrency(), q.doubleValue());

                wished.add(position);
            }
        }
        return wished;
    }

    /**
     * Returns the base currency (BTC) equivalent of all positions in a basket added
     * @param positions list with each position (each currency in each basket)
     * @param spots map with the price of each currency over the base currency (BTC)
     * @return
     */
    public static Map<String, BigDecimal> getEquivalentSumByBasket(List<PositionTO> positions, Map<String, SpotTO> spots) {
        Map<String, Map<String, BigDecimal>> equivalent = turnPositionsIntoEquivalent(positions, spots);

        Map<String, BigDecimal> eq = new HashMap<>(equivalent.size());
        equivalent.keySet().forEach(
                k -> eq.put(k, getEquivalentSum(equivalent.get(k)).setScale(10, RoundingMode.HALF_UP))
        );
        return eq;
    }

    private static BigDecimal getEquivalentSum(Map<String, BigDecimal> equivalents) {
        BigDecimal sum = new BigDecimal(0.0, mathContext);
        for (BigDecimal v : equivalents.values()){
            if (v == null) {
                throw new IllegalArgumentException("Received null position quantity");
            }
            sum = sum.add(v);
        }
        return sum.setScale(10, RoundingMode.HALF_UP);
    }

    /**
     * Calculate how much each position is in the base currency (BTC), in order to compare positions easily
     * @param positions list with each position (each currency in each basket)
     * @param spots map with the price of each currency over the base currency (BTC)
     * @return Map with values in base currency for each alt currency in each basket
     */
    public static Map<String, Map<String, BigDecimal>> turnPositionsIntoEquivalent(List<PositionTO> positions, Map<String, SpotTO> spots) {
        Map<String, Map<String, BigDecimal>> equivalent = new HashMap<>();
        spots.put("BTC", new SpotTO("BTCBTC", 1.0));      // Base for equivalency
        for (PositionTO p : positions) {
            BigDecimal quantity = new BigDecimal(p.getQuantity(), mathContext);
            BigDecimal price = BigDecimal.valueOf(spots.get(p.getCurrency()).getMid());
            BigDecimal eq;
            if (quantity == null || price == null) {
                LOG.warn("Quantity or price null for pair " + p.getCurrency());
                eq = new BigDecimal(0.0, mathContext);
            } else {
                eq = quantity.multiply(price, mathContext).setScale(10, RoundingMode.HALF_UP);
            }

            equivalent.putIfAbsent(p.getBasket().getLabel(), new HashMap<>());
            equivalent.get(p.getBasket().getLabel()).put(p.getCurrency(), eq.setScale(10, RoundingMode.HALF_UP));
        }
        return equivalent;
    }

    /**
     * Transforms a list of complete SpotTO objects into Map: name, spot
     * @param spots list to be transformed
     * @return Map with basic info from the list
     */
    public static Map<String, SpotTO> spotsAsMap(List<SpotTO> spots) {
        Map<String, SpotTO> res = new HashMap<>();
        if (spots == null) {
            return res;
        }
        spots.forEach(s -> {
            if (s.getInstrument() != null && s.getInstrument().contains("BTC")){
                res.putIfAbsent(s.getInstrument().replace("BTC", ""), s);
            }
        });

        res.putIfAbsent("BTC", new SpotTO("BTCBTC", 1.0));
        return res;
    }

    /**
     * Transforms a list of complete PositionTO objects into Map: name, spot
     * @param positions list to be transformed
     * @return Map with basic info from the list
     */
    public static Map<String, Map<String, Double>> positionsAsMap(List<PositionTO> positions) {
        Map<String, Map<String, Double>> res = new HashMap<>();
        if (positions == null) {
            return res;
        }
        positions.forEach(p -> {
            String currency = p.getCurrency();
            Double quantity = p.getQuantity();
            if (res.containsKey(p.getBasket().getLabel())) {
                res.get(p.getBasket().getLabel()).put(currency, quantity);
            } else {
                Map<String, Double> value = new HashMap<>();
                value.put(currency, quantity);
                res.put(p.getBasket().getLabel(), value);
            }
        });
        return res;
    }

}
