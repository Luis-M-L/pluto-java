package com.example.pluto.controllers;

import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.TradeTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface IPositionsController {

    @GetMapping(value = "/all")
    public List<PositionTO> getAllPositions();

    @GetMapping(value = "/last")
    public List<PositionTO> getAllCcyLastPositions();
    @GetMapping(value = "/basket/{basketId}")
    public List<PositionTO> getBasketPositions(@PathVariable(value = "basketId") Integer basketId);
    @PostMapping(value = "/update/{basketId}")
    public List<PositionTO> updatePositionsIfTradesFilled(@PathVariable(value = "basketId") Long basketId, @RequestBody(required = false) List<TradeTO> trades);

    }
