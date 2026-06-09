package org.chatbot.doc.global.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * MDC 로깅 필터
 * 모든 요청에 고유한 requestId를 부여하여 요청 흐름 추적
 */
@Slf4j
@Component
public class MdcLoggingFilter implements Filter {

    private static final String REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        String requestId = UUID.randomUUID().toString().substring(0,8);
        try{
            MDC.put(REQUEST_ID, requestId);

            log.info("[{}] {} {}", requestId, httpServletRequest.getMethod(), httpServletRequest.getRequestURI());

            chain.doFilter(request, response);
        } finally {
            // 요청 끝나면 반드시 MDC 초기화 (메모리 누수 방지)
            MDC.clear();
        }
    }
}
