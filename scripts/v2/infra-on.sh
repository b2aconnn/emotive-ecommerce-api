#!/usr/bin/env bash
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-northeast-2}"

cd "$(dirname "$0")/../../infra/v2/terraform"

terraform init -input=false

if terraform state list 2>/dev/null | grep -q '^aws_db_instance\.main$'; then
  DB_INSTANCE_ID=$(terraform output -raw rds_instance_id)
  echo "Starting RDS (${DB_INSTANCE_ID})..."
  if ! err=$(aws rds start-db-instance --region "$AWS_REGION" --db-instance-identifier "${DB_INSTANCE_ID}" 2>&1); then
    grep -q 'InvalidDBInstanceState' <<<"$err" || { echo "$err" >&2; exit 1; }
    echo "RDS가 이미 실행 중이거나 시작이 진행 중입니다."
  fi
  aws rds wait db-instance-available --region "$AWS_REGION" --db-instance-identifier "${DB_INSTANCE_ID}"
else
  echo "기존 RDS를 찾을 수 없습니다 (최초 실행) - terraform apply에서 새로 생성합니다."
fi

echo "Bringing up compute (ASG/ALB/Redis)..."
terraform apply -var-file=dev.tfvars -var="compute_enabled=true" -auto-approve

echo "ALB endpoint:"
terraform output -raw alb_dns_name
