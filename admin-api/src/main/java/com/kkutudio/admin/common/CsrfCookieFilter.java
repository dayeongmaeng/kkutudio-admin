package com.kkutudio.admin.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * CookieCsrfTokenRepository는 토큰을 지연 로딩하므로, 아무도 CsrfToken을 읽지 않으면
 * (SPA에는 이를 읽는 서버 사이드 뷰가 없음) XSRF-TOKEN 쿠키가 응답에 실리지 않는다.
 * 매 요청마다 강제로 읽어 쿠키가 내려가도록 한다.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
