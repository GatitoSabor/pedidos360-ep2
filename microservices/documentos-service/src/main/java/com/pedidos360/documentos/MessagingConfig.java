package com.pedidos360.documentos;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class MessagingConfig {
  @Bean MessageConverter jsonMessageConverter() { Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(); DefaultClassMapper mapper = new DefaultClassMapper(); mapper.setIdClassMapping(java.util.Map.of("pedido.event", PedidoEvent.class)); converter.setClassMapper(mapper); return converter; }
  @Bean SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter converter) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        ConditionalRejectingErrorHandler errorHandler = new ConditionalRejectingErrorHandler(); errorHandler.setRejectManual(true);
        factory.setConnectionFactory(connectionFactory); factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); factory.setPrefetchCount(1); factory.setDefaultRequeueRejected(false); factory.setErrorHandler(errorHandler); factory.setMessageConverter(converter);
    factory.setAdviceChain(RetryInterceptorBuilder.stateless().maxAttempts(3).backOffOptions(1000, 2.0, 4000).recoverer((message, cause) -> { throw new AmqpRejectAndDontRequeueException("Retries exhausted", true, cause); }).build()); return factory;
  }
}
