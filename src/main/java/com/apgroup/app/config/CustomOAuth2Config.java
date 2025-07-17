package com.apgroup.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Configuration
public class CustomOAuth2Config {

    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        return new OAuth2AuthorizationRequestResolver() {
            private final DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                    new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
                return customize(req);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
                return customize(req);
            }

            private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest request) {
                if (request == null) return null;
                return OAuth2AuthorizationRequest.from(request)
                        .authorizationRequestUri(request.getAuthorizationRequestUri() + "&prompt=login")
                        .build();
            }
        };
    }
}
