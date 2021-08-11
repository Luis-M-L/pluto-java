package com.example.pluto.exchanges;

import com.example.pluto.entities.SpotTO;

public interface ExchangeService {

    SpotTO getSpot(String instrument);

    SpotTO getSpot(String instrument, String time);

    SpotTO saveSpot(SpotTO spot);
}
