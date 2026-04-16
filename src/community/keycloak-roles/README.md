# GeoServer Keycloak Role Service

Community module that provides a read-only GeoServer **role service** backed by
[Keycloak](https://www.keycloak.org/). It synchronizes realm-level or client-level
roles from a Keycloak server into GeoServer via the
[Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/index.html).

## Features

* **Role synchronization** – fetches roles from a Keycloak realm or from a
  specific Keycloak client and makes them available as GeoServer roles.
* **Service account authentication** – connects to the Keycloak Admin REST API
  using the OAuth 2.0 `client_credentials` grant (service account), so no
  personal admin credentials are required.
* **Fully configurable via the admin UI** – all connection parameters (server
  URL, realm, client ID, service account credentials) can be edited in the
  GeoServer web administration interface under *Security → Users, Groups, and
  Roles → Role Services*.
* **Explicit synchronization** – roles are only fetched from Keycloak when the
  administrator clicks *Synchronize Roles* in the UI. Synchronized roles are
  cached locally so they survive server restarts without requiring a network
  call.
* **Connection test** – a *Test Connection* button verifies that the Keycloak
  server is reachable and the credentials are valid.

## Requirements

* GeoServer **3.0-SNAPSHOT** (or the version matching this branch)
* A running Keycloak server (tested with Keycloak **26.x**)
* A Keycloak client with **Service Accounts** enabled (see
  [Keycloak setup](#keycloak-setup) below)

## Build

From the GeoServer source root:

```bash
cd src
mvn clean install -Pkeycloak-roles
```

Or build only the module:

```bash
cd src/community/keycloak-roles
mvn clean install
```

## Running GeoServer with the module

When starting GeoServer from source via the `web/app` module, activate the
Maven profile:

```bash
cd src/web/app
mvn jetty:run -Pkeycloak-roles
```

If you use other community profiles, combine them:

```bash
mvn jetty:run -Pkeycloak-roles,<other-profiles>
```

For a binary GeoServer distribution, copy the built JAR and its dependencies
into `WEB-INF/lib`.

## Keycloak setup

The module authenticates against the Keycloak Admin REST API using a **service
account** (OAuth 2.0 `client_credentials` grant). Follow these steps to create
one:

1. In the Keycloak admin console, open the target **realm**.
2. Go to **Clients → Create client**.
3. Set a **Client ID** (e.g. `geoserver-role-sync`), choose
   *Client authentication: On* and enable **Service accounts roles**. You do
   not need any other capability (Standard flow, etc.).
4. After saving, go to the **Service account roles** tab of the newly created
   client.
5. Click *Assign role*, select *Filter by clients*, and assign the required
   `realm-management` roles:
   * `view-realm` – required to list realm-level roles
   * `view-clients` – required to list client-level roles (only needed if you
     want to sync client roles)
6. Note down the **Client ID** and the **Client secret** (found on the
   *Credentials* tab). You will need them when configuring the role service in
   GeoServer.

## Configuration in GeoServer

1. Log in to the GeoServer **web administration** interface.
2. Navigate to **Security → Users, Groups, and Roles**.
3. Under **Role Services**, click **Add new**.
4. Select **Keycloak** from the list of available role service types.
5. Fill in the configuration form:

| Field                        | Description                                                                                                                                            |
|------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Server URL**               | Base URL of the Keycloak server, e.g. `https://keycloak.example.com`                                                                                  |
| **Realm**                    | The Keycloak realm that contains the roles.                                                                                                            |
| **Client ID** *(optional)*   | If set, client-level roles of this Keycloak client are synchronised. Leave empty to synchronise **realm-level** roles instead.                          |
| **Service Account Client ID**| The `client_id` of the Keycloak client that has Service Accounts enabled (see [Keycloak setup](#keycloak-setup)).                                      |
| **Client Secret**            | The client secret of the service account client. This value is stored encrypted in the GeoServer data directory.                                       |
| **Administrator Role**       | *(optional)* Name of the Keycloak role that should map to GeoServer's `ROLE_ADMINISTRATOR`.                                                           |
| **Group Administrator Role** | *(optional)* Name of the Keycloak role that should map to GeoServer's `ROLE_GROUP_ADMIN`.                                                             |

6. Click **Test Connection** to verify the credentials.
7. Click **Save** to persist the configuration.
8. Click **Synchronize Roles** to fetch the roles from Keycloak. The number of
   synchronised roles is shown in the feedback message.

After synchronization, the Keycloak roles appear on the **Roles** tab of the
role service and can be used throughout GeoServer (layer security rules, service
security, etc.).

## How it works

### Synchronisation lifecycle

```
 ┌─────────────────────┐
 │  User clicks        │
 │ "Synchronize Roles" │
 └────────┬────────────┘
          │
          ▼
 ┌────────────────────────────┐
 │  Fetch roles from Keycloak │  (Admin REST API, client_credentials grant)
 │  via KeycloakClientHelper  │
 └────────┬───────────────────┘
          │
          ▼
 ┌──────────────────────────────┐
 │  Persist role names to local │  (roles.txt in the service config directory)
 │  cache file                  │
 └────────┬─────────────────────┘
          │
          ▼
 ┌──────────────────────────────┐
 │  Reload in-memory role maps  │  (AbstractRoleService.load())
 │  from cache file             │
 └──────────────────────────────┘
```

* **On startup / after save** – GeoServer calls `initializeFromConfig()` which
  reads from the local cache file only (no network call). If no cache file
  exists yet (first use), the role list is empty until the first explicit
  synchronization.
* **On explicit sync** – the *Synchronize Roles* button fetches fresh data from
  Keycloak, writes it to the cache file, and reloads the in-memory state.
