resource "aws_db_subnet_group" "main" {
  name       = "${var.project}-rds-subnet-group"
  subnet_ids = aws_subnet.public[*].id

  tags = { Name = "${var.project}-rds-subnet-group" }
}

resource "aws_db_instance" "main" {
  identifier     = "${var.project}-mysql"
  engine         = "mysql"
  engine_version = "8.0"
  instance_class = var.db_instance_class

  allocated_storage = 20
  storage_type      = "gp3"

  db_name  = var.db_name
  username = var.db_username
  password = random_password.db.result

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  multi_az               = false

  skip_final_snapshot = true
  deletion_protection = false
  apply_immediately   = true

  tags = { Name = "${var.project}-mysql" }
}
