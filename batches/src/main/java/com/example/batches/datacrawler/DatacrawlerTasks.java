package com.example.batches.datacrawler;

import com.example.batches.PlutoTasks;
import com.example.pluto.PlutoConstants;
import com.example.pluto.entities.InstrumentTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static com.example.pluto.PlutoConstants.HEADER_NAME_CONTENT_TYPE;
import static com.example.pluto.PlutoConstants.HEADER_VALUE_APPLICATION_JSON;

@Configuration
@EnableBatchProcessing
public class DatacrawlerTasks extends PlutoTasks {

    private static final Logger LOG = LoggerFactory.getLogger(DatacrawlerTasks.class);

    public static void registerSpots(){
        LOG.info("Registering spot data");
        getParesVigilados().forEach(par -> requestSave(par.getTicker()));
    }

    private static List<InstrumentTO> getParesVigilados(){
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.ORDENANZA.value(), PlutoConstants.Path.INSTRUMENTS.value()))).build();
        HttpResponse<String> response;
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
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(), PlutoConstants.Path.BITFINEX_SPOT.value(), instrument)))
                .method(RequestMethod.POST.name(), HttpRequest.BodyPublishers.noBody())
                .setHeader(HEADER_NAME_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON)
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
