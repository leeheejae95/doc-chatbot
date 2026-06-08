src/main/java/org/chatbot/doc/
├── config/
│   └── VectorStoreConfig.java       ← pgvector 설정
├── document/
│   ├── controller/
│   │   └── DocumentController.java  ← PDF 업로드 API
│   ├── service/
│   │   ├── DocumentService.java     ← 인터페이스
│   │   └── DocumentServiceImpl.java ← 구현체
├── chat/
│   ├── controller/
│   │   └── ChatController.java      ← 질문 API
│   ├── service/
│   │   ├── ChatService.java         ← 인터페이스
│   │   └── ChatServiceImpl.java     ← 구현체
└── global/
    ├── exception/
    │   ├── CustomException.java
    │   └── GlobalExceptionHandler.java
    └── response/
        └── ApiResponse.java         ← 공통 응답 포맷
