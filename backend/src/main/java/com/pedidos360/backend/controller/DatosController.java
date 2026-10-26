package com.pedidos360.backend.controller;

import com.pedidos360.backend.entity.Pedido;
import com.pedidos360.backend.repository.PedidoRepository;
import com.pedidos360.backend.messaging.PedidoEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:5173"})
@RestController
@RequestMapping({"/api/datos", "/api/pedidos/datos"})
/**
 * Expone el CRUD de pedidos. Las operaciones que modifican información publican
 * un evento solo después de que el pedido se guardó correctamente.
 */
public class DatosController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoEventProducer pedidoEventProducer;

    @GetMapping
    /** Devuelve todas las solicitudes disponibles para la vista React. */
    public List<Pedido> obtenerDatos() {
        return pedidoRepository.findAll();
    }

    @PostMapping
    /** Crea un pedido pendiente y notifica su creación de forma asíncrona. */
    public Pedido crearPedido(@RequestBody Pedido pedido) {
        pedido.setEstado("PENDIENTE");
        Pedido guardado = pedidoRepository.save(pedido);
        pedidoEventProducer.publish(guardado, "pedido.creado");
        return guardado;
    }
    
    @PutMapping("/{id}")
    /** Actualiza los datos editables de una solicitud existente. */
    public ResponseEntity<Pedido> actualizarPedido(@PathVariable Long id, @RequestBody Pedido detalles) {
        Optional<Pedido> pedidoExistente = pedidoRepository.findById(id);
        if (pedidoExistente.isPresent()) {
            Pedido p = pedidoExistente.get();
            p.setDescripcion(detalles.getDescripcion());
            p.setFechaInicio(detalles.getFechaInicio());
            p.setFechaTermino(detalles.getFechaTermino());
            Pedido actualizado = pedidoRepository.save(p);
            pedidoEventProducer.publish(actualizado, "pedido.actualizado");
            return ResponseEntity.ok(actualizado);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/revision")
    /** Aprueba o rechaza el pedido y envía el evento que corresponde al estado final. */
    public ResponseEntity<Pedido> revisarPedido(@PathVariable Long id, @RequestBody Pedido revision) {
        Optional<Pedido> pedidoExistente = pedidoRepository.findById(id);
        if (pedidoExistente.isPresent()) {
            Pedido p = pedidoExistente.get();
            p.setEstado(revision.getEstado());
            p.setComentarioAprobador(revision.getComentarioAprobador());
            Pedido actualizado = pedidoRepository.save(p);
            if ("APROBADO".equals(actualizado.getEstado())) {
                pedidoEventProducer.publish(actualizado, "pedido.aprobado");
            } else if ("RECHAZADO".equals(actualizado.getEstado())) {
                pedidoEventProducer.publish(actualizado, "pedido.rechazado");
            }
            return ResponseEntity.ok(actualizado);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    /** Elimina solo pedidos que existen; responde 404 para un identificador desconocido. */
    public ResponseEntity<Void> eliminarPedido(@PathVariable Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
