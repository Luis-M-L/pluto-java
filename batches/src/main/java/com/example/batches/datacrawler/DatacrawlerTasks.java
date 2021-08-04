package com.example.batches.datacrawler;

import com.example.pluto.entities.InstrumentTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class DatacrawlerTasks {

    private static final Logger LOG = LoggerFactory.getLogger(DatacrawlerTasks.class);

    public static void registerSpots(){
        LOG.info("Registering spot data");
        getParesVigilados().forEach(par -> requestSave(par.getTicker()));
    }

    private static List<InstrumentTO> getParesVigilados(){
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://persistencia:8080/instruments/")).build();
        HttpResponse<String> response = null;
        List<InstrumentTO> vigilados = new ArrayList<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            vigilados = new ObjectMapper().readValue(response.body(), new TypeReference<List<InstrumentTO>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return vigilados;
    }

    private static void requestSave(String instrument){
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://bitfinex:8080/bitfinex/spot/" + instrument))
                .method("POST", HttpRequest.BodyPublishers.noBody())
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
