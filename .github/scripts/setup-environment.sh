#!/bin/bash
set -e

# Git 설정
git config --global user.name "GitHub Actions Bot"
git config --global user.email "actions@github.com"

# GitHub CLI 설치
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update
sudo apt install gh -y

echo "✅ 환경 설정 완료"
