package com.example.pluto.bitfinex.controllers;

import com.example.pluto.bitfinex.authservices.PositionsService;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.TradeTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/position")
public class PositionsController {

    private static final Logger LOG = LoggerFactory.getLogger(PositionsController.class);

    @Autowired
    public PositionsService positionService;

    @GetMapping(value = "/all")
    public List<PositionTO> getAllPositions() {
        LOG.info("get all positions");
        return positionService.getAllPositions();
    }

    @GetMapping(value = "/basket/{basketId}")
    public List<PositionTO> getBasketPositions(@PathVariable(value = "basketId") Integer basketId) {
        LOG.info("get positions of basket " + basketId);
        return positionService.getBasketPositions(basketId);
    }

    @PostMapping(value = "/update/{basketId}")
    public List<PositionTO> updatePositions(@PathVariable(value = "basketId") Long basketId, @RequestBody(required = false) List<TradeTO> trades) {
        return positionService.updatePositions(basketId, trades);
    }

}
