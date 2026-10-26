package com.pedidos360.rabbitadmin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
/** Datos validados que identifican un binding de una cola con un exchange. */
public record BindingRequest(@NotBlank @Pattern(regexp = "^[a-z0-9][a-z0-9.-]{1,80}$") String queue, @NotBlank @Pattern(regexp = "^[a-z0-9][a-z0-9.-]{1,80}$") String exchange, @NotBlank String routingKey) { }
