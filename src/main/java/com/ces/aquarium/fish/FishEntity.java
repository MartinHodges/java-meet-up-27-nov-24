package com.ces.aquarium.fish;

import com.ces.aquarium.fishtank.FishTankEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "FISHES")
@Getter
@Setter
public class FishEntity {

    @Id
    @Column(name = "NAME")
    private String name;

    @Column(name = "TYPE")
    private String type;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FISH_TANK_ID")
    private FishTankEntity fishTank;
}
