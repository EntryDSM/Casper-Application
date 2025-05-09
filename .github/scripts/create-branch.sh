#!/bin/bash
set -e

# 환경 변수에서 필요한 정보 가져오기
PR_NUMBER=$1
PR_TITLE=$2
PR_BRANCH=$3
TARGET_REPO=$4
TARGET_BRANCH=$5
GITHUB_REPOSITORY=$6

# 새 브랜치 이름 설정 (PR 번호와 제목 활용)
BRANCH_NAME="auto-pr-${PR_NUMBER}-${PR_TITLE// /-}"
# 특수 문자 제거 및 소문자로 변환
BRANCH_NAME=$(echo $BRANCH_NAME | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9-]/-/g')
echo "BRANCH_NAME=$BRANCH_NAME"

# 대상 리포지토리 클론
git clone https://${GITHUB_TOKEN}@github.com/${TARGET_REPO}.git target-repo
cd target-repo

# 브랜치 생성 및 푸시
git checkout -b $BRANCH_NAME $TARGET_BRANCH

# 브랜치에 변경 사항 추가
echo "# 원본 PR: #${PR_NUMBER} - ${PR_TITLE}" >> PR_INFO.md
echo "원본 브랜치: ${PR_BRANCH}" >> PR_INFO.md
echo "원본 PR 링크: https://github.com/${GITHUB_REPOSITORY}/pull/${PR_NUMBER}" >> PR_INFO.md
echo "생성 시간: $(date)" >> PR_INFO.md

git add PR_INFO.md
git commit -m "Auto-created branch from PR #${PR_NUMBER}"
git push origin $BRANCH_NAME

echo $BRANCH_NAME # 브랜치 이름 출력
