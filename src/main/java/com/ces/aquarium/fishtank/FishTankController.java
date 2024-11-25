package com.ces.aquarium.fishtank;

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
@RequestMapping(path = "/api/v1/fish-tanks",  produces = {MediaType.APPLICATION_JSON_VALUE})
public class FishTankController {

    private final FishTankService fishTankService;

    /**
     * Get all fish tanks
     * @return A list of fish tanks
     */
    @Operation(summary = "List all fish tanks")
    @GetMapping
    public ResponseEntity<List<FishTankGetDTO>> getFishTanks(
    ) {
        log.info("Get all fish tanks");

        List<FishTankGetDTO> response = FishTankGetDTO.getDTO(fishTankService.getAllFishTanks());

        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific fish tank
     * @param name - the name of the fish tank
     * @return THe fish tank (if it exists)
     */
    @Operation(summary = "Fetch fish tank")
    @GetMapping("/{name}")
    public ResponseEntity<FishTankGetDTO> getFishTank(
            @Parameter(description = "Name of fish tank", required = true) @PathVariable("name") String name
    ) {
        log.info("Get fish tank {}", name);

        FishTankGetDTO response = FishTankGetDTO.getDTO(fishTankService.getFishTank(name));

        return ResponseEntity.ok(response);
    }

    /**
     * Create a fish
     * @param dto Definition of the fish
     * @return a fish (not in a tank)
     */
    @Operation(summary = "Create a fish tank")
    @PostMapping
    public ResponseEntity<FishTankGetDTO> createFishTank(
            @Parameter(description = "Details of the fish tank to be created", required = true) @RequestBody FishTankPostDTO dto
    ) {
        log.info("Create fish tank");

        FishTankGetDTO response = FishTankGetDTO.getDTO(fishTankService.createFishTank(dto));

        return ResponseEntity.ok(response);
    }

    /**
     * Update the details of the fish tank
     * @param name the name of the fish tank to be updated
     * @param dto the details to be changed
     * @return the updated fish tank
     */
    @Operation(summary = "Update fish tank")
    @PutMapping("/{name}")
    public ResponseEntity<FishTankGetDTO> updateFishTank(
            @Parameter(description = "Name of fish tank", required = true) @PathVariable("name") String name,
            @Parameter(description = "Details of the fish tank to be updated", required = true) @RequestBody FishTankPostDTO dto
    ) {
        log.info("Update fish tank {}", name);

        FishTankGetDTO response = FishTankGetDTO.getDTO(fishTankService.updateFishTank(name, dto));

        return ResponseEntity.ok(response);
    }

    /**
     * Delete the given fish tank if it exists (idempotent)
     * @param name the name of the fish tank to be deleted
     * @return void
     */
    @Operation(summary = "Delete fish tank")
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteFishTank(
            @Parameter(description = "Name of fish tank", required = true) @PathVariable("name") String name
    ) {
        log.info("Delete fish tank {}", name);

        fishTankService.deleteFishTank(name);

        return ResponseEntity.ok().build();
    }

    /**
     * Add the given fish to the given fish tank
     * @param tankName the name of the fish tank
     * @param fishName the name of the fish to be added to the tank
     * @return void
     */
    @Operation(summary = "Add fish to fish tank")
    @PutMapping("/{tank-name}/fishes/{fish-name}")
    public ResponseEntity<FishTankGetDTO> addFishToTank(
            @Parameter(description = "name of fish tank", required = true) @PathVariable("tank-name") String tankName,
            @Parameter(description = "name of fish", required = true) @PathVariable("fish-name") String fishName
    ) {
        log.info("Add fish {} to fish tank {}", fishName, tankName);

        FishTankGetDTO response = FishTankGetDTO.getDTO(fishTankService.addFishToTank(fishName, tankName));

        return ResponseEntity.ok(response);
    }

    /**
     * Remove the given fish to the given fish tank
     * @param tankName the name of the fish tank
     * @param fishName the name of the fish to be removed from the tank
     * @return void
     */
    @Operation(summary = "Remove fish from fish tank")
    @DeleteMapping("/{tank-name}/fishes/{fish-name}")
    public ResponseEntity<FishTankGetDTO> removeFishFromTank(
            @Parameter(description = "Name of fish tank", required = true) @PathVariable("tank-name") String tankName,
            @Parameter(description = "Name of fish", required = true) @PathVariable("fish-name") String fishName
    ) {
        log.info("Remove fish {} from fish tank {}", fishName, tankName);

        FishTankGetDTO response = FishTankGetDTO.getDTO(fishTankService.removeFishFromTank(fishName, tankName));

        return ResponseEntity.ok(response);
    }
}
