package com.example.pluto.apiclients.parsers;

import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;
import com.example.pluto.entities.errors.ExchangeError;

import java.util.List;

public interface IExchangeParser {

    SpotEntity parseSpot(String json);

    List<SpotEntity> parseSpots(String json);

    TradeTO parseTrade(String json);

    ExchangeError getError(String body);

    List<PositionTO> parsePositions(String json);
}
