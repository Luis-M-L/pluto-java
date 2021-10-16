package com.example.pluto.exchanges;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public abstract class ExchangeAPIClient {

    private HttpClient client;

    public ExchangeAPIClient(){
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                //.authenticator(Authenticator.getDefault())
                .build();
    }

    public HttpClient getClient(){
        return this.client;
    }

    public abstract boolean checkStatus();

    public abstract String publicGet(List<String> subpath, Map<String, String> params) throws IOException, InterruptedException;

    public abstract String authPost(List<String> subpath, Map<String, String> params) throws IOException, InterruptedException;

    public abstract String buildUri(String basepath, List<String> subpath, Map<String, String> params) throws IOException, InterruptedException;

}
