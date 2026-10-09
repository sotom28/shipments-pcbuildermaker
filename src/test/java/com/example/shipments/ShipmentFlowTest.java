package com.example.shipments;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShipmentFlowTest {

	@LocalServerPort
	int port;

	final HttpClient http = HttpClient.newHttpClient();

	HttpResponse<String> call(String method, String path, String user, String body) throws Exception {
		var b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "application/json");
		if (user != null) {
			b.header("X-User-Id", user);
		}
		b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
		return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
	}

	long firstId(String json) {
		Matcher m = Pattern.compile("\"id\":([0-9]+)").matcher(json);
		assertTrue(m.find(), json);
		return Long.parseLong(m.group(1));
	}

	long comunaId(String name) throws Exception {
		var r = call("GET", "/api/v1/geo/comunas?q=" + name.replace(" ", "%20"), null, null);
		assertEquals(200, r.statusCode());
		return firstId(r.body());
	}

	@Test
	void flujoCompletoDeEnvio() throws Exception {
		long providencia = comunaId("Providencia");
		long puntaArenas = comunaId("Punta Arenas");

		String addr = """
				{"label":"Casa","recipientName":"Ana Perez","phone":"+56912345678","street":"Av. Siempre Viva",
				 "streetNumber":"742","postalCode":"6200000","comunaId":%d}""".formatted(puntaArenas);
		var created = call("POST", "/api/v1/addresses", "buyer-1", addr);
		assertEquals(201, created.statusCode(), created.body());
		assertTrue(created.body().contains("\"defaultAddress\":true"));
		long addressId = firstId(created.body());

		// cotizacion: de Santiago a Magallanes es mas cara que dentro de la misma comuna
		var quote = call("POST", "/api/v1/shipments/quote", null,
				"{\"originComunaId\":%d,\"destinationComunaId\":%d,\"weightKg\":2.5}".formatted(providencia, puntaArenas));
		assertEquals(200, quote.statusCode(), quote.body());
		assertTrue(quote.body().contains("\"currency\":\"CLP\""));

		String shipment = """
				{"orderId":"ORD-1","sellerId":"seller-1","addressId":%d,"originComunaId":%d,"weightKg":2.5,
				 "serviceLevel":"EXPRESS"}""".formatted(addressId, providencia);
		var s = call("POST", "/api/v1/shipments", "buyer-1", shipment);
		assertEquals(201, s.statusCode(), s.body());
		long shipmentId = firstId(s.body());
		Matcher code = Pattern.compile("\"trackingCode\":\"(SHP-[A-Z0-9]{10})\"").matcher(s.body());
		assertTrue(code.find(), s.body());

		// duplicado de la misma orden
		assertEquals(409, call("POST", "/api/v1/shipments", "buyer-1", shipment).statusCode());

		// un tercero no lo ve; el vendedor si
		assertEquals(404, call("GET", "/api/v1/shipments/" + shipmentId, "intruso", null).statusCode());
		assertEquals(200, call("GET", "/api/v1/shipments/" + shipmentId, "seller-1", null).statusCode());

		// el comprador no puede avanzar el estado; el vendedor si, pero solo en transiciones validas
		String processing = "{\"status\":\"PROCESSING\",\"location\":\"Providencia\"}";
		assertEquals(403, call("PATCH", "/api/v1/shipments/" + shipmentId + "/status", "buyer-1", processing).statusCode());
		assertEquals(409, call("PATCH", "/api/v1/shipments/" + shipmentId + "/status", "seller-1",
				"{\"status\":\"DELIVERED\"}").statusCode());
		var ok = call("PATCH", "/api/v1/shipments/" + shipmentId + "/status", "seller-1", processing);
		assertEquals(200, ok.statusCode(), ok.body());

		// seguimiento publico sin datos personales
		var track = call("GET", "/api/v1/shipments/track/" + code.group(1).toLowerCase(), null, null);
		assertEquals(200, track.statusCode(), track.body());
		assertTrue(track.body().contains("\"status\":\"PROCESSING\""));
		assertTrue(!track.body().contains("Ana Perez"));

		var cancelled = call("POST", "/api/v1/shipments/" + shipmentId + "/cancel", "buyer-1", null);
		assertEquals(200, cancelled.statusCode(), cancelled.body());
		assertTrue(cancelled.body().contains("\"status\":\"CANCELLED\""));
	}

	@Test
	void direccionesRequierenIdentidadYDatosValidos() throws Exception {
		assertEquals(401, call("GET", "/api/v1/addresses", null, null).statusCode());
		var bad = call("POST", "/api/v1/addresses", "buyer-2", "{\"comunaId\":1}");
		assertEquals(400, bad.statusCode());
		var noComuna = call("POST", "/api/v1/addresses", "buyer-2", """
				{"recipientName":"X","phone":"+56912345678","street":"Calle","streetNumber":"1","comunaId":999999}""");
		assertEquals(404, noComuna.statusCode());
	}
}
