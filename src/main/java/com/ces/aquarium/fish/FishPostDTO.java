package com.ces.aquarium.fish;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class FishPostDTO {

    @NotEmpty
    String type;

    @NotEmpty
    String name;
}
