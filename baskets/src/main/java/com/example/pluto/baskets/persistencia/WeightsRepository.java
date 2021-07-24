package com.example.pluto.baskets.persistencia;

import com.example.pluto.entities.WeightsTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeightsRepository extends CrudRepository<WeightsTO, Long> {
}
