package com.ces.aquarium.fishtank;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface FishTankRepo extends CrudRepository<FishTankEntity, String> {

    public Set<FishTankEntity> findAll();

    public Optional<FishTankEntity> findByName(String name);
}
