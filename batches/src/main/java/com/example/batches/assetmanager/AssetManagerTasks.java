package com.example.batches.assetmanager;

import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.entities.WeightTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class AssetManagerTasks {

    private static MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);

    public static void rebalance() {
        List<BasketTO> baskets = getBaskets();
        Map<String, Double> spots = getSpots();
        List<PositionTO> positions = getPositions();

        double threshold = 0.05;
        Map<String, Double> deviation = calculateDeviation(baskets, spots, positions, threshold);
        //List<TradeTO> trades = calculateTrades(positions, flows);
    }

    private static Map<String, Double> calculateDeviation(List<BasketTO> baskets, Map<String, Double> spots, List<PositionTO> positions, double threshold) {
        Map<BasketTO, Double> basketEquivalentSums = getEquivalentSumByBasket(positions, spots);
        List<PositionTO> currentWishedPositions = getCurrentWishedPositions(baskets, spots, basketEquivalentSums);
        return null;
    }

    public static List<PositionTO> getCurrentWishedPositions(List<BasketTO> baskets, Map<String, Double> spots, Map<BasketTO, Double> sums) {
        List<PositionTO> wished = new ArrayList<>();

        for (BasketTO basket : baskets) {
            BigDecimal sum = new BigDecimal(sums.get(basket), mathContext);

            for (WeightTO weight : basket.getWeights()) {
                BigDecimal price = new BigDecimal(spots.get(weight.getCurrency()), mathContext);
                BigDecimal w = new BigDecimal(weight.getWeight(), mathContext);

                BigDecimal q = w.multiply(sum).divide(price, mathContext).setScale(10, RoundingMode.HALF_UP);
                PositionTO position = new PositionTO(null, basket, weight.getCurrency(), q.doubleValue());

                wished.add(position);
            }
        }
        return wished;
    }

    public static Map<BasketTO, Double> getEquivalentSumByBasket(List<PositionTO> positions, Map<String, Double> spots) {
        Map<BasketTO, Map<String, Double>> equivalent = turnPositionsIntoEquivalent(positions, spots);

        Map<BasketTO, Double> eq = new HashMap<>(equivalent.size());
        equivalent.keySet().forEach(
                k -> eq.put(k, getEquivalentSum(equivalent.get(k)))
        );
        return eq;
    }

    private static Double getEquivalentSum(Map<String, Double> equivalents) {
        Double sum = 0.0;
        for (Double v : equivalents.values()){
            if (v == null) {
                throw new IllegalArgumentException("Received null position quantity");
            }
            sum += v;
        }
        return sum;
    }

    public static Map<BasketTO, Map<String, Double>> turnPositionsIntoEquivalent(List<PositionTO> positions, Map<String, Double> spots) {
        Map<BasketTO, Map<String, Double>> equivalent = new HashMap<>();
        spots.put("BTC", 1.0);      // Base for equivalency
        for (PositionTO p : positions) {
            BigDecimal quantity = new BigDecimal(p.getQuantity(), mathContext);
            BigDecimal price = new BigDecimal(spots.get(p.getCurrency()), mathContext);
            BigDecimal eq = quantity.multiply(price, mathContext).setScale(10, RoundingMode.HALF_UP);

            equivalent.putIfAbsent(p.getBasket(), new HashMap<>());
            equivalent.get(p.getBasket()).put(p.getCurrency(), eq.doubleValue());
        }
        return equivalent;
    }

    private static List<PositionTO> getPositions() {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://trader:8080/spot/")).build();
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

        return positions;
    }

    private static Map<String, Double> getSpots() {
        // TODO: filtrar spots frente a BTC y usar como clave la altcoin
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://bitfinex:8080/spot/")).build();
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

        return asMap(spots);
    }

    /**
     * Transforms a list of complete SpotTO objects into Map: name, spot
     * @param spots list to be transformed
     * @return Map with basic info from the list
     */
    public static Map<String, Double> asMap(List<SpotTO> spots) {
        Map<String, Double> res = new HashMap<>();
        if (spots == null) {
            return res;
        }
        spots.forEach(s -> {
            res.putIfAbsent(s.getInstrument().replace("BTC", ""), s.getMid());
        });
        res.putIfAbsent("BTC", 1.0);
        return res;
    }

    private static List<BasketTO> getBaskets() {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://baskets:8080/basket/all/")).build();
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

        return baskets;
    }
}
