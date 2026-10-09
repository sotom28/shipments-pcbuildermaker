package com.example.shipments.Controller;

import com.example.shipments.Dto.GeoDtos.ComunaResponse;
import com.example.shipments.Dto.GeoDtos.RegionResponse;
import com.example.shipments.Service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Regiones y comunas de Chile. Publico (no requiere identidad). */
@RestController
@RequestMapping("/api/v1/geo")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geo;

    @GetMapping("/regions")
    public List<RegionResponse> regions() {
        return geo.listRegions();
    }

    @GetMapping("/regions/{regionId}/comunas")
    public List<ComunaResponse> comunasByRegion(@PathVariable Long regionId) {
        return geo.listComunas(regionId);
    }

    @GetMapping("/comunas")
    public List<ComunaResponse> searchComunas(@RequestParam("q") String q) {
        return geo.searchComunas(q);
    }
}
