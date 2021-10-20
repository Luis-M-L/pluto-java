package com.example.pluto.bitfinex;

import com.example.pluto.bitfinex.publicservices.BitfinexPublicService;
import com.example.pluto.entities.SpotTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BitfinexPublicServiceTest {

    /**
     * Parámetros para el test
     */
    private String ticker = "USDBTC";
    private String jsonBitfinex = "[[\"tBTCUSD\",37719,15.510102350000002,37721,13.328416660000002,-1551.62421105,-0.0395,37719,4069.14892134,39519,37210]]";
    private Timestamp timestamp = Timestamp.valueOf("2021-06-26 12:04:30");
    private SpotTO expectedSpotTO = new SpotTO("USDBTC", timestamp, 37719.0, 37721.0, 4069.14892134);

    @Mock
    BitfinexAPIClient apiClient;

    @Mock
    BitfinexParser bitfinexParser;

    @InjectMocks
    BitfinexPublicService service;

    @Before
    public void setUp() throws IOException, InterruptedException {
        MockitoAnnotations.initMocks(this);
        Map<String, String> params = new HashMap<>(1);
        params.put("symbols", "t"+ticker);
       Mockito.when(service.client.publicGet(Arrays.asList("v2", "tickers"), params))
               .thenReturn(jsonBitfinex);
       Mockito.when(service.parser.parseSpot(jsonBitfinex)).thenReturn(expectedSpotTO);
    }

    @Test
    public void testGetSpot(){
        SpotTO actualSpotTO = service.getSpot("USDBTC");
        Assert.assertEquals(expectedSpotTO, actualSpotTO);
    }
}
