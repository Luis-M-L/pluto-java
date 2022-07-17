package com.pluto.bitfinex.controllers;

import com.pluto.bitfinex.authservices.BitfinexAuthService;
import com.pluto.bitfinex.publicservices.BitfinexPublicService;
import com.pluto.entities.BookTO;
import com.pluto.entities.SpotEntity;
import com.pluto.entities.TradeTO;
import com.pluto.exchanges.ExchangeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/bitfinex")
public class BitfinexController implements ExchangeController {

    private static final Logger LOG = LoggerFactory.getLogger(BitfinexController.class);

    @Autowired
    public BitfinexPublicService bitfinexPublicService;

    @Autowired
    public BitfinexAuthService bitfinexAuthService;

    @Override
    public List<SpotEntity> getSpotsHist(String instruments) {
        return bitfinexPublicService.getSpotsHist(Arrays.stream(instruments.split(",")).collect(Collectors.toList()));
    }

    @Override
    public List<SpotEntity> getSpots(String instruments, Long start, Long end) {
        return bitfinexPublicService.getSpots(instruments, start, end);
    }

    @Override
    public List<SpotEntity> getSpots(String instruments) {
        LOG.info("get all spots");
        List<SpotEntity> spots = new ArrayList<>();
        return instruments == null ? bitfinexPublicService.getSpots() :
                bitfinexPublicService.getSpots(Arrays.stream(instruments.split(",")).collect(Collectors.toList()));
    }

    @Override
    public SpotEntity getSpot(String instrument, String time) {
        LOG.info("get Spot " + instrument);
        SpotEntity spot = null;
        if(null == time) {
            spot = bitfinexPublicService.getSpot(instrument);
        } else {
            spot = bitfinexPublicService.getSpot(instrument, time);
        }
        return spot;
    }

    @Override
    public boolean saveSpot(String instrument, SpotEntity spot) {
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
        return bitfinexAuthService.trade(defTrades);
    }

    @Override
    public List<TradeTO> updateChangedTrades(List<TradeTO> placed) {
        if (placed == null) {
            return new ArrayList<>(0);
        }
        return bitfinexAuthService.updateChanged(placed);
    }

    @GetMapping(value = "user")
    public void getUserInfo() {
        bitfinexAuthService.getUserInfo();
    }
}
