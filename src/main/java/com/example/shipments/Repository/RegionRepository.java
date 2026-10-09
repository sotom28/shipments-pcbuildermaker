package com.example.shipments.Repository;

import com.example.shipments.Model.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByCode(String code);

    List<Region> findAllByOrderBySortOrderAsc();
}
