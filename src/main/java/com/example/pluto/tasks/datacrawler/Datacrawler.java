package com.example.pluto.tasks.datacrawler;

import com.example.pluto.controllers.IBitfinexService;
import com.example.pluto.controllers.IOrdenanzaService;
import com.example.pluto.entities.InstrumentTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.example.pluto.PlutoConstants.HTTP_PREFIX;

public class Datacrawler {
    private static final Logger LOG = LoggerFactory.getLogger(Datacrawler.class);

    @Autowired
    public IBitfinexService bitfinex;

    @Autowired
    public IOrdenanzaService ordenanza;
    public void run(String[] args) {
        while (true) {
            registerSpots();
            try {
                Thread.sleep(Long.valueOf(args[0]));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void registerSpots(){
        LOG.info("Registering spot data");
        getParesVigilados().forEach(par -> requestSave(par.getTicker()));
    }

    private List<InstrumentTO> getParesVigilados(){
        return ordenanza.getInstruments();
    }

    private void requestSave(String instrument){
        bitfinex.saveSpot(instrument, null);
    }
    protected static String buildUrl(String... split) {
        StringBuilder sb = new StringBuilder(HTTP_PREFIX);
        for (int i = 0; i <= split.length - 1; i++) {
            sb.append(split[i]);
        }
        return sb.toString();
    }

}
