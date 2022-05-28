package com.example.batches.assetmanager;

import com.example.batches.PlutoBatchUtils;
import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;
import com.example.pluto.entities.WeightTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

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
        List<TradeTO> trading = new LinkedList<>();
        positions.forEach((ccy, p) -> {
            if (q.get(ccy) != null) {
                BigDecimal diff = q.get(ccy).subtract(p);
                if (diff.compareTo(threshold) == side && !"BTC".equals(ccy)) {
                    trading.add(new TradeTO(ccy+"BTC", diff));
                }
            }
        });
        return trading;
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
