package com.example.batches.datacrawler;

import org.springframework.batch.item.ItemWriter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PersistenciaWriter implements ItemWriter<byte[]> {

    private String path;

    public PersistenciaWriter(String path){
        this.path = path;
    }

    @Override
    public void write(List<? extends byte[]> list){
       list.forEach(element -> requestSave(element));
    }

    private void requestSave(byte[] body){
        HttpRequest request = HttpRequest.newBuilder(URI.create(path))
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
