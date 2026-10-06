#!/usr/bin/env bash
# 사용법: IMAGE=ghcr.io/owner/repo TAG=<commit-sha> ./deploy.sh
set -euo pipefail
cd "$(dirname "$0")"

: "${IMAGE:?IMAGE is required}"
: "${TAG:?TAG is required}"

COMPOSE="docker compose -f docker-compose.prod.yml"
PREV_TAG="$(cat .current_tag 2>/dev/null || true)"

export APP_IMAGE="$IMAGE"
export APP_TAG="$TAG"

echo ">> Deploying $APP_IMAGE:$APP_TAG (previous: ${PREV_TAG:-none})"
$COMPOSE pull app
$COMPOSE up -d --remove-orphans

echo ">> Waiting for health check..."
for i in $(seq 1 40); do
  if curl -fs http://127.0.0.1:8080/actuator/health | grep -q '"UP"'; then
    echo "$APP_TAG" > .current_tag

    # Nginx 템플릿은 컨테이너 시작 시점에만 렌더링되므로, 바뀌었을 때만 재생성
    NGINX_HASH="$(cat nginx/templates/* | sha256sum | cut -d' ' -f1)"
    if [ "$NGINX_HASH" != "$(cat .nginx_hash 2>/dev/null || true)" ]; then
      echo ">> Nginx config changed, recreating nginx"
      $COMPOSE up -d --force-recreate nginx
      echo "$NGINX_HASH" > .nginx_hash
    fi

    echo ">> Deploy succeeded"
    docker image prune -f > /dev/null
    exit 0
  fi
  sleep 3
done

echo ">> Health check failed. Recent logs:"
$COMPOSE logs --tail=200 app || true

if [ -n "$PREV_TAG" ]; then
  echo ">> Rolling back to $PREV_TAG"
  APP_TAG="$PREV_TAG" $COMPOSE up -d app
fi
exit 1
