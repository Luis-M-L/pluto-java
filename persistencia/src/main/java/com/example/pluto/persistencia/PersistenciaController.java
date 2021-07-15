package com.example.pluto.persistencia;

import com.example.pluto.entities.InstrumentTO;
import com.example.pluto.entities.SpotTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/persistence")
public class PersistenciaController {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenciaController.class);

    @Autowired
    PersistenciaService persistenciaService;

    @PostMapping(value = "/spot")
    public void saveSpot(@RequestBody SpotTO spot){
        LOG.info("saveSpot" + spot.toString());
        persistenciaService.save(spot);
    }

    @GetMapping(value = "/instruments")
    public List<InstrumentTO> getInstruments(){
        LOG.info("get instruments");
        return persistenciaService.getAllCryptocurrencies();
    }

    @PostMapping(value = "/instrument")
    public void saveInstrument(@RequestBody InstrumentTO ccy){
        LOG.info("saveInstrument " + ccy.toString());
        persistenciaService.save(ccy);
    }

}
