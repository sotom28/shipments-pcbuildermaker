package com.example.shipments.Dto;

import com.example.shipments.Model.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AddressDtos {

    private AddressDtos() {
    }

    public record AddressRequest(
            @Size(max = 50) String label,
            @NotBlank @Size(max = 120) String recipientName,
            @NotBlank @Pattern(regexp = "^\\+?[0-9 ()-]{8,20}$", message = "telefono invalido") String phone,
            @NotBlank @Size(max = 150) String street,
            @NotBlank @Size(max = 20) String streetNumber,
            @Size(max = 50) String apartment,
            @Pattern(regexp = "^$|^[0-9]{7}$", message = "el codigo postal chileno tiene 7 digitos") String postalCode,
            @Size(max = 255) String reference,
            @NotNull Long comunaId,
            Boolean defaultAddress) {
    }

    public record AddressResponse(
            Long id,
            String label,
            String recipientName,
            String phone,
            String street,
            String streetNumber,
            String apartment,
            String postalCode,
            String reference,
            Long comunaId,
            String comuna,
            Long regionId,
            String region,
            boolean defaultAddress,
            Instant createdAt,
            Instant updatedAt) {

        /** Requiere comuna y region cargadas. */
        public static AddressResponse from(Address a) { // si no se cargan comuna y region, se lanza 
            return new AddressResponse(a.getId(), a.getLabel(), a.getRecipientName(), a.getPhone(),
                    a.getStreet(), a.getStreetNumber(), a.getApartment(), a.getPostalCode(), a.getReference(),
                    a.getComuna().getId(), a.getComuna().getName(),
                    a.getComuna().getRegion().getId(), a.getComuna().getRegion().getName(),
                    a.isDefaultAddress(), a.getCreatedAt(), a.getUpdatedAt());
        }
    }
}
