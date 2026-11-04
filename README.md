# Luna web client

Luna's Java client, compiled for the browser with TeaVM, with a classic website
around the game. This private repository maintains the website and browser
integration while retaining the public desktop client's Git history. The intended
deployment is a public play website with privately maintained source.

Shared game code lives in `src/`, browser adapters in `browser/src/main/java/`,
and the surrounding website in `browser/web/`. Both JavaScript and WebAssembly
builds are produced. The browser currently uses fixed mode; enlarging its display
does not create a larger game viewport.

## Build or update through GitHub

| Task | Where to go | Result |
| --- | --- | --- |
| Build the website | **Actions → Build and publish webclient → Run workflow**, select `master` | Download **Luna-Webclient** from the run's artifacts as `Luna-Webclient.zip`. |
| Bring in desktop updates | **Actions → Sync upstream client → Run workflow**, select `master` | Review an update pull request and its browser build. Conflicts require human resolution. |

Merging changes into `master` also builds the website. The ZIP has `index.html` at
its root. Building it does not upload the website to a VPS or restart Luna. Follow
the [deployment guide](docs/DEPLOYMENT.md) to publish it and the
[upstream guide](docs/UPSTREAM.md) to maintain shared client updates.

## Build locally

Use Java 21 and Node.js 24. In Windows PowerShell, from the repository root:

```powershell
.\gradlew.bat :browser:browserSite
node browser/build-webclient.mjs
```

On Linux/macOS, use `./gradlew` instead of `./gradlew.bat`. The compiled client is
in `browser/build/site`; the deployable website is in `build/webclient-site`.
The packager requires a new or empty output directory. Keep an earlier candidate
and pass a fresh path when packaging another one:

```sh
node browser/build-webclient.mjs browser/build/site build/webclient-site-candidate-2
```

Desktop executables remain available from the shared source:

```powershell
.\gradlew.bat jar betaJar verifyBetaJar
```

These produce `build/libs/luna-dev.jar` and `build/libs/luna-beta.jar`, using their
corresponding properties files. See the [upstream guide's verification notes](docs/UPSTREAM.md#update-locally)
for the imported regression-fixture limitation. The original desktop README is
preserved in [DESKTOP-UPSTREAM.md](docs/DESKTOP-UPSTREAM.md).

## Hosting and privacy

The recommended VPS layout serves HTTPS static files and sends `/game` to Luna's
native WebSocket listener. The default `same-origin` configuration connects to
`wss://<website-host>/game` on an HTTPS site. Desktop clients continue using TCP
43594. Node.js is a build tool; this layout needs no Node relay on the VPS.

A Luna server build with native WebSocket support must be installed and configured
before this connection works. That code is published upstream; the repository's
initial website setup did not deploy it to the beta VPS. The included
[Caddy example](deploy/Caddyfile.example) uses a placeholder domain and enables no
website password gate. The real hostname still needs to be selected.

Private GitHub source does not hide files sent to players: they can inspect the
website JavaScript, WebAssembly, artwork, public settings and cache. Anyone may
open the intended public play website; Luna account login remains separate.
GitHub Pages is optional. At initial setup, the organization's Free plan did not
support Pages from this private repository, while the ZIP could be hosted on the
VPS. See the deployment guide for eligibility and configuration.

See [third-party notices](browser/web/legal/THIRD_PARTY_NOTICES.txt) for dependency
licenses and asset provenance. Those notices do not assign a blanket license to
the inherited client or game cache.
