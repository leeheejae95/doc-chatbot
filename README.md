# 📚 Doc Chatbot - 사내 기술문서 RAG 검색 챗봇

> Spring AI + Ollama + pgvector 기반의 사내 기술문서 검색 챗봇
> 문서를 업로드하면 자연어로 질문하고 답변을 받을 수 있습니다.

<br>

## 🚀 주요 기능

- **문서 업로드** - 기술 문서, 연동 스펙, API 명세서 등 업로드
- **벡터 임베딩 저장** - 문서를 청크 단위로 분할 후 pgvector에 저장
- **자연어 검색** - 질문과 유사한 문서 청크를 코사인 유사도로 검색
- **AI 답변 생성** - 검색된 컨텍스트 기반으로 llama3.2가 답변 생성

<br>

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| AI Framework | Spring AI 2.0.0-M4 |
| LLM | Ollama (llama3.2) |
| Embedding | Ollama (nomic-embed-text) |
| Vector DB | pgvector (PostgreSQL 17) |
| Build | Gradle |
| Container | Docker |

<br>

## 🏗 시스템 아키텍처

```
[클라이언트]
    │
    ▼
[REST API - Spring Boot]
    │
    ├── 문서 업로드 → Apache 파싱
    │                    │
    │               청크 분할 & 임베딩
    │                    │
    │              [pgvector DB 저장]
    │
    └── 질문 입력 → 벡터 유사도 검색 (HNSW / Cosine)
                         │
                   관련 문서 청크 추출
                         │
                  [Ollama llama3.2]
                         │
                     답변 생성 & 반환
```

<br>

## 📁 프로젝트 구조

```
src/main/java/org/chatbot/doc/
├── config/                         # 설정
├── document/
│   ├── controller/                 # 믄서 업로드 API
│   └── service/                    # 문서 파싱 & 임베딩 저장
├── chat/
│   ├── controller/                 # 질문 API
│   └── service/                    # RAG 검색 & 답변 생성
└── global/
    ├── exception/                  # 공통 예외 처리
    └── response/                   # 공통 응답 포맷
```

<br>

## ⚙️ 로컬 실행 방법

### 사전 요구사항
- Java 21
- Docker Desktop
- Ollama

### 1. Ollama 모델 설치
```bash
ollama pull llama3.2
ollama pull nomic-embed-text
```

### 2. pgvector 실행
```bash
docker-compose up -d
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

<br>

## 📌 개발 배경

SI/공공기관 개발 현장에서 표준 문서, API 명세서를 매번 수동으로 검색하는 비효율을 직접 경험했습니다.
*"EAI/FEP 헤더 또는 전문 포맷이 뭐였지?"*, *"UI공통 설정은 어떻게 정의했는지?"*, *"SQL작성시 표준 규격이 어떻게 되는지?"* 같은 질문에 즉시 답할 수 있는 도구가 필요했고, 이를 RAG 기반으로 직접 구현했습니다.

<br>

## 📈 핵심 설계 포인트

- **인터페이스 기반 설계** - Service 레이어 추상화로 LLM 교체 용이 (Ollama → Claude API)
- **전역 예외 처리** - GlobalExceptionHandler로 일관된 에러 응답
- **설정 외부화** - application.yml 기반으로 환경별 설정 분리
- **Auto Configuration 활용** - Spring AI 자동 설정 활용
