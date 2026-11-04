# Publishing the Luna web client

The primary deployment is the public GitHub Pages website:

**https://luna-rs.github.io/luna-webclient/**

The repository is public, so GitHub Free Pages can host it without a custom domain
or a paid private-repository Pages plan. The source history, basic 2005-style Luna
website and browser integration are community-visible. Anyone may open the play
website; game-account login remains inside Luna.

## Publish through GitHub

1. Open **Actions → Build and publish webclient → Run workflow**.
2. Select `master` and run it. A push or merge to `master` runs the same workflow.
3. Wait for both the website build and Pages deployment jobs to succeed.
4. Open the Pages URL above and verify the release and game connection.

The successful run also supplies the **Luna-Webclient** artifact, downloaded as
`Luna-Webclient.zip`. It has `index.html` at its root and includes `.nojekyll`.
Use it for a review copy or optional VPS hosting. Building/publishing these static
files does not deploy or restart the Luna game server.

The manual **Sync upstream client** workflow prepares desktop-source updates as
reviewable pull requests. A clean candidate is built before review; conflicts
require human resolution. Merging the reviewed pull request into `master` builds
and publishes the updated website. See [UPSTREAM.md](UPSTREAM.md) for the full
workflow and normal local merge procedure.

## Pages configuration

The repository's Pages publishing source is **GitHub Actions**, using its
`github-pages` environment. The build supports two repository Actions variables:

| Variable | Pages value |
| --- | --- |
| `PUBLISH_GITHUB_PAGES` | The exact value `true` enables the Pages deployment job on `master`. |
| `LUNA_WEBSOCKET_URL` | The real absolute `wss://.../game` endpoint used for browser game connections. |

The endpoint variable overrides `browser/pages-config.json`. Keep them aligned
when changing the gateway. The build contains public endpoint configuration only,
never a server password or private RSA key. `same-origin` is unsuitable for Pages:
GitHub serves static files and cannot forward `/game` to Luna.

If publishing is disabled deliberately, the workflow still builds its downloadable
ZIP. GitHub's [Pages documentation](https://docs.github.com/en/pages/getting-started-with-github-pages/what-is-github-pages)
explains public repository eligibility and static hosting. No custom hostname or
new DNS record is needed for the standard project URL.

## Current prototype connection and its limits

```text
GitHub Pages --> browser downloads client and cache
Browser -- WSS --> temporary Cloudflare endpoint --> PC Node relay -- TCP --> beta Luna
```

The current prototype uses the externally configured temporary Cloudflare gateway.
Its relay/tunnel processes run on the PC, which must stay online. Recreating a
Quick Tunnel changes its hostname: update `LUNA_WEBSOCKET_URL` and the checked-in
endpoint, then rebuild and deploy. A reachable gateway health page is not proof
that the beta game server is online or that login/gameplay works.

Native WebSocket support has been added to Luna's Java server and published
upstream. It is not installed on the beta VPS by this website setup. The current
Pages prototype still depends on the existing relay until a permanent endpoint
or native-listener deployment is tested and selected. Do not stop an old relay
while the published website or active sessions still use it.

For a future native listener, allow the exact Pages origin
`https://luna-rs.github.io` in Luna. The origin has no `/luna-webclient/` path.
GitHub Pages continues serving the static website; an external HTTPS proxy/tunnel
still provides the game's public WSS endpoint.

## Build locally

Use Java 21 and Node.js 24. In Windows PowerShell:

```powershell
.\gradlew.bat :browser:browserSite
node browser/build-webclient.mjs
```

On Linux/macOS, use `./gradlew` instead of `./gradlew.bat`. The completed static
website is `build/webclient-site`; compiled inputs are in `browser/build/site`.
The packager refuses a nonempty output directory. Keep an earlier candidate and
use a fresh directory for another build:

```sh
node browser/build-webclient.mjs browser/build/site build/webclient-site-candidate-2
```

A local `LUNA_WEBSOCKET_URL` environment variable can override the checked-in
endpoint. The output includes JS/Wasm, website and title assets, cache stores and
integrity manifest, public client settings, legal notices and
`release-provenance.json`. Use that provenance file to identify the shared source,
website build and cache fingerprint of the candidate. The browser's public RSA key
must match the game server; the private RSA key remains on the server.

## Public Luna and a future separate HiveScape website

Luna's community website and code stay public in this repository. GitHub exposes
its reachable history and lets people inspect and fork it. The delivered browser
client already includes rendering and in-game interfaces; those are part of the
shared client, not missing website features another builder must recreate.

HiveScape's future custom website may be maintained in a separate repository,
including privately. Source-repository privacy and website access are separate:
players still receive inspectable JavaScript, WebAssembly, artwork, public settings
and cache files needed to run any browser client. See the included
[third-party notices](../browser/web/legal/THIRD_PARTY_NOTICES.txt) for component
licenses and inherited asset provenance.

## Optional VPS deployment with native Luna WebSockets

This is an alternative to the current Pages/relay deployment, not an already
completed migration. It needs a real domain, a Luna server release containing the
native listener, and HTTPS configuration. `play.example.com` below is a placeholder;
no real hostname or website gate has been configured by this example.

For this alternative, set the website's endpoint to `same-origin` and remove its
external `LUNA_WEBSOCKET_URL` override before building the VPS artifact. On HTTPS,
that artifact connects to the website host's `/game` using `wss:`. Do not apply
that same-origin setting to the active GitHub Pages deployment.

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
containing it. It is published upstream but was not installed on the beta VPS by
this website setup. Deploy it through the normal game-server release procedure,
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
scp .\Luna-Webclient.zip luna@85.155.184.204:~/Luna-Webclient.zip
ssh luna@85.155.184.204
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


Use the [upstream guide](UPSTREAM.md) to bring in desktop changes, review the
candidate pull request, then publish through the selected hosting workflow.
