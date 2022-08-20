package com.example.pluto.bitfinex;

import com.example.pluto.bitfinex.parsers.BitfinexParser;
import com.example.pluto.bitfinex.publicservices.BitfinexPublicService;
import com.example.pluto.entities.SpotEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BitfinexPublicServiceTest {

    /**
     * Parámetros para el test
     */
    private String ticker = "USDBTC";
    private String jsonBitfinex = "[[\"tBTCUSD\",37719,15.510102350000002,37721,13.328416660000002,-1551.62421105,-0.0395,37719,4069.14892134,39519,37210]]";
    private Timestamp timestamp = Timestamp.valueOf("2021-06-26 12:04:30");
    private SpotEntity expectedSpotEntity = new SpotEntity("USDBTC", timestamp, BigDecimal.valueOf(37719.0), BigDecimal.valueOf(37721.0), BigDecimal.valueOf(4069.14892134));

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
               .thenReturn(new HttpResponse() {
                   @Override
                   public int statusCode() {
                       return 200;
                   }

                   @Override
                   public HttpRequest request() {
                       return null;
                   }

                   @Override
                   public Optional<HttpResponse> previousResponse() {
                       return Optional.empty();
                   }

                   @Override
                   public HttpHeaders headers() {
                       return null;
                   }

                   @Override
                   public Object body() {
                       return jsonBitfinex;
                   }

                   @Override
                   public Optional<SSLSession> sslSession() {
                       return Optional.empty();
                   }

                   @Override
                   public URI uri() {
                       return null;
                   }

                   @Override
                   public HttpClient.Version version() {
                       return null;
                   }
               });
       Mockito.when(service.parser.parseSpot(jsonBitfinex)).thenReturn(expectedSpotEntity);
    }

    @Test
    public void testGetSpot(){
        SpotEntity actualSpotEntity = service.getSpot("USDBTC");
        Assert.assertEquals(expectedSpotEntity, actualSpotEntity);
    }
}
