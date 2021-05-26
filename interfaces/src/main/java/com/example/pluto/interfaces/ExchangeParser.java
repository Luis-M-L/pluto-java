package com.example.pluto.interfaces;

import com.example.pluto.entities.SpotTO;

public interface ExchangeParser {

    SpotTO parseSpot(String json);

}
