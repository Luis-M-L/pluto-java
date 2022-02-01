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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PositionsService {

    private static final Logger LOG = LoggerFactory.getLogger(PositionsService.class);

    @Autowired
    public PositionRepository positionRepository;

    @Autowired
    public BasketRepository basketRepository;

    private static final MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);

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
        Double buyedQty = trade.getAmount() > 0 ? trade.getAmount() : - new BigDecimal(trade.getAmount()).multiply(trade.getPrice(), mathContext).setScale(10, RoundingMode.HALF_UP).doubleValue();
        Double selledQty = trade.getAmount() > 0 ? - new BigDecimal(trade.getAmount()).multiply(trade.getPrice(), mathContext).setScale(10, RoundingMode.HALF_UP).doubleValue() : trade.getAmount();
        String buyed = trade.getAmount() > 0 ? trade.getBase() : trade.getQuoted();
        String selled = trade.getAmount() > 0 ? trade.getQuoted() : trade.getBase();
        res.add(positionRepository.save(new PositionTO(null, basket, buyed, buyedQty + getLastAmount(basket, buyed))));
        res.add(positionRepository.save(new PositionTO(null, basket, selled, selledQty + getLastAmount(basket, selled))));
        return res;
    }

    private Double getLastAmount(BasketTO basket, String buyed) {
        PositionTO last = positionRepository.findLastByBasketCurrency(basket, buyed);
        return last != null && last.getQuantity() != null ? last.getQuantity() : 0.0;
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

    public List<PositionTO> updatePositionsIfTradesFilled(Long basketId, List<TradeTO> trades, Map<String, List<TradeTO>> inactiveTrades) {
        List<TradeTO> filledTrades = trades.stream().filter(t -> {
            List<TradeTO> pairTrades = inactiveTrades.get(t.getPair());
            if (pairTrades != null) {
                int idx = pairTrades.indexOf(t);
                return idx > -1 && TradeTO.CLOSED_STATUS.equals(pairTrades.get(idx).getStatus());
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        return updatePositions(basketId, filledTrades);
    }
}
