#!/usr/bin/env bash
# Downloads the images listed in samples/manifest.json into samples/data/.
#
# The images come from the private Iriscan dataset mirror on GitLab and are not stored
# in this repository: their licences (CASIA, ...) allow research use but not
# redistribution. Credentials: GITLAB_USER and GITLAB_TOKEN (a deploy token with
# read_repository on the iriscan/datasets group), or a git credential helper.
set -euo pipefail
cd "$(dirname "$0")/.."
manifest=samples/manifest.json
out=samples/data
mkdir -p "$out"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

auth=""
if [ -n "${GITLAB_USER:-}" ] && [ -n "${GITLAB_TOKEN:-}" ]; then auth="${GITLAB_USER}:${GITLAB_TOKEN}@"; fi

for repo in $(python3 -c "import json; print(' '.join(sorted({s['repository'] for s in json.load(open('$manifest'))['samples']})))"); do
  dir="$work/${repo//\//_}"
  paths=$(python3 -c "import json; print('\n'.join(s['path'] for s in json.load(open('$manifest'))['samples'] if s['repository']=='$repo'))")
  echo "== $repo ($(echo "$paths" | wc -l) files)"
  git clone --quiet --depth 1 --filter=blob:none --no-checkout "https://${auth}gitlab.com/iriscan/datasets/${repo}.git" "$dir"
  git -C "$dir" sparse-checkout init --no-cone
  echo "$paths" | sed 's#^#/#' | git -C "$dir" sparse-checkout set --no-cone --stdin
  git -C "$dir" checkout --quiet
  python3 - "$manifest" "$repo" "$dir" "$out" <<'PY'
import json, shutil, sys, os
manifest, repo, src, out = sys.argv[1:]
for s in json.load(open(manifest))['samples']:
    if s['repository'] != repo:
        continue
    ext = os.path.splitext(s['path'])[1]
    shutil.copyfile(os.path.join(src, s['path']), os.path.join(out, s['id'] + ext))
PY
done
echo "$(ls "$out" | wc -l) images in $out"
