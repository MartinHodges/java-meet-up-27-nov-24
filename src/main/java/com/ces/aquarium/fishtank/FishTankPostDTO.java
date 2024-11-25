package com.ces.aquarium.fishtank;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class FishTankPostDTO {

    @NotEmpty
    String name;

    @NotEmpty
    String type;
}
