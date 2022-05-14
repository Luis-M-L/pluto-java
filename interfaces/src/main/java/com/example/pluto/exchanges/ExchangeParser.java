package com.example.pluto.exchanges;

import com.example.pluto.entities.PositionTO;
import com.example.pluto.errors.ExchangeError;
import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;

import java.util.List;

public interface ExchangeParser {

    SpotEntity parseSpot(String json);

    TradeTO parseTrade(String json);

    ExchangeError getError(String body);

    List<PositionTO> parsePositions(String json);
}
