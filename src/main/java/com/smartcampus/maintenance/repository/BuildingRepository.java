package com.smartcampus.maintenance.repository;

import com.smartcampus.maintenance.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByActiveTrueOrderByNameAsc();
    Optional<Building> findByName(String name);
    boolean existsByName(String name);
    boolean existsByCode(String code);
}
