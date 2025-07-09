package org.geoserver.security.keycloak;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.OAuthRequestAuthenticator;
import org.keycloak.adapters.RequestAuthenticator;
import org.keycloak.adapters.spi.AdapterSessionStore;
import org.keycloak.adapters.spi.HttpFacade;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GeoServerKeycloakOAuthRequestAuthenticator extends OAuthRequestAuthenticator {

    public GeoServerKeycloakOAuthRequestAuthenticator(RequestAuthenticator requestAuthenticator, HttpFacade facade, KeycloakDeployment deployment, int sslRedirectPort, AdapterSessionStore tokenStore) {
        super(requestAuthenticator, facade, deployment, sslRedirectPort, tokenStore);
    }

    @Override
    protected String getRedirectUri(String state) {
        String uri = super.getRedirectUri(state);

        return setAllRequiredScopes(uri);
    }

    private String setAllRequiredScopes(String uri) {
        try {
            URI parsedUri = new URI(uri);
            String query = parsedUri.getQuery();
            StringBuilder updatedQuery = new StringBuilder();

            Set<String> requiredScopes = new HashSet<>(Arrays.asList("openid", "offline_access", "profile", "email"));
            String scopeValue = "";

            if (query == null) {
                return uri;
            }

            if (!query.contains("scope=")) {
                scopeValue = String.join(" ", requiredScopes);
                updatedQuery.append("scope=").append(scopeValue).append("&");
            }

            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("scope=")) {
                    scopeValue = param.substring(6);
                    Set<String> existingScopes = new HashSet<>(Arrays.asList(scopeValue.split(" ")));
                    requiredScopes.addAll(existingScopes);
                    scopeValue = String.join(" ", requiredScopes);
                    updatedQuery.append("scope=").append(scopeValue).append("&");
                } else {
                    updatedQuery.append(param).append("&");
                }
            }

            // Remove trailing '&'
            if (updatedQuery.length() > 0) {
                updatedQuery.setLength(updatedQuery.length() - 1);
            }

            URI updatedUri = new URI(
                parsedUri.getScheme(),
                parsedUri.getAuthority(),
                parsedUri.getPath(),
                updatedQuery.toString(),
                parsedUri.getFragment()
            );

            return updatedUri.toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI syntax: " + uri, e);
        }
    }
}
