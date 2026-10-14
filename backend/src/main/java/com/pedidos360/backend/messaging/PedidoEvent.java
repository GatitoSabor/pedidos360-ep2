package com.pedidos360.backend.messaging;

import java.time.Instant;

/** Contrato de eventos emitidos por pedidos-service hacia los demás microservicios. */
public record PedidoEvent(Long pedidoId, String tipo, String descripcion, String estado, Instant occurredAt) { }
