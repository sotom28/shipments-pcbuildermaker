package com.example.shipments.Data;

import com.example.shipments.Model.Comuna;
import com.example.shipments.Model.Region;
import com.example.shipments.Repository.ComunaRepository;
import com.example.shipments.Repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Carga (idempotente) las 16 regiones y 346 comunas de Chile desde
 * geo/chile-comunas.txt. Solo inserta lo que falta, asi que se puede
 * reiniciar el servicio sin duplicar datos.
 */
@Component
@RequiredArgsConstructor
public class GeoSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GeoSeeder.class);
    static final String RESOURCE = "geo/chile-comunas.txt";

    private final RegionRepository regions;
    private final ComunaRepository comunas;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        Region current = null;
        int order = 0;
        int newRegions = 0;
        int newComunas = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(RESOURCE).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("@")) {
                    String[] parts = line.substring(1).split("\\|", 3);
                    if (parts.length != 3) {
                        throw new IllegalStateException("Linea de region invalida en " + RESOURCE + ": " + line);
                    }
                    order++;
                    var existing = regions.findByCode(parts[0]);
                    current = existing.orElseGet(Region::new);
                    current.setCode(parts[0]);
                    current.setRomanNumeral(parts[1]);
                    current.setName(parts[2]);
                    current.setSortOrder(order);
                    current = regions.save(current);
                    if (existing.isEmpty()) {
                        newRegions++;
                    }
                } else {
                    if (current == null) {
                        throw new IllegalStateException("Comuna sin region en " + RESOURCE + ": " + line);
                    }
                    if (!comunas.existsByRegionAndName(current, line)) {
                        Comuna comuna = new Comuna();
                        comuna.setName(line);
                        comuna.setRegion(current);
                        comunas.save(comuna);
                        newComunas++;
                    }
                }
            }
        }
        if (newRegions > 0 || newComunas > 0) {
            log.info("Datos geograficos cargados: {} regiones y {} comunas nuevas", newRegions, newComunas);
        }
    }
}
