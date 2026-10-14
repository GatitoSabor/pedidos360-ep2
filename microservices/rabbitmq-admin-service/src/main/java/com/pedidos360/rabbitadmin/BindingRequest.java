package com.pedidos360.rabbitadmin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record BindingRequest(@NotBlank @Pattern(regexp = "^[a-z0-9][a-z0-9.-]{1,80}$") String queue, @NotBlank @Pattern(regexp = "^[a-z0-9][a-z0-9.-]{1,80}$") String exchange, @NotBlank String routingKey) { }
