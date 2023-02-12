package com.example.pluto.entities;

import org.junit.Assert;
import org.junit.Test;

public class InstrumentTOTest {

    private Long id = 12345L;
    private String ticker = "BTCUSD";

    @Test
    public void testConstructors(){
        InstrumentTO cryptoTO = new InstrumentTO();
        InstrumentTO crytpTO2 = new InstrumentTO(id, ticker);

        Assert.assertEquals(id, crytpTO2.getId());
        Assert.assertEquals(ticker, crytpTO2.getTicker());
    }

    @Test
    public void testGetSetId(){
        InstrumentTO cryptoTO = new InstrumentTO();
        cryptoTO.setId(id);
        Assert.assertEquals(id, cryptoTO.getId());
    }

    @Test
    public void testGetSetTicker(){
        InstrumentTO instrumentTO = new InstrumentTO();
        instrumentTO.setTicker(ticker);
        Assert.assertEquals(ticker, instrumentTO.getTicker());
    }
}
