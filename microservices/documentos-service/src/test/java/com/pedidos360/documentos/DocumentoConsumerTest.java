package com.pedidos360.documentos;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.rabbitmq.client.Channel;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DocumentoConsumerTest {
  private final DocumentoConsumer consumer = new DocumentoConsumer();

  @Test
  void acknowledgesSuccessfulMessages() throws Exception {
    Channel channel = mock(Channel.class);
    consumer.generate(new PedidoEvent(1L, "pedido.aprobado", "Solicitud válida", "APROBADO", Instant.now()), channel, 9L);
    verify(channel).basicAck(9L, false);
  }

  @Test
  void propagatesControlledFailuresForRetryAndDlq() {
    assertThrows(IllegalStateException.class, () -> consumer.generate(new PedidoEvent(1L, "pedido.aprobado", "[FALLAR]", "APROBADO", Instant.now()), mock(Channel.class), 9L));
  }
}
