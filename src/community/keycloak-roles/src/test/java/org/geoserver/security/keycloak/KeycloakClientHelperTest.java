package org.geoserver.security.keycloak;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeycloakClientHelperTest {

    @Mock
    private Keycloak keycloak;

    private KeycloakRoleServiceConfig config;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        KeycloakRoleServiceConfig defaultConfig = new KeycloakRoleServiceConfig();
        defaultConfig.setServerUrl("http://localhost:8080");
        defaultConfig.setRealm("GeoServer");
        defaultConfig.setServiceAccountClientId("sa-client");
        defaultConfig.setClientSecret("secret");

        this.config = defaultConfig;
    }

    @Test
    public void checksForNonSuccessfulConnection() {
        try (KeycloakClientHelper helper = new KeycloakClientHelper(this.config)) {
            when(keycloak.realm(config.getRealm())).thenThrow(new RuntimeException());
            ReflectionTestUtils.setField(helper, "keycloak", keycloak);
            assertThrows(RuntimeException.class, helper::testConnection);
        }
    }

    @Test
    public void checksForSuccessfulConnection() throws Exception {
        try (KeycloakClientHelper helper = new KeycloakClientHelper(config)) {
            RealmRepresentation realmRepresentation = mock(RealmRepresentation.class);
            RealmResource realmResource = mock(RealmResource.class);
            when(realmResource.toRepresentation()).thenReturn(realmRepresentation);
            when(keycloak.realm(config.getRealm())).thenReturn(realmResource);
            ReflectionTestUtils.setField(helper, "keycloak", keycloak);
            helper.testConnection();
        }
    }

//    @Nullable Object keycloakRefl = ReflectionTestUtils.getField(helper, "keycloak");
//    helper.close();

    @Test
    public void returnsTheRealmRoles() {
        try (KeycloakClientHelper helper = new KeycloakClientHelper(this.config)) {
            RealmResource realmResource = mock(RealmResource.class);
            RolesResource rolesResource = mock(RolesResource.class);
            RoleRepresentation roleRepresentationA = mock(RoleRepresentation.class);
            RoleRepresentation roleRepresentationB = mock(RoleRepresentation.class);
            RoleRepresentation roleRepresentationC = mock(RoleRepresentation.class);
            when(roleRepresentationA.getName()).thenReturn("Role A");
            when(roleRepresentationB.getName()).thenReturn("Role B");
            when(roleRepresentationC.getName()).thenReturn("Role C");
            when(rolesResource.list()).thenReturn(List.of(roleRepresentationA, roleRepresentationB, roleRepresentationC));
            when(realmResource.roles()).thenReturn(rolesResource);
            when(keycloak.realm(config.getRealm())).thenReturn(realmResource);
            ReflectionTestUtils.setField(helper, "keycloak", keycloak);

            List<String> roles = helper.fetchRoleNames();

            assertEquals(List.of("Role A", "Role B", "Role C"), roles);
        }
    }

    @Test
    public void returnsTheClientRoles() {
        try (KeycloakClientHelper helper = new KeycloakClientHelper(this.config)) {
            RealmResource realmResource = mock(RealmResource.class);

            RoleRepresentation roleRepresentationA = mock(RoleRepresentation.class);
            RoleRepresentation roleRepresentationB = mock(RoleRepresentation.class);
            RoleRepresentation roleRepresentationC = mock(RoleRepresentation.class);
            when(roleRepresentationA.getName()).thenReturn("Role A");
            when(roleRepresentationB.getName()).thenReturn("Role B");
            when(roleRepresentationC.getName()).thenReturn("Role C");
            RolesResource rolesResource = mock(RolesResource.class);
            when(realmResource.roles()).thenReturn(rolesResource);
            when(rolesResource.list()).thenReturn(List.of(roleRepresentationA, roleRepresentationB, roleRepresentationC));

            when(keycloak.realm(config.getRealm())).thenReturn(realmResource);
            ReflectionTestUtils.setField(helper, "keycloak", keycloak);

            List<String> roles = helper.fetchRoleNames();

            assertEquals(List.of("Role A", "Role B", "Role C"), roles);
        }
    }
}
