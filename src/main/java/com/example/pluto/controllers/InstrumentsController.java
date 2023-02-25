package com.example.pluto.controllers;

import com.example.pluto.entities.InstrumentTO;
import com.example.pluto.services.InstrumentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/instruments")
public class InstrumentsController {

    private static final Logger LOG = LoggerFactory.getLogger(InstrumentsController.class);

    @Autowired
    public InstrumentsService instrumentsService;

    @GetMapping
    public List<InstrumentTO> getInstruments(){
        LOG.info("get instruments");
        return instrumentsService.getAllCryptocurrencies();
    }

    @PostMapping
    public void saveInstrument(@RequestBody InstrumentTO ccy){
        LOG.info("saveInstrument " + ccy.toString());
        instrumentsService.save(ccy);
    }

}
