package com.example.pluto.persistencia.repositories;

import com.example.pluto.entities.SpotTO;
import com.example.pluto.repositories.SpotRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@EntityScan("com.example.pluto.entities")
public class SpotRepositoryImpl implements SpotRepository {

    @Override
    public <S extends SpotTO> S save(S s) {
        return null;
    }

    @Override
    public <S extends SpotTO> Iterable<S> saveAll(Iterable<S> iterable) {
        return null;
    }

    @Override
    public Optional<SpotTO> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public Iterable<SpotTO> findAll() {
        return null;
    }

    @Override
    public Iterable<SpotTO> findAllById(Iterable<Long> iterable) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(SpotTO spotTO) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> iterable) {

    }

    @Override
    public void deleteAll(Iterable<? extends SpotTO> iterable) {

    }

    @Override
    public void deleteAll() {

    }
}
