package com.GesCom.security.jwt;

import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.security.user.UsuarioDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsService usuarioDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getServletPath().contains("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Sin token: {} {}", request.getMethod(), request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = authHeader.substring(7);

        try {
            final String userEmail = jwtUtil.extractUsername(jwtToken);

            if (userEmail == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            final UsuarioDetails usuarioDetails =
                    (UsuarioDetails) usuarioDetailsService.loadUserByUsername(userEmail);

            if (!jwtUtil.isTokenValid(jwtToken, usuarioDetails)) {
                log.warn("Token inválido/expirado para usuario {} en {} {}", userEmail, request.getMethod(), request.getServletPath());
                filterChain.doFilter(request, response);
                return;
            }

            final var authToken = new UsernamePasswordAuthenticationToken(
                    usuarioDetails,
                    null,
                    usuarioDetails.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            log.warn("Error al procesar token JWT: {} - {} {}", e.getClass().getSimpleName(), e.getMessage(), request.getServletPath());
        }

        filterChain.doFilter(request, response);
    }
}
