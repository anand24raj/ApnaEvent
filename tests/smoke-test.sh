#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "Testing $BASE_URL"
curl -fsS "$BASE_URL/actuator/health" >/dev/null
echo "PASS: gateway health"

curl -fsS "$BASE_URL/customer/view-all" >/dev/null
echo "PASS: customer API"

curl -fsS "$BASE_URL/venue/view-all" >/dev/null
echo "PASS: venue API"

curl -fsS "$BASE_URL/event/view-all" >/dev/null
echo "PASS: event API"

echo "Smoke tests passed."
