package com.example.pluto.bitfinex.parsers;

import com.example.pluto.entities.SpotTO;
import com.example.pluto.entities.TradeTO;
import com.example.pluto.exchanges.ExchangeParser;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class BitfinexParser implements ExchangeParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitfinexParser.class);

    private static final String INFO_MSG = "Parsing %: %";

    @Override
    public SpotTO parseSpot(String json) {
        LOGGER.info(String.format(INFO_MSG, "spot", json));
        SpotTO spotTO = new SpotTO();

        // Si el JSON viene vacio no lo procesamos, devolvemos un objeto vacio
        if (json.length() < 3){
            return spotTO;
        }

        JSONParser parser = new JSONParser(json);
        try {
            List<Object> jsonArray = (List<Object>) parser.list().get(0);
            spotTO.setInstrument(((String) jsonArray.get(0)).substring(1));
            spotTO.setBid(numberToDouble(jsonArray.get(1)));
            spotTO.setOffer(numberToDouble(jsonArray.get(3)));
            spotTO.setVolume(numberToDouble(jsonArray.get(8)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Timestamp timestamp = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        spotTO.setTimestamp(timestamp);

        return spotTO;
    }

    @Override
    public TradeTO parseTrade(String json) {
        LOGGER.info(String.format(INFO_MSG, "trade", json));
        TradeTO tradeTO = new TradeTO();

        if (json.length() < 3 ) {
            return tradeTO;
        }

        JSONParser parser = new JSONParser(json);
        try {
            List<Object> lista = parser.list();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tradeTO;
    }

    private Double numberToDouble(Object obj) {
        Double result;
        if (obj instanceof BigInteger) {
            result = ((BigInteger) obj).doubleValue();
        } else if (obj instanceof BigDecimal){
            result = ((BigDecimal) obj).doubleValue();
        }else {
            result = (Double) obj;
        }
        return result;
    }
}
