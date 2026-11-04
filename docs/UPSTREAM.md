# Maintaining the shared desktop and browser client

[Luna's client repository](https://github.com/luna-rs/luna-client) maintains both
build targets. The game server remains a separate project in
[luna-rs/luna](https://github.com/luna-rs/luna). The canonical browser website is
https://luna-rs.github.io/luna-client/.

## Make a shared client change

1. Create a feature branch from the current client `master`, preserving unfinished
   local work on its existing branch.
2. Change the common game implementation in `src/` where possible. Keep browser
   platform adaptation in `browser/src/main/java/` and the surrounding website in
   `browser/web/`.
3. Build the desktop and browser targets, then verify the affected actions against
   a development server.
4. Review and merge the change into this repository's `master`. Its configured
   **Build and publish webclient** workflow rebuilds and publishes the browser site.

Desktop and browser fixes no longer need to be copied between repositories. There
is no separate **Sync upstream client** action. A source fix still needs to compile
and work on both platforms; a desktop-only API can require an adapter change.

## Preserve the shared source boundary

The browser build derives generated Java input from `src/` and substitutes the
thin adapters under `browser/src/main/java/`. Packet handling, game logic, scene
rendering and in-game interfaces continue to come from the shared client.

Do not add a second maintained `browser/src/main/java/client.java` or edit generated
files under `browser/build/`. Update the shared source or the generation rule when
an upstream method signature or platform API changes. The desktop launcher stays
independent of the browser startup and process lifecycle.

## Configuration profiles

The default browser build selects root `client.properties`. Choose a different
complete root profile with `-PclientProperties=<filename>`. For example, in Windows
PowerShell:

```powershell
.\gradlew.bat :browser:browserSite "-PclientProperties=client (2).properties"
```

Connection, public RSA, branding and browser settings come from the selected file.
Gradle uses Java's properties parser and produces the copied properties snapshot
and a generated `client-config.json`. The build and browser startup check that
these files match. Do not edit generated descriptors or use them as another
configuration source.

Repository Actions variable `CLIENT_PROPERTIES` selects only a root profile
filename. The shared build defaults to `client.properties`; Luna's Pages deployment
selects its beta profile explicitly. The deployment guide describes the current
[properties and publication flow](DEPLOYMENT.md).

The cache seed is shared build input under `data/bootstrap/`. Its ZIP and hash
manifest must describe the same compatible cache. Build outputs extract and verify
that seed; changing an endpoint does not require another cache copy or a different
game client implementation.

## Verification

With Java 21, build both desktop executables and the browser targets:

```sh
./gradlew allClientBuilds verifyBetaJar
```

On Windows PowerShell, use `.\gradlew.bat` with the same tasks. To verify a selected
browser profile for Pages after packaging:

```sh
node browser/build-webclient.mjs
node browser/configuration.mjs --pages build/webclient-site
```

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
