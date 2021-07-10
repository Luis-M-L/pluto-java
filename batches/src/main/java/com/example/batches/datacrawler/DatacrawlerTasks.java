package com.example.batches.datacrawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class DatacrawlerTasks {

    private static final Logger LOG = LoggerFactory.getLogger(DatacrawlerTasks.class);

    public static void registerSpots(){
        LOG.info("Registering spot data");
        getParesVigilados().forEach(par -> requestSave(getSpot(par)));
    }

    private static List<String> getParesVigilados(){
        return Arrays.asList("BTCUSD", "ETHUSD", "ETHBTC");
    }

    private static byte[] getSpot(String par){
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:48558/bitfinex/spot/" + par)).build();
        HttpResponse<byte[]> response = null;

        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            LOG.error(String.format("Error getting spot for %s", par));
            e.printStackTrace();
        } catch (InterruptedException e) {
            LOG.error(String.format("Error getting spot for %s", par));
            e.printStackTrace();
        }

        return response.body();
    }

    private static void requestSave(byte[] body){
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:48557/persistence/spot"))
                .method("POST", HttpRequest.BodyPublishers.ofByteArray(body))
                .setHeader("Content-Type", "application/json")
                .build();
        try{
            HttpResponse<byte[]> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() != 200){
                throw new IllegalStateException("Error saving " + request.toString());
            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        } catch (InterruptedException ie){
            ie.printStackTrace();
        }
    }
}
