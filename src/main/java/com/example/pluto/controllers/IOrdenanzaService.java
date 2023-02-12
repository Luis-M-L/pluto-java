package com.example.pluto.controllers;

import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.InstrumentTO;

import java.util.List;

public interface IOrdenanzaService {

    public List<BasketTO> getAllBaskets();
    public BasketTO getBasket(Long basketId);
    public void saveBasket(BasketTO basket);

    public List<InstrumentTO> getInstruments();
    public void saveInstrument(InstrumentTO ccy);
}
