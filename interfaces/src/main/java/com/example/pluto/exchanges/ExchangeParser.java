package com.example.pluto.exchanges;

import com.example.pluto.entities.SpotTO;

public interface ExchangeParser {

    SpotTO parseSpot(String json);

}
