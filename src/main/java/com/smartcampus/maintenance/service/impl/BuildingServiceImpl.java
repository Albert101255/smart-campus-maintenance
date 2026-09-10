package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.entity.Building;
import com.smartcampus.maintenance.repository.BuildingRepository;
import com.smartcampus.maintenance.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BuildingServiceImpl implements BuildingService {

    @Autowired
    private BuildingRepository buildingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Building> getAllActiveBuildings() {
        return buildingRepository.findByActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Building getById(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Building not found with ID: " + id));
    }

    @Override
    public Building createBuilding(String name, String code, String location) {
        if (buildingRepository.existsByName(name)) {
            throw new IllegalArgumentException("Building with this name already exists");
        }
        Building building = new Building(name.trim(), code != null ? code.trim() : null, location);
        return buildingRepository.save(building);
    }

    @Override
    public Building updateBuilding(Long id, String name, String code, String location, boolean active) {
        Building building = getById(id);
        building.setName(name.trim());
        building.setCode(code != null ? code.trim() : null);
        building.setLocation(location);
        building.setActive(active);
        return buildingRepository.save(building);
    }
}
