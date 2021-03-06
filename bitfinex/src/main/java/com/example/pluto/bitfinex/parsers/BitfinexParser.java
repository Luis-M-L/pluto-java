package com.example.pluto.bitfinex.parsers;

import com.example.pluto.entities.SpotTO;
import com.example.pluto.entities.TradeTO;
import com.example.pluto.exchanges.ExchangeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.stream.JsonParser;
import java.io.StringReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    public SpotTO parseSpot(String json) {
        LOGGER.info(String.format(INFO_MSG, "spot", json));
        SpotTO spot = new SpotTO();

        // si el json viene vacío no lo procesamos
        if (json.length() < 3) {
            return spot;
        }

        JsonArray array = getJsonArray(json).getJsonArray(0);
        spot.setInstrument(array.getString(0).substring(1));
        spot.setBid(array.getJsonNumber(1).doubleValue());
        spot.setOffer(array.getJsonNumber(3).doubleValue());
        spot.setVolume(array.getJsonNumber(8).doubleValue());
        Timestamp timestamp = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        spot.setTimestamp(timestamp);

        return spot;
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

    private TradeTO mapArrayToTrade(JsonArray innerArray) {
        TradeTO tradeTO = new TradeTO();
        tradeTO.setExchangeId(innerArray.getJsonNumber(0).longValue());
        tradeTO.setEffectiveTimestamp(new Timestamp(innerArray.getJsonNumber(4).longValue()));
        tradeTO.setPair(innerArray.getString(3).substring(1));
        tradeTO.setPrice(innerArray.getJsonNumber(16).bigDecimalValue());
        tradeTO.setAmount(innerArray.getJsonNumber(6).doubleValue());
        tradeTO.setStatus(innerArray.getString(13));
        return tradeTO;
    }

    public TradeTO convertOrderIntoTrade(TradeTO tradeTO, String json) {
        LOGGER.info(String.format(INFO_MSG, "trade", json));
        LOGGER.info(json);

        TradeTO parsed = parseTrade(json);
        parsed.setId(tradeTO.getId());
        parsed.setIssuedTimestamp(tradeTO.getIssuedTimestamp());

        return tradeTO;
    }

    public List<TradeTO> parseOrders(String json) {
        List<TradeTO> trades = new ArrayList();
        JsonArray array = getJsonArray(json);
        array.iterator().forEachRemaining(t -> trades.add(mapArrayToTrade((JsonArray) t)));
        return trades;
    }
}
