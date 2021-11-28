package com.example.pluto.bitfinex.authservices;

import com.example.pluto.bitfinex.repositories.PositionRepository;
import com.example.pluto.entities.PositionTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PositionsService {

    private static final Logger LOG = LoggerFactory.getLogger(PositionsService.class);

    @Autowired
    public PositionRepository repository;

    public List<PositionTO> getAllPositions() {
        List<PositionTO> all = new ArrayList<>();
        repository.findAll().forEach(p -> all.add(p));
        return all;
    }

    public List<PositionTO> getBasketPositions(Integer basketId) {
        List<PositionTO> all = new ArrayList<>();
        repository.findByBasket(basketId).forEach(p -> all.add(p));
        return all;
    }
}
