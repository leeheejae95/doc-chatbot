package org.chatbot.doc.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatbot.doc.auth.dto.request.LoginRequest;
import org.chatbot.doc.auth.dto.request.SignupRequest;
import org.chatbot.doc.auth.dto.response.AuthResponse;
import org.chatbot.doc.auth.service.AuthService;
import org.chatbot.doc.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입 / 로그인 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 이름으로 회원가입 후 JWT 토큰을 반환합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@RequestBody SignupRequest request) {
        log.info("[AuthController] 회원가입 요청 - email: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(authService.signup(request)));
    }

    @Operation(summary = "로그인", description = "이메일, 비밀번호로 로그인 후 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        log.info("[AuthController] 로그인 요청 - email: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }
}
