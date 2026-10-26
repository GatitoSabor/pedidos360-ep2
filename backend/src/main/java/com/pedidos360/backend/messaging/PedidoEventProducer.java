package com.pedidos360.backend.messaging;

import java.time.Instant;
import com.pedidos360.backend.entity.Pedido;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
/** Publica cambios de pedidos sin acoplar el controlador a los consumidores. */
public class PedidoEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public PedidoEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Serializa el estado relevante del pedido y lo publica con la routing key
     * recibida, por ejemplo pedido.creado o pedido.aprobado.
     */
    public void publish(Pedido pedido, String eventType) {
        PedidoEvent event = new PedidoEvent(pedido.getId(), eventType, pedido.getDescripcion(), pedido.getEstado(), Instant.now());
        rabbitTemplate.convertAndSend(RabbitTopologyConfig.EVENTS_EXCHANGE, eventType, event);
    }
}
