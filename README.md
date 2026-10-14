# Pedidos360 - Parcial 2 (EP3 y EP4)

Extensión de la EP1 que conserva React/Vite como frontend y separa las responsabilidades asíncronas en microservicios Spring Boot conectados a un clúster RabbitMQ de dos nodos.

## Arquitectura

```text
React/Vite -> API Gateway + JWT -> pedidos-service -> PostgreSQL
                                      |
                                      | Topic: pedidos.events
        +-----------------------------+----------------------------+
        |                              |                            |
notificaciones-service          documentos-service            auditoria-service
pedido.*                        pedido.aprobado               pedido.rechazado
notificaciones.queue            documentos.queue              auditoria.queue
        |                              |                            |
notificaciones.dlq              documentos.dlq                auditoria.dlq
        \_____________________ Direct: pedidos.dlx _____________/

rabbitmq-admin-service: REST para crear/eliminar queues, exchanges y bindings.
```

| Evento | Productor | Consumidor | Uso |
|---|---|---|---|
| `pedido.creado`, `pedido.actualizado` | pedidos-service | notificaciones-service | Informar cambios de una solicitud |
| `pedido.aprobado` | pedidos-service | notificaciones-service, documentos-service | Notificar y generar comprobante |
| `pedido.rechazado` | pedidos-service | notificaciones-service, auditoria-service | Notificar y registrar auditoría |

Cada cola principal es durable, tiene una DLQ durable y está unida al exchange `pedidos.dlx` de tipo **direct**. Los eventos de negocio se distribuyen mediante `pedidos.events`, de tipo **topic**. Los consumidores usan ACK manual; ante un fallo ejecutan tres intentos con backoff (1 s, 2 s y 4 s) y, al agotarlos, RabbitMQ rechaza sin requeue y entrega el mensaje en su DLQ. Los detalles de nombres y bindings están centralizados en `RabbitTopologyConfig` del `pedidos-service`.

## Componentes

- `frontend/`: cliente React existente, autenticado con Cognito/OIDC.
- `backend/`: **pedidos-service**, CRUD, PostgreSQL, validación JWT y productor de eventos.
- `microservices/notificaciones-service/`: listener de `notificaciones.queue`.
- `microservices/documentos-service/`: listener de `documentos.queue`.
- `microservices/auditoria-service/`: listener de `auditoria.queue`.
- `microservices/rabbitmq-admin-service/`: administración REST de RabbitMQ.
- `docker-compose.rabbitmq.yml`: dos nodos RabbitMQ (AMQP: 5672/5673; UI: 15672/15673).

## Ejecución local

1. Levantar RabbitMQ:

```powershell
docker compose -f docker-compose.rabbitmq.yml up -d
docker exec rabbitmq-1 rabbitmqctl cluster_status
```

2. En terminales separadas, iniciar los servicios en este orden: `backend`, `microservices/notificaciones-service`, `microservices/documentos-service`, `microservices/auditoria-service` y `microservices/rabbitmq-admin-service`.

```powershell
./mvnw.cmd spring-boot:run
```

Para demostrar mensajería local sin depender de RDS, inicia `backend` con el perfil H2: `./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=messaging-demo"`.

3. Iniciar `frontend` con `npm install` y `npm run dev`.

Los usuarios de RabbitMQ Management son `guest/guest` en `http://localhost:15672` y `http://localhost:15673`.

## API del administrador

El servicio de administración expone las siguientes operaciones, todas con validación de nombres:

```text
POST   /api/rabbitmq/queues       { "name": "demo.queue", "durable": true }
DELETE /api/rabbitmq/queues/{name}
POST   /api/rabbitmq/exchanges    { "name": "demo.events", "type": "topic", "durable": true }
DELETE /api/rabbitmq/exchanges/{name}
POST   /api/rabbitmq/bindings     { "queue": "demo.queue", "exchange": "demo.events", "routingKey": "pedido.*" }
```

## Evidencias EP3

1. Código de `RabbitTopologyConfig`, productores y tres `@RabbitListener`.
2. RabbitMQ Management mostrando `pedidos.events` (topic), `pedidos.dlx` (direct), tres queues y tres DLQ con bindings.
3. Logs de ACK exitoso y de NACK que termine en una DLQ.
4. Llamadas REST válidas e inválidas al administrador, probando la validación.
5. Compilación de cada microservicio y build del frontend.

Para forzar de forma controlada la demostración de reintentos/DLQ, crea o actualiza un pedido cuya descripción contenga `[FALLAR]` y apruébalo o recházalo. El listener correspondiente fallará tres veces y el mensaje aparecerá en su DLQ.

## Guion EP4

1. Mostrar login React/Cognito, token válido y petición exitosa por API Gateway.
2. Mostrar 401 con token ausente o inválido y 200 con token válido.
3. Mostrar backend, frontend y RDS desplegados; explicar que React sigue consumiendo el API Manager.
4. Levantar Docker Compose, mostrar `cluster_status` con dos nodos y ambas interfaces Management.
5. Crear/aprobar/rechazar un pedido y seguir los eventos hasta cada consumidor o DLQ.
6. Mostrar el administrador REST y justificar `topic` para eventos y `direct` para DLQ.
