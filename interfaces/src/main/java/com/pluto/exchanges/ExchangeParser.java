package com.pluto.exchanges;

import com.pluto.entities.PositionTO;
import com.pluto.errors.ExchangeError;
import com.pluto.entities.SpotEntity;
import com.pluto.entities.TradeTO;

import java.util.List;

public interface ExchangeParser {

    SpotEntity parseSpot(String json);

    List<SpotEntity> parseSpots(String json);

    TradeTO parseTrade(String json);

    ExchangeError getError(String body);

    List<PositionTO> parsePositions(String json);
}
