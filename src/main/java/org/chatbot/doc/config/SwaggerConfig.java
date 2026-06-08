package org.chatbot.doc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI 설정
 * 로컬 개발 및 API 테스트용
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI OpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DOC CHAT BOT")
                        .description("사내 기술문서 RAG 검색 챗봇 API")
                        .version("v1.0.0"));
    }
}
