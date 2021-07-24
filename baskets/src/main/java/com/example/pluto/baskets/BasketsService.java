package com.example.pluto.baskets;

import com.example.pluto.baskets.persistencia.BasketRepository;
import com.example.pluto.baskets.persistencia.WeightsRepository;
import com.example.pluto.entities.BasketTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BasketsService {

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
        basketRepository.save(basket);
    }

}
