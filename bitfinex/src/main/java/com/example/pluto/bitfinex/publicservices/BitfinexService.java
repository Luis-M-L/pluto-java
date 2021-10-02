package com.example.pluto.bitfinex.publicservices;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.bitfinex.BitfinexParser;
import com.example.pluto.bitfinex.repositories.SpotRepository;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.exchanges.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class BitfinexService implements ExchangeService {

    @Autowired
    public BitfinexAPIClient client;

    @Autowired
    public BitfinexParser parser;

    @Autowired
    public SpotRepository spotRepository;

    @Override
    public SpotTO getSpot(String instrument) {
        String spot = null;
        SpotTO spotTO = null;
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("symbols", "t"+instrument);
            spot = client.publicGet(Arrays.asList("v2", "tickers"), params);
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

    @Override
    public SpotTO saveSpot(SpotTO spot) {
        return spotRepository.save(spot);
    }

}
