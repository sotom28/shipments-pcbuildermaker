package com.example.shipments.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comunas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"region_id", "name"}),
        indexes = @Index(name = "idx_comuna_name", columnList = "name"))
@Getter
@Setter
@NoArgsConstructor
public class Comuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
}
