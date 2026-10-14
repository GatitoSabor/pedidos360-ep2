package com.pedidos360.notificaciones;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class NotificacionConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificacionConsumer.class);

    @RabbitListener(queues = "${messaging.queue}")
    public void notify(PedidoEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        if (event.descripcion().contains("[FALLAR]")) {
            log.warn("Fallo simulado para pedido {}; reintento con backoff", event.pedidoId());
            throw new IllegalStateException("Fallo de notificación simulado");
        }
        log.info("Notificación {} para pedido {}: {}", event.tipo(), event.pedidoId(), event.descripcion());
        channel.basicAck(tag, false);
    }
}
