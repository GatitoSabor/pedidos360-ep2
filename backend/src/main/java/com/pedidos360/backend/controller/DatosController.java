package com.pedidos360.backend.controller;

import com.pedidos360.backend.entity.Pedido;
import com.pedidos360.backend.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:5173"})
@RestController
@RequestMapping({"/api/datos", "/api/pedidos/datos"})
public class DatosController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @GetMapping
    public List<Pedido> obtenerDatos() {
        return pedidoRepository.findAll();
    }

    @PostMapping
    public Pedido crearPedido(@RequestBody Pedido pedido) {
        pedido.setEstado("PENDIENTE");
        return pedidoRepository.save(pedido);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Pedido> actualizarPedido(@PathVariable Long id, @RequestBody Pedido detalles) {
        Optional<Pedido> pedidoExistente = pedidoRepository.findById(id);
        if (pedidoExistente.isPresent()) {
            Pedido p = pedidoExistente.get();
            p.setDescripcion(detalles.getDescripcion());
            p.setFechaInicio(detalles.getFechaInicio());
            p.setFechaTermino(detalles.getFechaTermino());
            return ResponseEntity.ok(pedidoRepository.save(p));
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/revision")
    public ResponseEntity<Pedido> revisarPedido(@PathVariable Long id, @RequestBody Pedido revision) {
        Optional<Pedido> pedidoExistente = pedidoRepository.findById(id);
        if (pedidoExistente.isPresent()) {
            Pedido p = pedidoExistente.get();
            p.setEstado(revision.getEstado());
            p.setComentarioAprobador(revision.getComentarioAprobador());
            return ResponseEntity.ok(pedidoRepository.save(p));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPedido(@PathVariable Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}