package com.example.shipments.Service;

import com.example.shipments.Auth.AuthContext;
import com.example.shipments.Dto.ShipmentDtos.CreateShipmentRequest;
import com.example.shipments.Dto.ShipmentDtos.QuoteRequest;
import com.example.shipments.Dto.ShipmentDtos.QuoteResponse;
import com.example.shipments.Dto.ShipmentDtos.ShipmentResponse;
import com.example.shipments.Dto.ShipmentDtos.TrackingResponse;
import com.example.shipments.Dto.ShipmentDtos.UpdateStatusRequest;
import com.example.shipments.Exception.ApiExceptions.ConflictException;
import com.example.shipments.Exception.ApiExceptions.ForbiddenException;
import com.example.shipments.Exception.ApiExceptions.NotFoundException;
import com.example.shipments.Model.Address;
import com.example.shipments.Model.Shipment;
import com.example.shipments.Model.ShipmentStatus;
import com.example.shipments.Repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentService {

    /** Sin I/O/0/1 para que el codigo se pueda dictar sin confundir caracteres. */
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ShipmentRepository shipments;
    private final AddressService addresses;
    private final GeoService geo;
    private final TariffService tariff;

    @Transactional(readOnly = true)
    public QuoteResponse quote(QuoteRequest request) {
        return tariff.quote(geo.requireComuna(request.originComunaId()),
                geo.requireComuna(request.destinationComunaId()),
                request.weightKg(), request.serviceLevel());
    }

    public ShipmentResponse create(AuthContext auth, CreateShipmentRequest request) {
        if (auth.userId().equals(request.sellerId())) {
            throw new IllegalArgumentException("El comprador y el vendedor no pueden ser la misma persona");
        }
        if (shipments.existsByOrderIdAndBuyerIdAndStatusNotIn(request.orderId(), auth.userId(),
                Set.of(ShipmentStatus.CANCELLED, ShipmentStatus.RETURNED))) {
            throw new ConflictException("Ya existe un envio activo para la orden " + request.orderId());
        }

        Address address = addresses.require(auth, request.addressId());
        var origin = geo.requireComuna(request.originComunaId());
        var quote = tariff.quote(origin, address.getComuna(), request.weightKg(), request.serviceLevel());

        Shipment s = new Shipment();
        s.setTrackingCode(newTrackingCode());
        s.setOrderId(request.orderId().strip());
        s.setDescription(request.description());
        s.setBuyerId(auth.userId());
        s.setSellerId(request.sellerId().strip());
        s.setOriginComuna(origin);
        s.setRecipientName(address.getRecipientName());
        s.setRecipientPhone(address.getPhone());
        s.setStreet(address.getStreet());
        s.setStreetNumber(address.getStreetNumber());
        s.setApartment(address.getApartment());
        s.setPostalCode(address.getPostalCode());
        s.setReference(address.getReference());
        s.setDestinationComuna(address.getComuna());
        s.setServiceLevel(quote.serviceLevel());
        s.setWeightKg(request.weightKg());
        s.setShippingCost(quote.cost());
        s.setEstimatedDelivery(quote.estimatedDelivery());
        s.addEvent(ShipmentStatus.PENDING, origin.getName(), "Envio creado");
        return ShipmentResponse.from(shipments.save(s));
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> listMine(AuthContext auth, boolean asSeller) {
        var list = asSeller
                ? shipments.findBySellerIdOrderByCreatedAtDesc(auth.userId())
                : shipments.findByBuyerIdOrderByCreatedAtDesc(auth.userId());
        return list.stream().map(ShipmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ShipmentResponse get(AuthContext auth, Long id) {
        Shipment s = load(id);
        requireParticipant(auth, s);
        return ShipmentResponse.from(s);
    }

    @Transactional(readOnly = true)
    public TrackingResponse track(String trackingCode) {
        return shipments.findWithDetailsByTrackingCode(trackingCode.strip().toUpperCase())
                .map(TrackingResponse::from)
                .orElseThrow(() -> new NotFoundException("Codigo de seguimiento no encontrado"));
    }

    /** Avance de estado: solo el vendedor del envio o un administrador. */
    public ShipmentResponse updateStatus(AuthContext auth, Long id, UpdateStatusRequest request) {
        Shipment s = load(id);
        if (!auth.isAdmin() && !auth.userId().equals(s.getSellerId())) {
            throw new ForbiddenException("Solo el vendedor o un administrador puede actualizar el estado");
        }
        return transition(s, request.status(), request.location(), request.note());
    }

    /** El comprador puede cancelar mientras el envio no haya salido. */
    public ShipmentResponse cancel(AuthContext auth, Long id) {
        Shipment s = load(id);
        requireParticipant(auth, s);
        String by = auth.userId().equals(s.getBuyerId()) ? "el comprador" : "el vendedor";
        return transition(s, ShipmentStatus.CANCELLED, null, "Cancelado por " + by);
    }

    private ShipmentResponse transition(Shipment s, ShipmentStatus next, String location, String note) {
        if (!s.getStatus().canTransitionTo(next)) {
            throw new ConflictException("Transicion no permitida: " + s.getStatus() + " -> " + next
                    + ". Permitidas: " + s.getStatus().allowedNext());
        }
        s.setStatus(next);
        s.setUpdatedAt(Instant.now());
        s.addEvent(next, location, note);
        return ShipmentResponse.from(shipments.save(s));
    }

    private Shipment load(Long id) {
        return shipments.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Envio no encontrado: " + id));
    }

    /** 404 (no 403) para ajenos, para no revelar la existencia del envio. */
    private void requireParticipant(AuthContext auth, Shipment s) {
        boolean allowed = auth.isAdmin()
                || auth.userId().equals(s.getBuyerId())
                || auth.userId().equals(s.getSellerId());
        if (!allowed) {
            throw new NotFoundException("Envio no encontrado: " + s.getId());
        }
    }

    private String newTrackingCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("SHP-");
            for (int i = 0; i < 10; i++) {
                sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (shipments.existsByTrackingCode(code));
        return code;
    }
}
