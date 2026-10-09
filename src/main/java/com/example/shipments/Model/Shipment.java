package com.example.shipments.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipment_buyer", columnList = "buyerId"),
        @Index(name = "idx_shipment_seller", columnList = "sellerId"),
        @Index(name = "idx_shipment_order", columnList = "orderId")
})
@Getter
@Setter
@NoArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String trackingCode;

    /** Referencia libre a la orden/publicacion que origina el envio. */
    @Column(nullable = false, length = 64)
    private String orderId;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 64)
    private String buyerId;

    @Column(nullable = false, length = 64)
    private String sellerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_comuna_id", nullable = false)
    private Comuna originComuna;

    // Copia (snapshot) de la direccion de destino: editar o borrar la direccion
    // guardada del comprador no debe alterar un envio ya creado.
    @Column(nullable = false, length = 120)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String recipientPhone;

    @Column(nullable = false, length = 150)
    private String street;

    @Column(nullable = false, length = 20)
    private String streetNumber;

    @Column(length = 50)
    private String apartment;

    @Column(length = 10)
    private String postalCode;

    @Column(length = 255)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_comuna_id", nullable = false)
    private Comuna destinationComuna;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceLevel serviceLevel;

    @Column(nullable = false)
    private double weightKg;

    /** Costo del envio en CLP. */
    @Column(nullable = false)
    private long shippingCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    private LocalDate estimatedDelivery;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private List<ShipmentEvent> events = new ArrayList<>();

    public void addEvent(ShipmentStatus status, String location, String note) {
        ShipmentEvent event = new ShipmentEvent();
        event.setShipment(this);
        event.setStatus(status);
        event.setLocation(location);
        event.setNote(note);
        events.add(event);
    }
}
