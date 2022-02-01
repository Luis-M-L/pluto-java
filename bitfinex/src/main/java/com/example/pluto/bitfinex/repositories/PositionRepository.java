package com.example.pluto.bitfinex.repositories;

import com.example.pluto.entities.BasketTO;
import com.example.pluto.entities.PositionTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends CrudRepository<PositionTO, Long> {

    List<PositionTO> findByBasket(Integer basket);

    @Query(value = "SELECT a FROM PositionTO a WHERE a.currency = :buyed AND a.basket = :basket AND NOT EXISTS (SELECT b FROM PositionTO b WHERE b.id = a.id AND b.timestamp > a.timestamp)")
    PositionTO findLastByBasketCurrency(BasketTO basket, String buyed);
}