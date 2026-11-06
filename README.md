# Luna | Revision 377 Desktop and Browser Clients

The Java desktop client and browser client share one maintained source tree in
[luna-rs/luna-client](https://github.com/luna-rs/luna-client). Both connect to the
[Luna game server](https://github.com/luna-rs/luna), a permanent world where everyone is welcome to
play and progress. Luna focuses on faithful classic gameplay with limited changes, **5× XP**,
a standard economy with restricted item spawning, and **no character wipes**. Autonomous player
bots train, trade, fight, and progress alongside you.

Luna is actively developed, and feedback and bug reports help improve the world as you play.
**HiveScape** will be the home for our custom content and “what if?” ideas.

**[Play in your browser](https://luna-rs.github.io/luna-client/)**

The browser page uses the compact 2005 game-page layout. Desktop builds and
browser builds remain separate targets; fixes to shared game code reach both.

## Build and run locally

Use Java 21. Browser development also requires Node.js 24 and npm. Run commands
from this repository's root.

Build the desktop executables in Windows PowerShell:

```powershell
.\gradlew.bat jar betaJar verifyBetaJar
java -jar build/libs/luna-dev.jar
```

The outputs are `build/libs/luna-dev.jar` and `build/libs/luna-beta.jar`, with their
corresponding root properties profiles. The desktop cache is `~/luna/cache`.

Build and serve the browser client locally:

```powershell
npm --prefix browser ci
.\gradlew.bat :browser:browserSite
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
.\gradlew.bat :browser:browserSite "-PclientProperties=client (2).properties"
```

The browser's copied `client.properties` and `client-config.json` are generated
build outputs. Their integrity is checked together; rebuild after changing a
profile. There is no second file of connection settings to edit. See the
[deployment guide](docs/DEPLOYMENT.md) for public hosting and server requirements.

## Publish the browser client

Push or merge to `master`, or run **Actions → Build and publish webclient → Run
workflow** on `master`. The action builds the selected profile, supplies a
**Luna-Webclient** ZIP, and deploys Pages when enabled. Repository variable
`CLIENT_PROPERTIES` chooses the profile filename only; server addresses and
WebSocket URLs come from that properties file. `PUBLISH_GITHUB_PAGES=true` enables
Pages publication. Luna's public deployment explicitly selects the beta profile.

Shared desktop fixes are made in this repository. There is no separate webclient
repository or upstream-sync action to run. See [shared maintenance](docs/UPSTREAM.md).

## Source layout

- `src/`: shared Java game logic, packet handling, rendering and in-game interfaces.
- `browser/src/main/java/`: browser platform adapters and browser entry point.
- `browser/web/`: surrounding game page, browser input/presentation support and asset loading.
- `data/bootstrap/`: shared cache seed and integrity manifest.

The browser currently uses fixed mode at its native 765 × 503 framebuffer size.
A larger displayed image does not create a larger game viewport. Browser cache
files are downloaded from the built website and retained in browser storage.

The selected beta profile currently uses a temporary Cloudflare endpoint backed
by the existing PC relay. Its PC processes must remain available until a replacement
is deployed and verified. Native Luna WebSocket support is available in the server
source, but this client integration does not deploy or restart the live Luna server.

The repository and delivered browser files are public. See [third-party notices](browser/web/legal/THIRD_PARTY_NOTICES.txt)
for component licenses and asset provenance. They do not assign a blanket license
to the inherited client or game assets. The inherited regression-fixture limitation
is explained in [verification notes](docs/UPSTREAM.md#verification). The original
desktop README is retained in [DESKTOP-UPSTREAM.md](docs/DESKTOP-UPSTREAM.md).
