package com.example.shipments.Messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica en Kafka solo despues de que la transaccion de BD se confirma, para no anunciar un envio que luego se
 * revierte. Es "dispara y sigue": si Kafka no responde se registra el error y el envio ya guardado no se deshace.
 * La clave del mensaje es el id del envio, asi los eventos de un mismo envio se procesan en orden.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "shipments.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class ShipmentKafkaPublisher {

    public static final String TOPIC = "shipment-events";

    private final KafkaTemplate<String, Object> kafka;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publish(ShipmentKafkaEvent event) {
        try {
            kafka.send(TOPIC, String.valueOf(event.shipmentId()), event).whenComplete((result, error) -> {
                if (error != null) {
                    log.error("Kafka rechazo {} del envio {}: {}", event.eventType(), event.trackingCode(),
                            error.getMessage());
                } else {
                    log.info("Publicado {} del envio {} en {} (particion {}, offset {})", event.eventType(),
                            event.trackingCode(), TOPIC, result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (RuntimeException e) {
            log.error("No se pudo publicar {} del envio {} en {}: {}", event.eventType(), event.trackingCode(),
                    TOPIC, e.getMessage());
        }
    }
}
