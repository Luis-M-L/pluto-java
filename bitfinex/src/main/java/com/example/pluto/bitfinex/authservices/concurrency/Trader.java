package com.example.pluto.bitfinex.authservices.concurrency;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.bitfinex.authservices.BitfinexAuthService;
import com.example.pluto.bitfinex.parsers.BitfinexParser;
import com.example.pluto.bitfinex.repositories.TradeRepository;
import com.example.pluto.entities.TradeTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Trader implements Runnable {

    private Logger LOG = LoggerFactory.getLogger(Trader.class);

    private static final String EXCHANGE_LIMIT = "EXCHANGE LIMIT";

    private BitfinexAPIClient client;
    private TradeTO trade;
    private BitfinexAuthService service;
    private TradeRepository tradeRepository;
    private BitfinexParser parser;

    public Trader (BitfinexAuthService service, BitfinexAPIClient client, BitfinexParser parser, TradeRepository tradeRepository, TradeTO trade) {
        this.service = service;
        this.client = client;
        this.parser = parser;
        this.tradeRepository = tradeRepository;
        this.trade = trade;
    }

    @Override
    public void run() {
        HttpResponse response = client.authPost(Arrays.asList("v2", "auth", "w", "order", "submit"), new HashMap<>(), buildBody());
        if (response.statusCode() == 200) {
            if (response.body() != null) {
                TradeTO body = parser.convertOrderIntoTrade(trade, response.body().toString());
                body.setId(trade.getId());
                body.setIssuedTimestamp(trade.getIssuedTimestamp());
                TradeTO saved = tradeRepository.save(body);
                awaitFilling(saved);
            }
        }
    }

    private String buildBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"type\": \"").append(EXCHANGE_LIMIT).append("\", ")
            .append("\"symbol\": \"t").append(trade.getPair()).append("\", ")
            .append("\"price\": \"").append(trade.getPrice()).append("\", ")
            .append("\"amount\": \"").append(trade.getAmount()).append("\"}");
        return sb.toString();
    }

    private void awaitFilling(TradeTO trade) {
        synchronized (this){
            try {
                this.wait(30000);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            }
        }

        List<TradeTO> unactive = service.getUnactiveOrders(trade.getPair());
        boolean isTradeUnactive = unactive.stream().filter(t -> t.looksAlike(trade)).collect(Collectors.toList()).isEmpty();
        if (isTradeUnactive) {
            awaitFilling(trade);
        } else {
            trade.setStatus(unactive.get(0).getStatus());
            tradeRepository.save(trade);
        }
    }
}
