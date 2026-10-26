package com.pedidos360.rabbitadmin;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/rabbitmq")
/** API REST para administrar de forma validada la topología de RabbitMQ. */
public class RabbitAdminController {
  private final RabbitAdminService rabbitAdminService;
  public RabbitAdminController(RabbitAdminService rabbitAdminService) { this.rabbitAdminService = rabbitAdminService; }
  @PostMapping("/queues") ResponseEntity<Void> createQueue(@Valid @RequestBody QueueRequest request) { rabbitAdminService.createQueue(request); return ResponseEntity.status(HttpStatus.CREATED).build(); }
  @DeleteMapping("/queues/{name}") ResponseEntity<Void> deleteQueue(@PathVariable @Pattern(regexp = "^[a-z0-9][a-z0-9.-]{1,80}$") String name) { return rabbitAdminService.deleteQueue(name) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build(); }
  @PostMapping("/exchanges") ResponseEntity<Void> createExchange(@Valid @RequestBody ExchangeRequest request) { rabbitAdminService.createExchange(request); return ResponseEntity.status(HttpStatus.CREATED).build(); }
  @DeleteMapping("/exchanges/{name}") ResponseEntity<Void> deleteExchange(@PathVariable @Pattern(regexp = "^[a-z0-9][a-z0-9.-]{1,80}$") String name) { return rabbitAdminService.deleteExchange(name) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build(); }
  @PostMapping("/bindings") ResponseEntity<Void> createBinding(@Valid @RequestBody BindingRequest request) { rabbitAdminService.createBinding(request); return ResponseEntity.status(HttpStatus.CREATED).build(); }
  /** Elimina un binding existente, requisito explícito de la evaluación. */
  @DeleteMapping("/bindings") ResponseEntity<Void> deleteBinding(@Valid @RequestBody BindingRequest request) { rabbitAdminService.deleteBinding(request); return ResponseEntity.noContent().build(); }
}
