package com.example.pluto.services.authservices;

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

    public List<PositionTO> getAllCcyLastPositions() {
        List<PositionTO> last = new ArrayList<>();
        positionRepository.findLast().forEach(p -> last.add(p));
        return last;
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

        BigDecimal boughtQty;
        BigDecimal soldQty;
        String bought;
        String sold;

        if (BigDecimal.ZERO.compareTo(trade.getAmount()) == -1) {
            boughtQty = trade.getAmount();
            soldQty = trade.getAmount().multiply(trade.getPrice(), mathContext).setScale(10, RoundingMode.HALF_UP).negate(mathContext);
            bought = trade.getBase();
            sold = trade.getQuoted();
        } else {
            boughtQty = trade.getAmount().multiply(trade.getPrice(), mathContext).setScale(10, RoundingMode.HALF_UP).negate(mathContext);
            soldQty = trade.getAmount();
            bought = trade.getQuoted();
            sold = trade.getBase();
        }
        BigDecimal boughtCommission = BigDecimal.valueOf(0.002).multiply(boughtQty, mathContext).setScale(10, RoundingMode.HALF_UP);

        BigDecimal finallyBought = boughtQty.add(getLastAmount(basket, bought)).subtract(boughtCommission);
        BigDecimal finallySold = soldQty.add(getLastAmount(basket, sold));

        res.add(positionRepository.save(new PositionTO(null, basket, bought, finallyBought)));
        res.add(positionRepository.save(new PositionTO(null, basket, sold, finallySold)));
        return res;
    }

    private BigDecimal getLastAmount(BasketTO basket, String buyed) {
        PositionTO last = positionRepository.findLastByBasketCurrency(basket, buyed);
        return last != null && last.getQuantity() != null ? last.getQuantity() : BigDecimal.ZERO;
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
