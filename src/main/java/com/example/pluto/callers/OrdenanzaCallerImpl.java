package com.example.pluto.callers;

import com.example.pluto.controllers.BasketsController;
import com.example.pluto.controllers.IOrdenanzaService;
import com.example.pluto.controllers.InstrumentsController;
import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.InstrumentTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OrdenanzaCallerImpl implements IOrdenanzaService {

    @Autowired
    public BasketsController baskets;

    @Autowired
    public InstrumentsController instruments;


    @Override
    public List<BasketTO> getAllBaskets() {
        return baskets.getAllBaskets();
    }

    @Override
    public BasketTO getBasket(Long basketId) {
        return baskets.getBasket(basketId);
    }

    @Override
    public void saveBasket(BasketTO basket) {
        baskets.saveBasket(basket);
    }

    @Override
    public List<InstrumentTO> getInstruments() {
        return instruments.getInstruments();
    }

    @Override
    public void saveInstrument(InstrumentTO ccy) {
        saveInstrument(ccy);
    }
}
