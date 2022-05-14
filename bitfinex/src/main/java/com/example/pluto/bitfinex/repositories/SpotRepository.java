package com.example.pluto.bitfinex.repositories;

import com.example.pluto.entities.SpotEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotRepository extends CrudRepository<SpotEntity, Long> {

}
