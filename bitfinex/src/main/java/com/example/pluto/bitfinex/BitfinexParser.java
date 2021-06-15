package com.example.pluto.bitfinex;

import com.example.pluto.entities.SpotTO;
import com.example.pluto.exchanges.ExchangeParser;
import org.springframework.stereotype.Component;

@Component
public class BitfinexParser implements ExchangeParser {

    @Override
    public SpotTO parseSpot(String json) {
        return null;
    }
}
