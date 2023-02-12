package com.example.pluto.controllers;

import com.example.pluto.entities.BasketTO;
import com.example.pluto.services.BasketsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/basket")
public class BasketsController {

    private static final Logger LOG = LoggerFactory.getLogger(BasketsController.class);

    @Autowired
    public BasketsService basketsService;

    @GetMapping(value = "/all")
    public List<BasketTO> getAllBaskets(){
        LOG.info("get all baskets");
        return basketsService.getAllBaskets();
    }

    @GetMapping(value = "/{basketId}")
    public BasketTO getBasket(@PathVariable(value = "basketId") Long basketId){
        LOG.info("get basket " + basketId);
        return basketsService.getBasket(basketId);
    }

    @PostMapping
    public void saveBasket(@RequestBody BasketTO basket){
        LOG.info("save basket");
        basketsService.save(basket);
    }

}
