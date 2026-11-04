# Luna web client

A public community web client for Luna, with a basic 2005-style website around
the shared Java game client. TeaVM builds the same game source for the browser;
this independent repository retains the desktop client's Git history and merges
updates from [luna-rs/luna-client](https://github.com/luna-rs/luna-client).

Website: [luna-rs.github.io/luna-webclient](https://luna-rs.github.io/luna-webclient/).
No custom domain is required for GitHub Pages. Anyone may open the play website;
Luna account login remains inside the game.

## Publish or update through GitHub

| Task | Where to go | Result |
| --- | --- | --- |
| Publish the current website | **Actions → Build and publish webclient → Run workflow**, select `master` | Builds and deploys the configured GitHub Pages site; also supplies **Luna-Webclient.zip**. |
| Bring in desktop updates | **Actions → Sync upstream client → Run workflow**, select `master` | Creates an update pull request and validates a clean browser merge. Conflicts require human resolution. |

A push or merge to `master` also runs the build and Pages deployment. Review the
workflow result before sharing a release. The website build does not deploy or
restart the Luna game server. See [DEPLOYMENT.md](docs/DEPLOYMENT.md) for endpoint
configuration, Pages setup and optional VPS hosting, and
[UPSTREAM.md](docs/UPSTREAM.md) for maintaining shared client updates.

## Game connection

GitHub Pages serves static files. Browser game traffic uses the external secure
WebSocket endpoint in `browser/pages-config.json`, optionally overridden by the
`LUNA_WEBSOCKET_URL` repository variable. Pages cannot proxy `/game` itself.

The current prototype endpoint is a temporary Cloudflare address backed by the
existing PC relay. That PC and its relay/tunnel processes must stay running until
a replacement is deployed and verified. The URL changes when the temporary tunnel
is recreated. Luna's native Java WebSocket support is published upstream, but it
has not been deployed to the beta VPS by this website setup. Do not assume the Node
relay has already been removed.

## Build locally

Use Java 21 and Node.js 24. From the repository root in Windows PowerShell:

```powershell
.\gradlew.bat :browser:browserSite
node browser/build-webclient.mjs
```

On Linux/macOS, use `./gradlew` instead of `./gradlew.bat`. The complete static site
is in `build/webclient-site`; compiled client input is in `browser/build/site`.
The packager requires a new or empty destination. Keep an existing candidate and
use a fresh path for another build:

```sh
node browser/build-webclient.mjs browser/build/site build/webclient-site-candidate-2
```

The shared source can still build desktop executables:

```powershell
.\gradlew.bat jar betaJar verifyBetaJar
```

Outputs are `build/libs/luna-dev.jar` and `build/libs/luna-beta.jar`. The imported
regression-fixture limitation is explained once in the
[upstream verification notes](docs/UPSTREAM.md#update-locally). The original desktop
README is preserved in [DESKTOP-UPSTREAM.md](docs/DESKTOP-UPSTREAM.md).

## Source layout and visibility

Shared Java code lives in `src/`, thin browser adapters in
`browser/src/main/java/`, and the surrounding website in `browser/web/`. Builds
include JavaScript and WebAssembly backends. The browser currently uses fixed
mode; a larger displayed image does not create a larger game viewport.

This repository, its history, website and browser adapters are public and can be
inspected and forked. HiveScape's future custom website can be maintained separately,
including in a private repository. Private source still cannot hide JavaScript,
WebAssembly, images or cache files delivered to players' browsers.

See [third-party notices](browser/web/legal/THIRD_PARTY_NOTICES.txt) for dependency
licenses and asset provenance. They do not assign a blanket license to the
inherited client or game cache.
