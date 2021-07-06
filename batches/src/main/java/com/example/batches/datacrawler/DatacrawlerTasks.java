package com.example.batches.datacrawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Component
public class DatacrawlerTasks {

    private static final Logger LOG = LoggerFactory.getLogger(DatacrawlerTasks.class);

    @Value("${bitfinex.component.basepath}")
    public String DATASOURCE_BASE;

    @Value("${persistence.component.basepath}")
    public String PERSISTENCE_BASE;

    @Scheduled(fixedRate = 300000)
    public void registerSpots(){
        List<String> paresVigilados = getParesVigilados();
        LOG.info(String.format("Invocado getSpots() task para los pares %s", paresVigilados.toString()));

        for(String par : paresVigilados){
            try {
                byte[] spotJson = getSpot(par);
                boolean result = saveSpot(spotJson);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> getParesVigilados(){
        return Arrays.asList("USDBTC", "USDETH", "ETHBTH");
    }

    private byte[] getSpot(String par) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(DATASOURCE_BASE+par)).build();
        HttpResponse<byte[]> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
        return response.body();
    }

    private boolean saveSpot(byte[] spotJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(PERSISTENCE_BASE))
                                    .method("POST", HttpRequest.BodyPublishers.ofByteArray(spotJson))
                                    .setHeader("Content-Type", "application/json")
                                    .build();
        HttpResponse<byte[]> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
        return response.statusCode() == 200;
    }

}
