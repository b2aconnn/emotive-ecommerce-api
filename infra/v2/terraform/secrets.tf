resource "random_password" "db" {
  length           = 20
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "aws_ssm_parameter" "db_password" {
  name  = "/${var.project}/rds/password"
  type  = "SecureString"
  value = random_password.db.result

  tags = { Name = "${var.project}-rds-password" }
}
