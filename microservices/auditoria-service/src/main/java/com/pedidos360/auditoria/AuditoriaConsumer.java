package com.pedidos360.auditoria;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
@Service public class AuditoriaConsumer {
  private static final Logger log = LoggerFactory.getLogger(AuditoriaConsumer.class);
  @RabbitListener(queues = "${messaging.queue}")
  public void register(PedidoEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
    if (event.descripcion().contains("[FALLAR]")) throw new IllegalStateException("Fallo de auditoría simulado");
    log.info("Auditoría: pedido {} fue rechazado", event.pedidoId()); channel.basicAck(tag, false);
  }
}
