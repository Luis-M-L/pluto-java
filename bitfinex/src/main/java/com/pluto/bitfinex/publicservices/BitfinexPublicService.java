package com.pluto.bitfinex.publicservices;

import com.pluto.bitfinex.BitfinexAPIClient;
import com.pluto.bitfinex.parsers.BitfinexParser;
import com.pluto.bitfinex.repositories.SpotRepository;
import com.pluto.entities.SpotEntity;
import com.pluto.exchanges.ExchangeService;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
        return getSpots(Arrays.asList("v2", "tickers"), instruments, null);
    }

    @Override
    public List<SpotEntity> getSpotsHist(List<String> instruments) {
        List<SpotEntity> spots = new LinkedList<>();

        Map<String, String> params = new HashMap<>(4);
        params.put("limit", "250");
        long end = System.currentTimeMillis();
        long yearAgo = 1621116000000L;
        long start;
        do {
            start = end - 2500000L;
            params.put("start", String.valueOf(start));
            params.put("end", String.valueOf(end));
            List<SpotEntity> split = getSpots(Arrays.asList("v2", "tickers", "hist"), instruments, params);
            spots.addAll(split);

            end = start - 10000L;
        } while (yearAgo < start);
        return spots;
    }

    private List<SpotEntity> getSpots(List<String> path, List<String> instruments, Map<String, String> params) {
        List spots = null;
        params = params == null ? new HashMap<>(1) : params;
        params.put("symbols", getFormatedSymbols(instruments));
        HttpResponse response = null;
        try {
            response = client.publicGet(path, params);
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

    public List<SpotEntity> getSpots(String instruments, Long start, Long end) {
        LocalDateTime startDateTime = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC);
        Timestamp startTimestamp = Timestamp.valueOf(startDateTime);
        LocalDateTime endDateTime = LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.UTC);
        Timestamp endTime = Timestamp.valueOf(endDateTime);
        Iterator<String> it = Arrays.stream(instruments.split(",")).iterator();
        List<String> insts = new LinkedList<>();
        while (it.hasNext()) {
            insts.add(it.next()+"BTC");
        }
        return spotRepository.findAllByInstrumentStartEnd(insts, startTimestamp, endTime);
    }

    private String getFormatedSymbols(List<String> instruments) {
        StringBuilder sb = new StringBuilder();
        instruments.forEach(i -> {
            if (!"BTC".equals(i)) {
                sb.append(",t").append(i).append("BTC");
            }
        });
        sb.delete(0, 1);
        return sb.toString();
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
