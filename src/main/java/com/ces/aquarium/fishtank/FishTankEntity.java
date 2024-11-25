package com.ces.aquarium.fishtank;

import com.ces.aquarium.fish.FishEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "FISH_TANKS")
@Getter
@Setter
public class FishTankEntity {

    @Id
    @Column(name = "NAME")
    private String name;

    @Column(name = "TYPE")
    private String type;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "fishTank")
    Set<FishEntity> fishes = new HashSet<FishEntity>();

    public void addFish(FishEntity fish) {
        if (fish != null) {
            fishes.add(fish);
            fish.setFishTank(this);
        }
    }

    public void removeFish(FishEntity fish) {
        if (fish != null) {
            fishes.remove(fish);
            fish.setFishTank(null);
        }
    }

    public void removeAllFishes() {
        fishes.forEach(fish -> removeFish(fish));
    }
}
