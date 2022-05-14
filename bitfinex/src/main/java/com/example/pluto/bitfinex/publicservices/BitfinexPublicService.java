package com.example.pluto.bitfinex.publicservices;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.bitfinex.parsers.BitfinexParser;
import com.example.pluto.bitfinex.repositories.SpotRepository;
import com.example.pluto.entities.SpotEntity;
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
import java.util.*;

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
    public List<SpotEntity> getSpots() {
        Timestamp someago = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS).minusSeconds(60));
        Query q = em.createNamedQuery("getAllLast").setParameter("someago", someago);
        return q.getResultList();
    }

    @Override
    public List<SpotEntity> getSpots(List<String> instruments) {
        List spots = null;
        StringBuilder sb = new StringBuilder();
        instruments.forEach(i -> {
            if (!"BTC".equals(i)) {
                sb.append(",t").append(i).append("BTC");
            }
        });
        sb.delete(0, 1);
        Map<String, String> params = new HashMap<>(1);
        params.put("symbols", sb.toString());
        HttpResponse response = null;
        try {
            response = client.publicGet(Arrays.asList("v2", "tickers"), params);
            if (response.statusCode() == 200) {
                spots = parser.parseSpots(response.body().toString());
            } else {
                LOGGER.error("Error getting spot from Bitfinex: " + instruments, response.body());
                spots = new LinkedList();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return spots;
    }

    @Override
    public SpotEntity getSpot(String instrument) {
        String spot = null;
        SpotEntity spotEntity = null;
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("symbols", "t"+instrument);
            HttpResponse response =  client.publicGet(Arrays.asList("v2", "tickers"), params);
            if (response.statusCode() == 200) {
                spot = response.body().toString();
                spotEntity = parser.parseSpot(spot);
            } else {
                LOGGER.error("Error getting spot from Bitfinex: " + instrument, response.body());
                spotEntity = new SpotEntity();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return spotEntity;
    }

    @Override
    public SpotEntity getSpot(String instrument, String time) {
        return null;
    }

    @Override
    public SpotEntity saveSpot(SpotEntity spot) {
        return spotRepository.save(spot);
    }

}
