# Release Runner
VERSION="1.0.0"
GITHUB_TOKEN="YOUR_PAT_HERE"
REPO="OWNER/REPO"

curl -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $GITHUB_TOKEN" \
  https://api.github.com/repos/$REPO/actions/workflows/release.yml/dispatches \
  -d "{\"ref\":\"master\", \"inputs\": {\"version\": \"$VERSION\"}}"
