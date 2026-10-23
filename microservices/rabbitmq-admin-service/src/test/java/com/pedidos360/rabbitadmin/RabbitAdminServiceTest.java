package com.pedidos360.rabbitadmin;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

class RabbitAdminServiceTest {
  @Test
  void removesTheRequestedBinding() {
    RabbitAdmin rabbitAdmin = Mockito.mock(RabbitAdmin.class);
    RabbitAdminService service = new RabbitAdminService(rabbitAdmin);

    service.deleteBinding(new BindingRequest("demo.queue", "demo.events", "pedido.*"));

    verify(rabbitAdmin).removeBinding(Mockito.argThat(binding ->
        binding.getDestination().equals("demo.queue")
            && binding.getExchange().equals("demo.events")
            && binding.getRoutingKey().equals("pedido.*")));
  }
}
