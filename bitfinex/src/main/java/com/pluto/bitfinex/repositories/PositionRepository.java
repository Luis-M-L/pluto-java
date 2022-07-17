package com.pluto.bitfinex.repositories;

import com.pluto.entities.BasketTO;
import com.pluto.entities.PositionTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends CrudRepository<PositionTO, Long> {

    List<PositionTO> findByBasket(Integer basket);

    @Query(value = "SELECT a FROM PositionTO a WHERE a.currency = :buyed AND a.basket = :basket AND NOT EXISTS " +
            "(SELECT b FROM PositionTO b WHERE a.currency = b.currency AND a.basket = b.basket AND b.timestamp > a.timestamp)")
    PositionTO findLastByBasketCurrency(BasketTO basket, String buyed);

    @Query(value = "SELECT a FROM PositionTO a WHERE NOT EXISTS (SELECT b FROM PositionTO b WHERE b.currency = a.currency AND b.basket = a.basket AND b.timestamp > a.timestamp)")
    Iterable<PositionTO> findLast();

}