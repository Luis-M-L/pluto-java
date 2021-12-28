package com.example.pluto.bitfinex.authservices;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.bitfinex.authservices.concurrency.Trader;
import com.example.pluto.bitfinex.parsers.BitfinexParser;
import com.example.pluto.bitfinex.repositories.TradeRepository;
import com.example.pluto.entities.TradeTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BitfinexAuthService {

    @Value("${pluto.acting.threshold}")
    private double threshold;

    private static final Logger LOGGER = LoggerFactory.getLogger(BitfinexAuthService.class);

    @Autowired
    public BitfinexAPIClient client;

    @Autowired
    public BitfinexParser parser;

    @Autowired
    public BitfinexAuthService authService;

    @Autowired
    public PositionsService positionsService;

    @Autowired
    public TradeRepository tradeRepository;

    public BitfinexAuthService() {
    }

    public void trade(List<TradeTO> defTrades) {
        List<TradeTO> activeOrders = authService.getActiveOrders();
        defTrades.forEach( t -> {
            List<TradeTO> filteredActiveOrders = activeOrders.stream().filter(ao -> ao.looksAlike(t, threshold)).collect(Collectors.toList());
            if (filteredActiveOrders.isEmpty()){
                new Thread(new Trader(authService, positionsService, client, parser, tradeRepository, t)).start();
            }
        });
    }

    public List<TradeTO> getActiveOrders() {
        HttpResponse response = client.authPost(Arrays.asList("v2", "auth", "r", "orders"), new HashMap<>(), "");
        return parser.parseOrders(response.body().toString());
    }

    public List<TradeTO> getUnactiveOrders(String pair) {
        HttpResponse response = client.authPost(Arrays.asList("v2", "auth", "r", "orders", "t"+pair, "hist"), new HashMap<>(), "{\"limit\": 5}");
        return parser.parseOrders(response.body().toString());
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
