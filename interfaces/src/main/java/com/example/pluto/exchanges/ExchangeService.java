package com.example.pluto.exchanges;

import com.example.pluto.entities.SpotTO;

import java.util.List;

public interface ExchangeService {

    SpotTO getSpot(String instrument);

    SpotTO getSpot(String instrument, String time);

    SpotTO saveSpot(SpotTO spot);

    List<SpotTO> getSpots();
}
