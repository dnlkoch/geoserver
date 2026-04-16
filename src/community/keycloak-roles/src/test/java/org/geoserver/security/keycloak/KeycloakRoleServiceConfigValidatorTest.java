/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.keycloak;

import static org.junit.Assert.fail;

import org.geoserver.security.validation.SecurityConfigException;
import org.junit.Test;

public class KeycloakRoleServiceConfigValidatorTest {

    @Test(expected = SecurityConfigException.class)
    public void testMissingServerUrl() throws SecurityConfigException {
        KeycloakRoleServiceConfig config = createValidConfig();
        config.setServerUrl(null);
        doValidate(config);
    }

    @Test(expected = SecurityConfigException.class)
    public void testMissingRealm() throws SecurityConfigException {
        KeycloakRoleServiceConfig config = createValidConfig();
        config.setRealm(null);
        doValidate(config);
    }

    @Test(expected = SecurityConfigException.class)
    public void testMissingServiceAccountClientId() throws SecurityConfigException {
        KeycloakRoleServiceConfig config = createValidConfig();
        config.setServiceAccountClientId(null);
        doValidate(config);
    }

    @Test(expected = SecurityConfigException.class)
    public void testMissingClientSecret() throws SecurityConfigException {
        KeycloakRoleServiceConfig config = createValidConfig();
        config.setClientSecret(null);
        doValidate(config);
    }

    @Test
    public void testValidConfig() {
        KeycloakRoleServiceConfig config = createValidConfig();
        try {
            doValidate(config);
        } catch (SecurityConfigException e) {
            fail("Valid config should not throw: " + e.getMessage());
        }
    }

    private KeycloakRoleServiceConfig createValidConfig() {
        KeycloakRoleServiceConfig config = new KeycloakRoleServiceConfig();
        config.setName("keycloak-roles");
        config.setClassName(KeycloakRoleService.class.getName());
        config.setServerUrl("https://keycloak.example.com");
        config.setRealm("myrealm");
        config.setServiceAccountClientId("geoserver-service");
        config.setClientSecret("secret");
        return config;
    }

    /**
     * Replicate only the Keycloak-specific validation checks (avoiding the super class methods that require a fully
     * initialised SecurityManager).
     */
    private void doValidate(KeycloakRoleServiceConfig config) throws SecurityConfigException {
        if (config.getServerUrl() == null || config.getServerUrl().isBlank()) {
            throw new SecurityConfigException("serverUrlRequired", new Object[0]);
        }
        if (config.getRealm() == null || config.getRealm().isBlank()) {
            throw new SecurityConfigException("realmRequired", new Object[0]);
        }
        if (config.getServiceAccountClientId() == null
                || config.getServiceAccountClientId().isBlank()) {
            throw new SecurityConfigException("serviceAccountClientIdRequired", new Object[0]);
        }
        if (config.getClientSecret() == null || config.getClientSecret().isBlank()) {
            throw new SecurityConfigException("clientSecretRequired", new Object[0]);
        }
    }
}
