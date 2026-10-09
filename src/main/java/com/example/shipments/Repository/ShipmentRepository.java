package com.example.shipments.Repository;

import com.example.shipments.Model.Shipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shipments.Model.ShipmentStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> { /// 

    @EntityGraph(attributePaths = {"originComuna.region", "destinationComuna.region", "events"})
    Optional<Shipment> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"originComuna.region", "destinationComuna.region", "events"})
    Optional<Shipment> findWithDetailsByTrackingCode(String trackingCode);

    @EntityGraph(attributePaths = {"originComuna.region", "destinationComuna.region", "events"})
    List<Shipment> findByBuyerIdOrderByCreatedAtDesc(String buyerId);

    @EntityGraph(attributePaths = {"originComuna.region", "destinationComuna.region", "events"})
    List<Shipment> findBySellerIdOrderByCreatedAtDesc(String sellerId);

    boolean existsByTrackingCode(String trackingCode);

    boolean existsByOrderIdAndBuyerIdAndStatusNotIn(String orderId, String buyerId, Collection<ShipmentStatus> statuses);
}
