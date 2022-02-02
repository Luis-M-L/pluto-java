package com.example.batches;

import com.example.batches.assetmanager.AssetManagerTasks;
import com.example.pluto.PlutoConstants;
import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.entities.TradeTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.pluto.PlutoConstants.*;

public class PlutoBatchUtils {

    public static Logger LOG = LoggerFactory.getLogger(PlutoBatchUtils.class);

    protected static String buildUrl(String... split) {
        StringBuilder sb = new StringBuilder(HTTP_PREFIX);
        for (int i = 0; i <= split.length - 1; i++) {
            sb.append(split[i]);
        }
        return sb.toString();
    }

    protected static List<BasketTO> getBaskets() {
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
        LOG.info("Got baskets: " + baskets);
        return baskets;
    }

    protected static Map<String, SpotTO> getSpots() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(), PlutoConstants.Path.BITFINEX_SPOTS.value()))).build();
        HttpResponse<String> response;
        List<SpotTO> spots = new ArrayList<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            spots = new ObjectMapper().readValue(response.body(), new TypeReference<>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Got spots: " + spots);
        return AssetManagerTasks.spotsAsMap(spots);
    }

    protected static List<PositionTO> getCurrentPositions() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(Socket.BITFINEX.value(), Path.POSITION_LAST.value()))).build();
        HttpResponse<String> response = null;
        List<PositionTO> positions = new ArrayList<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            positions = new ObjectMapper().readValue(response.body(), new TypeReference<>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Got positions: " + positions);
        return positions;
    }

    protected static List<TradeTO> getUnactiveOrders(String pair) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(Socket.BITFINEX.value(), Path.BITFINEX_UNACTIVE.value(), pair))).build();
        HttpResponse<String> response = null;
        List<TradeTO> unactive = new ArrayList<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            unactive = new ObjectMapper().readValue(response.body(), new TypeReference<>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Got trades: " + unactive);
        return unactive;
    }

    protected static List<TradeTO> callTrader(List<TradeTO> trades) {
        LOG.info("trader calling");
        ObjectMapper mapper = new ObjectMapper();
        List<TradeTO> res = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(), PlutoConstants.Path.BITFINEX_TRADE.value())))
                    .header(HEADER_NAME_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(trades)))
                    .build();
            HttpResponse<String> response;
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            res = mapper.readValue(response.body(), new TypeReference<>() {});
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Submitted trades: " + trades);
        // TODO: notify if placed != trades
        return res;
    }

    protected static void updatePositions(Long basketId, List<TradeTO> trades) {
        LOG.info("Updating positions");
        ObjectMapper mapper = new ObjectMapper();
        List<PositionTO> res = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(Socket.BITFINEX.value(), Path.POSITION_UPDATE.value(), String.valueOf(basketId))))
                    .header(HEADER_NAME_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(trades)))
                    .build();
            HttpResponse<String> response;
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            res = mapper.readValue(response.body(), new TypeReference<>() {});
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
