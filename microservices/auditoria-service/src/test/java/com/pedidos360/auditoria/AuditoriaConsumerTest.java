package com.pedidos360.auditoria;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.rabbitmq.client.Channel;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuditoriaConsumerTest {
  private final AuditoriaConsumer consumer = new AuditoriaConsumer();

  @Test
  void acknowledgesSuccessfulMessages() throws Exception {
    Channel channel = mock(Channel.class);
    consumer.register(new PedidoEvent(1L, "pedido.rechazado", "Solicitud válida", "RECHAZADO", Instant.now()), channel, 11L);
    verify(channel).basicAck(11L, false);
  }

  @Test
  void propagatesControlledFailuresForRetryAndDlq() {
    assertThrows(IllegalStateException.class, () -> consumer.register(new PedidoEvent(1L, "pedido.rechazado", "[FALLAR]", "RECHAZADO", Instant.now()), mock(Channel.class), 11L));
  }
}
