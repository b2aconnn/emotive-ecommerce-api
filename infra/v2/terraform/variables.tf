variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "project" {
  description = "리소스 네이밍/태그 prefix (버전별로 달라야 동시 실행 시 이름 충돌이 안 남)"
  type        = string
  default     = "loopers-dev-v2"
}

variable "compute_enabled" {
  description = "토글: true면 ASG/ALB/ElastiCache 생성, false면 삭제"
  type        = bool
  default     = true
}

variable "asg_min_size" {
  description = "ASG 최소 인스턴스 수"
  type        = number
  default     = 1
}

variable "asg_max_size" {
  description = "ASG 최대 인스턴스 수"
  type        = number
  default     = 3
}

variable "asg_desired_capacity" {
  description = "ASG 목표 인스턴스 수 (부하테스트 중 수동 조절)"
  type        = number
  default     = 1
}

variable "app_instance_type" {
  description = "AWS Free Plan 계정은 free-tier-eligible 타입만 launch 가능 - t3.medium은 거부됨. m7i-flex.large(2vCPU/8GB)가 이 계정에서 확인된 최대 스펙"
  type        = string
  default     = "m7i-flex.large"
}

variable "db_instance_class" {
  description = "AWS Free Plan 계정은 프리티어 대상 클래스(db.t3.micro/db.t4g.micro 등)만 생성 가능 - db.t3.medium은 FreeTierRestrictionError로 거부됨"
  type        = string
  default     = "db.t4g.micro"
}

variable "redis_node_type" {
  type    = string
  default = "cache.t3.medium"
}

variable "db_name" {
  type    = string
  default = "loopers"
}

variable "db_username" {
  type    = string
  default = "application"
}

variable "app_port" {
  description = "앱 컨테이너가 리스닝하는 포트 (ALB/헬스체크 대상)"
  type        = number
  default     = 8080
}

variable "vpc_cidr" {
  type    = string
  default = "10.20.0.0/16"
}

variable "public_subnet_cidrs" {
  type    = list(string)
  default = ["10.20.1.0/24", "10.20.2.0/24"]
}

variable "availability_zones" {
  type    = list(string)
  default = ["ap-northeast-2a", "ap-northeast-2c"]
}

variable "alb_ingress_cidr" {
  description = "ALB 80 포트 인바운드 허용 CIDR (부하테스트 클라이언트가 접근해야 하므로 기본은 전체 허용)"
  type        = string
  default     = "0.0.0.0/0"
}
