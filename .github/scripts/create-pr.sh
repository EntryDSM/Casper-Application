#!/bin/bash
set -e

# 환경 변수에서 필요한 정보 가져오기
PR_NUMBER=$1
PR_TITLE=$2
PR_BODY=$3
BRANCH_NAME=$4
TARGET_REPO=$5
TARGET_BRANCH=$6
GITHUB_REPOSITORY=$7

# GitHub CLI 인증
echo "${GITHUB_TOKEN}" | gh auth login --with-token

# PR 제목과 본문 준비
NEW_PR_TITLE="[Auto] ${PR_TITLE}"
NEW_PR_BODY="자동으로 생성된 PR입니다.

원본 PR: https://github.com/${GITHUB_REPOSITORY}/pull/${PR_NUMBER}

${PR_BODY}"

# PR 생성
gh pr create \
  --title "$NEW_PR_TITLE" \
  --body "$NEW_PR_BODY" \
  --base "$TARGET_BRANCH" \
  --head "$BRANCH_NAME" \
  --repo "$TARGET_REPO"

echo "✅ PR 생성 완료: ${NEW_PR_TITLE}"
