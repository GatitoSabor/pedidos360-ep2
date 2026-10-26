package com.pedidos360.rabbitadmin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
/** Datos validados para declarar una cola, evitando nombres no permitidos. */
public record QueueRequest(@NotBlank @Pattern(regexp = "^[a-z0-9][a-z0-9.-]{1,80}$") String name, boolean durable) { }
