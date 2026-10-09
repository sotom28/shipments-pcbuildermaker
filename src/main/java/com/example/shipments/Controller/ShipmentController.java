package com.example.shipments.Controller;

import com.example.shipments.Auth.AuthContext;
import com.example.shipments.Dto.ShipmentDtos.CreateShipmentRequest;
import com.example.shipments.Dto.ShipmentDtos.QuoteRequest;
import com.example.shipments.Dto.ShipmentDtos.QuoteResponse;
import com.example.shipments.Dto.ShipmentDtos.ShipmentResponse;
import com.example.shipments.Dto.ShipmentDtos.TrackingResponse;
import com.example.shipments.Dto.ShipmentDtos.UpdateStatusRequest;
import com.example.shipments.Service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipments;

    /** Cotizacion: publica, sirve para mostrar el costo antes del checkout. */
    @PostMapping("/quote")
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
        return shipments.quote(request);
    }

    /** Seguimiento por codigo: publico, sin datos personales. */
    @GetMapping("/track/{trackingCode}")
    public TrackingResponse track(@PathVariable String trackingCode) {
        return shipments.track(trackingCode);
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role,
            @Valid @RequestBody CreateShipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shipments.create(new AuthContext(userId, role), request));
    }

    /** Mis envios como comprador (por defecto) o como vendedor (?as=seller). */
    @GetMapping
    public List<ShipmentResponse> listMine(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role,
            @RequestParam(value = "as", defaultValue = "buyer") String as) {
        if (!as.equals("buyer") && !as.equals("seller")) {
            throw new IllegalArgumentException("El parametro 'as' debe ser 'buyer' o 'seller'");
        }
        return shipments.listMine(new AuthContext(userId, role), as.equals("seller"));
    }

    @GetMapping("/{id}")
    public ShipmentResponse get(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role,
            @PathVariable Long id) {
        return shipments.get(new AuthContext(userId, role), id);
    }

    @PatchMapping("/{id}/status")
    public ShipmentResponse updateStatus(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return shipments.updateStatus(new AuthContext(userId, role), id, request);
    }

    @PostMapping("/{id}/cancel")
    public ShipmentResponse cancel(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role,
            @PathVariable Long id) {
        return shipments.cancel(new AuthContext(userId, role), id);
    }
}
