package com.example.pluto.persistencia;

import com.example.pluto.entities.InstrumentTO;
import com.example.pluto.entities.SpotTO;
import com.example.pluto.persistencia.repositories.InstrumentRepository;
import com.example.pluto.persistencia.repositories.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersistenciaService {

    @Autowired
    public SpotRepository spotRepository;

    @Autowired
    public InstrumentRepository instrumentRepository;

    public PersistenciaService() {
    }

    public void save(SpotTO spot) {
        spotRepository.save(spot);
    }

    public List<InstrumentTO> getAllCryptocurrencies() {
        List<InstrumentTO> all = new ArrayList<>();
        instrumentRepository.findAll().forEach(c -> all.add(c));
        return all;
    }

    public void save(InstrumentTO ccy){
        instrumentRepository.save(ccy);
    }
}
