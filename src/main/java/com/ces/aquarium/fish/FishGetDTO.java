package com.ces.aquarium.fish;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class FishGetDTO {

    private String name;
    private String type;

    static public FishGetDTO getDTO(FishEntity fish) {
        FishGetDTO dto = new FishGetDTO(fish.getName(), fish.getType());
        return dto;
    }

    static public List<FishGetDTO> getDTO(Set<FishEntity> fishes) {
        List<FishGetDTO> dtos = fishes.stream()
                .map(FishGetDTO::getDTO)
                .toList();
        return dtos;
    }
}
