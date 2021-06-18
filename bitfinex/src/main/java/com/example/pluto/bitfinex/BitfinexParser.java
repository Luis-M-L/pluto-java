package com.example.pluto.bitfinex;

import com.example.pluto.entities.SpotTO;
import com.example.pluto.exchanges.ExchangeParser;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Component
public class BitfinexParser implements ExchangeParser {

    @Override
    public SpotTO parseSpot(String json) {
        SpotTO spotTO = new SpotTO();
        Timestamp timestamp = Timestamp.from(Instant.now());
        String[] trocitos = json.substring(2, json.length() - 3).split(",");

        spotTO.setTimestamp(timestamp);
        spotTO.setInstrument(trocitos[0].substring(2, trocitos[0].length()-1));
        spotTO.setBid(Double.valueOf(trocitos[1]));
        spotTO.setOffer(Double.valueOf(trocitos[3]));
        spotTO.setVolume(Double.valueOf(trocitos[8]));

        return spotTO;
    }
}
