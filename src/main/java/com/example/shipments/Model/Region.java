package com.example.shipments.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "regions")
@Getter
@Setter
@NoArgsConstructor
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo CUT de la region (15 = Arica y Parinacota ... 12 = Magallanes). */
    @Column(nullable = false, unique = true, length = 2)
    private String code;

    @Column(nullable = false, length = 4)
    private String romanNumeral;

    @Column(nullable = false, length = 100)
    private String name;

    /** Posicion norte -> sur (1..16). */
    @Column(nullable = false)
    private int sortOrder;
}
