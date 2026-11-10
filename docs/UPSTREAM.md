# Maintaining the shared desktop and browser client

[Luna's client repository](https://github.com/luna-rs/luna-client) maintains both
build targets. The game server remains a separate project in
[luna-rs/luna](https://github.com/luna-rs/luna). Each downstream deployment owns
its own hosting, cache and connection profile. The default client connects to
`127.0.0.1:43594`; run a compatible local server separately.

## Make a shared client change

1. Create a feature branch from the current client `master`, preserving unfinished
   local work on its existing branch.
2. Change the common game implementation in `src/` where possible. Keep browser
   platform adaptation in `browser/src/main/java/` and the surrounding website in
   `browser/web/`.
3. Build the desktop and browser targets, then verify the affected actions against
   a development server.
4. Review and merge the change into this repository's `master`. Its
   **Compile development clients** workflow checks desktop and browser compilation.

Shared desktop and browser fixes belong in this repository. A source fix needs
to compile and work on both platforms; a desktop-only API can require an adapter
change. Downstream forks manage their own updates and deployment workflows.

## Preserve the shared source boundary

The browser build derives generated Java input from `src/` and substitutes the
thin adapters under `browser/src/main/java/`. Packet handling, game logic, scene
rendering and in-game interfaces continue to come from the shared client.

Do not add a second maintained `browser/src/main/java/client.java` or edit generated
files under `browser/build/`. Update the shared source or the generation rule when
an upstream method signature or platform API changes. The desktop launcher stays
independent of the browser startup and process lifecycle.

## Configuration profiles

The desktop JAR packages root `client.properties`. The default browser build
selects the same file. Choose a different
complete root profile with `-PclientProperties=<filename>`. For example, in Windows
PowerShell:

```powershell
.\gradlew.bat :browser:browserSite "-PclientProperties=custom.properties" "-PcacheZip=C:/path/to/cache.zip"
```

Connection, public RSA, branding and browser settings come from the selected file.
Gradle uses Java's properties parser and produces the copied properties snapshot
and a generated `client-config.json`. The build and browser startup check that
these files match. Do not edit generated descriptors or use them as another
configuration source.

Public CI only compiles source. Downstream packaging supplies a profile and
explicit local `-PcacheZip` archive; see [deployment](DEPLOYMENT.md).

The build requires your explicit local `-PcacheZip` archive, validates its six
core stores and generates the ignored shared cache seed and SHA-256 manifest
under `data/bootstrap/`. To update assets, supply a new compatible cache ZIP and
rebuild instead of editing the generated seed or manifest. No game cache is
downloaded automatically. Changing an endpoint does not require a different cache
or game client implementation.

## Verification

With Java 21, compile both source targets without a cache:

```sh
./gradlew compileJava :browser:compileJava
```

Build the desktop and browser packages using your own cache:

```sh
./gradlew allClientBuilds verifyClientJar -PcacheZip=/path/to/cache.zip
```

On Windows PowerShell, use `.\gradlew.bat` with the same tasks. Browser preview and
packaging tools also require Node.js 24. To package a selected browser profile
for your own static host and check its configuration for GitHub Pages:

```sh
node browser/build-webclient.mjs
node browser/configuration.mjs --pages build/webclient-site
```

The packaging destination must be new or empty. Pages requires a separately
hosted secure WebSocket endpoint; `same-origin` is for the local preview or your
own same-host `/game` proxy. Public CI runs compilation only and does not publish
the resulting website, client packages or cache. See [deployment](DEPLOYMENT.md).

The imported `check` task currently needs unpublished local fixtures
`ClientConfigurationFixture` and `GameMessageFormattingTest`. A clean clone cannot
run the full inherited regression suite without them. The build command above
compiles the maintained targets and verifies desktop packaging; it does not claim
that the full regression suite ran. Keep additional verification fixtures local
unless their publication is explicitly chosen.

Check the behavior relevant to the change: browser and desktop login, logout and
relogin, movement, map loading, packet-driven interfaces and input. Browser changes
also need cache startup, native display size, website controls and focus checks.
A successful compile or a loaded page does not establish working gameplay.

## Keeping your own fork current

A downstream fork can use ordinary Git fetches and reviewed merges from the
public client repository. First inspect `git remote -v`. If your fork has no
`upstream` remote, add it once:

```sh
git remote add upstream https://github.com/luna-rs/luna-client.git
```

Starting with a clean working tree, fetch and prepare an update branch:

```sh
git fetch origin master
git fetch upstream master
git switch -c feature/update-luna origin/master
git merge --no-ff --no-edit upstream/master
```

Resolve conflicts deliberately, including changes to your selected properties
profile. `git merge --abort` stops an unresolved merge without discarding the
original branch. Build and test before pushing an update branch and opening a
pull request into your fork. Replacing your maintained tree or force-pushing
`master` is unnecessary for this workflow.

Keep generated websites, local accounts, private keys, credentials and agent
verification output out of commits. Public client properties contain only public
configuration. Retain the notices under [`browser/web/legal/`](../browser/web/legal/).
