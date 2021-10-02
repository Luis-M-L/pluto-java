package com.example.pluto.bitfinex;

import com.example.pluto.exchanges.ExchangeAPIClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Component
public class BitfinexAPIClient extends ExchangeAPIClient {

    public static String publicUrl = "https://api-pub.bitfinex.com";
    public static String authUrl = "https://api.bitfinex.com";

    public BitfinexAPIClient() {
        super();
    }

    @Override
    public boolean checkStatus() {
        return false;
    }

    @Override
    public String publicGet(List<String> subpath, Map<String, String> params) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(buildUri(publicUrl, subpath, params)))
                                        .build();
        HttpResponse<String> response = super.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    @Override
    public String authPost(List<String> subpath, Map<String, String> params) throws IOException, InterruptedException {
        return null;
    }

    @Override
    public String buildUri(String basepath, List<String> subpath, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(basepath);
        subpath.forEach(s -> sb.append("/").append(s));
        if (!params.keySet().isEmpty()) {
            sb.append("?");
            params.keySet().forEach(k -> sb.append(k).append("=").append(params.get(k)));
        }
        return sb.toString();
    }

}
