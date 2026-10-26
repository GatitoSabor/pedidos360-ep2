resource "aws_security_group" "db_sg" {
  name        = "pedidos360_db_sg"
  description = "Permitir trafico a PostgreSQL"

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_instance" "postgres" {
  identifier             = "pedidos360-db"
  engine                 = "postgres"
  instance_class         = "db.t3.micro"
  allocated_storage      = 20
  db_name                = "pedidos360db"
  username               = "admindb"
  password               = var.db_password
  skip_final_snapshot    = true # Vital para que AWS Academy te deje borrarla después sin errores
  publicly_accessible    = false
  vpc_security_group_ids = [aws_security_group.db_sg.id]
}

output "db_endpoint" {
  value = aws_db_instance.postgres.endpoint
}
