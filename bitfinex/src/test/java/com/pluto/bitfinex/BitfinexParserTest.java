package com.pluto.bitfinex;

import com.pluto.bitfinex.parsers.BitfinexParser;
import com.pluto.entities.SpotEntity;
import com.pluto.entities.TradeTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class BitfinexParserTest {

    public BitfinexParser parser;

    private String jsonTicker;
    private String jsonTrade;
    private Timestamp timestamp = Timestamp.valueOf("2021-06-18 10:19:30");

    @Before
    public void setUp(){
        parser = new BitfinexParser();
        jsonTicker = "[[\"tBTCUSD\",37719,15.510102350000002,37721,13.328416660000002,-1551.62421105,-0.0395,37719,4069.14892134,39519,37210]]";
        jsonTrade = "[1637093199,\"on-req\",null,null,[[78617700474,null,1637093199923,\"tIOTETH\",1637093199924,1637093199924,-51,-49,\"EXCHANGE LIMIT\",null,null,null,0,\"ACTIVE\",null,null,0.00032,0,0,0,null,null,null,0,0,null,null,null,\"API>BFX\",null,null,null]],null,\"SUCCESS\",\"Submitting 1 orders.\"]";
    }

    @Test
    public void testParseSpot(){
        MockedStatic<Timestamp> instantMockedStatic = Mockito.mockStatic(Timestamp.class);
        instantMockedStatic.when(() -> Timestamp.from(Mockito.any())).thenReturn(timestamp);

        SpotEntity spotEntity = parser.parseSpot(jsonTicker);

        Assert.assertEquals("tBTCUSD","t"+ spotEntity.getInstrument());
        Assert.assertEquals(timestamp, spotEntity.getTimestamp());
        Assert.assertEquals((Double) 37719.0, spotEntity.getBid());
        Assert.assertEquals((Double) 37721.0, spotEntity.getOffer());
        Assert.assertEquals((Double) 4069.14892134, spotEntity.getVolume());
    }

    @Test
    public void testParseTrade() {
        TradeTO tradeTO = parser.convertOrderIntoTrade(new TradeTO(), jsonTrade);

        Assert.assertEquals(new Timestamp(1637093199924L), tradeTO.getEffectiveTimestamp());
        Assert.assertEquals(Long.valueOf(1637093199923L), tradeTO.getExchangeId());
        Assert.assertEquals("IOTETH", tradeTO.getPair());
        Assert.assertEquals(BigDecimal.valueOf(32e-5), tradeTO.getPrice());
        Assert.assertEquals(Double.valueOf(-49.0), tradeTO.getAmount());
        Assert.assertEquals(TradeTO.ACTIVE_STATUS, tradeTO.getStatus());
    }
}
