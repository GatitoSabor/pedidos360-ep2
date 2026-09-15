variable "db_username" {
  type    = string
  default = "admindb"
}

variable "db_password" {
  type      = string
  sensitive = true
  default   = "CloudNative2026!"
}