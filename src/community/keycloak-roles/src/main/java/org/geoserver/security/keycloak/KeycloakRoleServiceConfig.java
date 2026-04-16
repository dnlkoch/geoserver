/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.keycloak;

import org.geoserver.security.config.BaseSecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;

import java.io.Serial;

/**
 * Configuration for the Keycloak role service.
 *
 * <p>Holds the connection parameters for the Keycloak server and controls which roles (realm-level or client-level) are
 * synchronized into GeoServer.
 */
public class KeycloakRoleServiceConfig extends BaseSecurityNamedServiceConfig implements SecurityRoleServiceConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The Keycloak server URL, e.g. {@code https://keycloak.example.com}. */
    private String serverUrl;

    /** The Keycloak realm that contains the roles. */
    private String realm;

    /**
     * Optional – the Keycloak client-id whose client-level roles should be synchronized. When blank, realm-level roles
     * are used instead.
     *
     * TODO Allow multiple clients comma separated?
     */
    private String clientId;

    /**
     * The client-id of the Keycloak service account used to authenticate against the Admin REST API. This client must
     * have Service Accounts enabled and the appropriate realm-management roles assigned.
     */
    private String serviceAccountClientId;

    /** The client secret of the service account used to authenticate against the Keycloak Admin REST API. */
    private String clientSecret;

    /** The name of the local role that maps to {@code ROLE_ADMINISTRATOR}. */
    private String adminRoleName;

    /** The name of the local role that maps to {@code ROLE_GROUP_ADMIN}. */
    private String groupAdminRoleName;

    public KeycloakRoleServiceConfig() {}

    public KeycloakRoleServiceConfig(KeycloakRoleServiceConfig other) {
        super(other);
        this.serverUrl = other.getServerUrl();
        this.realm = other.getRealm();
        this.clientId = other.getClientId();
        this.serviceAccountClientId = other.getServiceAccountClientId();
        this.clientSecret = other.getClientSecret();
        this.adminRoleName = other.getAdminRoleName();
        this.groupAdminRoleName = other.getGroupAdminRoleName();
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getServiceAccountClientId() {
        return serviceAccountClientId;
    }

    public void setServiceAccountClientId(String serviceAccountClientId) {
        this.serviceAccountClientId = serviceAccountClientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String getAdminRoleName() {
        return adminRoleName;
    }

    @Override
    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }

    @Override
    public String getGroupAdminRoleName() {
        return groupAdminRoleName;
    }

    @Override
    public void setGroupAdminRoleName(String groupAdminRoleName) {
        this.groupAdminRoleName = groupAdminRoleName;
    }
}
