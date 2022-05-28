package com.example.pluto.exchanges;

import com.example.pluto.entities.SpotEntity;

import java.util.List;

public interface ExchangeService {

    SpotEntity getSpot(String instrument);

    SpotEntity getSpot(String instrument, String time);

    SpotEntity saveSpot(SpotEntity spot);

    List<SpotEntity> getSpots();

    List<SpotEntity> getSpots(List<String> instruments);

    List<SpotEntity> getSpotsHist(List<String> instruments);
}
