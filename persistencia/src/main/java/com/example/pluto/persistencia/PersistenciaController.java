package com.example.pluto.persistencia;

import com.example.pluto.entities.SpotTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/persistence")
public class PersistenciaController {

    @Autowired
    PersistenciaService persistenciaService;

    @PostMapping(value = "/spot")
    void saveSpot(@RequestBody SpotTO spot){
        System.out.println("saveSpot" + spot.toString());
        persistenciaService.save(spot);
    }

}
