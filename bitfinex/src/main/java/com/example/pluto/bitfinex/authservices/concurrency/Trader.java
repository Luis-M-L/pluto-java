package com.example.pluto.bitfinex.authservices.concurrency;

import com.example.pluto.PlutoConstants;
import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.bitfinex.authservices.BitfinexAuthService;
import com.example.pluto.bitfinex.authservices.PositionsService;
import com.example.pluto.bitfinex.parsers.BitfinexParser;
import com.example.pluto.bitfinex.repositories.TradeRepository;
import com.example.pluto.errors.ExchangeError;
import com.example.pluto.entities.TradeTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class Trader implements Callable<TradeTO> {

    private Logger LOG = LoggerFactory.getLogger(Trader.class);

    private BitfinexAPIClient client;
    private TradeTO trade;
    private BitfinexAuthService bitfinexAuthService;
    private PositionsService positionsService;
    private TradeRepository tradeRepository;
    private BitfinexParser parser;

    public Trader (BitfinexAuthService bitfinexAuthService, PositionsService positionsService, BitfinexAPIClient client, BitfinexParser parser, TradeRepository tradeRepository, TradeTO trade) {
        this.bitfinexAuthService = bitfinexAuthService;
        this.positionsService = positionsService;
        this.client = client;
        this.parser = parser;
        this.tradeRepository = tradeRepository;
        this.trade = trade;
    }

    @Override
    public TradeTO call() {
        TradeTO saved = null;
        HttpResponse response = client.authPost(Arrays.asList("v2", "auth", "w", "order", "submit"), new HashMap<>(), buildBody());
        if (response != null && response.statusCode() == 200) {
            if (response.body() != null) {
                TradeTO body = parser.convertOrderIntoTrade(trade, response.body().toString());
                body.setId(trade.getId());
                body.setIssuedTimestamp(trade.getIssuedTimestamp());
                saved = tradeRepository.save(body);
            }
        } else {
            ExchangeError error = response != null ? parser.getError(String.valueOf(response.body())) : new ExchangeError(ExchangeError.NO_RESPONSE_FROM_EXCHANGE);
            LOG.error(error.getMessage());
        }
        return saved;
    }

    private String buildBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"type\": \"").append(PlutoConstants.OrderType.EXCHANGE_MARKET.value()).append("\", ")
            .append("\"symbol\": \"t").append(trade.getPair()).append("\", ")
            .append("\"price\": \"").append(trade.getPrice()).append("\", ")
            .append("\"amount\": \"").append(trade.getAmount()).append("\"}");
        return sb.toString();
    }

}
