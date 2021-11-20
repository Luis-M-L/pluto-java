package com.example.pluto.exchanges;

import com.example.pluto.entities.SpotTO;
import com.example.pluto.entities.TradeTO;

public interface ExchangeParser {

    SpotTO parseSpot(String json);

    TradeTO parseTrade(TradeTO tradeTO, String json);
}
