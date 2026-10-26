package com.pedidos360.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/** Punto de entrada del microservicio que gestiona los pedidos. */
public class BackendApplication {

	/** Inicia el contexto Spring, la API REST, JPA y la topología RabbitMQ. */
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
