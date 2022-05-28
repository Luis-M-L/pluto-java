package com.example.pluto.bitfinex.parsers;

import com.example.pluto.bitfinex.parsers.to.SpotTO;
import com.example.pluto.bitfinex.parsers.to.WalletTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;
import com.example.pluto.errors.ExchangeError;
import com.example.pluto.exchanges.ExchangeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.stream.JsonParser;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
public class BitfinexParser implements ExchangeParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitfinexParser.class);

    private static final String INFO_MSG = "Parsing %s : %s";

    private JsonArray getJsonArray(String json) {
        JsonParser parser = Json.createParser(new StringReader(json));
        parser.next();
        return parser.getArray();
    }

    @Override
    public SpotEntity parseSpot(String json) {
        LOGGER.info(String.format(INFO_MSG, "spot", json));
        SpotEntity spot = new SpotEntity();

        // si el json viene vacío no lo procesamos
        if (json.length() < 3) {
            return spot;
        }

        JsonArray array = getJsonArray(json).getJsonArray(0);
        spot.setInstrument(array.getString(0).substring(1));
        spot.setBid(array.getJsonNumber(1).bigDecimalValue());
        spot.setOffer(array.getJsonNumber(3).bigDecimalValue());
        spot.setVolume(array.getJsonNumber(8).bigDecimalValue());
        Timestamp timestamp = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        spot.setTimestamp(timestamp);

        return spot;
    }

    public List<SpotEntity> parseSpots(String json) {
        JsonArray array = getJsonArray(json);
        List<SpotEntity> spots = new LinkedList<>();
        array.iterator().forEachRemaining(s -> spots.add(new SpotTO((JsonArray) s).toSpot()));
        return spots;
    }

    @Override
    public TradeTO parseTrade(String json) {
        // si el json viene vacio no lo procesamos
        if (json.length() < 3) {
            return new TradeTO();
        }

        JsonArray array = getJsonArray(json);
        JsonArray innerArray = array.getJsonArray(4).getJsonArray(0);
        TradeTO tradeTO = mapArrayToTrade(innerArray);

        return tradeTO;
    }

    @Override
    public ExchangeError getError(String body) {
        ExchangeError error = new ExchangeError();
        JsonArray array = getJsonArray(body);
        error.setErrorCode(String.valueOf(array.getJsonNumber(1)));
        error.setMessage(array.getString(2));
        return error;
    }

    @Override
    public List<PositionTO> parsePositions(String json) {
        LOGGER.debug("Positions received: " + json);
        JsonArray array = getJsonArray(json);
        List<PositionTO> positions = new LinkedList<>();
        array.iterator().forEachRemaining(t -> positions.add(new WalletTO((JsonArray) t).toPositionTO()));
        return positions;
    }

    private TradeTO mapArrayToTrade(JsonArray innerArray) {
        TradeTO tradeTO = new TradeTO();
        tradeTO.setExchangeId(innerArray.getJsonNumber(2).longValue());
        tradeTO.setEffectiveTimestamp(new Timestamp(innerArray.getJsonNumber(4).longValue()));
        tradeTO.setPair(innerArray.getString(3).substring(1));
        tradeTO.setPrice(innerArray.getJsonNumber(16).bigDecimalValue());
        tradeTO.setAmount(innerArray.getJsonNumber(7).bigDecimalValue());
        String fillingReport = innerArray.getString(13);
        updateTradeWithFillInfo(tradeTO, fillingReport);
        return tradeTO;
    }

    public TradeTO convertOrderIntoTrade(TradeTO tradeTO, String json) {
        LOGGER.info(String.format(INFO_MSG, "trade", json));
        LOGGER.info(json);

        TradeTO parsed = parseTrade(json);
        parsed.setId(tradeTO.getId());
        parsed.setIssuedTimestamp(tradeTO.getIssuedTimestamp());

        return parsed;
    }

    public List<TradeTO> parseOrders(String json) {
        List<TradeTO> trades = new ArrayList();
        JsonArray array = getJsonArray(json);
        array.iterator().forEachRemaining(t -> trades.add(mapArrayToTrade((JsonArray) t)));
        return trades;
    }

    private void updateTradeWithFillInfo(TradeTO trade, String filling) {
        filling = filling.replace(" ", "");
        String[] splits = filling.split("@");
        trade.setStatus(splits[0]);

        if (splits.length > 1) {
            Double[][] sumandos = new Double[splits.length - 1][2];
            double total = 0.0;
            for (int i = 1; i < splits.length; i++) {
                sumandos[i - 1][0] = Double.valueOf(splits[i].substring(0, splits[i].indexOf('(')));
                Double quantity = Double.valueOf(splits[i].substring(splits[i].indexOf('(') + 1, splits[i].indexOf(')')));
                total += quantity;
                sumandos[i - 1][1] = quantity;
            }

            Double price = sumandos.length == 0 ? trade.getPrice().doubleValue() : 0.0;
            for (int a = 0; a < sumandos.length; a++) {
                price += sumandos[a][0] * sumandos[a][1] / total;
            }
            trade.setPrice(new BigDecimal(price));
        }
    }

    public static BigDecimal getBigDecimalFromJsonArray(JsonArray array, int idx) {
        return array.isNull(idx) ? null : array.getJsonNumber(idx).bigDecimalValue();
    }
}
