package com.example.pluto.services;

import com.example.pluto.entities.InstrumentTO;
import com.example.pluto.entities.SpotEntity;
import com.example.pluto.repositories.InstrumentRepository;
import com.example.pluto.repositories.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InstrumentsService {

    @Autowired
    public SpotRepository spotRepository;

    @Autowired
    public InstrumentRepository instrumentRepository;

    public InstrumentsService() {
    }

    public void save(SpotEntity spot) {
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
