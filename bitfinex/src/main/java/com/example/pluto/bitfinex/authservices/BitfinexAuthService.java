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
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class BitfinexAuthService {

    @Value("${pluto.acting.threshold}")
    private double threshold;

    private static final Logger LOG = LoggerFactory.getLogger(BitfinexAuthService.class);

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

    public ExecutorService executor = Executors.newWorkStealingPool();

    public BitfinexAuthService() {
    }

    public List<TradeTO> trade(List<TradeTO> defTrades) {
        List<TradeTO> openTrades = null;
        try {
            List<TradeTO> activeOrders = authService.getActiveOrders();
            List<Future<TradeTO>> sentTrades = sendTradeIfUnexistent(defTrades, activeOrders);
            executor.awaitTermination(10, TimeUnit.SECONDS);
            openTrades = tradeFuturesToTradeList(sentTrades);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
        return openTrades != null ? openTrades : new ArrayList<>(0);
    }

    private List<Future<TradeTO>> sendTradeIfUnexistent(List<TradeTO> defTrades, List<TradeTO> activeOrders) {
        List<Future<TradeTO>> sentTrades = new ArrayList<>(defTrades != null ? defTrades.size() : 0);
        defTrades.forEach(t -> {
            List<TradeTO> filteredActiveOrders = activeOrders.stream().filter(ao -> ao.looksAlike(t, threshold)).collect(Collectors.toList());
            if (filteredActiveOrders.isEmpty()){
                Future<TradeTO> sentTrade = executor.submit(new Trader(authService, positionsService, client, parser, tradeRepository, t));
                sentTrades.add(sentTrade);
            }
        });
        return sentTrades;
    }

    private List<TradeTO> tradeFuturesToTradeList(List<Future<TradeTO>> sentTrades) {
        List<TradeTO> openTrades = new ArrayList<>(sentTrades.size());
        sentTrades.forEach(f -> {
            try {
                if (f.get() != null) {
                    openTrades.add(f.get());
                }
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            } catch (ExecutionException e) {
                LOG.error(e.getMessage());
            }
        });
        return openTrades;
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
            LOG.error("Error retrieving user info from Bitfinex", response.body());
        }
        return userInfo;
    }

    public List<TradeTO> filterUnactive(List<TradeTO> placed) {
        List<TradeTO> filtered = new ArrayList<>();
        Map<String, List<TradeTO>> unactive = new HashMap<>();
        for (TradeTO p : placed) {
            String pair = p.getPair();
            if (!unactive.containsKey(pair)) {
                unactive.put(pair, getUnactiveOrders(pair));
            }
            unactive.get(pair).forEach(u -> {
                if (u.getExchangeId() != null && u.getExchangeId().equals(p.getExchangeId())) {
                    filtered.add(u);
                }
            });
        }
        return filtered;
    }

    public List<TradeTO> updateIfChanged(List<TradeTO> placed) {
        List<TradeTO> updated = new ArrayList<>();
        tradeRepository.saveAll(filterUnactive(placed)).forEach(t -> updated.add(t));
        return updated;
    }

}
