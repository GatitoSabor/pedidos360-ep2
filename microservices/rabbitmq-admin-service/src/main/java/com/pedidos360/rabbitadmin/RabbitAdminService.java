package com.pedidos360.rabbitadmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {
  private final RabbitAdmin rabbitAdmin;
  public RabbitAdminService(RabbitAdmin rabbitAdmin) { this.rabbitAdmin = rabbitAdmin; }
  public void createQueue(QueueRequest request) { rabbitAdmin.declareQueue(new Queue(request.name(), request.durable())); }
  public boolean deleteQueue(String name) { return rabbitAdmin.deleteQueue(name); }
  public void createExchange(ExchangeRequest request) {
    Exchange exchange = "direct".equals(request.type()) ? new DirectExchange(request.name(), request.durable(), false) : new TopicExchange(request.name(), request.durable(), false);
    rabbitAdmin.declareExchange(exchange);
  }
  public boolean deleteExchange(String name) { return rabbitAdmin.deleteExchange(name); }
  public void createBinding(BindingRequest request) {
    rabbitAdmin.declareBinding(new Binding(request.queue(), Binding.DestinationType.QUEUE, request.exchange(), request.routingKey(), null));
  }
  public void deleteBinding(BindingRequest request) {
    rabbitAdmin.removeBinding(new Binding(request.queue(), Binding.DestinationType.QUEUE, request.exchange(), request.routingKey(), null));
  }
}
