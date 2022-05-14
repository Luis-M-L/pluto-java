package com.example.batches.assetmanager;

import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AssetManagerTasksTest {

    private static final MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);

    public static List<SpotEntity> spotsList;
    public static Map<String, SpotEntity> spotsMap;
    public static List<PositionTO> positions;
    public static Map<String, Map<String, BigDecimal>> equivalent;
    public static Map<String, BigDecimal> equivalentSum;
    public static List<BasketTO> baskets;
    public static List<PositionTO> wished;
    public static Map<PositionTO, BigDecimal> deviations;
    public static Map<PositionTO, BigDecimal> filteredDeviations;

    @BeforeEach
    public void setUp(){
        /*fillSpotsList();
        fillSpotsMap();
        BasketTO basketBasica = fillPositionsBasica();
        BasketTO basketGrowth = fillPositionsGrowth();
        fillEquivalent(basketBasica, basketGrowth);
        fillEquivalentSum(basketBasica, basketGrowth);
        fillBaskets(basketBasica, basketGrowth);
        fillWished();
        fillDeviations();
        fillFilteredDeviations();*/
    }

    @Test
    public void testGetSpots() {
        AssetManagerTasks amt = new AssetManagerTasks();
        amt.balance();
    }

    @Test
    public void testSpotsAsMap() {
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<String, SpotEntity> actual = amt.spotsAsMap(spotsList);
        assertEquals(spotsMap.get("BTC"), actual.get("BTC"));
        assertEquals(spotsMap.get("BCH"), actual.get("BCH"));
        assertEquals(spotsMap.get("ETH"), actual.get("ETH"));
        assertEquals(spotsMap.get("XMR"), actual.get("XMR"));
        assertEquals(spotsMap.get("ADA"), actual.get("ADA"));
        assertEquals(spotsMap.get("IOT"), actual.get("IOT"));
    }

}
