package com.smartcampus.maintenance.service;

import com.smartcampus.maintenance.entity.Building;
import java.util.List;

public interface BuildingService {
    List<Building> getAllActiveBuildings();
    List<Building> getAllBuildings();
    Building getById(Long id);
    Building createBuilding(String name, String code, String location);
    Building updateBuilding(Long id, String name, String code, String location, boolean active);
}
