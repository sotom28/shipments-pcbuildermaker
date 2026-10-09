package com.example.shipments.Repository;

import com.example.shipments.Model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("select a from Address a join fetch a.comuna c join fetch c.region "
            + "where a.ownerId = :ownerId order by a.defaultAddress desc, a.id desc")
    List<Address> findByOwner(@Param("ownerId") String ownerId);

    @Query("select a from Address a join fetch a.comuna c join fetch c.region "
            + "where a.id = :id and a.ownerId = :ownerId")
    Optional<Address> findByIdAndOwner(@Param("id") Long id, @Param("ownerId") String ownerId);

    long countByOwnerId(String ownerId);
}
