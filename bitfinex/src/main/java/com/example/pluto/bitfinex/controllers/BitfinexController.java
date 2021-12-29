package com.example.pluto.bitfinex.controllers;

import com.example.pluto.bitfinex.authservices.BitfinexAuthService;
import com.example.pluto.bitfinex.publicservices.BitfinexPublicService;
import com.example.pluto.entities.BookTO;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.entities.TradeTO;
import com.example.pluto.exchanges.ExchangeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/bitfinex")
public class BitfinexController implements ExchangeController {

    private static final Logger LOG = LoggerFactory.getLogger(BitfinexController.class);

    @Autowired
    public BitfinexPublicService bitfinexPublicService;

    @Autowired
    public BitfinexAuthService bitfinexAuthService;

    @Override
    public List<SpotTO> getSpots(){
        LOG.info("get all spots");
        List<SpotTO> spots = new ArrayList<>();
        return bitfinexPublicService.getSpots();
    }

    @Override
    public SpotTO getSpot(String instrument, String time) {
        LOG.info("get Spot " + instrument);
        SpotTO spot = null;
        if(null == time) {
            spot = bitfinexPublicService.getSpot(instrument);
        } else {
            spot = bitfinexPublicService.getSpot(instrument, time);
        }
        return spot;
    }

    @Override
    public boolean saveSpot(String instrument, SpotTO spot) {
        LOG.info("save Spot " + instrument);
        boolean success = false;
        if(null == spot) {
            spot = bitfinexPublicService.getSpot(instrument);
        }
        spot = bitfinexPublicService.saveSpot(spot);
        success = spot.getId() != null;
        return success;
    }

    @Override
    public int getVolume(String instrument, String time) {
        return 0;
    }

    @Override
    public BookTO getBook(String instrument, String time) {
        return null;
    }

    @Override
    public List<TradeTO> trade(List<TradeTO> defTrades) {
        LOG.info("Received trades: " + defTrades);
        bitfinexAuthService.trade(defTrades);
        return defTrades;
    }

    @Override
    public List<TradeTO> updateChangedTrades(List<TradeTO> placed) {
        return bitfinexAuthService.updateIfChanged(placed);
    }

    @GetMapping(value = "user")
    public void getUserInfo() {
        bitfinexAuthService.getUserInfo();
    }
}
