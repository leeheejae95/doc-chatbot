package org.chatbot.doc.auth.service;

import org.chatbot.doc.auth.dto.request.LoginRequest;
import org.chatbot.doc.auth.dto.request.SignupRequest;
import org.chatbot.doc.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}
