package com.pedidos360.notificaciones;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.rabbitmq.client.Channel;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificacionConsumerTest {
  private final NotificacionConsumer consumer = new NotificacionConsumer();

  @Test
  void acknowledgesSuccessfulMessages() throws Exception {
    Channel channel = mock(Channel.class);
    consumer.notify(new PedidoEvent(1L, "pedido.creado", "Solicitud válida", "PENDIENTE", Instant.now()), channel, 7L);
    verify(channel).basicAck(7L, false);
  }

  @Test
  void propagatesControlledFailuresForRetryAndDlq() {
    Channel channel = mock(Channel.class);
    assertThrows(IllegalStateException.class, () -> consumer.notify(new PedidoEvent(1L, "pedido.creado", "[FALLAR]", "PENDIENTE", Instant.now()), channel, 7L));
  }
}
