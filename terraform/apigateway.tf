variable "backend_url" {
  type        = string
  description = "URL publica de la tarea ECS Fargate"
  default     = "http://44.195.79.115:8080" 
}

resource "aws_apigatewayv2_api" "api_pedidos" {
  name          = "api-pedidos360"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["http://localhost:4200", "http://localhost:5173", "http://localhost:3000"]  
    allow_methods = ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
    allow_headers = ["Content-Type", "Authorization"]
    max_age       = 300
  }
}

resource "aws_apigatewayv2_authorizer" "jwt_auth" {
  api_id           = aws_apigatewayv2_api.api_pedidos.id
  authorizer_type  = "JWT"
  identity_sources = ["$request.header.Authorization"]
  name             = "cognito-authorizer"

  jwt_configuration {
    issuer   = "https://cognito-idp.us-east-1.amazonaws.com/${aws_cognito_user_pool.pool.id}"
  }
}

resource "aws_apigatewayv2_integration" "backend_ecs" {
  api_id                 = aws_apigatewayv2_api.api_pedidos.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  # Esta línea asegura que la ruta llegue intacta al backend
  integration_uri        = "${var.backend_url}/api/pedidos/{proxy}" 
  payload_format_version = "1.0"
}

resource "aws_apigatewayv2_route" "route_read" {
  api_id               = aws_apigatewayv2_api.api_pedidos.id
  route_key            = "GET /api/pedidos/{proxy+}"
  target               = "integrations/${aws_apigatewayv2_integration.backend_ecs.id}"
  authorization_type   = "JWT"
  authorizer_id        = aws_apigatewayv2_authorizer.jwt_auth.id
  authorization_scopes = ["pedidos/api.read"]
}

resource "aws_apigatewayv2_route" "route_write" {
  api_id               = aws_apigatewayv2_api.api_pedidos.id
  route_key            = "POST /api/pedidos/{proxy+}"
  target               = "integrations/${aws_apigatewayv2_integration.backend_ecs.id}"
  authorization_type   = "JWT"
  authorizer_id        = aws_apigatewayv2_authorizer.jwt_auth.id
  authorization_scopes = ["pedidos/api.write"]
}

resource "aws_apigatewayv2_route" "route_update" {
  api_id               = aws_apigatewayv2_api.api_pedidos.id
  route_key            = "PUT /api/pedidos/{proxy+}"
  target               = "integrations/${aws_apigatewayv2_integration.backend_ecs.id}"
  authorization_type   = "JWT"
  authorizer_id        = aws_apigatewayv2_authorizer.jwt_auth.id
  authorization_scopes = ["pedidos/api.update"]
}

resource "aws_apigatewayv2_route" "route_delete" {
  api_id               = aws_apigatewayv2_api.api_pedidos.id
  route_key            = "DELETE /api/pedidos/{proxy+}"
  target               = "integrations/${aws_apigatewayv2_integration.backend_ecs.id}"
  authorization_type   = "JWT"
  authorizer_id        = aws_apigatewayv2_authorizer.jwt_auth.id
  authorization_scopes = ["pedidos/api.delete"]
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.api_pedidos.id
  name        = "$default"
  auto_deploy = true
}

output "api_gateway_url" {
  value = aws_apigatewayv2_stage.default.invoke_url
}

resource "aws_apigatewayv2_route" "route_patch" {
  api_id               = aws_apigatewayv2_api.api_pedidos.id
  route_key            = "PATCH /api/pedidos/{proxy+}"
  target               = "integrations/${aws_apigatewayv2_integration.backend_ecs.id}"
  authorization_type   = "JWT"
  authorizer_id        = aws_apigatewayv2_authorizer.jwt_auth.id
  authorization_scopes = ["pedidos/api.update"]
}