package com.example.pluto.persistencia;

import com.example.pluto.entities.SpotTO;
import com.example.pluto.persistencia.repositories.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersistenciaService {

    @Autowired
    SpotRepository spotRepository;

    public PersistenciaService() {
    }

    public void save(Object obj) {
        SpotTO spot = (SpotTO) obj;
        spotRepository.save(spot);
    }

}
