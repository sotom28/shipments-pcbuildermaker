package com.example.shipments.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Direccion de despacho guardada por un usuario (owner = azure oid). */
@Entity
@Table(name = "addresses", indexes = @Index(name = "idx_address_owner", columnList = "ownerId"))
@Getter
@Setter
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String ownerId;

    /** Alias: "Casa", "Oficina"... */
    @Column(length = 50)
    private String label;

    @Column(nullable = false, length = 120)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 150)
    private String street;

    @Column(nullable = false, length = 20)
    private String streetNumber;

    /** Depto / oficina / casa. */
    @Column(length = 50)
    private String apartment;

    @Column(length = 10)
    private String postalCode;

    /** Referencia para el repartidor. */
    @Column(length = 255)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comuna_id", nullable = false)
    private Comuna comuna;

    @Column(nullable = false)
    private boolean defaultAddress;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
