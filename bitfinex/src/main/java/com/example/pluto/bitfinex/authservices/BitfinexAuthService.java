package com.example.pluto.bitfinex.authservices;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.entities.TradeTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class BitfinexAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitfinexAuthService.class);

    @Autowired
    public BitfinexAPIClient client;

    private HashMap<String, Double> tradesQueue;

    public boolean trade(List<TradeTO> defTrades) {
        defTrades.forEach( t -> {
            new Thread(new Trader(client, t, tradesQueue)).start();
        });
        return false;
    }

    public String getUserInfo() {
        HttpResponse response = client.authPost(Arrays.asList("v2", "auth", "r", "info", "user"), new HashMap<>(), "");
        String userInfo = "";
        if (response.statusCode() == 200) {
            userInfo = response.body().toString();
        } else {
            LOGGER.error("Error retrieving user info from Bitfinex", response.body());
        }
        return userInfo;
    }

}
