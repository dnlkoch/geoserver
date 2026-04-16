/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.keycloak;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class KeycloakRoleServiceConfigTest {

    @Test
    public void testDefaults() {
        KeycloakRoleServiceConfig config = new KeycloakRoleServiceConfig();
        assertNull(config.getServerUrl());
        assertNull(config.getRealm());
        assertNull(config.getClientId());
        assertNull(config.getServiceAccountClientId());
        assertNull(config.getClientSecret());
        assertNull(config.getAdminRoleName());
        assertNull(config.getGroupAdminRoleName());
    }

    @Test
    public void testSettersAndGetters() {
        KeycloakRoleServiceConfig config = new KeycloakRoleServiceConfig();
        config.setServerUrl("https://keycloak.example.com");
        config.setRealm("myrealm");
        config.setClientId("myclient");
        config.setServiceAccountClientId("geoserver-service");
        config.setClientSecret("secret");
        config.setAdminRoleName("ADMIN");
        config.setGroupAdminRoleName("GROUP_ADMIN");

        assertEquals("https://keycloak.example.com", config.getServerUrl());
        assertEquals("myrealm", config.getRealm());
        assertEquals("myclient", config.getClientId());
        assertEquals("geoserver-service", config.getServiceAccountClientId());
        assertEquals("secret", config.getClientSecret());
        assertEquals("ADMIN", config.getAdminRoleName());
        assertEquals("GROUP_ADMIN", config.getGroupAdminRoleName());
    }

    @Test
    public void testCopyConstructor() {
        KeycloakRoleServiceConfig original = new KeycloakRoleServiceConfig();
        original.setName("test-service");
        original.setServerUrl("https://keycloak.example.com");
        original.setRealm("myrealm");
        original.setClientId("myclient");
        original.setServiceAccountClientId("geoserver-service");
        original.setClientSecret("secret");
        original.setAdminRoleName("ADMIN");
        original.setGroupAdminRoleName("GROUP_ADMIN");

        KeycloakRoleServiceConfig copy = new KeycloakRoleServiceConfig(original);

        assertEquals(original.getServerUrl(), copy.getServerUrl());
        assertEquals(original.getRealm(), copy.getRealm());
        assertEquals(original.getClientId(), copy.getClientId());
        assertEquals(original.getServiceAccountClientId(), copy.getServiceAccountClientId());
        assertEquals(original.getClientSecret(), copy.getClientSecret());
        assertEquals(original.getAdminRoleName(), copy.getAdminRoleName());
        assertEquals(original.getGroupAdminRoleName(), copy.getGroupAdminRoleName());
    }

    @Test
    public void testRealmRolesWhenClientIdBlank() {
        KeycloakRoleServiceConfig config = new KeycloakRoleServiceConfig();
        config.setClientId("");
        // An empty clientId means "use realm roles"
        assertNotNull(config.getClientId());
        assertEquals("", config.getClientId());
    }
}
