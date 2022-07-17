package com.pluto.bitfinex.controllers;

import com.pluto.bitfinex.authservices.BitfinexAuthService;
import com.pluto.bitfinex.authservices.PositionsService;
import com.pluto.entities.PositionTO;
import com.pluto.entities.TradeTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/position")
public class PositionsController {

    private static final Logger LOG = LoggerFactory.getLogger(PositionsController.class);

    @Autowired
    public PositionsService positionService;

    @Autowired
    public BitfinexAuthService bitfinexAuthService;

    @GetMapping(value = "/all")
    public List<PositionTO> getAllPositions() {
        LOG.info("get all positions");
        return positionService.getAllPositions();
    }

    @GetMapping(value = "/last")
    public List<PositionTO> getAllCcyLastPositions() {
        LOG.info("Get last positions for all currencies");
        return bitfinexAuthService.getPositions();
    }

    @GetMapping(value = "/basket/{basketId}")
    public List<PositionTO> getBasketPositions(@PathVariable(value = "basketId") Integer basketId) {
        LOG.info("get positions of basket " + basketId);
        return positionService.getBasketPositions(basketId);
    }

    @PostMapping(value = "/update/{basketId}")
    public List<PositionTO> updatePositionsIfTradesFilled(@PathVariable(value = "basketId") Long basketId, @RequestBody(required = false) List<TradeTO> trades) {
        LOG.info("Update positions related to trades: " + trades);
        Set<String> pairs = getAllPairs(trades);
        Map<String, List<TradeTO>> unactiveTrades = new HashMap<>(pairs.size());
        pairs.forEach(p -> unactiveTrades.putIfAbsent(p, bitfinexAuthService.getUnactiveOrders(p)));
        return positionService.updatePositionsIfTradesFilled(basketId, trades, unactiveTrades);
    }

    private Set<String> getAllPairs(List<TradeTO> trades) {
        Set<String> pairs = new HashSet<>();
        trades.forEach(t -> {
            if (!pairs.contains(t.getPair()) && t.getPair() != null) {
                pairs.add(t.getPair());
            }
        });
        return pairs;
    }

}
