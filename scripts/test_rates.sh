#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ENDPOINT="${ENDPOINT:-/api/v1/logistic/rates}"

# ---- helpers ----
urlencode() {
  # minimal urlencode for query values (works fine for numbers/codes)
  local s="$1"
  s="${s// /%20}"
  echo -n "$s"
}

build_url() {
  local originPostcode="$1"
  local originCountryCode="$2"
  local destinationPostcode="$3"
  local destinationCountryCode="$4"
  local heightCm="$5"
  local lengthCm="$6"
  local widthCm="$7"
  local weightKg="$8"

  echo -n "${BASE_URL}${ENDPOINT}?"
  echo -n "originPostcode=$(urlencode "$originPostcode")"
  echo -n "&originCountryCode=$(urlencode "$originCountryCode")"
  echo -n "&destinationPostcode=$(urlencode "$destinationPostcode")"
  echo -n "&destinationCountryCode=$(urlencode "$destinationCountryCode")"
  echo -n "&heightCm=$(urlencode "$heightCm")"
  echo -n "&lengthCm=$(urlencode "$lengthCm")"
  echo -n "&widthCm=$(urlencode "$widthCm")"
  echo -n "&weightKg=$(urlencode "$weightKg")"
}

hit() {
  local name="$1"
  shift
  local url
  url="$(build_url "$@")"

  echo "============================================================"
  echo "CASE: $name"
  echo "GET  : $url"
  echo "------------------------------------------------------------"

  # Capture response body + status + total time
  # -sS : silent but show errors
  # -w  : write out status/time after body
  local out http_code total_time
  out="$(curl -sS -H "Accept: application/json" -w "\nHTTP_CODE:%{http_code}\nTOTAL_TIME:%{time_total}\n" "$url")"

  http_code="$(echo "$out" | sed -n 's/^HTTP_CODE://p')"
  total_time="$(echo "$out" | sed -n 's/^TOTAL_TIME://p')"

  echo "Status: $http_code"
  echo "Time  : ${total_time}s"
  echo "Body  :"
  # print everything except the last 2 lines (HTTP_CODE/TOTAL_TIME)
  echo "$out" | sed '$d' | sed '$d'
  echo
}

# ---- test matrix ----

# Domestic (MY -> MY), varying sizes / weights
hit "domestic small (5x5x5 cm, 0.5 kg)"     13200 MY 20596 MY 5 5 5 0.5
hit "domestic small (5x5x5 cm, 0.5 kg)"     13200 MY 20596 MY 5 5 5 0.5
hit "Domestic medium (20x15x10 cm, 2 kg)"   13200 MY 20596 MY 20 15 10 2
hit "Domestic medium (20x15x10 cm, 2 kg)"   13200 MY 20596 MY 20 15 10 2
hit "Domestic bulky (60x40x30 cm, 10 kg)"   13200 MY 20596 MY 60 40 30 10
hit "Domestic bulky (60x40x30 cm, 10 kg)"   13200 MY 20596 MY 60 40 30 10

# Domestic edge-ish cases (very light / very heavy)
hit "Domestic very light (10x10x5 cm, 0.1 kg)" 13200 MY 20596 MY 10 10 5 0.1
hit "Domestic very light (10x10x5 cm, 0.1 kg)" 13200 MY 20596 MY 10 10 5 0.1
hit "Domestic heavy (40x40x40 cm, 25 kg)"       13200 MY 20596 MY 40 40 40 25
hit "Domestic heavy (40x40x40 cm, 25 kg)"       13200 MY 20596 MY 40 40 40 25

# International examples (MY -> SG, MY -> US, MY -> AU)
# Note: destinationPostcode formats differ by country, but your API expects a single string.
hit "International MY -> SG (small, 1 kg)"  13200 MY 018989 SG 10 10 10 1
hit "International MY -> SG (small, 1 kg)"  13200 MY 018989 SG 10 10 10 1
hit "International MY -> SG (small, 1 kg)"  13200 MY 018989 SG 10 10 10 1
hit "International MY -> SG (small, 1 kg)"  13200 MY 018989 SG 10 10 10 1
hit "International MY -> BN (medium, 3 kg)" 13200 MY 10001  BN 30 20 15 3
hit "International MY -> BN (medium, 3 kg)" 13200 MY 10001  BN 30 20 15 3
hit "International MY -> BN (medium, 3 kg)" 13200 MY 10001  BN 30 20 15 3
hit "International MY -> BN (medium, 3 kg)" 13200 MY 10001  BN 30 20 15 3

hit "Missing required param (should 400)"  50000 MY "" MY 10 10 10 1    # empty postcode
hit "Negative weight (should fail validation)" 50000 MY 43000 MY 10 10 10 -1

echo "All test requests completed."

