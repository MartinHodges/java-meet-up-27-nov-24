package com.ces.aquarium.fish;

import com.ces.aquarium.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class FishService {

    private final FishRepo fishRepo;

    public FishEntity getFish(String name) {

        FishEntity fishTank = fishRepo.findById(name)
                .orElseThrow(() -> new ResourceNotFoundException("Fish not found"));
        return fishTank;
    }

    public Set<FishEntity> getAllFishes() {
        Set<FishEntity> fishTanks = fishRepo.findAll();

        return fishTanks;
    }

    public FishEntity createFish(FishPostDTO dto) {

        FishEntity fish = new FishEntity();

        fish.setName(dto.getName());
        fish.setType(dto.getType());

        fish = fishRepo.save(fish);

        return fish;
    }

    public FishEntity updateFish(String name, FishPostDTO dto) {

        FishEntity fish = getFish(name);

        fish.setType(dto.getType());

        fish = fishRepo.save(fish);

        return fish;
    }

    public void deleteFish(String name) {

        fishRepo.deleteById(name);
    }
}
