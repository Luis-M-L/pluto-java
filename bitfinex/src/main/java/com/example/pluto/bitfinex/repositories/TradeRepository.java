package com.example.pluto.bitfinex.repositories;

import com.example.pluto.entities.TradeTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends CrudRepository<TradeTO, Long> {
}
