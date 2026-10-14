package com.pedidos360.backend.messaging;

import java.time.Instant;
import com.pedidos360.backend.entity.Pedido;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PedidoEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public PedidoEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Pedido pedido, String eventType) {
        PedidoEvent event = new PedidoEvent(pedido.getId(), eventType, pedido.getDescripcion(), pedido.getEstado(), Instant.now());
        rabbitTemplate.convertAndSend(RabbitTopologyConfig.EVENTS_EXCHANGE, eventType, event);
    }
}
