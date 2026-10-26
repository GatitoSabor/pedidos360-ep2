package com.pedidos360.documentos;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
/** Genera el comprobante lógico de los pedidos aprobados recibidos desde RabbitMQ. */
@Service public class DocumentoConsumer {
  private static final Logger log = LoggerFactory.getLogger(DocumentoConsumer.class);
  @RabbitListener(queues = "${messaging.queue}")
  /** Hace ACK al terminar; [FALLAR] permite demostrar la recuperación mediante DLQ. */
  public void generate(PedidoEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
    if (event.descripcion().contains("[FALLAR]")) throw new IllegalStateException("Fallo de documento simulado");
    log.info("Comprobante generado para pedido aprobado {}", event.pedidoId()); channel.basicAck(tag, false);
  }
}
