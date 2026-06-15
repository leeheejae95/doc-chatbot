# 📚 Doc Chatbot - 사내 기술문서 RAG 검색 챗봇

> Spring AI + Ollama + pgvector 기반의 사내 기술문서 검색 챗봇
> 문서를 업로드하면 자연어로 질문하고 답변을 받을 수 있습니다.

<br>

## 🚀 주요 기능

- **다중 문서 업로드** - PDF, Word, Excel, PPT, TXT, HWP 지원
- **벡터 임베딩 저장** - 문서를 청크 단위로 분할 후 pgvector에 저장
- **자연어 검색** - 질문과 유사한 문서 청크를 코사인 유사도로 검색
- **AI 답변 생성** - 검색된 컨텍스트 기반으로 llama3.2가 답변 생성
- **대화 히스토리** - conversationId 기반 멀티턴 대화 지원
- **스트리밍 응답** - SSE 기반 실시간 타이핑 효과 응답
- **문서 관리** - 업로드 문서 목록 조회 및 삭제 (청크 동시 삭제)

<br>

## 🛠 기술 스택

| 분류 | 기술 | 선택 이유 |
|------|------|----------|
| Language | Java 21 | |
| Framework | Spring Boot 4.0.6 | |
| ORM | Spring Data JPA | |
| AI Framework | Spring AI 2.0.0-M4 | RAG 파이프라인 통합 지원 |
| LLM | Ollama (llama3.2) | 로컬 실행 개발 가능 |
| Embedding | Ollama (nomic-embed-text) | 한국어 지원 |
| Vector DB | pgvector (PostgreSQL 17) | Spring AI 공식 지원, 추가 인프라 불필요 |
| Streaming | Spring WebFlux | SSE 기반 스트리밍 응답 |
| Build | Gradle | |
| Container | Docker | |

<br>

## 📊 성능 측정 결과

| 항목 | 결과 |
|------|------|
| Chunk Size | 256토큰 (한글 기준 100~150자) |
| 임베딩 차원 | 768차원 (nomic-embed-text) |
| TopK | 5 (3/5/10 테스트 후 최적값 선정) |
| Similarity Threshold | 0.3 (정확도와 재현율 균형점) |
| 인덱스 | HNSW (대용량 고속 검색) |
| Distance | Cosine Distance |

### Chunk Size 256 선택 이유
- **512토큰**: 청크가 너무 커서 관련 없는 내용 포함 가능성 높음 → 검색 정확도 저하
- **128토큰**: 청크가 너무 작아서 문맥 단절 발생 → 답변 품질 저하
- **256토큰**: 하나의 개념을 담기 적절한 크기, 검색 정확도 최적

### TopK 실험 결과
- **TopK 3**: 관련 문서 청크 누락 발생 → 답변 불완전
- **TopK 10**: 불필요한 문맥 증가 → LLM 혼란으로 답변 품질 저하
- **TopK 5**: 정확도와 문맥 양의 균형 최적 → 채택

### Similarity Threshold 0.3 선택 이유
- **0.5 이상**: 검색 결과 부족으로 "관련 내용을 찾을 수 없습니다" 빈번 발생
- **0.2 이하**: 관련 없는 문서 청크 포함으로 LLM 오답 증가
- **0.3**: 정확도(Precision)와 재현율(Recall) 균형점 → 채택

### pgvector 선택 이유
- Spring AI 공식 지원으로 별도 클라이언트 구현 불필요
- 기존 PostgreSQL에 확장 추가만으로 벡터 검색 가능 (추가 인프라 비용 없음)
- HNSW 인덱스로 대용량 데이터 고속 검색 지원

### Cosine Distance 선택 이유
- 텍스트 유사도는 벡터의 크기보다 방향이 중요
- 문서 길이에 관계없이 의미적 유사도 측정 가능
- Euclidean Distance 대비 문서 길이 편향 없음

<br>

## 🏗 시스템 아키텍처

```
[클라이언트]
    │
    ▼
[REST API - Spring Boot]
    │
    ├── 문서 업로드
    │       │
    │   PDFBox / Apache Tika 파싱
    │       │
    │   256토큰 청크 분할 (TokenTextSplitter)
    │       │
    │   nomic-embed-text 임베딩 (768차원)
    │       │
    │   [pgvector DB - HNSW 인덱스 저장]
    │   [documents 테이블 - 문서 메타데이터 저장]
    │
    └── 질문 입력
            │
        질문 → 768차원 벡터 변환
            │
        Cosine 유사도 검색 (TopK=5, Threshold=0.3)
            │
        관련 문서 청크 추출
            │
        Prompt Augmentation (질문 + 청크 컨텍스트 조합)
            │
        [Ollama llama3.2 - 답변 생성]
            │
        SSE 스트리밍 반환 (WebFlux Flux<String>)
```

<br>

## 📁 프로젝트 구조

```
src/main/java/org/chatbot/doc/
├── config/                         # 전역 설정 (ChatClient, Swagger)
├── document/
│   ├── entity/                     # 문서 엔티티 (JPA)
│   ├── repository/                 # 문서 JpaRepository
│   ├── controller/                 # 문서 관리 API
│   └── service/                    # 문서 파싱 & 임베딩 저장
├── chat/
│   ├── config/                     # chat config 설정
│   ├── dto/                        # 요청/응답 DTO
│   ├── controller/                 # 질문 API (동기/스트리밍)
│   └── service/                    # RAG 검색 & 답변 생성
└── global/
    ├── exception/                  # ErrorCode Enum, CustomException
    ├── filter/                     # MDC 로깅 필터
    ├── handler/                    # 전역 예외 처리 핸들러
    └── response/                   # 공통 응답 포맷 (ApiResponse)
```

<br>

## ⚙️ 로컬 실행 방법

### 사전 요구사항
- Java 21
- IntelliJ IDEA
- Docker Desktop
- Ollama

### 1. Ollama 모델 설치
```
ollama pull llama3.2
ollama pull nomic-embed-text
```

### 2. OLLAMA_MODELS 환경변수 설정 (한글 경로 문제 방지)
```
시스템 환경변수 추가
OLLAMA_MODELS = C:\ollama\models
```

### 3. pgvector 실행
```
docker-compose up -d
```

### 4. 애플리케이션 실행
```

```

### 5. Swagger UI 접속
```
http://localhost:8080/swagger-ui.html
```

<br>

## 📌 개발 배경

SI/공공기관 개발 현장에서 표준 문서, API 명세서를 매번 수동으로 찾아야하는 비효율을 직접 경험했습니다.
*"EAI/FEP 헤더 또는 전문 포맷이 뭐였지?"*, *"UI공통 설정은 어떻게 정의했는지?"*, *"SQL작성시 표준 규격이 어떻게 되는지?"* 같은 질문에 즉시 답할 수 있는 도구가 필요했고, 이를 RAG 기반으로 직접 구현했습니다.

<br>

## 📈 핵심 설계 포인트

- **인터페이스 기반 설계** - ChatService, DocumentService 인터페이스 추상화로 LLM 교체 용이
  - Ollama → Claude API → OpenAI 전환 시 구현체(Impl)만 교체, Controller/비즈니스 로직 수정 불필요
- **ErrorCode Enum** - 도메인별 에러코드 체계화로 프론트 분기처리 지원
- **전역 예외 처리** - GlobalExceptionHandler로 모든 예외를 일관된 ApiResponse 포맷으로 응답
- **MDC 로깅** - 요청별 고유 requestId 부여로 동시 다중 요청 추적 가능
- **@Transactional 원자성 보장** - 문서 업로드 시 documents 테이블과 vector_store 동시 성공/실패 처리
- **대화 히스토리** - MessageWindowChatMemory로 최근 20개 메시지 유지, conversationId 없으면 자동 UUID 생성
- **WebFlux 한정 사용** - 전체 리액티브가 아닌 SSE 스트리밍 응답(/chat/stream)에만 Flux<String> 적용

<br>

## 🔌 API 명세

| Method | URL | 설명 |
|--------|-----|------|
| POST | /api/document/upload | 문서 업로드 (중복 방지) |
| GET | /api/document | 문서 목록 조회 (최신순) |
| DELETE | /api/document/{id} | 문서 삭제 (vector_store 청크 동시 삭제) |
| POST | /api/chat/ask | 질문 답변 (동기, JSON 응답) |
| POST | /api/chat/stream | 질문 답변 (비동기, SSE 스트리밍) |

<br>

## 🔮 개선 과제

- **Claude API 교체** - 답변 품질 향상 및 한국어 이해도 개선
- **RetrievalAugmentationAdvisor 적용** - Spring AI 공식 RAG 파이프라인으로 QueryTransformer, DocumentRanker 추가
- **JdbcChatMemoryRepository** - 대화 히스토리 DB 영구 저장
- **GitHub Actions CI/CD** - 자동 빌드 및 테스트
- **실제 성능 수치 측정** - 100페이지 이상 문서 업로드 시간, HNSW 적용 전후 검색 응답시간 측정
