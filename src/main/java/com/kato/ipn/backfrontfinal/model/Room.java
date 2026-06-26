package com.kato.ipn.backfrontfinal.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber;

    @Column(nullable = false)
    private String type; // SIMPLE, DOBLE, SUITE

    private int capacity;

    @Column(name = "price_per_night", precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(nullable = false)
    private String status; // DISPONIBLE, OCUPADA, MANTENIMIENTO

    @Column(columnDefinition = "TEXT")
    private String description;
}
