# Keeping the web client in sync

This is the independent private [Luna web client repository](https://github.com/luna-rs/luna-webclient).
Its `master` branch maintains Luna's browser integration and website. The public
[Luna desktop client](https://github.com/luna-rs/luna-client) is the upstream source
for shared client changes. The repositories retain common Git ancestry, so updates
can be merged while preserving this repository's browser work.

| Remote | Repository | Purpose |
| --- | --- | --- |
| `origin` | `https://github.com/luna-rs/luna-webclient.git` | Private web client; push feature branches and review pull requests here. |
| `upstream` | `https://github.com/luna-rs/luna-client.git` | Public desktop client; fetch updates from `master`. |

The imported history was pinned to:

- Desktop release source: `eedce45bb6deea24b174cc48f43565f3a2307acb` (`v0.5.0-alpha`).
- Browser prototype source: `e5d5316b418a76e259e88776038f664f4b21987a`, which includes the TeaVM integration and the fixed-mode native-display-pixel correction.

Those are provenance records. Later commits in this repository contain its own
website and integration changes. The original public commits remain available
upstream; new private commits should be pushed only to this repository.

## Update through GitHub

1. Open this repository's **Actions** tab and choose **Sync upstream client**.
2. Select **Run workflow**, choose `master`, and run the workflow.
3. Read the workflow result. When there are upstream changes and the merge is
   clean, it creates a `sync/upstream-...` branch and an update pull request in
   this private repository. Its validation job builds the browser client.
4. Review the shared client changes and any effects on the browser adapters.
   Build and test the update before merging the pull request into `master`.

The sync workflow prepares an update for review. It does not make conflict
resolution decisions for you. If it encounters conflicts, it aborts its local
merge and opens a draft pull request from an upstream update branch, with the
conflicting paths listed for human resolution. Use the local procedure below to
make a resolved update branch and pull request; link it from the draft if it
supersedes that draft. If organization policy prevents the workflow from opening
a pull request, use the comparison link in its result to open one yourself.

If upstream changes GitHub Actions workflow files, GitHub may refuse the sync
workflow's token when pushing those changes. Use the authenticated local merge
and push procedure below in that case; do not discard the workflow changes or
expand a stored token's permissions to bypass the refusal.

The sync updates release/source provenance for the selected upstream revision;
it preserves this repository's configured WebSocket endpoint. Do not replace the
private branch with the upstream tree or force push `master` to make conflicts
disappear.

## Update locally

Start with a clean working tree. Keep unfinished work on its own branch before
starting an upstream update. Check that the remotes point to the intended
repositories:

```sh
git remote -v
```

If this clone does not yet have the public remote, add it once:

```sh
git remote add upstream https://github.com/luna-rs/luna-client.git
```

Fetch both repositories and create a new update branch from the private version.
Replace the example date with a unique branch name:

```sh
git fetch origin master
git fetch upstream master
git switch -c codex/sync-upstream-YYYYMMDD origin/master
git merge --no-ff --no-edit upstream/master
```

When a merge conflicts, inspect `git status`, edit the conflicting files, and
stage only the resolved paths. Then finish the merge with `git commit`. To stop
an unresolved merge and retain the original branch state, use `git merge --abort`.
This workflow uses normal merges; it does not require rebasing or force pushes.

Build the desktop and browser targets with Java 21:

```sh
./gradlew allClientBuilds verifyBetaJar
```

The imported `check` task currently needs unpublished local fixtures
`ClientConfigurationFixture` and `GameMessageFormattingTest`. They are not tracked
in this repository, so a clean clone cannot run the full inherited regression
suite without those fixtures. The command above builds the maintained targets and
verifies beta packaging; it does not claim that full regression check passed.
Keep additional test fixtures local unless their publication is explicitly chosen.

On Windows PowerShell, use `./gradlew.bat` with the same tasks. A successful build
does not establish working gameplay: check browser and desktop login, logout and
relogin, movement and region loading, packet-driven interfaces, and the website's
input and display behavior against a development server.

Push the update branch to the private remote and open a pull request into its
`master` branch:

```sh
git push -u origin codex/sync-upstream-YYYYMMDD
```

Review the pull request before merging. Do not push private website changes to
`upstream` unless you deliberately intend to publish them there.

## Preserve the shared source boundary

The maintained game client lives in `src/`. The browser build generates its
shared input from those Java sources and substitutes platform adapters from
`browser/src/main/java/` for desktop-only facilities. Keep browser rendering,
input, networking, storage and audio adaptation there. The surrounding website
lives in `browser/web/`.

Do not create a second maintained `browser/src/main/java/client.java`. Shared
packet parsing, game logic, rendering and in-game interfaces should continue to
come from the common source. An upstream method signature or platform API change
can require a corresponding adapter or generation-rule update; resolve that
deliberately instead of copying the desktop implementation over the browser one.

Review endpoint configuration and deployment settings separately from source
merges. An upstream development address or temporary tunnel URL is not a suitable
replacement for this website's configured server. See [DEPLOYMENT.md](DEPLOYMENT.md)
for building and hosting the selected deployment.

Keep generated sites, caches extracted for testing, local accounts, credentials,
private keys and agent verification artifacts out of source commits. Required
upstream build inputs such as the cache seed and Gradle wrapper remain distinct
from generated build output. Retain the applicable notices in
[`browser/web/legal/`](../browser/web/legal/); those component notices do not assign
a blanket license to the inherited client or game assets.
