package com.example.pluto.requesters;

import com.example.pluto.PlutoConstants;
import com.example.pluto.apiclients.tos.SpotTO;
import com.example.pluto.controllers.IBitfinexService;
import com.example.pluto.entities.BookTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.pluto.PlutoConstants.HEADER_NAME_CONTENT_TYPE;
import static com.example.pluto.PlutoConstants.HEADER_VALUE_APPLICATION_JSON;

public class BitfinexRequesterImpl implements IBitfinexService {

    public boolean saveSpot(String instrument, SpotTO spot) {
        // TODO: if spot != null pass it in the body of the request
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(), PlutoConstants.Path.BITFINEX_SPOT.value(), instrument)))
                .method(RequestMethod.POST.name(), HttpRequest.BodyPublishers.noBody())
                .setHeader(PlutoConstants.HEADER_NAME_CONTENT_TYPE, PlutoConstants.HEADER_VALUE_APPLICATION_JSON)
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
        // TODO: check response body to determine if success
        return true;
    }
    protected static String buildUrl(String... split) {
        StringBuilder sb = new StringBuilder(PlutoConstants.HTTP_PREFIX);
        for (int i = 0; i <= split.length - 1; i++) {
            sb.append(split[i]);
        }
        return sb.toString();
    }

    @Override
    public List<SpotEntity> getSpotsHist(String instruments) {
        return null;
    }

    @Override
    public List<SpotEntity> getSpots(String instruments, Long start, Long end) {
        String bounds = String.format("/%s/%s", start, end);
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(),
                        PlutoConstants.Path.BITFINEX_SPOTS.value(),
                        instruments+bounds)))
                .build();
        HttpResponse<String> response;
        List<SpotEntity> spots = new LinkedList<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            spots = new ObjectMapper().readValue(response.body(), new TypeReference<>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return spots;
    }

    @Override
    public Map<String, SpotEntity> getSpots(String instruments) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(),
                        PlutoConstants.Path.BITFINEX_SPOTS.value(),
                        instruments)))
                .build();
        HttpResponse<String> response;
        Map<String, SpotEntity> spots = new HashMap<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            spots = new ObjectMapper().readValue(response.body(), new TypeReference<>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return spots;
    }

    @Override
    public SpotEntity getSpot(String instrument, String time) {
        return null;
    }

    @Override
    public boolean saveSpot(String instrument, SpotEntity spot) {
        return false;
    }

    @Override
    public int getVolume(String instrument, String time) {
        return 0;
    }

    @Override
    public BookTO getBook(String instrument, String time) {
        return null;
    }

    @Override
    public List<TradeTO> trade(List<TradeTO> trades) {
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
        // TODO: notify if placed != trades
        return res;
    }

    @Override
    public List<TradeTO> updateChangedTrades(List<TradeTO> trades) {
        ObjectMapper mapper = new ObjectMapper();
        List<TradeTO> res = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(), PlutoConstants.Path.BITFINEX_POSTTRADE.value())))
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
        return res;
    }

    @Override
    public List<PositionTO> getAllPositions() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(), PlutoConstants.Path.POSITION_LAST.value()))).build();
        HttpResponse<String> response = null;
        List<PositionTO> positionTOList = new LinkedList<>();
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            positionTOList = new ObjectMapper().readValue(response.body(), new TypeReference<>(){});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        Map<String, BigDecimal> positions = new HashMap<>(positionTOList.size());
//        positionTOList.forEach(p -> positions.put(p.getCurrency(), p.getQuantity()));
        return positionTOList;
    }

    @Override
    public List<PositionTO> getAllCcyLastPositions() {
        return null;
    }

    @Override
    public List<PositionTO> getBasketPositions(Integer basketId) {
        return null;
    }

    @Override
    public List<PositionTO> updatePositionsIfTradesFilled(Long basketId, List<TradeTO> trades) {
        ObjectMapper mapper = new ObjectMapper();
        List<PositionTO> res = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(), PlutoConstants.Path.POSITION_UPDATE.value(), String.valueOf(basketId))))
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
        return res;
    }

    private static List<Map<String, SpotEntity>> spotsMapInList(List<SpotEntity> spots) {
        spots = spots.stream().sorted(Comparator.comparing(SpotEntity::getTimestamp)).collect(Collectors.toList());

        Timestamp ref = spots.get(0).getTimestamp();
        List<Map<String, SpotEntity>> snapshots = new LinkedList<>();
        Map<String, SpotEntity> saving = new HashMap<>();
        for (SpotEntity s : spots) {
            if (!s.getTimestamp().equals(ref)) {
                ref = s.getTimestamp();
                snapshots.add(saving);
                saving = new HashMap<>();
            }
            saving.put(s.getInstrument().replace("BTC", ""), s);
        }
        snapshots.add(saving);
        return snapshots;
    }
}
