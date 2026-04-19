# GitHub Feature Workflow Skill

## Objective
요구사항을 바탕으로 GitHub 이슈 생성, 브랜치 작업, 코드 작성 후 PR 생성까지의 E2E 과정을 자동화합니다.

## Required Tools
- GitHub MCP (Issue, Pull Request 관리)
- Bash (로컬 Git 명령어 실행)
- File Edit (코드 작성 및 수정)

## Step-by-Step Workflow
반드시 다음 순서를 엄격하게 준수하여 작업을 수행하십시오.

1. **요구사항 확인**: 사용자에게 구현할 기능의 상세 요구사항을 요청하고 대기합니다.
2. **이슈 생성**: GitHub MCP를 사용하여 수신한 요구사항 기반의 새 Issue를 생성하고, 반환된 Issue Number를 기록합니다.
3. **브랜치 생성**: Bash 도구를 사용하여 다음 규칙으로 브랜치를 생성하고 체크아웃합니다.
   - 명령어: `git checkout -b feat/{Issue-Number}`
4. **코드 구현**: 로컬 파일 시스템에 접근하여 요구사항에 맞게 코드를 작성하고 수정합니다.
5. **커밋 및 푸시**: Bash 도구를 사용하여 변경 사항을 원격 저장소에 푸시합니다.
   - 명령어: `git add .`
   - 명령어: `git commit -m "feat: 핵심 요약 (#Issue-Number)"`
   - 명령어: `git push -u origin HEAD`
6. **PR 생성**: GitHub MCP를 사용하여 Pull Request를 엽니다. PR 본문에 `Resolves #{Issue-Number}`를 포함시켜 이슈와 연결합니다.

