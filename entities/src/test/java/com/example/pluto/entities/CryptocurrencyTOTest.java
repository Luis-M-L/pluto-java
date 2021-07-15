package com.example.pluto.entities;

import org.junit.Assert;
import org.junit.Test;

public class CryptocurrencyTOTest {

    private Long id = 12345L;
    private String ticker = "BTCUSD";

    @Test
    public void testConstructors(){
        CryptocurrencyTO cryptoTO = new CryptocurrencyTO();
        CryptocurrencyTO crytpTO2 = new CryptocurrencyTO(id, ticker);

        Assert.assertEquals(id, crytpTO2.getId());
        Assert.assertEquals(ticker, crytpTO2.getTicker());
    }

    @Test
    public void testGetSetId(){
        CryptocurrencyTO cryptoTO = new CryptocurrencyTO();
        cryptoTO.setId(id);
        Assert.assertEquals(id, cryptoTO.getId());
    }

    @Test
    public void testGetSetTicker(){
        CryptocurrencyTO cryptocurrencyTO = new CryptocurrencyTO();
        cryptocurrencyTO.setTicker(ticker);
        Assert.assertEquals(ticker, cryptocurrencyTO.getTicker());
    }
}
