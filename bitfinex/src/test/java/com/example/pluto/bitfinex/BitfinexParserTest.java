package com.example.pluto.bitfinex;

import com.example.pluto.bitfinex.parsers.BitfinexParser;
import com.example.pluto.entities.SpotTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;

public class BitfinexParserTest {

    public BitfinexParser parser;

    private String jsonTicker;
    private Timestamp timestamp = Timestamp.valueOf("2021-06-18 10:19:30");

    @Before
    public void setUp(){
        parser = new BitfinexParser();
        jsonTicker = "[[\"tBTCUSD\",37719,15.510102350000002,37721,13.328416660000002,-1551.62421105,-0.0395,37719,4069.14892134,39519,37210]]";
    }

    @Test
    public void testParseSpot(){
        MockedStatic<Timestamp> instantMockedStatic = Mockito.mockStatic(Timestamp.class);
        instantMockedStatic.when(() -> Timestamp.from(Mockito.any())).thenReturn(timestamp);

        SpotTO spotTO = parser.parseSpot(jsonTicker);

        Assert.assertEquals("tBTCUSD","t"+spotTO.getInstrument());
        Assert.assertEquals(timestamp, spotTO.getTimestamp());
        Assert.assertEquals((Double) 37719.0, spotTO.getBid());
        Assert.assertEquals((Double) 37721.0, spotTO.getOffer());
        Assert.assertEquals((Double) 4069.14892134, spotTO.getVolume());
    }
}
