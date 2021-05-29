package com.example.pluto.repositories;

import com.example.pluto.entities.SpotTO;
import org.springframework.data.repository.CrudRepository;

public interface SpotRepository extends CrudRepository<SpotTO, Long> {

}
