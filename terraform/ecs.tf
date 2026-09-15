resource "aws_ecs_cluster" "main" {
  name = "pedidos360-cluster"
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
      image     = "public.ecr.aws/nginx/nginx:latest"
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
    security_groups  = tolist(aws_db_instance.postgres.vpc_security_group_ids)
  }
}