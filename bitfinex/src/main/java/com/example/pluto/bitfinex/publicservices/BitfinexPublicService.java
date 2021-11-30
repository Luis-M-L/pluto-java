package com.example.pluto.bitfinex.publicservices;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.bitfinex.parsers.BitfinexParser;
import com.example.pluto.bitfinex.repositories.SpotRepository;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.exchanges.ExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BitfinexPublicService implements ExchangeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitfinexPublicService.class);

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
            HttpResponse response =  client.publicGet(Arrays.asList("v2", "tickers"), params);
            if (response.statusCode() == 200) {
                spot = response.body().toString();
                spotTO = parser.parseSpot(spot);
            } else {
                LOGGER.error("Error getting spot from Bitfinex: " + instrument, response.body());
                spotTO = new SpotTO();
            }
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
