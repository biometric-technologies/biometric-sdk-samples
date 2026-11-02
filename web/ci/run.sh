#!/usr/bin/env bash
# CI entry point of the web sample (README, "Platform contract"). Needs the SDK source in sdk/ and
# the photos in samples/data/ (scripts/fetch-samples.sh). Builds the server against sdk/, starts it
# and runs the check through its API with curl: encode every photo, compare every pair of the same
# modality. Writes results/web.md and exits non-zero if anything was not as expected.
set -euo pipefail
cd "$(dirname "$0")/../.."
if [ ! -f sdk/settings.gradle.kts ]; then
  echo "No biometric-sdk checkout in sdk/: the check builds against the SDK source, not a release" >&2
  exit 1
fi
# the SDK's Gradle wrapper, so the server builds with the Gradle version the SDK is built with
sdk/gradlew -p web/server installDist

# Start the server and wait until it listens (setting up the SDK includes downloading the face model)
mkdir -p results
port=${PORT:-8080}
api=http://localhost:$port/api
if curl -s -o /dev/null "$api/"; then echo "Port $port is taken: choose another one with PORT=" >&2; exit 1; fi
web/server/build/install/web-server/bin/web-server "$port" > results/web-server.log 2>&1 &
server=$!
trap 'kill $server 2>/dev/null || true' EXIT
for ((waited = 0; waited < 600; waited++)); do
  curl -s -o /dev/null "$api/" && break
  kill -0 $server 2>/dev/null || break # the server has stopped
  sleep 1
done
curl -s -o /dev/null "$api/" || { cat results/web-server.log; echo "The server did not start" >&2; exit 1; }

reply=$(mktemp)
# POST to the API: prints the HTTP status, the answer (empty if there is none) is in $reply
post() { : > "$reply"; curl -sS -o "$reply" -w '%{http_code}' "$@" || true; }
# fetch-samples.sh saves every photo as samples/data/<id>.<extension of the original>
photo() { ls samples/data/"$1".* 2>/dev/null | head -1; }

rows=() failures=()
for modality in iris face; do
  ids=() persons=() templates=()
  while read -r id person; do
    [ -n "$(photo "$id")" ] || { echo "$id is not in samples/data: run scripts/fetch-samples.sh" >&2; exit 1; }
    ids+=("$id")
    persons+=("$person")
  done < <(jq -r --arg m "$modality" '.samples[] | select(.modality == $m) | "\(.id) \(.person)"' samples/manifest.json)

  # 1. Encode every photo: the file as it is in the request body, the template back in base64
  encoded=0
  for i in "${!ids[@]}"; do
    templates[i]=""
    status=$(post --data-binary "@$(photo "${ids[$i]}")" "$api/encode?modality=$modality")
    if [ "$status" = 200 ]; then
      templates[i]=$(jq -r .template "$reply")
      encoded=$((encoded + 1))
    else
      failures+=("${ids[$i]}: not encoded, HTTP $status $(cat "$reply")")
    fi
  done

  # 2. Compare every pair of encoded photos: the same person must match, different people must not
  compared=0 as_expected=0
  for ((i = 0; i < ${#ids[@]}; i++)); do
    for ((j = i + 1; j < ${#ids[@]}; j++)); do
      [ -n "${templates[$i]}" ] && [ -n "${templates[$j]}" ] || continue
      status=$(jq -n --arg a "${templates[$i]}" --arg b "${templates[$j]}" '{a: $a, b: $b}' |
        post --data-binary @- "$api/compare?modality=$modality")
      same=$([ "${persons[$i]}" = "${persons[$j]}" ] && echo true || echo false)
      line="${ids[$i]} / ${ids[$j]}, same person: $same, HTTP $status $(cat "$reply")"
      echo "$line"
      compared=$((compared + 1))
      if [ "$status" = 200 ] && [ "$(jq .match "$reply")" = "$same" ]; then
        as_expected=$((as_expected + 1))
      else
        failures+=("$line")
      fi
    done
  done
  rows+=("| $modality | $encoded / ${#ids[@]} | $as_expected / $compared |")
done

# The table every platform writes (README, "Platform contract"); CI posts it on the pull request.
{
  echo "### web"
  echo "| modality | encoded | comparisons as expected |"
  echo "|---|---|---|"
  printf '%s\n' "${rows[@]}"
  echo
  if [ ${#failures[@]} = 0 ]; then echo "Failures: none"; else echo "Failures:"; printf -- '- %s\n' "${failures[@]}"; fi
} | tee results/web.md
[ ${#failures[@]} = 0 ]
