package com.jcs.javacommunitysite.atproto.service;

import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.session.AtprotoJwtSession;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@Service
public class AtprotoSessionService {
    
    private static final String ATPROTO_SESSION_KEY = "atproto_session";
    private static final String ATPROTO_CLIENT_KEY = "atproto_client";
    
    public AtprotoClient createSession(String pdsHost, String handle, String password) 
            throws IOException, AtprotoUnauthorized {
        
        AtprotoJwtSession session = AtprotoJwtSession.fromCredentials(pdsHost, handle, password);
        AtprotoClient client = new AtprotoClient(session);
        
        // Store in HTTP session
        HttpSession httpSession = getCurrentHttpSession();
        httpSession.setAttribute(ATPROTO_SESSION_KEY, session);
        httpSession.setAttribute(ATPROTO_CLIENT_KEY, client);
        
        return client;
    }
    
    public Optional<AtprotoClient> getCurrentClient() {
        HttpSession httpSession = getCurrentHttpSession();
        if (httpSession == null) {
            return Optional.empty();
        }
        
        AtprotoClient client = (AtprotoClient) httpSession.getAttribute(ATPROTO_CLIENT_KEY);
        return Optional.ofNullable(client);
    }
    
    public Optional<AtprotoJwtSession> getCurrentSession() {
        HttpSession httpSession = getCurrentHttpSession();
        if (httpSession == null) {
            return Optional.empty();
        }
        
        AtprotoJwtSession session = (AtprotoJwtSession) httpSession.getAttribute(ATPROTO_SESSION_KEY);
        return Optional.ofNullable(session);
    }
    
    public void clearSession() {
        HttpSession httpSession = getCurrentHttpSession();
        if (httpSession != null) {
            httpSession.removeAttribute(ATPROTO_SESSION_KEY);
            httpSession.removeAttribute(ATPROTO_CLIENT_KEY);
        }
    }
    
    public boolean isAuthenticated() {
        return getCurrentClient().isPresent();
    }
    
    private HttpSession getCurrentHttpSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest().getSession(true);
    }
}