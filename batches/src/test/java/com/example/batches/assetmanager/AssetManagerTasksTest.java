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

    @BeforeEach
    public void setUp(){
        spotsList = new ArrayList<>(5);
        spotsList.add(new SpotTO("ETHBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.12, 0.1, 10000.0));
        spotsList.add(new SpotTO("BCHBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.02, 0.018, 5000.0));
        spotsList.add(new SpotTO("XMRBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.0051, 0.0049, 3000.0));
        spotsList.add(new SpotTO("ADABTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.0000442, 0.000044, 4000.0));
        spotsList.add(new SpotTO("IOTBTC", Timestamp.valueOf("2020-08-17 21:45:00"), 0.000023, 0.00002, 500.0));

        spotsMap = new HashMap<>(6);
        spotsMap.put("ETH", 0.11);
        spotsMap.put("BCH", 0.019);
        spotsMap.put("XMR", 0.005);
        spotsMap.put("ADA", 0.0000441);
        spotsMap.put("IOT", 0.0000215);

        positions = new ArrayList<>(3);
        List<WeightTO> weightsBasica = Arrays.asList(
                    new WeightTO(null, "BTC", 0.8, null),
                    new WeightTO(null, "ETH", 0.15, null),
                    new WeightTO(null, "XMR", 0.05, null)
                );
        BasketTO basketBasica = new BasketTO(9L, "basica", weightsBasica);
        positions.add(new PositionTO(1L, basketBasica, "BTC", 0.5));
        positions.add(new PositionTO(2L, basketBasica, "ETH", 0.1));
        positions.add(new PositionTO(2L, basketBasica, "XMR", 0.033));

        equivalent = new HashMap<>(2);
        equivalent.put(basketBasica, new HashMap<String, Double>());
        equivalent.get(basketBasica).put("BTC", 0.5);
        equivalent.get(basketBasica).put("ETH", 0.011);
        equivalent.get(basketBasica).put("XMR", 0.033*0.005);
    }

    @Test
    public void testTurnPositionsIntoEquivalent(){
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<BasketTO, Map<String, Double>> actual = amt.turnPositionsIntoEquivalent(positions, spotsMap);

        assertEquals(equivalent, actual);
    }

    @Test
    public void testAsMap(){
        AssetManagerTasks amt = new AssetManagerTasks();
        Map<String, Double> actual = amt.asMap(spotsList);

        assertEquals(spotsMap, actual);
    }
}
