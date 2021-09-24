package com.example.batches.assetmanager;

import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.entities.WeightTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class AssetManagerTasksTest {

    public static List<SpotTO> spotsList;
    public static Map<String, Double> spotsMap;
    public static List<PositionTO> positions;
    public static Map<BasketTO, Map<String, Double>> equivalent;
    public static Map<BasketTO, Double> equivalentSum;
    public static List<BasketTO> baskets;
    public static List<PositionTO> wished;
    public static Map<PositionTO, Double> deviations;

    @BeforeEach
    public void setUp(){
        fillSpotsList();
        fillSpotsMap();
        BasketTO basketBasica = fillPositionsBasica();
        BasketTO basketGrowth = fillPositionsGrowth();
        fillEquivalent(basketBasica, basketGrowth);
        fillEquivalentSum(basketBasica, basketGrowth);
        fillBaskets(basketBasica, basketGrowth);
        fillWished();
        fillDeviations();
    }

    @Test
    public void testGetCurrentWishedPositions() {
        AssetManagerTasks amt = new AssetManagerTasks();
        List<PositionTO> actual = amt.getCurrentWishedPositions(baskets, spotsMap, equivalentSum);

        assertEquals(wished, actual);
    }

    @Test
    public void testTurnPositionsIntoEquivalent() {
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<BasketTO, Map<String, Double>> actual = amt.turnPositionsIntoEquivalent(positions, spotsMap);

        assertEquals(equivalent, actual);
    }

    @Test
    public void testGetEquivalentSumByBasket(){
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<BasketTO, Double> actual = amt.getEquivalentSumByBasket(positions, spotsMap);

        assertEquals(equivalentSum, actual);
    }

    @Test
    public void testAsMap() {
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<String, Double> actual = amt.spotsAsMap(spotsList);

        assertEquals(spotsMap, actual);
    }

    @Test
    public void testGetDeviations() {
        AssetManagerTasks amt = new AssetManagerTasks();
        List<PositionTO> idealPositions = new ArrayList<>(positions);
        Map<PositionTO, Double> actual = amt.getDeviations(positions, wished);

        assertEquals(deviations, actual);
    }

    private static void fillBaskets(BasketTO basketBasica, BasketTO basketGrowth) {
        baskets = new ArrayList<>(2);
        baskets.add(basketBasica);
        baskets.add(basketGrowth);
    }

    private static void fillEquivalentSum(BasketTO basketBasica, BasketTO basketGrowth) {
        equivalentSum = new HashMap<>(2);
        equivalentSum.put(basketBasica, 0.511165);
        equivalentSum.put(basketGrowth, 1.000055);
    }

    private static void fillEquivalent(BasketTO basketBasica, BasketTO basketGrowth) {
        equivalent = new HashMap<>(2);
        equivalent.put(basketBasica, new HashMap<>());
        equivalent.get(basketBasica).put("BTC", 0.5);
        equivalent.get(basketBasica).put("ETH", 0.011);
        equivalent.get(basketBasica).put("XMR", 0.000165);
        equivalent.put(basketGrowth, new HashMap<>());
        equivalent.get(basketGrowth).put("BTC", 0.15);
        equivalent.get(basketGrowth).put("ADA", 0.29988);
        equivalent.get(basketGrowth).put("XMR", 0.25);
        equivalent.get(basketGrowth).put("ETH", 0.2002);
        equivalent.get(basketGrowth).put("IOT", 0.099975);
    }

    private static BasketTO fillPositionsGrowth() {
        List<WeightTO> weightsGrowth = Arrays.asList(
                new WeightTO(null, "BTC", 0.15, null),
                new WeightTO(null, "ETH", 0.2, null),
                new WeightTO(null, "XMR", 0.25, null),
                new WeightTO(null, "ADA", 0.3, null),
                new WeightTO(null, "IOT", 0.1, null)
        );
        BasketTO basketGrowth = new BasketTO(8L, "growth", weightsGrowth);
        positions.add(new PositionTO(4L, basketGrowth, "IOT", 4650.0));
        positions.add(new PositionTO(5L, basketGrowth, "ETH", 1.82));
        positions.add(new PositionTO(6L, basketGrowth, "XMR", 50.0));
        positions.add(new PositionTO(7L, basketGrowth, "ADA", 6800.0));
        positions.add(new PositionTO(8L, basketGrowth, "BTC", 0.15));
        return basketGrowth;
    }

    private static BasketTO fillPositionsBasica() {
        positions = new ArrayList<>(3);
        List<WeightTO> weightsBasica = Arrays.asList(
                new WeightTO(null, "BTC", 0.8, null),
                new WeightTO(null, "ETH", 0.15, null),
                new WeightTO(null, "XMR", 0.05, null)
        );
        BasketTO basketBasica = new BasketTO(9L, "basica", weightsBasica);
        positions.add(new PositionTO(1L, basketBasica, "BTC", 0.5));
        positions.add(new PositionTO(2L, basketBasica, "ETH", 0.1));
        positions.add(new PositionTO(3L, basketBasica, "XMR", 0.033));
        return basketBasica;
    }

    private static void fillSpotsMap() {
        spotsMap = new HashMap<>(6);
        spotsMap.put("BTC", 1.0);
        spotsMap.put("ETH", 0.11);
        spotsMap.put("BCH", 0.019);
        spotsMap.put("XMR", 0.005);
        spotsMap.put("ADA", 0.0000441);
        spotsMap.put("IOT", 0.0000215);
    }

    private static void fillSpotsList() {
        spotsList = new ArrayList<>(5);
        spotsList.add(new SpotTO("ETHBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.12, 0.1, 10000.0));
        spotsList.add(new SpotTO("BCHBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.02, 0.018, 5000.0));
        spotsList.add(new SpotTO("XMRBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.0051, 0.0049, 3000.0));
        spotsList.add(new SpotTO("ADABTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.0000442, 0.000044, 4000.0));
        spotsList.add(new SpotTO("IOTBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.000023, 0.00002, 500.0));
    }

    private void fillWished() {
        wished = new ArrayList<>(8);
        wished.add(new PositionTO(null, baskets.get(0), "BTC", 0.408932));
        wished.add(new PositionTO(null, baskets.get(0), "ETH", 0.6970431818));
        wished.add(new PositionTO(null, baskets.get(0), "XMR", 5.11165));
        wished.add(new PositionTO(null, baskets.get(1), "BTC", 0.15000825));
        wished.add(new PositionTO(null, baskets.get(1), "ETH", 1.8182818182));
        wished.add(new PositionTO(null, baskets.get(1), "XMR", 50.00275));
        wished.add(new PositionTO(null, baskets.get(1), "ADA", 6803.0952380952));
        wished.add(new PositionTO(null, baskets.get(1), "IOT", 4651.4186046512));
    }

    private void fillDeviations() {
        deviations = new HashMap<>(8);
        deviations.put(positions.get(0), 0.091068);
        deviations.put(positions.get(1), -0.5970431818);
        deviations.put(positions.get(2), -5.07865);
        deviations.put(positions.get(3), -1.4186046512);
        deviations.put(positions.get(4), 0.0017181818);
        deviations.put(positions.get(5), -0.00275);
        deviations.put(positions.get(6), -3.0952380952);
        deviations.put(positions.get(7), -0.00000825);
    }
}
