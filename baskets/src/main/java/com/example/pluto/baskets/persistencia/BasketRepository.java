package com.example.pluto.baskets.persistencia;

import com.example.pluto.entities.BasketTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketRepository extends CrudRepository<BasketTO, Long> {
}
