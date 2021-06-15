package com.example.pluto.bitfinex;

import com.example.pluto.exchanges.ExchangeAPIClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class BitfinexAPIClient extends ExchangeAPIClient {
    
    public enum uris {
        SPOT("https://api-pub.bitfinex.com/v2/tickers?symbols=tBTCUSD");

        public final String url;

        private uris(String label){
            this.url = label;
        }
    }

    public BitfinexAPIClient() {
        super();
    }

    @Override
    public boolean checkStatus() {
        return false;
    }

    @Override
    public String getSpot(String instrument) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(uris.SPOT.url))
                                        .build();
        HttpResponse<String> response = super.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

}
