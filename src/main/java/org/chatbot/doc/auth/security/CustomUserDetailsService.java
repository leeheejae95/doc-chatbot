package org.chatbot.doc.auth.security;

import lombok.RequiredArgsConstructor;
import org.chatbot.doc.auth.repository.UserRepository;
import org.chatbot.doc.global.exception.CustomException;
import org.chatbot.doc.global.exception.ErrorCode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));
    }
}
