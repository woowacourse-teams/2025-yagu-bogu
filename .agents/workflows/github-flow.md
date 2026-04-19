---
description: GitHub 이슈 생성부터 브랜치 작업, PR 생성까지 E2E 자동화
---
# GitHub Feature Workflow

## Objective
요구사항을 바탕으로 GitHub 이슈 생성, 브랜치 작업, 코드 작성 후 PR 생성까지의 과정을 자동화합니다.

## Required Context & Tools
- GitHub MCP 활성화 확인
- 로컬 Git CLI 접근 권한
- 에디터 및 파일 시스템 접근 권한

## Workflow Steps
다음 순서를 엄격하게 준수하여 작업을 수행합니다:

1. **요구사항 확인**: 구현할 기능의 상세 요구사항을 요청하고 대기합니다.
2. **이슈 생성**: GitHub MCP를 사용하여 요구사항 기반의 새 Issue를 생성하고, 반환된 Issue 번호를 기록합니다.
3. **브랜치 생성**: 터미널을 사용하여 다음 규칙으로 브랜치를 생성하고 체크아웃합니다.
   - `git checkout -b feat/{Issue-Number}`
4. **코드 구현**: 에디터 뷰 및 파일 시스템에 접근하여 코드를 작성 및 수정합니다.
5. **커밋 및 푸시**: 터미널을 사용하여 변경 사항을 원격 저장소에 푸시합니다.
   - `git add .`
   - `git commit -m "feat: 핵심 요약 (#Issue-Number)"`
   - `git push -u origin HEAD`
6. **PR 생성**: GitHub MCP를 사용하여 Pull Request를 생성합니다. PR 본문에 `Resolves #{Issue-Number}`를 포함합니다.

