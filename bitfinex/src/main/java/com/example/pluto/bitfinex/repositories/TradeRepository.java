package com.example.pluto.bitfinex.repositories;

import com.example.pluto.entities.TradeTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends CrudRepository<TradeTO, Long> {

    List<TradeTO> findByStatus(String status);

}
