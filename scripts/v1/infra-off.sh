#!/usr/bin/env bash
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-northeast-2}"

cd "$(dirname "$0")/../../infra/v1/terraform"

terraform init -input=false

echo "Tearing down compute (ASG/ALB/Redis)..."
terraform apply -var-file=dev.tfvars -var="compute_enabled=false" -auto-approve

RDS_IN_STATE=""
for i in 1 2 3; do
  if terraform state list 2>/dev/null | grep -q '^aws_db_instance\.main$'; then
    RDS_IN_STATE=1
    break
  fi
  sleep 2
done

if [ -n "$RDS_IN_STATE" ]; then
  DB_INSTANCE_ID=$(terraform output -raw rds_instance_id)
  echo "Stopping RDS (${DB_INSTANCE_ID})..."
  if ! err=$(aws rds stop-db-instance --region "$AWS_REGION" --db-instance-identifier "${DB_INSTANCE_ID}" 2>&1); then
    grep -q 'InvalidDBInstanceState' <<<"$err" || { echo "$err" >&2; exit 1; }
    echo "RDS가 이미 정지되어 있거나 정지가 진행 중입니다."
  fi
else
  echo "RDS 인스턴스가 없습니다 (건너뜀)."
fi

echo "Done. Compute + Redis destroyed, RDS stopped."
