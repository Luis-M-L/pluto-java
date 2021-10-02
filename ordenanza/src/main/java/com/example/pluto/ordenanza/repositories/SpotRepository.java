package com.example.pluto.ordenanza.repositories;

import com.example.pluto.entities.SpotTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotRepository extends CrudRepository<SpotTO, Long> {

}
