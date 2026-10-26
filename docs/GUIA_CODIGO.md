# Guía comentada del código de Pedidos360

Esta guía complementa los comentarios del código y sirve como apoyo para explicar la implementación durante la defensa.

## Frontend React

- `frontend/src/main.jsx`: punto de entrada de Vite. Monta la aplicación y configura el proveedor OIDC que entrega sesión y token JWT a los componentes.
- `frontend/src/App.jsx`: pantalla principal. Lee el token con `useAuth`, identifica los grupos Cognito `solicitante` y `aprobador`, y ejecuta el CRUD mediante `fetch` con el encabezado `Authorization: Bearer`.
- `frontend/src/App.css` e `index.css`: estilos de la interfaz, estados visuales y tabla de solicitudes.

## Backend pedidos-service

- `BackendApplication`: arranque de Spring Boot.
- `config/SecurityConfig`: perfil normal. Exige un JWT emitido por Cognito para cada petición y configura CORS.
- `config/MessagingDemoSecurityConfig`: perfil local `messaging-demo`. Permite ejecutar la demostración RabbitMQ con H2 sin requerir conectividad a Cognito. No se aplica en el perfil normal.
- `controller/DatosController`: API CRUD de pedidos. Guarda primero la operación y luego publica el evento de negocio correspondiente.
- `entity/Pedido`: modelo persistido con descripción, período, estado y comentario de aprobación.
- `repository/PedidoRepository`: acceso JPA a la tabla `pedidos`.
- `messaging/PedidoEvent`: contrato JSON compartido entre productor y consumidores.
- `messaging/PedidoEventProducer`: construye el evento y lo publica en `pedidos.events` con una routing key.
- `messaging/RabbitTopologyConfig`: fuente única de nombres de exchanges, colas, DLQ y bindings. Declara la topología durable al iniciar.

## Microservicios consumidores

Los tres servicios siguen el mismo patrón: reciben JSON, realizan ACK manual cuando el procesamiento termina y lanzan una excepción controlada cuando la descripción contiene `[FALLAR]`. El interceptor de reintentos intenta tres veces con esperas de 1, 2 y 4 segundos; si se agotan, rechaza sin requeue y RabbitMQ deriva el mensaje a la DLQ.

- `notificaciones-service`: escucha `notificaciones.queue` con `pedido.*`.
- `documentos-service`: escucha `documentos.queue` con `pedido.aprobado`.
- `auditoria-service`: escucha `auditoria.queue` con `pedido.rechazado`.
- En cada servicio, `MessagingConfig` crea el convertidor JSON y configura ACK manual, prefetch, reintentos y recuperador DLQ.

## Microservicio administrador RabbitMQ

- `RabbitAdminController`: expone la API REST bajo `/api/rabbitmq`.
- `RabbitAdminService`: usa `RabbitAdmin` para crear y eliminar colas, exchanges y bindings.
- `QueueRequest`, `ExchangeRequest` y `BindingRequest`: validan nombres y tipos admitidos antes de operar RabbitMQ.
- `RabbitAdminConfig`: declara el bean `RabbitAdmin` conectado al `ConnectionFactory` de Spring AMQP.

## Pruebas

- `BackendApplicationTests`: inicia el contexto del backend con H2 y sin declarar la topología RabbitMQ.
- `NotificacionConsumerTest`, `DocumentoConsumerTest` y `AuditoriaConsumerTest`: verifican ACK exitoso y fallo controlado de cada consumidor.
- `RabbitAdminServiceTest`: verifica operaciones de creación y eliminación de bindings del administrador.

## Cómo seguir el flujo completo

1. React inicia sesión con Cognito y recibe JWT.
2. React llama al backend con el token.
3. `DatosController` actualiza `Pedido` mediante `PedidoRepository`.
4. `PedidoEventProducer` publica el evento a `pedidos.events`.
5. Los bindings llevan el mensaje a una o más colas.
6. Cada consumidor confirma el mensaje o, tras reintentos agotados, lo envía a la DLQ.
