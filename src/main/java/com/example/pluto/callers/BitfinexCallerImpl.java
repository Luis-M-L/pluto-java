package com.example.pluto.callers;

import com.example.pluto.controllers.IBitfinexService;
import com.example.pluto.controllers.bitfinex.BitfinexController;
import com.example.pluto.controllers.bitfinex.PositionsController;
import com.example.pluto.entities.BookTO;
import com.example.pluto.entities.PositionTO;
import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class BitfinexCallerImpl implements IBitfinexService {
    @Autowired
    public BitfinexController bitfinex;

    @Autowired
    public PositionsController positions;

    @Override
    public List<SpotEntity> getSpotsHist(String instruments) {
        return bitfinex.getSpotsHist(instruments);
    }

    @Override
    public List<SpotEntity> getSpots(String instruments, Long start, Long end) {
        return bitfinex.getSpots(instruments, start, end);
    }

    @Override
    public Map<String, SpotEntity> getSpots(String instruments) {
        return bitfinex.getSpots(instruments);
    }

    @Override
    public SpotEntity getSpot(String instrument, String time) {
        return bitfinex.getSpot(instrument, time);
    }

    @Override
    public boolean saveSpot(String instrument, SpotEntity spot) {
        return bitfinex.saveSpot(instrument, spot);
    }

    @Override
    public int getVolume(String instrument, String time) {
        return bitfinex.getVolume(instrument, time);
    }

    @Override
    public BookTO getBook(String instrument, String time) {
        return bitfinex.getBook(instrument, time);
    }

    @Override
    public List<TradeTO> trade(List<TradeTO> defTrades) {
        return bitfinex.trade(defTrades);
    }

    @Override
    public List<TradeTO> updateChangedTrades(List<TradeTO> placed) {
        return bitfinex.updateChangedTrades(placed);
    }

    @Override
    public List<PositionTO> getAllPositions() {
        return positions.getAllPositions();
    }

    @Override
    public List<PositionTO> getAllCcyLastPositions() {
        return positions.getAllCcyLastPositions();
    }

    @Override
    public List<PositionTO> getBasketPositions(Integer basketId) {
        return positions.getBasketPositions(basketId);
    }

    @Override
    public List<PositionTO> updatePositionsIfTradesFilled(Long basketId, List<TradeTO> trades) {
        return positions.updatePositionsIfTradesFilled(basketId, trades);
    }
}
