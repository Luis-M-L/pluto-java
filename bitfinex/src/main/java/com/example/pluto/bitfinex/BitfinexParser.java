package com.example.pluto.bitfinex;

import com.example.pluto.entities.SpotTO;
import com.example.pluto.exchanges.ExchangeParser;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class BitfinexParser implements ExchangeParser {

    @Override
    public SpotTO parseSpot(String json) {
        SpotTO spotTO = new SpotTO();
        System.out.println("json " + json);

        // Si el JSON viene vacio no lo procesamos, devolvemos un objeto vacio
        if(json.length() < 3){
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
