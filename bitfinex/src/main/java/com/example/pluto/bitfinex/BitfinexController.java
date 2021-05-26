package com.example.pluto.bitfinex;

import com.example.pluto.entities.BookTO;
import com.example.pluto.entities.SnapshotTO;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.interfaces.ExchangeController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;

@RestController
@RequestMapping(value = "/bitfinex")
public class BitfinexController implements ExchangeController {

    @Autowired
    BitfinexService bitfinexService;

    @Override
    public SnapshotTO getSnapshot(String instrument, String time) {
        SnapshotTO snapshot = null;
        if(null == time) {
            snapshot = bitfinexService.getSnapshot(instrument);
        } else {
            snapshot = bitfinexService.getSnapshot(instrument, time);
        }
        return snapshot;
    }

    @Override
    public SpotTO getSpot(String instrument, String time) {
        System.out.println("getSpot");
        SpotTO spot = null;
        if(null == time) {
            spot = bitfinexService.getSpot(instrument);
        } else {
            spot = bitfinexService.getSpot(instrument, time);
        }
        return spot;
    }

    @Override
    public int getVolume(String instrument, String time) {
        return 0;
    }

    @Override
    public BookTO getBook(String instrument, String time) {
        return null;
    }
}
