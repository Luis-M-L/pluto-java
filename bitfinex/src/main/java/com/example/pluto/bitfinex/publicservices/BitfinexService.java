package com.example.pluto.bitfinex.publicservices;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.bitfinex.BitfinexParser;
import com.example.pluto.bitfinex.repositories.SpotRepository;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.exchanges.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BitfinexService implements ExchangeService {

    @Autowired
    public BitfinexAPIClient client;

    @Autowired
    public BitfinexParser parser;

    @Autowired
    public SpotRepository spotRepository;

    @Autowired
    public EntityManager em;

    @Override
    public List<SpotTO> getSpots() {
        Timestamp someago = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS).minusSeconds(60));
        Query q = em.createNamedQuery("getAllLast").setParameter("someago", someago);
        return q.getResultList();
    }

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
