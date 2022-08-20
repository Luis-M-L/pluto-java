package com.example.pluto.ordenanza.repositories;

import com.example.pluto.entities.SpotEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotRepository extends CrudRepository<SpotEntity, Long> {

}
