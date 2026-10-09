package com.example.shipments.Service;

import com.example.shipments.Auth.AuthContext;
import com.example.shipments.Dto.AddressDtos.AddressRequest;
import com.example.shipments.Dto.AddressDtos.AddressResponse;
import com.example.shipments.Exception.ApiExceptions.NotFoundException;
import com.example.shipments.Model.Address;
import com.example.shipments.Repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private static final int MAX_ADDRESSES_PER_USER = 20;

    private final AddressRepository addresses;
    private final GeoService geo;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(AuthContext auth) {
        return addresses.findByOwner(auth.userId()).stream().map(AddressResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse get(AuthContext auth, Long id) {
        return AddressResponse.from(require(auth, id));
    }

    public AddressResponse create(AuthContext auth, AddressRequest request) {
        if (addresses.countByOwnerId(auth.userId()) >= MAX_ADDRESSES_PER_USER) {
            throw new IllegalArgumentException("Maximo " + MAX_ADDRESSES_PER_USER + " direcciones por usuario");
        }
        Address address = new Address();
        address.setOwnerId(auth.userId());
        apply(address, request);
        // La primera direccion del usuario queda como predeterminada.
        boolean makeDefault = Boolean.TRUE.equals(request.defaultAddress())
                || addresses.countByOwnerId(auth.userId()) == 0;
        if (makeDefault) {
            clearDefault(auth.userId());
        }
        address.setDefaultAddress(makeDefault);
        return AddressResponse.from(addresses.save(address));
    }

    public AddressResponse update(AuthContext auth, Long id, AddressRequest request) {
        Address address = require(auth, id);
        apply(address, request);
        if (Boolean.TRUE.equals(request.defaultAddress()) && !address.isDefaultAddress()) {
            clearDefault(auth.userId());
            address.setDefaultAddress(true);
        }
        address.setUpdatedAt(Instant.now());
        return AddressResponse.from(addresses.save(address));
    }

    public AddressResponse makeDefault(AuthContext auth, Long id) {
        Address address = require(auth, id);
        if (!address.isDefaultAddress()) {
            clearDefault(auth.userId());
            address.setDefaultAddress(true);
            address.setUpdatedAt(Instant.now());
        }
        return AddressResponse.from(addresses.save(address));
    }

    public void delete(AuthContext auth, Long id) {
        Address address = require(auth, id);
        addresses.delete(address);
        addresses.flush();
        if (address.isDefaultAddress()) {
            addresses.findByOwner(auth.userId()).stream().findFirst().ifPresent(next -> {
                next.setDefaultAddress(true);
                addresses.save(next);
            });
        }
    }

    /** Direccion del usuario autenticado; 404 tambien si es de otro (no revela su existencia). */
    public Address require(AuthContext auth, Long id) {
        return addresses.findByIdAndOwner(id, auth.userId())
                .orElseThrow(() -> new NotFoundException("Direccion no encontrada: " + id));
    }

    private void apply(Address address, AddressRequest r) {
        address.setLabel(blankToNull(r.label()));
        address.setRecipientName(r.recipientName().strip());
        address.setPhone(r.phone().strip());
        address.setStreet(r.street().strip());
        address.setStreetNumber(r.streetNumber().strip());
        address.setApartment(blankToNull(r.apartment()));
        address.setPostalCode(blankToNull(r.postalCode()));
        address.setReference(blankToNull(r.reference()));
        address.setComuna(geo.requireComuna(r.comunaId()));
    }

    private void clearDefault(String ownerId) {
        addresses.findByOwner(ownerId).stream()
                .filter(Address::isDefaultAddress)
                .forEach(a -> a.setDefaultAddress(false));
        addresses.flush();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
