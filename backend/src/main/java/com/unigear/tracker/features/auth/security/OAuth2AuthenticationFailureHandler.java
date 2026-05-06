package com.unigear.tracker.features.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.authorizedRedirectUri:http://localhost:3000}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      AuthenticationException exception) throws IOException, ServletException {

        String targetRedirectUri = resolveRedirectUri(request);
        String targetUrl = UriComponentsBuilder.fromUriString(targetRedirectUri + "/oauth2/callback")
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        clearRedirectUriCookie(response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveRedirectUri(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("oauth2_redirect_uri".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }
        return redirectUri;
    }

    private void clearRedirectUriCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("oauth2_redirect_uri", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}