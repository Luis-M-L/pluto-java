package com.example.batches.assetmanager;

import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.entities.WeightTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AssetManagerTasksTest {

    private static MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);

    public static List<SpotTO> spotsList;
    public static Map<String, BigDecimal> spotsMap;
    public static List<PositionTO> positions;
    public static Map<String, Map<String, BigDecimal>> equivalent;
    public static Map<String, BigDecimal> equivalentSum;
    public static List<BasketTO> baskets;
    public static List<PositionTO> wished;
    public static Map<PositionTO, BigDecimal> deviations;
    public static Map<PositionTO, BigDecimal> filteredDeviations;

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
        fillFilteredDeviations();
    }

    @Test
    public void testGetCurrentWishedPositions() {
        AssetManagerTasks amt = new AssetManagerTasks();
        List<PositionTO> actual = amt.getPositionsByDesign(baskets, spotsMap, equivalentSum);

        assertEquals(wished, actual);
    }

    @Test
    public void testTurnPositionsIntoEquivalent() {
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<String, Map<String, BigDecimal>> actual = amt.turnPositionsIntoEquivalent(positions, spotsMap);

        assertEquals(equivalent, actual);
    }

    @Test
    public void testGetEquivalentSumByBasket(){
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<String, BigDecimal> actual = amt.getEquivalentSumByBasket(positions, spotsMap);

        assertEquals(equivalentSum, actual);
    }

    @Test
    public void testSpotsAsMap() {
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<String, BigDecimal> actual = amt.spotsAsMap(spotsList);

        assertEquals(spotsMap, actual);
    }

    @Test
    public void testGetDeviations() {
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<PositionTO, BigDecimal> actual = amt.getDeviations(positions, wished);

        assertEquals(deviations, actual);
    }

    @Test
    public void testFilterDeviations() {
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<PositionTO, BigDecimal> actual = amt.filterDeviations(deviations, 0.05, positions, spotsMap);

        assertEquals(deviations, actual);

        Map<PositionTO, BigDecimal> actual2 = amt.filterDeviations(deviations, 1.0, positions, spotsMap);
        assertNotEquals(deviations, actual2);
    }

    @Test
    public void testGetPositionValue() {
        AssetManagerTasks amt = new AssetManagerTasks();
        BigDecimal position = amt.getPositionValue(amt.positionsAsMap(positions), positions.get(0));

        BigDecimal expected = new BigDecimal(positions.get(0).getQuantity(), mathContext).setScale(10, RoundingMode.HALF_UP);
        assertEquals(expected, position);
    }

    private static void fillBaskets(BasketTO basketBasica, BasketTO basketGrowth) {
        baskets = new ArrayList<>(2);
        baskets.add(basketBasica);
        baskets.add(basketGrowth);
    }

    private static void fillEquivalentSum(BasketTO basketBasica, BasketTO basketGrowth) {
        equivalentSum = new HashMap<>(2);
        equivalentSum.put(basketBasica.getLabel(), new BigDecimal(0.511165, mathContext).setScale(10, RoundingMode.HALF_UP));
        equivalentSum.put(basketGrowth.getLabel(), new BigDecimal(1.000055, mathContext).setScale(10, RoundingMode.HALF_UP));
    }

    private static void fillEquivalent(BasketTO basketBasica, BasketTO basketGrowth) {
        equivalent = new HashMap<>(2);
        equivalent.put(basketBasica.getLabel(), new HashMap<>());
        equivalent.get(basketBasica.getLabel()).put("BTC", new BigDecimal(0.5      , mathContext).setScale(10, RoundingMode.HALF_UP));
        equivalent.get(basketBasica.getLabel()).put("ETH", new BigDecimal(0.011    , mathContext).setScale(10, RoundingMode.HALF_UP));
        equivalent.get(basketBasica.getLabel()).put("XMR", new BigDecimal(0.000165 , mathContext).setScale(10, RoundingMode.HALF_UP));
        equivalent.put(basketGrowth.getLabel(), new HashMap<>());
        equivalent.get(basketGrowth.getLabel()).put("BTC", new BigDecimal(0.15     , mathContext).setScale(10, RoundingMode.HALF_UP));
        equivalent.get(basketGrowth.getLabel()).put("ADA", new BigDecimal(0.29988  , mathContext).setScale(10, RoundingMode.HALF_UP));
        equivalent.get(basketGrowth.getLabel()).put("XMR", new BigDecimal(0.25     , mathContext).setScale(10, RoundingMode.HALF_UP));
        equivalent.get(basketGrowth.getLabel()).put("ETH", new BigDecimal(0.2002   , mathContext).setScale(10, RoundingMode.HALF_UP));
        equivalent.get(basketGrowth.getLabel()).put("IOT", new BigDecimal(0.099975 , mathContext).setScale(10, RoundingMode.HALF_UP));
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
        spotsMap.put("BTC", new BigDecimal(1.0, mathContext));
        spotsMap.put("ETH", new BigDecimal(0.11, mathContext));
        spotsMap.put("BCH", new BigDecimal(0.019, mathContext));
        spotsMap.put("XMR", new BigDecimal(0.005, mathContext));
        spotsMap.put("ADA", new BigDecimal(0.0000441, mathContext));
        spotsMap.put("IOT", new BigDecimal(0.0000215, mathContext));
    }

    private static void fillSpotsList() {
        spotsList = new ArrayList<>(6);
        spotsList.add(new SpotTO("ETHIOT", Timestamp.valueOf("2021-10-02 18:35:00"), 0.000377, 0.000375, 200.0));
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
        deviations.put(positions.get(0), new BigDecimal(0.091068        , mathContext).setScale(10, RoundingMode.HALF_UP));
        deviations.put(positions.get(1), new BigDecimal(-0.5970431818   , mathContext).setScale(10, RoundingMode.HALF_UP));
        deviations.put(positions.get(2), new BigDecimal(-5.07865        , mathContext).setScale(10, RoundingMode.HALF_UP));
        deviations.put(positions.get(3), new BigDecimal(-1.4186046512   , mathContext).setScale(10, RoundingMode.HALF_UP));
        deviations.put(positions.get(4), new BigDecimal(0.0017181818    , mathContext).setScale(10, RoundingMode.HALF_UP));
        deviations.put(positions.get(5), new BigDecimal(-0.00275        , mathContext).setScale(10, RoundingMode.HALF_UP));
        deviations.put(positions.get(6), new BigDecimal(-3.0952380952   , mathContext).setScale(10, RoundingMode.HALF_UP));
        deviations.put(positions.get(7), new BigDecimal(-0.00000825     , mathContext).setScale(10, RoundingMode.HALF_UP));
    }

    private void fillFilteredDeviations() {
        filteredDeviations = new HashMap<>();
        filteredDeviations.put(positions.get(0), deviations.get(positions.get(0)));
        filteredDeviations.put(positions.get(1), deviations.get(positions.get(1)));
    }

}
