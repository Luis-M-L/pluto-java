package com.example.pluto.persistencia.repositories;

import com.example.pluto.entities.SpotTO;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotRepository extends CrudRepository<SpotTO, Long> {

}
