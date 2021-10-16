package com.example.pluto.bitfinex;

import com.example.pluto.bitfinex.publicservices.BitfinexService;
import com.example.pluto.entities.BookTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.exchanges.ExchangeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/bitfinex")
public class BitfinexController implements ExchangeController {

    private static final Logger LOG = LoggerFactory.getLogger(BitfinexController.class);

    @Autowired
    BitfinexService bitfinexService;

    @Override
    public List<SpotTO> getSpots(){
        LOG.info("get all spots");
        List<SpotTO> spots = new ArrayList<>();
        return bitfinexService.getSpots();
    }

    @Override
    public SpotTO getSpot(String instrument, String time) {
        LOG.info("get Spot " + instrument);
        SpotTO spot = null;
        if(null == time) {
            spot = bitfinexService.getSpot(instrument);
        } else {
            spot = bitfinexService.getSpot(instrument, time);
        }
        return spot;
    }

    @Override
    public boolean saveSpot(String instrument, SpotTO spot) {
        LOG.info("save Spot " + instrument);
        boolean success = false;
        if(null == spot) {
            spot = bitfinexService.getSpot(instrument);
        }
        spot = bitfinexService.saveSpot(spot);
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
    public List<PositionTO> getPositions() {
        return new ArrayList<>();
    }

    @Override
    public boolean trade() {
        return false;
    }
}
