import com.pluto.entities.BasketTO;
import com.pluto.entities.SpotEntity;
import com.pluto.entities.TradeTO;
import com.pluto.entities.WeightTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public abstract class Calculator {

    public static Logger logger = LoggerFactory.getLogger(Calculator.class);

    abstract Map<String, SpotEntity> getSpots(Set<String> currencies);
    abstract Map<String, BigDecimal> getPositions();

    public static Map<String, BigDecimal> getWeights() {
        Optional<BasketTO> basket = RESTClient.getBaskets().stream().filter(b -> Long.compare(1L, b.getId()) == 0).findFirst();
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
            if ("BTC".equals(c)) {
                equivalent = equivalent.add(positions.get(c));
            } else if (spots.get(c) != null && p != null) {
                equivalent = equivalent.add(p.multiply(spots.get(c).getBid()));
            }
        }
        return equivalent;
    }

    protected static Map<String, BigDecimal> getUpperBounds(BigDecimal btc, Map<String, SpotEntity> spots, Map<String, BigDecimal> weights) {
        Map<String, BigDecimal> bounds = new HashMap<>();
        weights.forEach((ccy, w) -> {
            //logger.debug("Getting lower bound for " + ccy);
            if (spots.get(ccy) != null) {
                BigDecimal spot = spots.get(ccy).getBid();
                bounds.put(ccy, w.multiply(btc).divide(spot, RoundingMode.HALF_UP));
            }
        });
        return bounds;
    }

    protected static Map<String, BigDecimal> getLowerBounds(BigDecimal btc, Map<String, SpotEntity> spots, Map<String, BigDecimal> weights) {
        Map<String, BigDecimal> bounds = new HashMap<>();
        weights.forEach((ccy, w) -> {
            //logger.debug("Getting lower bound for " + ccy);
            if (spots.get(ccy) != null) {
                BigDecimal spot = spots.get(ccy).getOffer();
                bounds.put(ccy, w.multiply(btc).divide(spot, RoundingMode.HALF_UP));
            }
        });
        return bounds;
    }

    protected static List<TradeTO> getSellingTrades(Map<String, BigDecimal> qBid, Map<String, BigDecimal> positions, BigDecimal threshold) {
        return getTrades(qBid, positions, threshold, 1);
    }

    protected static List<TradeTO> getBuyingTrades(Map<String, BigDecimal> qAsk, Map<String, BigDecimal> positions, BigDecimal threshold) {
        return getTrades(qAsk, positions, threshold, -1);
    }

    protected static List<TradeTO> getTrades(Map<String, BigDecimal> q, Map<String, BigDecimal> positions, BigDecimal threshold, int side) {
        List<TradeTO> trading = new LinkedList<>();
        positions.forEach((ccy, p) -> {
            if (q.get(ccy) != null) {
                BigDecimal diff = q.get(ccy).subtract(p);
                if (diff.compareTo(threshold) == side && !"BTC".equals(ccy)) {
                    trading.add(new TradeTO(ccy+"BTC", diff));
                }
            }
        });
        logger.debug("Trades built: " + trading.toString());
        return trading;
    }
}
