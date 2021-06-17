package com.example.pluto.entities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

public class BookTOTest {

    SpotTO spot;
    Double bid = 1.0;
    Double offer = 2.0;
    Double mid = 1.5;

    @Before
    public void setUp(){
        spot = new SpotTO();
    }

    @Test
    public void testConstructors(){
        SpotTO spot1 = new SpotTO();
        SpotTO spot2 = new SpotTO(1.0, 2.0);

        Assert.assertEquals(SpotTO.class, spot1.getClass());
        Assert.assertEquals(SpotTO.class, spot2.getClass());

        Assert.assertNull(spot1.getBid());
        Assert.assertNull(spot1.getOffer());
        Assert.assertNull(spot1.getMid());
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
}
