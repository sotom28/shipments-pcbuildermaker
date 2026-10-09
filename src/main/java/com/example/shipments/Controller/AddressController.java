package com.example.shipments.Controller;

import com.example.shipments.Auth.AuthContext;
import com.example.shipments.Dto.AddressDtos.AddressRequest;
import com.example.shipments.Dto.AddressDtos.AddressResponse;
import com.example.shipments.Service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Libreta de direcciones del usuario autenticado. */
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addresses;

    @GetMapping
    public List<AddressResponse> list(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role) {
        return addresses.list(new AuthContext(userId, role));
    }

    @GetMapping("/{id}")
    public AddressResponse get(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role,
            @PathVariable Long id) {
        return addresses.get(new AuthContext(userId, role), id);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addresses.create(new AuthContext(userId, role), request));
    }

    @PutMapping("/{id}")
    public AddressResponse update(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        return addresses.update(new AuthContext(userId, role), id, request);
    }

    @PatchMapping("/{id}/default")
    public AddressResponse makeDefault(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role,
            @PathVariable Long id) {
        return addresses.makeDefault(new AuthContext(userId, role), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = AuthContext.USER_ID_HEADER, required = false) String userId,
            @RequestHeader(value = AuthContext.ROLE_HEADER, required = false) String role,
            @PathVariable Long id) {
        addresses.delete(new AuthContext(userId, role), id);
        return ResponseEntity.noContent().build();
    }
}
