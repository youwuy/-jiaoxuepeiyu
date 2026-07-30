package com.qizhifu.jiaoxuepeiyu.auth;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final AuthService authService;

    public AuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = BearerTokenResolver.resolve(request.getHeader(AUTHORIZATION_HEADER));
        if (token.isPresent()) {
            try {
                AuthenticatedUserContext.set(request, authService.currentUser(token.get()));
            } catch (AuthenticationException ex) {
                AuthenticatedUserContext.setAuthenticationError(request, ex);
            }
        }
        filterChain.doFilter(request, response);
    }
}
