package com.example.batches.assetmanager;

import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.entities.WeightTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetManagerTasks {

    public static Logger LOG = LoggerFactory.getLogger(AssetManagerTasks.class);

    private static MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);

    public static void rebalance() {
        List<BasketTO> baskets = getBaskets();
        Map<String, BigDecimal> spots = getSpots();
        List<PositionTO> positions = getPositions();

        double threshold = 0.05;
        Map<PositionTO, BigDecimal> deviation = getPositionsToUpdate(baskets, spots, positions, threshold);
        submitOrders(deviation);
    }

    private static void submitOrders(Map<PositionTO, BigDecimal> deviation) {
        LOG.info("Submiting orders: " + null);
        // TODO: call to trader service
    }

    private static Map<PositionTO, BigDecimal> getPositionsToUpdate(List<BasketTO> baskets, Map<String, BigDecimal> spots, List<PositionTO> positions, double threshold) {
        Map<BasketTO, BigDecimal> basketEquivalentSums = getEquivalentSumByBasket(positions, spots);
        List<PositionTO> currentWishedPositions = getCurrentWishedPositions(baskets, spots, basketEquivalentSums);
        Map<PositionTO, BigDecimal> deviations = getDeviations(positions, currentWishedPositions);
        Map<PositionTO, BigDecimal> rebalancing = filterDeviations(deviations, threshold, basketEquivalentSums, spots);
        return rebalancing;
    }

    /**
     * Filter deviations map to keep only the ones which must be corrected attending to the threshold
     * @param deviations deviation values for each position
     * @param threshold this limits which positions must be corrected and which not to (x per one, 100% = 1)
     * @param basketEquivalentSums base to calculate deviation as percentage in the basket in order to compare to threshold
     * @param spots map with the price of each currency over the base currency (BTC)
     * @return
     */
    public static Map<PositionTO, BigDecimal> filterDeviations(Map<PositionTO, BigDecimal> deviations, double threshold, Map<BasketTO, BigDecimal> basketEquivalentSums, Map<String, BigDecimal> spots) {
        Map<PositionTO, BigDecimal> filtered = new HashMap<>();
        for (PositionTO p : deviations.keySet()) {
            BigDecimal deviationBtcEq = deviations.get(p).multiply(spots.get(p.getCurrency()));
            BigDecimal deviationOverSum = deviationBtcEq.divide(basketEquivalentSums.get(p.getBasket()), mathContext);
            if (deviationOverSum.abs().doubleValue() > threshold) {
                filtered.put(p, deviations.get(p));
            }
        }
        return filtered;
    }

    /**
     * Calculates the difference between what is and what should be for each position
     * @param positions actual positions
     * @param currentWishedPositions positions that should be caring the basket design and current spots
     * @return the excess in each position
     */
    public static Map<PositionTO, BigDecimal> getDeviations(List<PositionTO> positions, List<PositionTO> currentWishedPositions) {
        Map<PositionTO, BigDecimal> deviations = new HashMap<>();
        Map<BasketTO, Map<String, Double>> pos = positionsAsMap(positions);
        Map<BasketTO, Map<String, Double>> currPos = positionsAsMap(currentWishedPositions);
        positions.forEach(p -> {
            BigDecimal minuendo = new BigDecimal(pos.get(p.getBasket()).get(p.getCurrency()), mathContext).setScale(10, RoundingMode.HALF_UP);
            BigDecimal sustraendo = new BigDecimal(currPos.get(p.getBasket()).get(p.getCurrency()), mathContext).setScale(10, RoundingMode.HALF_UP);
            BigDecimal diferencia = minuendo.add(sustraendo.negate(), mathContext).setScale(10, RoundingMode.HALF_UP);
            deviations.put(p, diferencia);
        });
        return deviations;
    }

    /**
     * Calculates what the positions should be to fit into our designed weights with current spots
     * @param baskets designed baskets with the weights
     * @param spots map with the price of each currency over the base currency (BTC)
     * @param sums sumatory in base currency (BTC) of all the positions in a basket
     * @return the positions that we should have
     */
    public static List<PositionTO> getCurrentWishedPositions(List<BasketTO> baskets, Map<String, BigDecimal> spots, Map<BasketTO, BigDecimal> sums) {
        List<PositionTO> wished = new ArrayList<>();

        for (BasketTO basket : baskets) {
            BigDecimal sum = sums.get(basket);

            for (WeightTO weight : basket.getWeights()) {
                BigDecimal price = spots.get(weight.getCurrency());
                BigDecimal w = new BigDecimal(weight.getWeight(), mathContext);

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
    public static Map<BasketTO, BigDecimal> getEquivalentSumByBasket(List<PositionTO> positions, Map<String, BigDecimal> spots) {
        Map<BasketTO, Map<String, BigDecimal>> equivalent = turnPositionsIntoEquivalent(positions, spots);

        Map<BasketTO, BigDecimal> eq = new HashMap<>(equivalent.size());
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
    public static Map<BasketTO, Map<String, BigDecimal>> turnPositionsIntoEquivalent(List<PositionTO> positions, Map<String, BigDecimal> spots) {
        Map<BasketTO, Map<String, BigDecimal>> equivalent = new HashMap<>();
        spots.put("BTC", new BigDecimal(1.0, mathContext));      // Base for equivalency
        for (PositionTO p : positions) {
            BigDecimal quantity = new BigDecimal(p.getQuantity(), mathContext);
            BigDecimal price = spots.get(p.getCurrency());
            BigDecimal eq = quantity.multiply(price, mathContext).setScale(10, RoundingMode.HALF_UP);

            equivalent.putIfAbsent(p.getBasket(), new HashMap<>());
            equivalent.get(p.getBasket()).put(p.getCurrency(), eq.setScale(10, RoundingMode.HALF_UP));
        }
        return equivalent;
    }

    private static List<PositionTO> getPositions() {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://bitfinex:8080/bitfinex/positions/")).build();
        HttpResponse<String> response = null;
        List<PositionTO> positions = new ArrayList<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            positions = new ObjectMapper().readValue(response.body(), new TypeReference<List<PositionTO>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Got positions: " + positions);
        return positions;
    }

    private static Map<String, BigDecimal> getSpots() {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://bitfinex:8080/bitfinex/spots/")).build();
        HttpResponse<String> response = null;
        List<SpotTO> spots = new ArrayList<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            spots = new ObjectMapper().readValue(response.body(), new TypeReference<List<SpotTO>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Got spots: " + spots);
        return AssetManagerTasks.spotsAsMap(spots);
    }

    /**
     * Transforms a list of complete SpotTO objects into Map: name, spot
     * @param spots list to be transformed
     * @return Map with basic info from the list
     */
    public static Map<String, BigDecimal> spotsAsMap(List<SpotTO> spots) {
        Map<String, BigDecimal> res = new HashMap<>();
        if (spots == null) {
            return res;
        }
        spots.forEach(s -> {
            if (s.getInstrument() != null && s.getInstrument().contains("BTC")){
                res.putIfAbsent(s.getInstrument().replace("BTC", ""), new BigDecimal(s.getMid(), mathContext));
            }
        });

        res.putIfAbsent("BTC", new BigDecimal(1.0, mathContext));
        return res;
    }

    /**
     * Transforms a list of complete PositionTO objects into Map: name, spot
     * @param positions list to be transformed
     * @return Map with basic info from the list
     */
    public static Map<BasketTO, Map<String, Double>> positionsAsMap(List<PositionTO> positions) {
        Map<BasketTO, Map<String, Double>> res = new HashMap<>();
        if (positions == null) {
            return res;
        }
        positions.forEach(p -> {
            String currency = p.getCurrency();
            Double quantity = p.getQuantity();
            if (res.containsKey(p.getBasket())) {
                res.get(p.getBasket()).put(currency, quantity);
            } else {
                Map<String, Double> value = new HashMap<>();
                value.put(currency, quantity);
                res.put(p.getBasket(), value);
            }
        });
        return res;
    }

    private static List<BasketTO> getBaskets() {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://ordenanza:8080/basket/all/")).build();
        HttpResponse<String> response = null;
        List<BasketTO> baskets = new ArrayList<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            baskets = new ObjectMapper().readValue(response.body(), new TypeReference<List<BasketTO>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Got baskets: " + baskets);
        return baskets;
    }
}
