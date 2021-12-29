package com.example.pluto.bitfinex.authservices;

import com.example.pluto.bitfinex.repositories.BasketRepository;
import com.example.pluto.bitfinex.repositories.PositionRepository;
import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.TradeTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PositionsService {

    private static final Logger LOG = LoggerFactory.getLogger(PositionsService.class);

    @Autowired
    public PositionRepository positionRepository;

    @Autowired
    public BasketRepository basketRepository;

    public List<PositionTO> getAllPositions() {
        List<PositionTO> all = new ArrayList<>();
        positionRepository.findAll().forEach(p -> all.add(p));
        return all;
    }

    public List<PositionTO> getBasketPositions(Integer basketId) {
        List<PositionTO> all = new ArrayList<>();
        List<PositionTO> basketPositions = positionRepository.findByBasket(basketId);
        if (basketPositions != null) {
            basketPositions.forEach(p -> all.add(p));
        }
        return all;
    }

    public List<PositionTO> updatePositions(TradeTO trade, BasketTO basket) {
        List<PositionTO> res = new ArrayList<>(2);
        Double buyedQty = trade.getAmount() > 0 ? trade.getAmount() : new BigDecimal(trade.getAmount()).divide(trade.getPrice()).doubleValue();
        Double selledQty = trade.getAmount() > 0 ? new BigDecimal(trade.getAmount()).divide(trade.getPrice()).doubleValue() : trade.getAmount();
        String buyed = trade.getAmount() > 0 ? trade.getBase() : trade.getQuoted();
        String selled = trade.getAmount() > 0 ? trade.getQuoted() : trade.getBase();
        res.add(positionRepository.save(new PositionTO(null, basket, buyed, buyedQty)));
        res.add(positionRepository.save(new PositionTO(null, basket, selled, selledQty)));
        return res;
    }

    public List<PositionTO> updatePositions(Long basketId, List<TradeTO> trades) {
        List<PositionTO> involvedPositions = new ArrayList<>();
        Optional<BasketTO> queryResult = basketRepository.findById(basketId);
        if (!queryResult.isPresent()) {
            LOG.error("Basket " + basketId + " not found in database.");
        } else {
            BasketTO basket = queryResult.get();
            trades.forEach(t -> involvedPositions.addAll(updatePositions(t, basket)));
        }
        return involvedPositions;
    }
}
