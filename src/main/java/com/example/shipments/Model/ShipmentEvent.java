package com.example.shipments.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "shipment_events")
@Getter
@Setter
@NoArgsConstructor
public class ShipmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(length = 150)
    private String location;

    @Column(length = 255)
    private String note;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
