package com.ces.aquarium.fish;

import org.springframework.data.repository.CrudRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.Set;

public interface FishRepo extends CrudRepository<FishEntity, String> {

    public Set<FishEntity> findAll();

    public Optional<FishEntity> findByName(String name);

    public void deleteByName(String name);
}
