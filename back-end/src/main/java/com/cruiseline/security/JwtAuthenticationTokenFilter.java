package com.cruiseline.security;


import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cruiseline.model.User;
import com.cruiseline.service.UserService;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    static final String AUTHORIZATION = "Authorization";
    static final String UTF_8 = "UTF-8";
    static final int BEGIN_INDEX = 7;
    private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    UserService userService;
    @Autowired
    UsernamePasswordAuthenticationTokenFactory usernamePasswordAuthenticationTokenFactory;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authToken = request.getHeader(AUTHORIZATION);
        if(authToken != null) {
            try {
                authToken = new String(authToken.substring(BEGIN_INDEX).getBytes(), UTF_8);
                SecurityContext context = SecurityContextHolder.getContext();
                if(context.getAuthentication() == null) {
                    logger.info("Checking authentication for token " + authToken);
                    User u = userService.validateUser(authToken, request.getRemoteAddr());
                    if(u != null) {
                        logger.info("User " + u.getUsername() + " found.");
                        Authentication authentication = usernamePasswordAuthenticationTokenFactory.create(u);
                        context.setAuthentication(authentication);
                    }
                }
            } catch (StringIndexOutOfBoundsException e) {
                logger.error(e.getMessage());
            }

        }
        chain.doFilter(request, response);
    }

}
