package com.example.shipments.Messaging;

import com.example.shipments.Model.Shipment;

import java.time.Instant;

/**
 * Payload JSON que viaja por Kafka (topic shipment-events). Se llama distinto de la entidad ShipmentEvent
 * (historial de seguimiento). analytics-service debe tener un record con los mismos nombres de campo.
 * <p>
 * Se arma DENTRO de la transaccion de ShipmentService: la comuna y la region son lazy y open-in-view esta apagado,
 * fuera de la transaccion daria LazyInitializationException.
 */
public record ShipmentKafkaEvent(
        String eventType,          // SHIPMENT_CREATED | SHIPMENT_STATUS_CHANGED
        Instant occurredAt,
        Long shipmentId,
        String trackingCode,
        String orderId,  
        String buyerId,
        String sellerId,
        String originComuna,
        String originRegion,
        String destinationComuna,
        String destinationRegion,
        String serviceLevel,
        double weightKg,
        long shippingCost,         // CLP
        String status,
        String previousStatus) {   // null al crear

    public static final String CREATED = "SHIPMENT_CREATED";
    public static final String STATUS_CHANGED = "SHIPMENT_STATUS_CHANGED";

    public static ShipmentKafkaEvent of(String eventType, Shipment s, String previousStatus) {
        return new ShipmentKafkaEvent(
                eventType,
                Instant.now(),
                s.getId(),
                s.getTrackingCode(),
                s.getOrderId(),
                s.getBuyerId(),
                s.getSellerId(),
                s.getOriginComuna().getName(),
                s.getOriginComuna().getRegion().getName(),
                s.getDestinationComuna().getName(),
                s.getDestinationComuna().getRegion().getName(),
                s.getServiceLevel().name(),
                s.getWeightKg(),
                s.getShippingCost(),
                s.getStatus().name(),
                previousStatus);
    }
}
