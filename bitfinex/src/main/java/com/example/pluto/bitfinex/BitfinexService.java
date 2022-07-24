package com.example.pluto.bitfinex;

import com.example.pluto.entities.SnapshotTO;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.interfaces.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BitfinexService implements ExchangeService {

    @Autowired
    BitfinexAPIClient client;

    @Autowired
    BitfinexParser parser;

    @Override
    public SnapshotTO getSnapshot(String instrument) {
        return null;
    }

    @Override
    public SnapshotTO getSnapshot(String instrument, String time) {
        return null;
    }

    @Override
    public SpotTO getSpot(String instrument) {
        String spot = null;
        SpotTO spotTO = null;
        try {
            spot = client.getSpot(instrument);
            spotTO = parser.parseSpot(spot);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return spotTO;
    }

    @Override
    public SpotTO getSpot(String instrument, String time) {
        return null;
    }

}