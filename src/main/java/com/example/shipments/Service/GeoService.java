package com.example.shipments.Service;

import com.example.shipments.Dto.GeoDtos.ComunaResponse;
import com.example.shipments.Dto.GeoDtos.RegionResponse;
import com.example.shipments.Exception.ApiExceptions.NotFoundException;
import com.example.shipments.Model.Comuna;
import com.example.shipments.Repository.ComunaRepository;
import com.example.shipments.Repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeoService {

    private static final int MAX_SEARCH_RESULTS = 25;

    private final RegionRepository regions;
    private final ComunaRepository comunas;

    public List<RegionResponse> listRegions() {
        return regions.findAllByOrderBySortOrderAsc().stream().map(RegionResponse::from).toList();
    }

    public List<ComunaResponse> listComunas(Long regionId) {
        if (!regions.existsById(regionId)) {
            throw new NotFoundException("Region no encontrada: " + regionId);
        }
        return comunas.findByRegionIdOrderByNameAsc(regionId).stream().map(ComunaResponse::from).toList();
    }

    /** Busqueda por nombre (contiene, sin distinguir mayusculas) para autocompletar. */
    public List<ComunaResponse> searchComunas(String query) {
        if (query == null || query.strip().length() < 2) {
            throw new IllegalArgumentException("El parametro q debe tener al menos 2 caracteres");
        }
        return comunas.search(query.strip(), PageRequest.of(0, MAX_SEARCH_RESULTS)).stream()
                .map(ComunaResponse::from).toList();
    }

    /** Comuna con su region inicializada; 404 si no existe. */
    public Comuna requireComuna(Long id) {
        return comunas.findByIdWithRegion(id)
                .orElseThrow(() -> new NotFoundException("Comuna no encontrada: " + id));
    }
}
