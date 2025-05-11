#!/bin/bash

commit_msg=$(cat "$1")

regex="^(feat|fix|chore|docs|style|refactor|test|perf|ci|build|revert) \([[:space:]]*#[[:space:]]*[0-9]+[[:space:]]*\) : .+$"

if [[ "$commit_msg" =~ $regex ]]; then
    echo "✅ 커밋 메시지 형식이 올바릅니다."
    exit 0
else
    echo "❌ 커밋 메시지 형식이 잘못되었습니다."
    exit 1
fi
