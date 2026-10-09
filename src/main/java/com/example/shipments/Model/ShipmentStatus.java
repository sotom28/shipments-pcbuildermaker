package com.example.shipments.Model;

import java.util.EnumSet;
import java.util.Set;

public enum ShipmentStatus {
    PENDING, //// PEDIENTE
    PROCESSING, /// PROCESANDO 
    IN_TRANSIT, //// EN TRANSITO
    OUT_FOR_DELIVERY,  /// REPATIENDO 
    DELIVERED,  ///// ENTREGADO
    CANCELLED, ///// CANCELADO 
    RETURNED;  /// DEVUELTO 

    public Set<ShipmentStatus> allowedNext() {
        return switch (this) {
            case PENDING -> EnumSet.of(PROCESSING, CANCELLED);
            case PROCESSING -> EnumSet.of(IN_TRANSIT, CANCELLED);
            case IN_TRANSIT -> EnumSet.of(OUT_FOR_DELIVERY, DELIVERED, RETURNED);
            case OUT_FOR_DELIVERY -> EnumSet.of(DELIVERED, RETURNED);
            case DELIVERED, CANCELLED, RETURNED -> EnumSet.noneOf(ShipmentStatus.class);
        };
    }

    public boolean canTransitionTo(ShipmentStatus next) {
        return allowedNext().contains(next);
    }
}
