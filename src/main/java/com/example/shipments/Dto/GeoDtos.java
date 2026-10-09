package com.example.shipments.Dto;

import com.example.shipments.Model.Comuna;
import com.example.shipments.Model.Region;

public final class GeoDtos {

    private GeoDtos() {
    }

    public record RegionResponse(Long id, String code, String romanNumeral, String name) {
        public static RegionResponse from(Region r) {
            return new RegionResponse(r.getId(), r.getCode(), r.getRomanNumeral(), r.getName());
        }
    }

    public record ComunaResponse(Long id, String name, Long regionId, String regionName) {
        /** Requiere la region cargada (join fetch) o una sesion abierta. */
        public static ComunaResponse from(Comuna c) {
            return new ComunaResponse(c.getId(), c.getName(), c.getRegion().getId(), c.getRegion().getName());
        }
    }
}
