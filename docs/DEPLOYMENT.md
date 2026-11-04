# Building and deploying the Luna website

The selected layout is a public HTTPS play website on the Luna VPS, with source
kept in the private GitHub repository. Anyone may open the website; game-account
login remains inside Luna. The domain `play.example.com` below is a placeholder.
No real hostname has been selected or deployed by this repository setup.

## The simple update path

1. Run **Actions → Build and publish webclient → Run workflow** on `master`.
2. Download the successful run's **Luna-Webclient** artifact as `Luna-Webclient.zip`.
3. Upload it to the VPS and extract it into a new release directory.
4. Switch the website's `current` symlink and verify the published client.

The first deployment also needs the one-time Luna WebSocket and HTTPS setup
below. Later static website releases do not require a game-server restart.

## What runs where

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

## Build the static site

A push or merge to `master`, or the manual **Build and publish webclient** workflow,
produces a downloadable artifact. It does not upload the artifact to a VPS or
restart Luna. `Luna-Webclient.zip` has `index.html` at its root. Preserve its
subdirectories and hidden `.nojekyll` file when extracting it.

For local builds, use Java 21 and Node.js 24. In Windows PowerShell:

```powershell
.\gradlew.bat :browser:browserSite
node browser/build-webclient.mjs
```

On Linux/macOS, substitute `./gradlew` for `./gradlew.bat`. Output is
`build/webclient-site`. The packager refuses a nonempty destination; keep an
existing candidate and use a fresh output path for another build:

```sh
node browser/build-webclient.mjs browser/build/site build/webclient-site-candidate-2
```

The default `browser/pages-config.json` uses `"websocketUrl": "same-origin"`.
On HTTPS, runtime configuration selects the website origin plus `/game` using
`wss:`. Leave the `LUNA_WEBSOCKET_URL` repository variable unset for this VPS layout;
remove any old temporary-tunnel override. For a separate game endpoint, set that
public repository variable before building, or set the same environment variable
for a local build. External public endpoints use an absolute `wss://.../game` URL.

The output contains JS/Wasm, website assets, cache stores and their integrity
manifest, public client settings, legal notices and `release-provenance.json`.
The provenance file records the shared source, website build and cache fingerprint.
The browser's public RSA key must match the deployed game server. The server's
private key must stay outside the website output and source repository.

## One-time Luna server setup

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

## One-time HTTPS setup

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

## Upload and activate a release

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

## Verify a published release

- Open the real HTTPS URL and confirm its certificate, website and client startup.
- Inspect `release-provenance.json` and startup/cache logs for the intended build and cache.
- Confirm the browser upgrades `/game` on the intended secure host.
- Log in, walk, interact with an object or NPC, use inventory/equipment and chat, then log out and back in.
- Log in with the desktop client on TCP 43594 as well.
- Verify that server records show real browser client addresses through Caddy.

Retire an older relay only after these checks pass and its existing sessions have
drained. A successful build or loaded webpage is not proof of working gameplay.

## Optional website access gate

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

## Optional GitHub Pages hosting

Pages cannot proxy `/game`; it requires a separate public secure Luna WebSocket
endpoint. For this private organization repository it also requires an eligible
plan. At initial setup, the organization's Free plan did not support Pages from a
private repository. See [GitHub's eligibility documentation](https://docs.github.com/en/pages/getting-started-with-github-pages/what-is-github-pages).
The downloadable artifact works independently of Pages eligibility.

If an eligible plan and Pages deployment are chosen:

1. Set repository Actions variable `LUNA_WEBSOCKET_URL` to the real absolute
   `wss://.../game` URL. `same-origin` cannot proxy game traffic on Pages.
2. Add the exact Pages website origin to Luna's `allowed_origins`, without a
   repository subpath, and apply server settings through its normal restart.
3. Configure Pages publishing as GitHub Actions and its required environment permissions.
4. Set repository variable `PUBLISH_GITHUB_PAGES` to the exact value `true`.
5. Run **Build and publish webclient** on `master` and inspect the separate Pages job.

Leave `PUBLISH_GITHUB_PAGES` unset or false for VPS hosting. A private source
repository does not automatically make a Pages website private.

Use the [upstream guide](UPSTREAM.md) to bring in desktop changes, review the
candidate pull request, then build and publish the selected release as above.
