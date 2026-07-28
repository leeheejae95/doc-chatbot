package org.chatbot.doc.auth;

import org.chatbot.doc.auth.dto.request.LoginRequest;
import org.chatbot.doc.auth.dto.request.SignupRequest;
import org.chatbot.doc.auth.dto.response.AuthResponse;
import org.chatbot.doc.auth.entity.UserEntity;
import org.chatbot.doc.auth.repository.UserRepository;
import org.chatbot.doc.auth.security.JwtUtil;
import org.chatbot.doc.auth.service.impl.AuthServiceImpl;
import org.chatbot.doc.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        SignupRequest request = new SignupRequest("test@test.com", "password123", "테스터");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(UserEntity.class))).willAnswer(i -> i.getArgument(0));
        given(jwtUtil.generateToken(request.getEmail())).willReturn("mocked.jwt.token");

        AuthResponse response = authService.signup(request);

        assertThat(response.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getName()).isEqualTo("테스터");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_duplicateEmail_throwException() {
        SignupRequest request = new SignupRequest("duplicate@test.com", "password123", "테스터");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 사용중인 이메일입니다.");
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        LoginRequest request = new LoginRequest("test@test.com", "password123");

        UserEntity user = UserEntity.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .name("테스터")
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.generateToken(user.getEmail())).willReturn("mocked.jwt.token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_userNotFound_throwException() {
        LoginRequest request = new LoginRequest("notfound@test.com", "password123");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_wrongPassword_throwException() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongPassword");

        UserEntity user = UserEntity.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .name("테스터")
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
