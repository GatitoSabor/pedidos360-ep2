resource "aws_ecs_cluster" "main" {
  name = "pedidos360-cluster"
}

# NUEVO: Creamos el grupo de logs en CloudWatch
resource "aws_cloudwatch_log_group" "backend_logs" {
  name              = "/ecs/pedidos360-backend"
  retention_in_days = 1
}

# 1. Creamos el Security Group correcto para ECS (Puerto 8080 abierto)
resource "aws_security_group" "ecs_sg" {
  name        = "pedidos360-ecs-sg"
  description = "Permitir trafico a Spring Boot"

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_ecs_task_definition" "backend" {
  family                   = "pedidos360-backend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = "arn:aws:iam::321479310670:role/LabRole"
  task_role_arn            = "arn:aws:iam::321479310670:role/LabRole"

  container_definitions = jsonencode([
    {
      name      = "backend"
      # 2. Apuntamos a tu imagen real, no a Nginx
      image     = "321479310670.dkr.ecr.us-east-1.amazonaws.com/pedidos360-backend:latest"
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]
      environment = [
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/${aws_db_instance.postgres.db_name}" },
        { name = "SPRING_DATASOURCE_USERNAME", value = var.db_username },
        { name = "SPRING_DATASOURCE_PASSWORD", value = var.db_password }
      ]
      
      # NUEVO: Configuración para enviar los logs a CloudWatch
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.backend_logs.name
          "awslogs-region"        = "us-east-1"
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "backend" {
  name            = "pedidos360-backend-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = ["subnet-0db4b7648d72b071a"]
    assign_public_ip = true
    # 3. Asignamos el Security Group correcto
    security_groups  = [aws_security_group.ecs_sg.id]
  }
}