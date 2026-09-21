output "alb_dns_name" {
  description = "ALB DNS 이름 (compute_enabled=false 이면 null)"
  value       = try(aws_lb.app[0].dns_name, null)
}

output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

output "rds_endpoint" {
  value = aws_db_instance.main.address
}

output "rds_instance_id" {
  value = aws_db_instance.main.id
}
