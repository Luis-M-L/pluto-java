package com.example.batches.assetmanager;

import com.example.pluto.entities.*;
import com.example.pluto.errors.PlutoRestError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
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
        submitOrders(deviation, spots);
    }

    private static void submitOrders(Map<PositionTO, BigDecimal> deviation, Map<String, BigDecimal> spots) {
        LOG.info("Submiting orders");
        Map<Long, List<TradeTO>> tradesByBaskets = new HashMap<>();
        List<TradeTO> trades = new ArrayList(deviation.size());
        for (PositionTO k : deviation.keySet()) {
            String pair = k.getCurrency() + "BTC";
            BigDecimal price = spots.get(k.getCurrency());
            Double amount = - deviation.get(k).doubleValue();
            TradeTO trade = new TradeTO(pair, price, amount);
            if (!tradesByBaskets.containsKey(k.getBasket().getId())) {
                tradesByBaskets.put(k.getBasket().getId(), new ArrayList<>());
            }
            tradesByBaskets.get(k.getBasket().getId()).add(trade);
        }
        tradesByBaskets.forEach((k, v) -> {
            updateLocalBooks(k, callTrader(v));
        });
    }

    private static void updateLocalBooks(Long k, List<TradeTO> placedOrders) {
        List<TradeTO> recentlyFilled = checkOrdersStatus(placedOrders);
        updatePositions(k, recentlyFilled);
    }

    private static List<TradeTO> checkOrdersStatus(List<TradeTO> placedOrders) {
        LOG.info("Checking orders to update positions");
        ObjectMapper mapper = new ObjectMapper();
        List<TradeTO> res = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://bitfinex:8080/bitfinex/update/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(placedOrders)))
                    .build();
            HttpResponse<String> response = null;
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                PlutoRestError error = mapper.readValue(response.body(), new TypeReference<PlutoRestError>() {});
                LOG.error(error.getError());
            } else {
                res = mapper.readValue(response.body(), new TypeReference<List<TradeTO>>() {});
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    private static Map<PositionTO, BigDecimal> getPositionsToUpdate(List<BasketTO> baskets, Map<String, BigDecimal> spots, List<PositionTO> positions, double threshold) {
        Map<String, BigDecimal> basketEquivalentSums = getEquivalentSumByBasket(positions, spots);
        List<PositionTO> currentWishedPositions = getCurrentWishedPositions(baskets, spots, basketEquivalentSums);
        Map<PositionTO, BigDecimal> deviations = getDeviations(positions, currentWishedPositions);
        Map<PositionTO, BigDecimal> rebalancing = filterDeviations(deviations, threshold, basketEquivalentSums, spots);
        return rebalancing;
    }

    /**
     * Filters deviations map to keep only the ones which must be corrected attending to the threshold
     * @param deviations deviation values for each position
     * @param threshold this limits which positions must be corrected and which not to (x per one, 100% = 1)
     * @param basketEquivalentSums base to calculate deviation as percentage in the basket in order to compare to threshold
     * @param spots map with the price of each currency over the base currency (BTC)
     * @return
     */
    public static Map<PositionTO, BigDecimal> filterDeviations(Map<PositionTO, BigDecimal> deviations, double threshold, Map<String, BigDecimal> basketEquivalentSums, Map<String, BigDecimal> spots) {
        Map<PositionTO, BigDecimal> filtered = new HashMap<>();
        for (PositionTO p : deviations.keySet()) {
            BigDecimal deviationBtcEq = deviations.get(p).multiply(spots.get(p.getCurrency()));
            BigDecimal deviationOverSum = deviationBtcEq.divide(basketEquivalentSums.get(p.getBasket().getLabel()), mathContext);
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
        Map<String, Map<String, Double>> pos = positionsAsMap(positions);
        Map<String, Map<String, Double>> currPos = positionsAsMap(currentWishedPositions);
        positions.forEach(p -> {
            BigDecimal minuendo = getPositionValue(pos, p);
            BigDecimal sustraendo = getPositionValue(currPos, p);
            BigDecimal diferencia = minuendo.add(sustraendo.negate(), mathContext).setScale(10, RoundingMode.HALF_UP);
            deviations.put(p, diferencia);
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
        }
        return new BigDecimal(value, mathContext).setScale(10, RoundingMode.HALF_UP);
    }

    /**
     * Calculates what the positions should be to fit into our designed weights with current spots
     * @param baskets designed baskets with the weights
     * @param spots map with the price of each currency over the base currency (BTC)
     * @param sums sumatory in base currency (BTC) of all the positions in a basket
     * @return the positions that we should have
     */
    public static List<PositionTO> getCurrentWishedPositions(List<BasketTO> baskets, Map<String, BigDecimal> spots, Map<String, BigDecimal> sums) {
        List<PositionTO> wished = new ArrayList<>();

        for (BasketTO basket : baskets) {
            BigDecimal sum = sums.get(basket.getLabel());

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
    public static Map<String, BigDecimal> getEquivalentSumByBasket(List<PositionTO> positions, Map<String, BigDecimal> spots) {
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
    public static Map<String, Map<String, BigDecimal>> turnPositionsIntoEquivalent(List<PositionTO> positions, Map<String, BigDecimal> spots) {
        Map<String, Map<String, BigDecimal>> equivalent = new HashMap<>();
        spots.put("BTC", new BigDecimal(1.0, mathContext));      // Base for equivalency
        for (PositionTO p : positions) {
            BigDecimal quantity = new BigDecimal(p.getQuantity(), mathContext);
            BigDecimal price = spots.get(p.getCurrency());
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

    private static List<PositionTO> getPositions() {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://bitfinex:8080/position/all")).build();
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

    private static void updatePositions(Long basketId, List<TradeTO> trades) {
        LOG.info("Updating positions");
        ObjectMapper mapper = new ObjectMapper();
        List<TradeTO> res = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://bitfinex:8080/position/update/"+basketId))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(trades)))
                    .build();
            HttpResponse<String> response = null;
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            res = mapper.readValue(response.body(), new TypeReference<List<TradeTO>>() {});
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    private static List<TradeTO> callTrader(List<TradeTO> trades) {
        LOG.info("trader calling");
        ObjectMapper mapper = new ObjectMapper();
        List<TradeTO> res = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://bitfinex:8080/bitfinex/trade/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(trades)))
                    .build();
            HttpResponse<String> response = null;
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            res = mapper.readValue(response.body(), new TypeReference<List<TradeTO>>() {});
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Submited trades: " + trades);
        return res;
    }
}
