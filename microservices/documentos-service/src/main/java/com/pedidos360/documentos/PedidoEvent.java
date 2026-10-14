package com.pedidos360.documentos;
import java.time.Instant;
public record PedidoEvent(Long pedidoId, String tipo, String descripcion, String estado, Instant occurredAt) { }
