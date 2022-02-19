package com.example.batches.assetmanager;

import com.example.pluto.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class AssetManagerTasksTest {

    private static final MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);

    public static List<SpotTO> spotsList;
    public static Map<String, SpotTO> spotsMap;
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
        Map<String, SpotTO> actual = amt.spotsAsMap(spotsList);
        assertEquals(spotsMap.get("BTC"), actual.get("BTC"));
        assertEquals(spotsMap.get("BCH"), actual.get("BCH"));
        assertEquals(spotsMap.get("ETH"), actual.get("ETH"));
        assertEquals(spotsMap.get("XMR"), actual.get("XMR"));
        assertEquals(spotsMap.get("ADA"), actual.get("ADA"));
        assertEquals(spotsMap.get("IOT"), actual.get("IOT"));
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

        BigDecimal expected = positions.get(0).getQuantity().setScale(10, RoundingMode.HALF_UP);
        assertEquals(expected, position);
    }

    @Test
    public void testBuildTrades() {
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<PositionTO, BigDecimal> localDeviation = new HashMap<>(1);
        PositionTO iotPosition = positions.stream().filter(p -> "IOT".equals(p.getCurrency())).collect(Collectors.toList()).iterator().next();
        localDeviation.put(iotPosition, new BigDecimal(8.0).setScale(10));
        Map<Long, List<TradeTO>> trades = amt.buildTrades(localDeviation, spotsMap);

        assertEquals(new BigDecimal(8.0).setScale(10).negate(), trades.get(iotPosition.getBasket().getId()).get(0).getAmount());
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
        positions.add(new PositionTO(4L, basketGrowth, "IOT", BigDecimal.valueOf(4650.0)));
        positions.add(new PositionTO(5L, basketGrowth, "ETH", BigDecimal.valueOf(1.82)));
        positions.add(new PositionTO(6L, basketGrowth, "XMR", BigDecimal.valueOf(50.0)));
        positions.add(new PositionTO(7L, basketGrowth, "ADA", BigDecimal.valueOf(6800.0)));
        positions.add(new PositionTO(8L, basketGrowth, "BTC", BigDecimal.valueOf(0.15)));
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
        positions.add(new PositionTO(1L, basketBasica, "BTC", BigDecimal.valueOf(0.5)));
        positions.add(new PositionTO(2L, basketBasica, "ETH", BigDecimal.valueOf(0.1)));
        positions.add(new PositionTO(3L, basketBasica, "XMR", BigDecimal.valueOf(0.033)));
        return basketBasica;
    }

    private static void fillSpotsMap() {
        spotsMap = new HashMap<>(6);
        spotsMap.put("BTC", new SpotTO("BTCBTC", 1.0));
        spotsMap.put("BCH", new SpotTO("BCHBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.018, 0.02, 5000.0));
        spotsMap.put("ETH", new SpotTO("ETHBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.1, 0.12, 10000.0));
        spotsMap.put("XMR", new SpotTO("XMRBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.0049, 0.0051, 3000.0));
        spotsMap.put("ADA", new SpotTO("ADABTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.000044, 0.0000442, 4000.0));
        spotsMap.put("IOT", new SpotTO("IOTBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.00002, 0.000023, 500.0));
    }

    private static void fillSpotsList() {
        spotsList = new ArrayList<>(6);
        spotsList.add(new SpotTO("ETHIOT", Timestamp.valueOf("2021-10-02 18:35:00"), 0.000375, 0.000377, 200.0));
        spotsList.add(new SpotTO("ETHBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.1, 0.12, 10000.0));
        spotsList.add(new SpotTO("BCHBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.018, 0.02, 5000.0));
        spotsList.add(new SpotTO("XMRBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.0049, 0.0051, 3000.0));
        spotsList.add(new SpotTO("ADABTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.000044, 0.0000442, 4000.0));
        spotsList.add(new SpotTO("IOTBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.00002, 0.000023, 500.0));
    }

    private void fillWished() {
        wished = new ArrayList<>(8);
        wished.add(new PositionTO(null, baskets.get(0), "BTC", BigDecimal.valueOf(0.408932).setScale(10)));
        wished.add(new PositionTO(null, baskets.get(0), "ETH", BigDecimal.valueOf(0.6970431818).setScale(10)));
        wished.add(new PositionTO(null, baskets.get(0), "XMR", BigDecimal.valueOf(5.11165).setScale(10)));
        wished.add(new PositionTO(null, baskets.get(1), "BTC", BigDecimal.valueOf(0.15000825).setScale(10)));
        wished.add(new PositionTO(null, baskets.get(1), "ETH", BigDecimal.valueOf(1.8182818182).setScale(10)));
        wished.add(new PositionTO(null, baskets.get(1), "XMR", BigDecimal.valueOf(50.00275).setScale(10)));
        wished.add(new PositionTO(null, baskets.get(1), "ADA", BigDecimal.valueOf(6803.0952380952).setScale(10)));
        wished.add(new PositionTO(null, baskets.get(1), "IOT", BigDecimal.valueOf(4651.4186046512).setScale(10)));
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
