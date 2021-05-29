package com.example.pluto.persistencia;

import com.example.pluto.entities.SpotTO;
import com.example.pluto.repositories.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PersistenciaService {

    private static Map<Class, CrudRepository> repositoryFactory;
/*
    @Autowired
    private SpotRepository spotRepository;
*/
    public PersistenciaService() {
        repositoryFactory = new HashMap<>();
        //repositoryFactory.put(SpotTO.class, spotRepository);
    }

    public void save(Object spot) {
        repositoryFactory.get(spot).save(spot);
    }

}
