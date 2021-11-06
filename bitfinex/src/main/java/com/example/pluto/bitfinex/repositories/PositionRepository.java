package com.example.pluto.bitfinex.repositories;

import com.example.pluto.entities.PositionTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends CrudRepository<PositionTO, Long> {

}