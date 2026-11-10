# Hosting your own development browser client

This public repository provides source and local build tools. Supply your own
server, connection profile, compatible cache ZIP and HTTPS host.

## Configure your own deployment

Edit the selected root properties profile. The normal build uses `client.properties`.
Use a complete alternative such as `client (2).properties` only when selecting it
explicitly for that build.

| Properties | What to configure |
| --- | --- |
| `clientName` | Your client title and world label. |
| `connectHost`, `connectPort`, `portOffset` | Your game TCP endpoint; the effective port is `connectPort + portOffset`. The optional Node proxy reuses this target. |
| `rsaEnabled`, `rsaModulus`, `rsaExponent` | Public login encryption values matching your game server. |
| `websocketUrl` | Your browser game endpoint. Use an absolute `wss://.../game` URL for Pages. Preserve any query your selected endpoint requires. |
| `websiteUrl` | Your Main Menu/project website link. |
| `webBindAddress`, `webPort` | Local preview listener, normally `127.0.0.1:8090`. |
| `webOrigin` | Exact website origin accepted by the optional Node proxy, without a path or trailing slash. |
| `websocketProxyPort` | Local listener port for the optional standalone Node proxy, normally `8810`. |

For a Pages site at `https://yourname.github.io/your-client/`, its origin is
`https://yourname.github.io`. The repository path is not part of the origin.

The server must separately enable its WebSocket listener and allow that website
origin. Configure TLS through the server's HTTPS proxy or tunnel, and keep the
matching RSA private key on the game server. A downloaded client properties file
cannot configure those server services. Never add private RSA exponents, TLS keys,
account passwords or deployment credentials to the client properties or website.

Gradle reads the selected file with Java's properties parser. It generates
`client-config.json` beside the copied `client.properties`; build and startup
integrity checks keep those outputs together. Rebuild after changing properties.
There is no manually maintained browser endpoint JSON or workflow endpoint override.
The local packaging command records development provenance from the current Git commit.

Cache assets come from the shared `data/bootstrap/cache.zip` and its `cache.sha256`
manifest. The build extracts the six core stores, verifies their hashes and
includes them with the website. Browser asset URLs work relative to the deployed
site, including a GitHub Pages repository subpath. Players download the cache and
retain it in browser storage. Desktop clients use their configured `~/luna/cache`.
To change game assets, update the compatible cache build input and its manifest;
connection settings remain in properties. Do not modify generated cache files or
descriptors to customize a deployment.

## Local browser development

Use Java 21 and Node.js 24. From the repository root in Windows PowerShell:

```powershell
npm --prefix browser ci
.\gradlew.bat :browser:browserSite "-PcacheZip=C:/path/to/cache.zip"
node browser/serve.mjs
```

With the default properties, open **http://127.0.0.1:8090/** and run a compatible
development Luna server on the configured TCP address. `same-origin` connects to
`/game` on the local preview, which forwards binary game traffic to that fixed
configured TCP target. The preview does not start the game server.

Alternatively, set `websocketUrl` to a local native listener such as
`ws://127.0.0.1:43595/game`. That listener needs to allow the local page's origin.
A public endpoint used from a local preview also needs to accept the local origin;
client settings do not silently change its server policy.

On Linux/macOS use `./gradlew` instead of `.\gradlew.bat`. Supply your own
compatible cache with `-PcacheZip=/path/to/cache.zip` for packaging builds.

## Build the deployable website

After the browser build, package the static website:

```sh
node browser/build-webclient.mjs
```

The output is `build/webclient-site`, with `index.html` at its root. The packager
requires a new or empty destination. Keep an existing candidate and pass a fresh
path when packaging another build:

```sh
node browser/build-webclient.mjs browser/build/site build/webclient-site-candidate-2
```

For a Pages deployment, check the selected configuration:

```sh
node browser/configuration.mjs --pages build/webclient-site
```

Pages needs an external secure WebSocket endpoint. This check rejects `same-origin`,
plain `ws:` and loopback endpoints for a public Pages deployment. A same-origin
endpoint is appropriate for the local preview or a configured same-host VPS proxy.

The website contains JS/Wasm, title and page assets, the cache and integrity
manifest, public properties, generated descriptor, legal notices and
`release-provenance.json`. Use provenance to identify the build, selected
connection and cache fingerprint.

## Public source and delivered files

This client repository, its shared rendering/in-game interfaces, browser adapters
and basic website are public. Players also receive inspectable JavaScript,
WebAssembly, artwork, public settings and cache files. HiveScape can maintain a
future branded website separately; making source private would not hide the
files delivered to its players' browsers. Retain the included
[third-party notices](../browser/web/legal/THIRD_PARTY_NOTICES.txt).

## Optional VPS deployment with native Luna WebSockets

This is an alternative to the current Pages/relay deployment, not an already
completed migration. It needs a real domain, a Luna server release containing the
native listener, and HTTPS configuration. `play.example.com` below is a placeholder;
no real hostname or website gate has been configured by this example.

For this alternative, set `websocketUrl=same-origin` in a selected properties
profile before building the VPS artifact. On HTTPS, that artifact connects to the
website host's `/game` using `wss:`. Do not select this same-origin profile for the
active GitHub Pages deployment.

### What runs where

```text
Browser -- HTTPS --> Caddy -- static files --> /var/www/luna-webclient/current
                       |
                       +-- /game WebSocket --> Luna on 127.0.0.1:43595

Desktop client ---------------------- TCP --> Luna on port 43594
```

Both transports use the same Luna world and login/game protocol. The native
WebSocket listener does not open another game TCP connection. Node.js is used
while building the website; this VPS layout needs no Node relay. Caddy supplies
HTTPS and WebSocket TLS termination, while Luna's internal listener stays local.

### One-time Luna server setup

The server needs native WebSocket support from [luna-rs/luna](https://github.com/luna-rs/luna),
introduced by commit `c9b10f537ebcc2224cabc466cbfe51539ce14a8b`, or a later revision
containing it. Deploy through your normal game-server release procedure,
preserving player saves, cache, runtime configuration and RSA keys.

Keep the existing desktop `game.port` at 43594. In the server's existing
`data/luna.jsonc`, configure this section inside `game`:

```json
"websocket": {
  "enabled": true,
  "bind_address": "127.0.0.1",
  "port": 43595,
  "path": "/game",
  "allowed_origins": ["https://play.example.com"],
  "trusted_proxy_addresses": ["127.0.0.1"],
  "forwarded_ip_header": "X-Real-IP",
  "max_http_content_length": 8192,
  "max_frame_payload_length": 1048576,
  "max_pending_outbound_bytes": 8388608,
  "handshake_timeout_millis": 10000
}
```

Replace the origin with the real website origin, without a trailing slash or URL
path. Add other exact origins only when they should connect. Listener settings
apply on a game-server restart, scheduled as part of a server release. Uploading
a website ZIP does not change or restart that server.

The Caddy example overwrites `X-Real-IP` with its actual socket peer. Luna trusts
only the local proxy to supply this value, so player bans and connection limits
use the player's address instead of loopback. The internal listener stays bound
to 127.0.0.1. This exact example assumes direct browser-to-Caddy traffic: a
Cloudflare-managed DNS record should initially be DNS-only, not proxied. Adding
a CDN or another proxy requires correct trusted client-IP handling in Caddy and
verification that Luna receives the actual player address.

After rollout, confirm that both listeners are bound and that desktop login on
43594 still works. Verify the WebSocket transport through a real game login as
well; an open port alone does not establish a working game protocol.

### One-time HTTPS setup

Choose a hostname you control and point its public A record at the VPS. Add an
AAAA record only if IPv6 also reaches the configured VPS and firewall. Public
ports 80 and 443 must reach Caddy; 43595 remains local. The ordinary desktop game
port keeps its existing access policy.

Install Caddy using its [official instructions](https://caddyserver.com/docs/install).
Its [automatic HTTPS documentation](https://caddyserver.com/docs/automatic-https)
explains DNS, port and persistent certificate-storage requirements. Replace the
domain in [deploy/Caddyfile.example](../deploy/Caddyfile.example) and merge that
site block into `/etc/caddy/Caddyfile`, preserving any existing sites. Do not
replace unrelated Caddy configuration with the example.

After the first static release below is present, validate the actual configuration
and reload the installed Caddy service:

```sh
sudo caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile
sudo systemctl reload caddy
```

These are standard Linux service paths; use the installation's service procedure
if they differ. Caddy's [reverse proxy](https://caddyserver.com/docs/caddyfile/directives/reverse_proxy)
handles WebSocket upgrades. This template has not been installed or tested against
a live production hostname.

### Upload and activate a release

Download `Luna-Webclient.zip` to the local machine. From PowerShell, change the
local file path if necessary and upload it to the existing VPS account:

```powershell
scp .\Luna-Webclient.zip deploy@your-game-host:~/Luna-Webclient.zip
ssh deploy@your-game-host
```

An administrator creates these specific website directories once, assigning them
to the existing `luna` deployment account. Caddy needs read/traverse access. This
does not change the game server:

```sh
sudo install -d -m 0755 -o luna -g luna /var/www/luna-webclient /var/www/luna-webclient/releases
```

Run the following in the VPS shell. Replace the release ID with a unique version
and short build commit for each upload; use a simple directory name:

```sh
set -eu
release_id="web-YYYYMMDD-SHORTCOMMIT"
release_dir="/var/www/luna-webclient/releases/$release_id"
current_link="/var/www/luna-webclient/current"
next_link="/var/www/luna-webclient/current.$release_id"

test ! -e "$release_dir"
test ! -L "$release_dir"
mkdir -- "$release_dir"
unzip -q "$HOME/Luna-Webclient.zip" -d "$release_dir"
test -f "$release_dir/index.html"
test -f "$release_dir/release-provenance.json"
test -f "$release_dir/legal/THIRD_PARTY_NOTICES.txt"
chmod -R a+rX "$release_dir"

if [ -e "$next_link" ] || [ -L "$next_link" ]; then
  echo "Candidate link exists; choose a fresh release ID." >&2
  exit 1
fi
if [ -e "$current_link" ] && [ ! -L "$current_link" ]; then
  echo "Current is a real directory; inspect it before publishing." >&2
  exit 1
fi
ln -s -- "$release_dir" "$next_link"
mv -T -- "$next_link" "$current_link"
```

The final Linux rename atomically switches the website symlink. Prior release
directories remain available. For rollback, repeat the new-link-and-rename
procedure with the exact previous release directory after checking it exists.
Do not extract over the currently served directory or delete release history.

Later static updates need neither a Caddy reload nor a Luna restart. Existing
sessions retain their loaded client until refresh/relogin. Changes to game-server
code or listener configuration are a separate server release.

### Verify a published release

- Open the real HTTPS URL and confirm its certificate, website and client startup.
- Inspect `release-provenance.json` and startup/cache logs for the intended build and cache.
- Confirm the browser upgrades `/game` on the intended secure host.
- Log in, walk, interact with an object or NPC, use inventory/equipment and chat, then log out and back in.
- Log in with the desktop client on TCP 43594 as well.
- Verify that server records show real browser client addresses through Caddy.

Retire an older relay only after these checks pass and its existing sessions have
drained. A successful build or loaded webpage is not proof of working gameplay.

### Optional website access gate

The selected deployment is public: no extra website password is configured.
Private source access and website access are separate decisions. Players can
inspect downloaded JavaScript, WebAssembly, artwork, settings and cache files.

If tester-only access is wanted later, Caddy supports
[`basic_auth`](https://caddyserver.com/docs/caddyfile/directives/basic_auth) using
hashed passwords. Protect both assets and `/game`, then test first connection,
login, logout and reconnect through that gate. End-to-end WebSocket authentication
through such a gate has not been verified by this setup. Keep gate credentials
and password hashes in server configuration, not in static files or Git. Luna
account login remains a separate requirement.


Use the [shared maintenance guide](UPSTREAM.md) to develop and verify changes,
then publish through the selected hosting workflow.
