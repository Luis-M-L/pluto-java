package com.example.pluto.entities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SpotEntityTest {

    SpotEntity spot;

    private String instrument = "BTCUSD";
    private Timestamp timestamp = Timestamp.valueOf("2021-06-26 12:04:30");
    private BigDecimal bid = new BigDecimal(5.0);
    private BigDecimal offer = new BigDecimal(3.0);
    private BigDecimal mid = new BigDecimal(4.0);
    private BigDecimal volume = new BigDecimal(4069.14892134);

    @Before
    public void setUp(){
        spot = new SpotEntity();
    }

    @Test
    public void testConstructors(){
        SpotEntity spot1 = new SpotEntity();
        SpotEntity spot2 = new SpotEntity(instrument, timestamp, BigDecimal.ONE, BigDecimal.valueOf(2.0), volume);

        Assert.assertEquals(SpotEntity.class, spot1.getClass());
        Assert.assertEquals(SpotEntity.class, spot2.getClass());

        Assert.assertNull(spot1.getBid());
        Assert.assertNull(spot1.getOffer());
        Assert.assertNull(spot1.getMid());
    }

    @Test
    public void testGetSetInstrument(){
        spot.setInstrument(instrument);
        Assert.assertEquals(instrument, spot.getInstrument());
    }

    @Test
    public void testGetSetTimestamp(){
        Timestamp time = Timestamp.valueOf("2021-06-18 00:07:00");
        spot.setTimestamp(time);
        Assert.assertEquals(time, spot.getTimestamp());
    }

    @Test
    public void testGetSetBid(){
        spot.setBid(bid);
        Assert.assertEquals(bid, spot.getBid());
        Assert.assertEquals(null, spot.getMid());
        Assert.assertEquals(null, spot.getOffer());
    }

    @Test
    public void testGetSetOffer(){
        spot.setOffer(offer);
        Assert.assertEquals(null, spot.getBid());
        Assert.assertEquals(null, spot.getMid());
        Assert.assertEquals(offer, spot.getOffer());
    }

    @Test
    public void testGetSetMid(){
        spot.setBid(bid);
        spot.setOffer(offer);
        Assert.assertEquals(bid, spot.getBid());
        Assert.assertEquals(mid, spot.getMid());
        Assert.assertEquals(offer, spot.getOffer());
    }

    @Test
    public void testGetSetVolume(){
        spot.setVolume(volume);
        Assert.assertEquals(volume, spot.getVolume());
    }
}
