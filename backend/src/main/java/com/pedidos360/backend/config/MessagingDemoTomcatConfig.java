package com.pedidos360.backend.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Ajuste exclusivo para ejecutar la demostración local en determinados equipos
 * Windows con JDK 17.0.12. Ese JDK puede fallar al iniciar el selector NIO
 * tradicional de Tomcat con un socket interno de dominio Unix. NIO2 usa el
 * mecanismo asíncrono de Windows y permite mantener la demostración HTTP local.
 * Este bean no se carga fuera del perfil {@code messaging-demo}.
 */
@Configuration
@Profile("messaging-demo")
public class MessagingDemoTomcatConfig {

    /**
     * Cambia solo el protocolo del servidor HTTP embebido de la demo. No afecta
     * las rutas REST, JPA, RabbitMQ ni la configuración productiva.
     */
    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> messagingDemoTomcatCustomizer() {
        return factory -> factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
    }
}
