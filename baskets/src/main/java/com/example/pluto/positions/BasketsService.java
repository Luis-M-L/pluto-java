package com.example.pluto.positions;

import com.example.pluto.positions.repositories.BasketRepository;
import com.example.pluto.positions.repositories.WeightsRepository;
import com.example.pluto.entities.BasketTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BasketsService {

    private static final Logger LOG = LoggerFactory.getLogger(BasketsService.class);

    @Autowired
    public BasketRepository basketRepository;

    @Autowired
    public WeightsRepository weightsRepository;

    public List<BasketTO> getAllBaskets(){
        List<BasketTO> all = new ArrayList<>();
        basketRepository.findAll().forEach(b -> all.add(b));
        return all;
    }

    public BasketTO getBasket(Long basketId){
        Optional<BasketTO> result;
        result = basketRepository.findById(basketId);
        return result.get() != null ? result.get() : new BasketTO();
    }

    public void save(BasketTO basket) {
        if(!getAllBaskets().contains(basket)) {
            basketRepository.save(basket);
            basket.getWeights().forEach(w -> weightsRepository.save(w));
        } else {
            LOG.warn("Tried to persist an existing basket: " + basket.getLabel());
        }
    }

}
