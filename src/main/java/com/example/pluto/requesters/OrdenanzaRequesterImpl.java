package com.example.pluto.requesters;

import com.example.pluto.PlutoConstants;
import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.InstrumentTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static com.example.pluto.PlutoConstants.HTTP_PREFIX;

public class OrdenanzaRequesterImpl /*implements IOrdenanzaService [Restore Override annotations]*/ {

    public List<BasketTO> getAllBaskets() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.ORDENANZA.value(), PlutoConstants.Path.BASKET_ALL.value()))).build();
        HttpResponse<String> response;
        List<BasketTO> baskets = new ArrayList<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            baskets = new ObjectMapper().readValue(response.body(), new TypeReference<>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return baskets;
    }

    public BasketTO getBasket(Long basketId) {
        return null;
    }

    public void saveBasket(BasketTO basket) {

    }

    public List<InstrumentTO> getInstruments() {
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

    public void saveInstrument(InstrumentTO ccy) {

    }

    protected static String buildUrl(String... split) {
        StringBuilder sb = new StringBuilder(HTTP_PREFIX);
        for (int i = 0; i <= split.length - 1; i++) {
            sb.append(split[i]);
        }
        return sb.toString();
    }
}
