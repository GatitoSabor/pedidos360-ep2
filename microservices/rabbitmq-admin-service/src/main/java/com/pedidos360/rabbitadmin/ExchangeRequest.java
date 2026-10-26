package com.pedidos360.rabbitadmin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.AssertTrue;
/** Datos validados para declarar un exchange durable de tipo direct o topic. */
public record ExchangeRequest(@NotBlank @Pattern(regexp = "^[a-z0-9][a-z0-9.-]{1,80}$") String name, @NotBlank String type, boolean durable) {
  @AssertTrue(message = "type debe ser direct o topic") public boolean hasSupportedType() { return "direct".equals(type) || "topic".equals(type); }
}
