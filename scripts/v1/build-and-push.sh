#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

AWS_REGION="${AWS_REGION:-ap-northeast-2}"
IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD)}"

ECR_REPO_URL=$(cd infra/v1/terraform && terraform output -raw ecr_repository_url)
ECR_REGISTRY="${ECR_REPO_URL%%/*}"

echo "Logging in to ECR (${ECR_REGISTRY})..."
aws ecr get-login-password --region "${AWS_REGION}" | docker login --username AWS --password-stdin "${ECR_REGISTRY}"

echo "Building and pushing docker image (linux/amd64)..."
docker buildx build --platform linux/amd64 \
  -t "${ECR_REPO_URL}:${IMAGE_TAG}" -t "${ECR_REPO_URL}:latest" \
  --push .

echo "Pushed: ${ECR_REPO_URL}:${IMAGE_TAG}"
