package com.example.pluto.instruments.repositories;

import com.example.pluto.entities.InstrumentTO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentRepository extends CrudRepository<InstrumentTO, Long> {
}
