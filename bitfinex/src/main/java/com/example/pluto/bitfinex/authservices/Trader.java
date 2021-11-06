package com.example.pluto.bitfinex.authservices;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.bitfinex.repositories.TradeRepository;
import com.example.pluto.entities.TradeTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Trader implements Runnable {

    private BitfinexAPIClient client;
    private TradeTO trade;
    private Map<String, Double> tradesQueue;

    @Autowired
    public TradeRepository tradeRepository;

    public Trader (BitfinexAPIClient client, TradeTO trade, Map<String, Double> tradesQueue) {
        this.client = client;
        this.trade = trade;
        this.tradesQueue = tradesQueue;
    }

    @Override
    public void run() {
        HttpResponse response = client.authPost(Arrays.asList("v2", "auth", "w", "order", "submit"), new HashMap<>(), buildBody());
        if (response.statusCode() == 200) {
            tradesQueue.remove(trade);
        }
    }

    private String buildBody() {
        return "{" +
                "\"type\": \"EXCHANGE LIMIT\", " +
                "\"symbol\": \"tIOTETH\", " +
                "\"price\": \"0.00029\", " +
                "\"amount\": \"-50\"" +
                "}";
    }
}
