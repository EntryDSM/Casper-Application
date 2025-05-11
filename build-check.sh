#!/bin/bash

cd "$(git rev-parse --show-toplevel)" || exit 1

echo "Gradle 빌드 검증 시작..."

./gradlew clean build --no-daemon
BUILD_RESULT=$?

if [ $BUILD_RESULT -ne 0 ]; then
    echo "❌ 빌드 실패! 커밋이 중단됩니다."
    exit 1
else
    echo "✅ 빌드 성공! 커밋 계속 진행합니다."
    exit 0
fi