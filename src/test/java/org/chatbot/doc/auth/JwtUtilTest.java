package org.chatbot.doc.auth;

import org.chatbot.doc.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil 단위 테스트")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET = "doc-chatbot-jwt-secret-key-must-be-at-least-256bits-long-for-hs256";
    private static final long EXPIRATION = 3600000L;
    private static final String TEST_EMAIL = "test@test.com";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION);
    }

    @Test
    @DisplayName("토큰 생성 성공")
    void generateToken_success() {
        String token = jwtUtil.generateToken(TEST_EMAIL);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 성공")
    void extractEmail_success() {
        String token = jwtUtil.generateToken(TEST_EMAIL);

        String extracted = jwtUtil.extractEmail(token);

        assertThat(extracted).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("유효한 토큰 검증 - true 반환")
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken(TEST_EMAIL);

        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("변조된 토큰 검증 - false 반환")
    void validateToken_tamperedToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("invalid.token.value")).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 - false 반환")
    void validateToken_expiredToken_returnsFalse() {
        JwtUtil expiredJwtUtil = new JwtUtil(SECRET, -1L);
        String expiredToken = expiredJwtUtil.generateToken(TEST_EMAIL);

        assertThat(jwtUtil.validateToken(expiredToken)).isFalse();
    }
}
