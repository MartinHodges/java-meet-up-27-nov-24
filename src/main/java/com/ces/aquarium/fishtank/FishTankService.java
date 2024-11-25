package com.ces.aquarium.fishtank;

import com.ces.aquarium.fish.FishEntity;
import com.ces.aquarium.exceptions.ResourceNotFoundException;
import com.ces.aquarium.fish.FishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class FishTankService {

    private final FishService fishService;
    private final FishTankRepo fishTankRepo;

    public FishTankEntity getFishTank(String name) {

        FishTankEntity fishTank = fishTankRepo.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Fish tank not found"));
        return fishTank;
    }

    public Set<FishTankEntity> getAllFishTanks() {

        Set<FishTankEntity> tanks = fishTankRepo.findAll();

        return tanks;
    }

    public FishTankEntity createFishTank(FishTankPostDTO dto) {

        FishTankEntity fishTank = new FishTankEntity();

        fishTank.setName(dto.getName());
        fishTank.setType(dto.getType());

        fishTank = fishTankRepo.save(fishTank);

        return fishTank;
    }

    public FishTankEntity updateFishTank(String name, FishTankPostDTO dto) {

        FishTankEntity fishTank = fishTankRepo.findById(name)
                .orElseThrow(() -> new ResourceNotFoundException("Fish tank not found"));

        fishTank.setName(dto.getName());

        fishTank = fishTankRepo.save(fishTank);

        return fishTank;
    }

    public void deleteFishTank(String name) {

        Optional<FishTankEntity> fishTankFound = fishTankRepo.findById(name);

        if (fishTankFound.isPresent()) {
            FishTankEntity fishTank = fishTankFound.get();
            fishTank.removeAllFishes();
            fishTankRepo.deleteById(name);
        }
    }

    public FishTankEntity addFishToTank(String fishName, String tankName) {

        FishTankEntity fishTank = fishTankRepo.findById(tankName)
                .orElseThrow(() -> new ResourceNotFoundException("Fish tank not found"));

        FishEntity fish = fishService.getFish(fishName);

        fishTank.addFish(fish);
        fishTank = fishTankRepo.save(fishTank);

        return fishTank;
    }

    public FishTankEntity removeFishFromTank(String fishName, String tankName) {

        FishTankEntity fishTank = fishTankRepo.findById(tankName)
                .orElseThrow(() -> new ResourceNotFoundException("Fish tank not found"));

        FishEntity fish = fishService.getFish(fishName);

        fishTank.removeFish(fish);
        fishTank = fishTankRepo.save(fishTank);

        return fishTank;
    }
}
