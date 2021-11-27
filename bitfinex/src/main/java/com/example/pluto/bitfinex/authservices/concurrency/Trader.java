package com.example.pluto.bitfinex.authservices.concurrency;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.bitfinex.authservices.BitfinexAuthService;
import com.example.pluto.bitfinex.parsers.BitfinexParser;
import com.example.pluto.bitfinex.repositories.TradeRepository;
import com.example.pluto.entities.TradeTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Trader implements Runnable {

    private static final String EXCHANGE_LIMIT = "EXCHANGE LIMIT";

    private BitfinexAPIClient client;
    private TradeTO trade;

    @Autowired
    public BitfinexAuthService service;

    @Autowired
    public TradeRepository tradeRepository;

    @Autowired
    public BitfinexParser parser;

    public Trader (BitfinexAPIClient client, BitfinexParser parser, TradeTO trade) {
        this.client = client;
        this.parser = parser;
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
                waitFilling(saved);
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

    private void waitFilling(TradeTO trade) {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<TradeTO> unactive = service.getUnactiveOrders(trade.getPair());
        if (unactive.contains(trade)) {
            trade.setStatus(unactive.get(unactive.indexOf(trade)).getStatus());
            tradeRepository.save(trade);
        } else {
            waitFilling(trade);
        }
    }
}
