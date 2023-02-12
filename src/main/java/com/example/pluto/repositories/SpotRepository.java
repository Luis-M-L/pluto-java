package com.example.pluto.repositories;

import com.example.pluto.entities.SpotEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface SpotRepository extends CrudRepository<SpotEntity, Long> {

    @Query(value = "SELECT a FROM SpotEntity a WHERE a.instrument in :instruments and a.timestamp between :start and :end")
    List<SpotEntity> findAllByInstrumentStartEnd(@Param("instruments") List<String> instruments, @Param("start") Timestamp start, @Param("end") Timestamp end);

    @Query(value = "SELECT a FROM SpotEntity a WHERE a.instrument in (:instruments)")
    List<SpotEntity> findAllByInstrumentIn(@Param("instruments") List<String> instruments);

    List<SpotEntity> findAllByInstrument(String instrument);

    List<SpotEntity> findAllByTimestampGreaterThan(Timestamp start);

    List<SpotEntity> findAllByTimestampLessThan(Timestamp end);
}
