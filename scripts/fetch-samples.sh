#!/usr/bin/env bash
# Downloads the images listed in samples/manifest.json into samples/data/ (ignored by git).
#
#   source "gitlab": a file in the private Iriscan dataset mirror (iriscan/datasets on GitLab).
#     Needs GITLAB_USER and GITLAB_TOKEN (a deploy token with read_repository), or your own
#     GitLab login through git. These images (CASIA) may be used for research but not
#     redistributed, so they are never committed anywhere.
#   source "zip": a member of a public zip archive (the face set is CC BY 4.0); archives are
#     cached in samples/cache/.
set -euo pipefail
cd "$(dirname "$0")/.."
manifest=samples/manifest.json
out=samples/data
cache=samples/cache
mkdir -p "$out" "$cache"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

auth=""
if [ -n "${GITLAB_USER:-}" ] && [ -n "${GITLAB_TOKEN:-}" ]; then auth="${GITLAB_USER}:${GITLAB_TOKEN}@"; fi

field() { python3 -c "import json,sys; print('\n'.join(sorted({s[sys.argv[2]] for s in json.load(open(sys.argv[1]))['samples'] if s.get('source')==sys.argv[3]})))" "$manifest" "$1" "$2"; }

# GitLab mirror: one sparse, blob-less clone per repository with only the listed files
for repo in $(field repository gitlab); do
  dir="$work/${repo//\//_}"
  paths=$(python3 -c "import json,sys; print('\n'.join(s['path'] for s in json.load(open(sys.argv[1]))['samples'] if s.get('repository')==sys.argv[2]))" "$manifest" "$repo")
  echo "== gitlab $repo ($(echo "$paths" | wc -l) files)"
  git clone --quiet --depth 1 --filter=blob:none --no-checkout "https://${auth}gitlab.com/iriscan/datasets/${repo}.git" "$dir"
  git -C "$dir" sparse-checkout init --no-cone
  echo "$paths" | sed 's#^#/#' | git -C "$dir" sparse-checkout set --no-cone --stdin
  git -C "$dir" checkout --quiet
  python3 - "$manifest" "$repo" "$dir" "$out" <<'PY'
import json, os, shutil, sys
manifest, repo, src, out = sys.argv[1:]
for s in json.load(open(manifest))['samples']:
    if s.get('source') == 'gitlab' and s['repository'] == repo:
        shutil.copyfile(os.path.join(src, s['path']), os.path.join(out, s['id'] + os.path.splitext(s['path'])[1]))
PY
done

# public zip archives
for url in $(field url zip); do
  archive="$cache/$(basename "$url").zip"
  [ -s "$archive" ] || { echo "== download $url"; curl -fsSL -o "$archive" "$url"; }
done
python3 - "$manifest" "$cache" "$out" <<'PY'
import fnmatch, json, os, sys, zipfile
manifest, cache, out = sys.argv[1:]
for s in json.load(open(manifest))['samples']:
    if s.get('source') != 'zip':
        continue
    with zipfile.ZipFile(os.path.join(cache, os.path.basename(s['url']) + '.zip')) as z:
        names = sorted(n for n in z.namelist() if fnmatch.fnmatch(os.path.basename(n), s['member'] + '.*'))
        if not names:
            sys.exit(f"{s['id']}: no member {s['member']} in {s['url']}")
        with open(os.path.join(out, s['id'] + os.path.splitext(names[0])[1].lower()), 'wb') as f:
            f.write(z.read(names[0]))
PY
echo "$(ls "$out" | wc -l) images in $out"
