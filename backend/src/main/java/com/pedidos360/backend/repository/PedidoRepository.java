package com.pedidos360.backend.repository;

import com.pedidos360.backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/** Adaptador Spring Data que entrega el CRUD de la entidad Pedido. */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
  
}
