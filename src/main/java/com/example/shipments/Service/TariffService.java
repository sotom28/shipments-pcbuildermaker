package com.example.shipments.Service;

import com.example.shipments.Dto.ShipmentDtos.QuoteResponse;
import com.example.shipments.Model.Comuna;
import com.example.shipments.Model.ServiceLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Tarifa referencial en CLP segun la distancia entre regiones (posicion
 * norte-sur) y el peso. No es una tarifa de courier real: los valores
 * son configurables con shipping.tariff.*.
 */
@Service
public class TariffService {

    private final long sameComuna;
    private final long sameRegion;
    private final long interRegionBase;
    private final long perRegionStep;
    private final long perExtraKg;
    private final double expressMultiplier;

    public TariffService(
            @Value("${shipping.tariff.same-comuna:2990}") long sameComuna,
            @Value("${shipping.tariff.same-region:3990}") long sameRegion,
            @Value("${shipping.tariff.inter-region-base:4990}") long interRegionBase,
            @Value("${shipping.tariff.per-region-step:900}") long perRegionStep,
            @Value("${shipping.tariff.per-extra-kg:500}") long perExtraKg,
            @Value("${shipping.tariff.express-multiplier:1.6}") double expressMultiplier) {
        this.sameComuna = sameComuna;
        this.sameRegion = sameRegion;
        this.interRegionBase = interRegionBase;
        this.perRegionStep = perRegionStep;
        this.perExtraKg = perExtraKg;
        this.expressMultiplier = expressMultiplier;
    }

    public QuoteResponse quote(Comuna origin, Comuna destination, double weightKg, ServiceLevel level) {
        ServiceLevel service = level == null ? ServiceLevel.STANDARD : level;
        int regionSteps = Math.abs(origin.getRegion().getSortOrder() - destination.getRegion().getSortOrder());

        long base;
        int days;
        if (origin.getId().equals(destination.getId())) {
            base = sameComuna;
            days = 1;
        } else if (regionSteps == 0) {
            base = sameRegion;
            days = 2;
        } else {
            base = interRegionBase + perRegionStep * regionSteps;
            days = 2 + (regionSteps + 1) / 2;
        }

        long extraKg = Math.max(0, (long) Math.ceil(weightKg) - 1);
        long cost = base + perExtraKg * extraKg;

        if (service == ServiceLevel.EXPRESS) {
            cost = Math.round(cost * expressMultiplier / 10.0) * 10;
            days = Math.max(1, (days + 1) / 2);
        }
        return new QuoteResponse(cost, "CLP", days, LocalDate.now().plusDays(days), service);
    }
}
