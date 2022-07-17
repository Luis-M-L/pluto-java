import com.pluto.PlutoConstants;
import com.pluto.entities.BasketTO;
import com.pluto.entities.PositionTO;
import com.pluto.entities.SpotEntity;
import com.pluto.entities.TradeTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.pluto.PlutoConstants.*;

public class RESTClient {

    public static Logger LOG = LoggerFactory.getLogger(RESTClient.class);

    protected static String buildUrl(String... split) {
        StringBuilder sb = new StringBuilder(HTTP_PREFIX);
        for (int i = 0; i <= split.length - 1; i++) {
            sb.append(split[i]);
        }
        return sb.toString();
    }

    public static Map<String, SpotEntity> spotsAsMap(List<SpotEntity> spots) {
        Map<String, SpotEntity> res = new HashMap<>();
        if (spots == null) {
            return res;
        }
        spots.forEach(s -> {
            if (s.getInstrument() != null && s.getInstrument().contains("BTC")){
                res.putIfAbsent(s.getInstrument().replace("BTC", ""), s);
            }
        });

        res.putIfAbsent("BTC", new SpotEntity("BTCBTC", BigDecimal.ONE));
        return res;
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

    private static String getInstrumentsSubpath(Set<String> instruments) {
        StringBuilder sb = new StringBuilder();
        instruments.forEach(i -> sb.append(",").append(i));
        sb.delete(0, 1);
        return sb.toString();
    }
    public static Map<String, SpotEntity> getSpots(Set<String> instruments) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(),
                                                                            PlutoConstants.Path.BITFINEX_SPOTS.value(),
                                                                            getInstrumentsSubpath(instruments))))
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
        LOG.info("Got spots: " + spots);
        return spotsAsMap(spots);
    }

    public static List<Map<String, SpotEntity>> getSpotsHist(Set<String> instruments, long start, long end) {
        String bounds = String.format("/%s/%s", start, end);
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(PlutoConstants.Socket.BITFINEX.value(),
                                                                            PlutoConstants.Path.BITFINEX_SPOTS.value(),
                                                                            getInstrumentsSubpath(instruments), bounds)))
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
        return spotsMapInList(spots);
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

    public static Map<String, BigDecimal> getPositions() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(Socket.BITFINEX.value(), Path.POSITION_LAST.value()))).build();
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
        LOG.info("Got positions: " + positionTOList);
        Map<String, BigDecimal> positions = new HashMap<>(positionTOList.size());
        positionTOList.forEach(p -> positions.put(p.getCurrency(), p.getQuantity()));
        return positions;
    }

    protected static List<TradeTO> callTrader(final List<TradeTO> trades) {
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

    protected static void updatePositions(final Long basketId, final List<TradeTO> trades) {
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

    protected static void updateRecentTrades(final List<TradeTO> trades) {
        LOG.info("Updating trades");
        ObjectMapper mapper = new ObjectMapper();
        List<TradeTO> res = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(Socket.BITFINEX.value(), Path.BITFINEX_POSTTRADE.value())))
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
