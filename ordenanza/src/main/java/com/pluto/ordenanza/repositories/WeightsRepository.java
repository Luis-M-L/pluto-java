package com.pluto.ordenanza.repositories;

import com.pluto.entities.WeightTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeightsRepository extends CrudRepository<WeightTO, Long> {
}
