package com.api.tasklist.filter;

import com.api.tasklist.service.JwtService;
import com.api.tasklist.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ApplicationContext context;
    private final HandlerExceptionResolver resolver;

    @Autowired
    public JwtFilter(JwtService jwtService, ApplicationContext context, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver){
        this.jwtService = jwtService;
        this.context = context;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            String token = extractToken(request);
            if(token!=null && SecurityContextHolder.getContext().getAuthentication()==null){
                String username = jwtService.extractUsername(token);
                if(username!=null){
                    UserDetails userDetails = context.getBean(UserService.class).loadUserByUsername(username);
                    if(jwtService.isTokenValid(token, userDetails)){
                        logger.debug("Token is valid for user: {}", username);
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
            filterChain.doFilter(request, response);
        }catch (ExpiredJwtException e){
            logger.error("Token has expired: {}", e.getMessage());
            resolver.resolveException(request, response, null, e);
        } catch (JwtException e) {
            logger.warn("JWT validation failed: {}", e.getMessage());
            resolver.resolveException(request, response, null, e);
        } catch (Exception e) {
            logger.error("Unexpected error in JWT filter", e);
            filterChain.doFilter(request, response);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
