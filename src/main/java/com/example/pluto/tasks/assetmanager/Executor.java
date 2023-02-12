package com.example.pluto.tasks.assetmanager;

import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;

import java.math.BigDecimal;
import java.util.*;

public class Executor extends Calculator implements Runnable {

    @Override
    public void run() {
        BigDecimal threshold = BigDecimal.valueOf(0.01);    // Desviaciones del 1%
        Map<String, BigDecimal> positions = getPositions();
        Map<String, BigDecimal> weights = getWeights();

        Set<String> currencies = new HashSet<>(2);
        currencies.addAll(weights.keySet());
        currencies.addAll(positions.keySet());

        Map<String, SpotEntity> spots = getSpots(currencies);
        BigDecimal btc = getBTCEquivalent(positions, spots);
        Map<String, BigDecimal> qBid = getUpperBounds(btc, spots, weights);
        Map<String, BigDecimal> qAsk = getLowerBounds(btc, spots, weights);
        List<TradeTO> trading = getSellingTrades(qBid, positions, threshold);
        trading.addAll(getBuyingTrades(qAsk, positions, threshold));
        bitfinex.trade(trading);
    }

    @Override
    public Map<String, SpotEntity> getSpots(Set<String> currencies) {
        Set<String> pairs = new HashSet<>(currencies.size());
        currencies.forEach(c -> pairs.add(c+"BTC"));
        StringBuilder sb = new StringBuilder();
        pairs.forEach(p -> sb.append(",").append(p));
        return bitfinex.getSpots(sb.toString().substring(1));
    }

    @Override
    public Map<String, BigDecimal> getPositions() {
        List<PositionTO> positions = bitfinex.getAllCcyLastPositions();
        Map<String, BigDecimal> pos = new HashMap<>();
        positions.forEach(p -> pos.put(p.getCurrency(), p.getQuantity()));
        return pos;
    }

}
