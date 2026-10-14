package com.pedidos360.rabbitadmin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record QueueRequest(@NotBlank @Pattern(regexp = "^[a-z0-9][a-z0-9.-]{1,80}$") String name, boolean durable) { }
