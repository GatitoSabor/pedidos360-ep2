resource "aws_cognito_user_pool" "pool" {
  name = "pedidos360-pool"

  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_uppercase = true
    require_numbers   = true
    require_symbols   = false
  }

  admin_create_user_config {
    allow_admin_create_user_only = true
  }
}

resource "aws_cognito_user_pool_domain" "hosted_ui" {
  domain       = "pedidos360-williams-${data.aws_caller_identity.current.account_id}"
  user_pool_id = aws_cognito_user_pool.pool.id
}

resource "aws_cognito_resource_server" "resource" {
  identifier   = "pedidos"
  name         = "Pedidos API Server"
  user_pool_id = aws_cognito_user_pool.pool.id

  scope {
    scope_name        = "api.read"
    scope_description = "Permite leer/consultar pedidos (GET)"
  }

  scope {
    scope_name        = "api.write"
    scope_description = "Permite crear nuevos pedidos (POST)"
  }

  scope {
    scope_name        = "api.update"
    scope_description = "Permite modificar o actualizar pedidos (PUT/PATCH)"
  }

  scope {
    scope_name        = "api.delete"
    scope_description = "Permite eliminar pedidos (DELETE)"
  }
}

resource "aws_cognito_user_pool_client" "spa" {
  name         = "spa-pedidos360"
  user_pool_id = aws_cognito_user_pool.pool.id

  generate_secret                      = false
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  supported_identity_providers         = ["COGNITO"]

  allowed_oauth_scopes = [
    "openid",
    "email",
    "profile",
    "pedidos/api.read",
    "pedidos/api.write",
    "pedidos/api.update",
    "pedidos/api.delete"
  ]

  callback_urls = ["http://localhost:4200/"]
  logout_urls   = ["http://localhost:4200/"]

  explicit_auth_flows   = ["ALLOW_USER_PASSWORD_AUTH", "ALLOW_REFRESH_TOKEN_AUTH"]
  access_token_validity = 60
  id_token_validity     = 60

  token_validity_units {
    access_token = "minutes"
    id_token     = "minutes"
  }

  depends_on = [aws_cognito_resource_server.resource]
}
