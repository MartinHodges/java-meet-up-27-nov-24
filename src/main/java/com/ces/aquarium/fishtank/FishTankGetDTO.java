package com.ces.aquarium.fishtank;

import com.ces.aquarium.fish.FishGetDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class FishTankGetDTO {

    private String name;
    private String type;
    private List<FishGetDTO> fishes;

    static public FishTankGetDTO getDTO(FishTankEntity tank) {
        FishTankGetDTO dto = new FishTankGetDTO(
                tank.getName(),
                tank.getType(),
                FishGetDTO.getDTO(tank.getFishes()));
        return dto;
    }

    static public List<FishTankGetDTO> getDTO(Set<FishTankEntity> tanks) {
        List<FishTankGetDTO> dtos = tanks.stream()
                .map(FishTankGetDTO::getDTO)
                .toList();
        return dtos;
    }
}
