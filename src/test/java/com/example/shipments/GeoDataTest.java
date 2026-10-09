package com.example.shipments;

import com.example.shipments.Repository.ComunaRepository;
import com.example.shipments.Repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GeoDataTest {

	@Autowired
	RegionRepository regions;

	@Autowired
	ComunaRepository comunas;

	@Test
	void chileTieneDieciseisRegionesY346Comunas() {
		assertEquals(16, regions.count());
		assertEquals(346, comunas.count());
	}

	@Test
	void regionesOrdenadasDeNorteASur() {
		var list = regions.findAllByOrderBySortOrderAsc();
		assertEquals("Arica y Parinacota", list.get(0).getName());
		assertEquals("Metropolitana de Santiago", list.get(6).getName());
		assertEquals("Magallanes y de la Antártica Chilena", list.get(15).getName());
	}

	@Test
	void cadaRegionTieneComunas() {
		regions.findAll().forEach(r ->
				assertTrue(!comunas.findByRegionIdOrderByNameAsc(r.getId()).isEmpty(), r.getName()));
	}

	@Test
	void santiagoTiene52Comunas() {
		var rm = regions.findByCode("13").orElseThrow();
		assertEquals(52, comunas.findByRegionIdOrderByNameAsc(rm.getId()).size());
	}
}
