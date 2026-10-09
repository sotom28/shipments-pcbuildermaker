package com.example.shipments.Dto;

import com.example.shipments.Model.ServiceLevel;
import com.example.shipments.Model.Shipment;
import com.example.shipments.Model.ShipmentEvent;
import com.example.shipments.Model.ShipmentStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ShipmentDtos {

    private ShipmentDtos() {
    }

    
    public record QuoteRequest(
            @NotNull Long originComunaId,
            @NotNull Long destinationComunaId,
            @NotNull @DecimalMin(value = "0.01", message = "el peso debe ser mayor a 0")
            @DecimalMax(value = "50", message = "el peso maximo es 50 kg") Double weightKg,
            ServiceLevel serviceLevel) {
    }

    public record QuoteResponse(long cost, String currency, int estimatedDays, LocalDate estimatedDelivery,
                                ServiceLevel serviceLevel) {
    }

    public record CreateShipmentRequest(
            @NotBlank @Size(max = 64) String orderId,
            @Size(max = 255) String description,
            @NotBlank @Size(max = 64) String sellerId,
            @NotNull Long addressId,
            @NotNull Long originComunaId,
            @NotNull @DecimalMin(value = "0.01", message = "el peso debe ser mayor a 0")
            @DecimalMax(value = "50", message = "el peso maximo es 50 kg") Double weightKg,
            ServiceLevel serviceLevel) {
    }

    public record UpdateStatusRequest(
            @NotNull ShipmentStatus status,
            @Size(max = 150) String location,
            @Size(max = 255) String note) {
    }

    public record EventResponse(ShipmentStatus status, String location, String note, Instant createdAt) {
        public static EventResponse from(ShipmentEvent e) {
            return new EventResponse(e.getStatus(), e.getLocation(), e.getNote(), e.getCreatedAt());
        }
    }

    public record Destination(String recipientName, String recipientPhone, String street, String streetNumber,
                              String apartment, String postalCode, String reference, Long comunaId, String comuna,
                              Long regionId, String region) {
    }

    /** Vista completa: solo para comprador, vendedor o admin. */
    public record ShipmentResponse(
            Long id,
            String trackingCode,
            String orderId,
            String description,
            String buyerId,
            String sellerId,
            Long originComunaId,
            String originComuna,
            String originRegion,
            Destination destination,
            ServiceLevel serviceLevel,
            double weightKg,
            long shippingCost,
            String currency,
            ShipmentStatus status,
            LocalDate estimatedDelivery,
            Instant createdAt,
            Instant updatedAt,
            List<EventResponse> events) {

        public static ShipmentResponse from(Shipment s) {
            var dest = s.getDestinationComuna();
            var origin = s.getOriginComuna();
            return new ShipmentResponse(s.getId(), s.getTrackingCode(), s.getOrderId(), s.getDescription(),
                    s.getBuyerId(), s.getSellerId(),
                    origin.getId(), origin.getName(), origin.getRegion().getName(),
                    new Destination(s.getRecipientName(), s.getRecipientPhone(), s.getStreet(), s.getStreetNumber(),
                            s.getApartment(), s.getPostalCode(), s.getReference(),
                            dest.getId(), dest.getName(), dest.getRegion().getId(), dest.getRegion().getName()),
                    s.getServiceLevel(), s.getWeightKg(), s.getShippingCost(), "CLP", s.getStatus(),
                    s.getEstimatedDelivery(), s.getCreatedAt(), s.getUpdatedAt(),
                    s.getEvents().stream().map(EventResponse::from).toList());
        }
    }

    /** Vista publica del seguimiento: sin datos personales del destinatario. */
    public record TrackingResponse(
            String trackingCode,
            ShipmentStatus status,
            String originComuna,
            String destinationComuna,
            String destinationRegion,
            ServiceLevel serviceLevel,
            LocalDate estimatedDelivery,
            List<EventResponse> events) {

        public static TrackingResponse from(Shipment s) {
            return new TrackingResponse(s.getTrackingCode(), s.getStatus(),
                    s.getOriginComuna().getName(),
                    s.getDestinationComuna().getName(), s.getDestinationComuna().getRegion().getName(),
                    s.getServiceLevel(), s.getEstimatedDelivery(),
                    s.getEvents().stream().map(EventResponse::from).toList());
        }
    }
}
