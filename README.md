# Luna | Revision 377 Desktop and Browser Clients

The desktop and browser clients share generic revision 377 Java source for use
with your own compatible server, connection profile and cache. The default
connection is localhost. This repository does not publish player launchers,
game releases, caches or a live player website.

## Build and run locally

Use Java 21. Browser development also requires Node.js 24 and npm. Run commands
from this repository's root.

Build the desktop executables in Windows PowerShell:

```powershell
.\gradlew.bat jar verifyClientJar "-PcacheZip=C:/path/to/cache.zip"
java -jar build/libs/luna-dev.jar
```

The output is `build/libs/luna-dev.jar`, using `client.properties`. The desktop cache is `~/luna/cache`.

Build and serve the browser client locally:

```powershell
npm --prefix browser ci
.\gradlew.bat :browser:browserSite "-PcacheZip=C:/path/to/cache.zip"
node browser/serve.mjs
```

Open **http://127.0.0.1:8090/** with the default `client.properties`. Start the
compatible development game server separately. The default local preview uses
its `/game` WebSocket-to-TCP bridge to the configured `connectHost` and effective
`connectPort`. It does not start the game server.

On Linux/macOS, use `./gradlew` in place of `.\gradlew.bat`.

## Configure your own client

Edit `client.properties` for the normal build. Existing settings provide the
client name, TCP address/port, world, membership options and the server's **public**
RSA modulus/exponent. Browser settings live in that same file:

| Setting | Purpose |
| --- | --- |
| `websocketUrl` | Browser game endpoint: `same-origin` for the local preview or a configured same-host proxy; an absolute `wss://.../game` URL for GitHub Pages. |
| `websiteUrl` | Main Menu and project website links. |
| `webBindAddress`, `webPort` | Local preview listener; defaults are `127.0.0.1` and `8090`. |
| `webOrigin` | Exact website origin allowed by the optional Node proxy, without a repository path or trailing slash. |
| `websocketProxyPort` | Listener port for the optional standalone Node proxy. |

Choose another complete root profile explicitly when needed:

```powershell
.\gradlew.bat :browser:browserSite "-PclientProperties=custom.properties" "-PcacheZip=C:/path/to/cache.zip"
```

The browser's copied `client.properties` and `client-config.json` are generated
build outputs. Their integrity is checked together; rebuild after changing a
profile. There is no second file of connection settings to edit. See the
[deployment guide](docs/DEPLOYMENT.md) for public hosting and server requirements.

## Build and hosting responsibilities

Pushes to `master` run **Compile development clients** without a cache. The
workflow compiles source and does not deploy or publish a game package.

Packaging requires `-PcacheZip=<path>` with one copy each of
`main_file_cache.dat` and `main_file_cache.idx0` through `main_file_cache.idx4`.
The build validates those stores and prepares ignored `data/bootstrap/` assets.
No game cache is fetched automatically. Keep your deployment profiles, cache,
credentials and generated packages outside public source control.

To host your own browser build, run `node browser/build-webclient.mjs` after
building, then deploy the generated static directory to your HTTPS host. See
[deployment](docs/DEPLOYMENT.md) and [shared maintenance](docs/UPSTREAM.md).

## Source layout

- `src/`: shared Java game logic, packet handling, rendering and in-game interfaces.
- `browser/src/main/java/`: browser platform adapters and browser entry point.
- `browser/web/`: surrounding game page, browser input/presentation support and asset loading.
- `data/bootstrap/`: shared cache seed and integrity manifest.

The browser currently uses fixed mode at its native 765 × 503 framebuffer size.
A larger displayed image does not create a larger game viewport. Browser cache
files are downloaded from the built website and retained in browser storage.

This source repository is public. Browser-delivered files are readable by that browser. See [third-party notices](browser/web/legal/THIRD_PARTY_NOTICES.txt)
for component licenses and asset provenance. They do not assign a blanket license
to the inherited client or game assets. The inherited regression-fixture limitation
is explained in [verification notes](docs/UPSTREAM.md#verification). The original
desktop README is retained in [DESKTOP-UPSTREAM.md](docs/DESKTOP-UPSTREAM.md).
