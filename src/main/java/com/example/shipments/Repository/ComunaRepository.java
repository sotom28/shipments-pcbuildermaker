package com.example.shipments.Repository;

import com.example.shipments.Model.Comuna;
import com.example.shipments.Model.Region;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {

    boolean existsByRegionAndName(Region region, String name);

    List<Comuna> findByRegionIdOrderByNameAsc(Long regionId);

    @Query("select c from Comuna c join fetch c.region "
            + "where lower(c.name) like lower(concat('%', :q, '%')) order by c.name asc")
    List<Comuna> search(@Param("q") String q, Pageable pageable);

    @Query("select c from Comuna c join fetch c.region where c.id = :id")
    Optional<Comuna> findByIdWithRegion(@Param("id") Long id);
}
