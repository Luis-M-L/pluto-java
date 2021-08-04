package com.example.pluto.persistencia;

import com.example.pluto.entities.InstrumentTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/instruments")
public class PersistenciaController {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenciaController.class);

    @Autowired
    PersistenciaService persistenciaService;

    @GetMapping
    public List<InstrumentTO> getInstruments(){
        LOG.info("get instruments");
        return persistenciaService.getAllCryptocurrencies();
    }

    @PostMapping
    public void saveInstrument(@RequestBody InstrumentTO ccy){
        LOG.info("saveInstrument " + ccy.toString());
        persistenciaService.save(ccy);
    }

}
