data "aws_ssm_parameter" "al2023_ami" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

locals {
  ecr_registry = split("/", aws_ecr_repository.app.repository_url)[0]

  # commerce-api는 jpa.yml/redis.yml의 datasource 프로퍼티를 Spring 릴렉스드 바인딩
  # 규칙(점/하이픈 -> 언더스코어, 대문자화)에 맞는 환경변수로 주입해야 dev 프로필의
  # 하드코딩된 localhost 값을 오버라이드할 수 있다.
  # Kafka는 이번 인프라 범위에서 제외되지만 spring.kafka.bootstrap-servers 프로퍼티는
  # 플레이스홀더(${BOOTSTRAP_SERVERS})라 값이 없으면 부팅 자체가 실패하므로,
  # 존재하지 않는 브로커 주소를 채우고 max.block.ms로 발행 시 빠르게 실패하도록 한다.
  app_user_data = <<-EOF
    #!/bin/bash
    set -euo pipefail

    AWS_REGION="${var.aws_region}"
    ECR_REGISTRY="${local.ecr_registry}"
    ECR_REPO_URL="${aws_ecr_repository.app.repository_url}"
    SSM_DB_PASSWORD_NAME="${aws_ssm_parameter.db_password.name}"
    DB_HOST="${aws_db_instance.main.address}"
    DB_PORT="3306"
    DB_NAME="${var.db_name}"
    DB_USERNAME="${var.db_username}"
    REDIS_HOST="${try(aws_elasticache_cluster.redis[0].cache_nodes[0].address, "")}"
    APP_PORT="${var.app_port}"

    yum install -y docker
    systemctl enable docker
    systemctl start docker

    aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$ECR_REGISTRY"

    DB_PASSWORD=$(aws ssm get-parameter --name "$SSM_DB_PASSWORD_NAME" --with-decryption --region "$AWS_REGION" --query Parameter.Value --output text)

    docker pull "$ECR_REPO_URL:latest"

    docker run -d --name commerce-api --restart unless-stopped \
      -p "$APP_PORT:$APP_PORT" \
      -e SERVER_PORT="$APP_PORT" \
      -e SPRING_PROFILES_ACTIVE="dev" \
      -e DATASOURCE_MYSQL_JPA_MAIN_JDBC_URL="jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME" \
      -e DATASOURCE_MYSQL_JPA_MAIN_USERNAME="$DB_USERNAME" \
      -e DATASOURCE_MYSQL_JPA_MAIN_PASSWORD="$DB_PASSWORD" \
      -e DATASOURCE_REDIS_MASTER_HOST="$REDIS_HOST" -e DATASOURCE_REDIS_MASTER_PORT="6379" \
      -e DATASOURCE_REDIS_REPLICAS_0_HOST="$REDIS_HOST" -e DATASOURCE_REDIS_REPLICAS_0_PORT="6379" \
      -e MANAGEMENT_SERVER_PORT="$APP_PORT" \
      -e SPRING_KAFKA_BOOTSTRAP_SERVERS="unavailable:9092" \
      -e SPRING_KAFKA_PROPERTIES_MAX_BLOCK_MS="1000" \
      "$ECR_REPO_URL:latest"
  EOF
}

resource "aws_launch_template" "app" {
  name_prefix   = "${var.project}-app-"
  image_id      = data.aws_ssm_parameter.al2023_ami.value
  instance_type = var.app_instance_type

  iam_instance_profile {
    name = aws_iam_instance_profile.app_instance.name
  }

  vpc_security_group_ids = [aws_security_group.app.id]

  user_data = base64encode(local.app_user_data)

  tag_specifications {
    resource_type = "instance"
    tags          = { Name = "${var.project}-app" }
  }
}

resource "aws_lb" "app" {
  count = var.compute_enabled ? 1 : 0

  name               = "${var.project}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id

  tags = { Name = "${var.project}-alb" }
}

resource "aws_lb_target_group" "app" {
  count = var.compute_enabled ? 1 : 0

  name     = "${var.project}-app-tg"
  port     = var.app_port
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    path                = "/actuator/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    interval            = 15
    timeout             = 5
    matcher             = "200"
  }

  tags = { Name = "${var.project}-app-tg" }
}

resource "aws_lb_listener" "app" {
  count = var.compute_enabled ? 1 : 0

  load_balancer_arn = aws_lb.app[0].arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app[0].arn
  }
}

resource "aws_autoscaling_group" "app" {
  count = var.compute_enabled ? 1 : 0

  name                      = "${var.project}-app-asg"
  min_size                  = var.asg_min_size
  max_size                  = var.asg_max_size
  desired_capacity          = var.asg_desired_capacity
  vpc_zone_identifier       = aws_subnet.public[*].id
  target_group_arns         = [aws_lb_target_group.app[0].arn]
  health_check_type         = "ELB"
  health_check_grace_period = 600

  launch_template {
    id      = aws_launch_template.app.id
    version = "$Latest"
  }

  tag {
    key                 = "Name"
    value               = "${var.project}-app"
    propagate_at_launch = true
  }
}
