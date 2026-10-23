package com.pedidos360.backend.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Único lugar donde se declaran nombres, exchanges, colas, DLQ y bindings. */
@Configuration
public class RabbitTopologyConfig {
    public static final String EVENTS_EXCHANGE = "pedidos.events";
    public static final String DLX_EXCHANGE = "pedidos.dlx";
    public static final String NOTIFICATIONS_QUEUE = "notificaciones.queue";
    public static final String DOCUMENTS_QUEUE = "documentos.queue";
    public static final String AUDIT_QUEUE = "auditoria.queue";
    public static final String NOTIFICATIONS_DLQ = "notificaciones.dlq";
    public static final String DOCUMENTS_DLQ = "documentos.dlq";
    public static final String AUDIT_DLQ = "auditoria.dlq";

    @Bean TopicExchange pedidosEventsExchange() { return new TopicExchange(EVENTS_EXCHANGE, true, false); }
    @Bean DirectExchange pedidosDlxExchange() { return new DirectExchange(DLX_EXCHANGE, true, false); }
    @Bean MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper mapper = new DefaultClassMapper();
        mapper.setIdClassMapping(java.util.Map.of("pedido.event", PedidoEvent.class));
        converter.setClassMapper(mapper);
        return converter;
    }

    @Bean RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean Queue notificationsQueue() { return mainQueue(NOTIFICATIONS_QUEUE, NOTIFICATIONS_DLQ); }
    @Bean Queue documentsQueue() { return mainQueue(DOCUMENTS_QUEUE, DOCUMENTS_DLQ); }
    @Bean Queue auditQueue() { return mainQueue(AUDIT_QUEUE, AUDIT_DLQ); }
    @Bean Queue notificationsDlq() { return QueueBuilder.durable(NOTIFICATIONS_DLQ).build(); }
    @Bean Queue documentsDlq() { return QueueBuilder.durable(DOCUMENTS_DLQ).build(); }
    @Bean Queue auditDlq() { return QueueBuilder.durable(AUDIT_DLQ).build(); }

    @Bean Binding notificationBinding() { return BindingBuilder.bind(notificationsQueue()).to(pedidosEventsExchange()).with("pedido.*"); }
    @Bean Binding documentBinding() { return BindingBuilder.bind(documentsQueue()).to(pedidosEventsExchange()).with("pedido.aprobado"); }
    @Bean Binding auditBinding() { return BindingBuilder.bind(auditQueue()).to(pedidosEventsExchange()).with("pedido.rechazado"); }
    @Bean Binding notificationDlxBinding() { return BindingBuilder.bind(notificationsDlq()).to(pedidosDlxExchange()).with(NOTIFICATIONS_DLQ); }
    @Bean Binding documentDlxBinding() { return BindingBuilder.bind(documentsDlq()).to(pedidosDlxExchange()).with(DOCUMENTS_DLQ); }
    @Bean Binding auditDlxBinding() { return BindingBuilder.bind(auditDlq()).to(pedidosDlxExchange()).with(AUDIT_DLQ); }

    /** Declara la infraestructura antes de que los consumidores intenten suscribirse. */
    @Bean
    @Profile("!test")
    ApplicationRunner initializeRabbitTopology(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }

    private Queue mainQueue(String queueName, String dlqName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(dlqName)
                .build();
    }
}
