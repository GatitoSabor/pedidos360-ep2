package com.pedidos360.notificaciones;

import java.time.Instant;
public record PedidoEvent(Long pedidoId, String tipo, String descripcion, String estado, Instant occurredAt) { }
