package com.example.pluto.bitfinex.authservices;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.bitfinex.parsers.BitfinexParser;
import com.example.pluto.bitfinex.repositories.TradeRepository;
import com.example.pluto.entities.TradeTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Trader implements Runnable {

    private static final String EXCHANGE_LIMIT = "EXCHANGE LIMIT";

    private BitfinexAPIClient client;
    private TradeTO trade;
    private Map<String, Double> tradesQueue;

    @Autowired
    public TradeRepository tradeRepository;

    @Autowired
    public BitfinexParser parser;

    public Trader (BitfinexAPIClient client, BitfinexParser parser, TradeTO trade, Map<String, Double> tradesQueue) {
        this.client = client;
        this.parser = parser;
        this.trade = trade;
        this.tradesQueue = tradesQueue;
    }

    @Override
    public void run() {
        HttpResponse response = client.authPost(Arrays.asList("v2", "auth", "w", "order", "submit"), new HashMap<>(), buildBody());
        if (response.statusCode() == 200) {
            tradesQueue.remove(trade);
            if (response.body() != null) {
                TradeTO body = parser.parseTrade(trade, response.body().toString());
                tradeRepository.save(body);
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
}
