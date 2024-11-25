package com.ces.aquarium.fish;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(path = "/api/v1/fishes",  produces = {MediaType.APPLICATION_JSON_VALUE})
public class FishController {

    private final FishService fishService;

    /**
     * Get all fishes
     * @return A list of fishes
     */
    @Operation(summary = "List all fishes")
    @GetMapping
    public ResponseEntity<List<FishGetDTO>> getAllFishes(
    ) {
        log.info("Get all fishes");

        List<FishGetDTO> response = FishGetDTO.getDTO(fishService.getAllFishes());

        return ResponseEntity.ok(response);
    }


    /**
     * Get a specific fish
     * @param name - the name of the fish
     * @return THe fish (if it exists)
     */
    @Operation(summary = "Fetch fish")
    @GetMapping("/{name}")
    public ResponseEntity<FishGetDTO> getFish(
            @Parameter(description = "Name of fish", required = true) @PathVariable("name") String name
    ) {
        log.info("Get fish {}", name);

        FishGetDTO response = FishGetDTO.getDTO(fishService.getFish(name));

        return ResponseEntity.ok(response);
    }


    /**
     * Create a fish
     * @param dto Definition of the fish
     * @return a fish (not in a tank)
     */
    @Operation(summary = "Create a fish")
    @PostMapping
    public ResponseEntity<FishGetDTO> createFish(
            @Parameter(description = "Details of the fish to be created", required = true) @RequestBody FishPostDTO dto
    ) {
        log.info("Create fish");

        FishGetDTO response = FishGetDTO.getDTO(fishService.createFish(dto));

        return ResponseEntity.ok(response);
    }


    /**
     * Update the details of the fish
     * @param name the name of the fish to be updated
     * @param dto the details to be changed
     * @return the updated fish
     */
    @Operation(summary = "Update fish")
    @PutMapping("/{name}")
    public ResponseEntity<FishGetDTO> updateFish(
            @Parameter(description = "Id of fish", required = true) @PathVariable("name") String name,
            @Parameter(description = "Details of the fish to be updated", required = true) @RequestBody FishPostDTO dto
    ) {
        log.info("Update fish {}", name);

        FishGetDTO response = FishGetDTO.getDTO(fishService.updateFish(name, dto));

        return ResponseEntity.ok(response);
    }


    /**
     * Delete the given fish if it exists (idempotent)
     * @param name the name of the fish to be deleted
     * @return void
     */
    @Operation(summary = "Delete fish")
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteFish(
            @Parameter(description = "Name of fish", required = true) @PathVariable("name") String name
    ) {
        log.info("Delete fish {}", name);

        fishService.deleteFish(name);

        return ResponseEntity.ok().build();
    }
}
