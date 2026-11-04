/**
 * Prepares reviewable upstream merges while preserving this repository's browser integration.
 *
 * @author lare96
 */
import {spawnSync} from 'node:child_process';
import {appendFile, mkdir, readFile, writeFile} from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const upstreamRepository = 'luna-rs/luna-client';
const upstreamUrl = `https://github.com/${upstreamRepository}.git`;
const baseBranch = 'master';

function git(cwd, args, allowFailure = false) {
    const result = spawnSync('git', args, {cwd, encoding: 'utf8', maxBuffer: 4 * 1024 * 1024});
    if (result.error) throw result.error;
    if (result.status !== 0 && !allowFailure) {
        throw new Error(`Git ${args[0]} failed: ${(result.stderr || result.stdout || '').trim().slice(-4000)}`);
    }
    return {code: result.status, stdout: result.stdout.trim(), stderr: result.stderr.trim()};
}

/**
 * Creates a clean merge candidate, or records a conflicting upstream head for a draft pull request.
 * The maintained master ref is never changed, and unresolved merge state is always aborted.
 *
 * @param {object} options The isolated checkout and explicitly selected upstream.
 * @returns {Promise<object>} The candidate revision and any merge conflicts.
 */
export async function prepareCandidate({cwd, upstream = upstreamUrl, suffix = 'local'}) {
    if (!/^[a-zA-Z0-9-]{1,80}$/.test(suffix)) throw new Error('The sync suffix must be a short identifier');
    if (git(cwd, ['status', '--porcelain', '--untracked-files=normal']).stdout) {
        throw new Error('Sync requires a clean checkout; existing local changes were preserved');
    }
    git(cwd, ['fetch', '--no-tags', 'origin', `refs/heads/${baseBranch}:refs/remotes/origin/${baseBranch}`]);
    const baseCommit = git(cwd, ['rev-parse', `refs/remotes/origin/${baseBranch}`]).stdout;
    git(cwd, ['fetch', '--no-tags', '--', upstream, `refs/heads/${baseBranch}`]);
    const upstreamCommit = git(cwd, ['rev-parse', 'FETCH_HEAD']).stdout;
    if (![baseCommit, upstreamCommit].every(value => /^[a-f0-9]{40}$/.test(value))) {
        throw new Error('Both repositories must resolve to complete Git commits');
    }
    if (git(cwd, ['merge-base', '--is-ancestor', upstreamCommit, baseCommit], true).code === 0) {
        return {status: 'current', baseCommit, upstreamCommit};
    }
    if (git(cwd, ['merge-base', baseCommit, upstreamCommit], true).code !== 0) {
        throw new Error('The maintained client and upstream must retain shared Git history');
    }
    const branch = `sync/upstream-${upstreamCommit.slice(0, 12)}-${suffix}`;
    git(cwd, ['check-ref-format', '--branch', branch]);
    git(cwd, ['checkout', '-b', branch, baseCommit]);
    git(cwd, ['config', 'user.name', 'github-actions[bot]']);
    git(cwd, ['config', 'user.email', '41898282+github-actions[bot]@users.noreply.github.com']);
    const merge = git(cwd, ['merge', '--no-ff', '--no-edit', upstreamCommit], true);
    if (merge.code !== 0) {
        const conflicts = git(cwd, ['diff', '--name-only', '--diff-filter=U', '-z']).stdout
            .split('\0').filter(Boolean);
        if (git(cwd, ['rev-parse', '--verify', '-q', 'MERGE_HEAD'], true).code === 0) {
            git(cwd, ['merge', '--abort']);
        }
        if (!conflicts.length) throw new Error(`Upstream could not be merged: ${merge.stderr.slice(-2000)}`);
        return {status: 'conflicts', baseCommit, upstreamCommit, branch,
            candidateCommit: upstreamCommit, conflicts};
    }
    const configPath = path.join(cwd, 'browser/pages-config.json');
    const config = JSON.parse(await readFile(configPath, 'utf8'));
    config.sourceCommit = upstreamCommit;
    config.release = `upstream-${upstreamCommit.slice(0, 12)}`;
    await writeFile(configPath, JSON.stringify(config, null, 2) + '\n');
    if (git(cwd, ['diff', '--quiet', '--', 'browser/pages-config.json'], true).code !== 0) {
        git(cwd, ['add', '--', 'browser/pages-config.json']);
        git(cwd, ['commit', '-m', `Record upstream client ${upstreamCommit.slice(0, 12)}`]);
    }
    return {status: 'merged', baseCommit, upstreamCommit, branch,
        candidateCommit: git(cwd, ['rev-parse', 'HEAD']).stdout, conflicts: []};
}

async function api(repository, token, method, resource, body) {
    const response = await fetch(`https://api.github.com/repos/${repository}${resource}`, {
        method,
        headers: {Authorization: `Bearer ${token}`, Accept: 'application/vnd.github+json', 'Content-Type': 'application/json',
            'X-GitHub-Api-Version': '2022-11-28'},
        body: body === undefined ? undefined : JSON.stringify(body),
        signal: AbortSignal.timeout(30000)
    });
    const text = await response.text();
    let result;
    try { result = JSON.parse(text); } catch { result = {}; }
    if (!response.ok) throw new Error(`GitHub ${method} ${resource} returned ${response.status}: ${result.message || 'request failed'}`);
    return result;
}

/**
 * Pushes only the prepared review branch and opens a pull request in this repository.
 *
 * @param {object} options The candidate and the workflow's own repository token.
 * @returns {Promise<object>} Pull request or manual compare information.
 */
export async function publishCandidate({cwd, candidate, repository, token, runUrl}) {
    if (!/^[a-zA-Z0-9_.-]+\/[a-zA-Z0-9_.-]+$/.test(repository)) throw new Error('Invalid workflow repository');
    if (!token) throw new Error('The workflow requires its own GITHUB_TOKEN');
    if (candidate.status === 'current') return candidate;
    const pulls = await api(repository, token, 'GET', `/pulls?state=open&base=${baseBranch}&per_page=100`);
    const existing = pulls.find(pull => pull.head?.ref?.startsWith(`sync/upstream-${candidate.upstreamCommit.slice(0, 12)}-`)
        && pull.body?.includes(`https://github.com/${upstreamRepository}/commit/${candidate.upstreamCommit}`));
    if (existing) {
        return {...candidate, status: 'existing', branch: existing.head.ref,
            candidateCommit: existing.head.sha, pullRequestUrl: existing.html_url};
    }
    git(cwd, ['push', 'origin', `${candidate.candidateCommit}:refs/heads/${candidate.branch}`]);
    const compareUrl = `https://github.com/${repository}/compare/${baseBranch}...${candidate.branch}?expand=1`;
    const conflicted = candidate.status === 'conflicts';
    const body = [
        `Upstream: https://github.com/${upstreamRepository}/commit/${candidate.upstreamCommit}`,
        `Webclient base: ${candidate.baseCommit}`,
        '',
        conflicted
            ? 'The automatic merge found conflicts. This draft contains the clean upstream head; resolve its conflicts against master before merging.'
            : 'This branch contains a normal merge with the maintained webclient. Its browser build is validated in the originating Sync upstream client workflow.',
        'The maintained master branch has not been changed. Review this pull request and merge it yourself.',
        'A human merge to master starts Build and publish webclient. It always provides a downloadable static website.',
        ...(runUrl ? [`Candidate build and sync result: ${runUrl}`] : []),
        '',
        ...(conflicted ? ['Conflicting paths:', ...candidate.conflicts.slice(0, 50).map(name => `- ${JSON.stringify(name)}`), '',
            'Do not replace master with upstream or discard the browser integration to resolve these conflicts.']
            : ['The deployment config changes only its recorded upstream source/release label; the endpoint in browser/pages-config.json is preserved.'])
    ].join('\n');
    try {
        const pull = await api(repository, token, 'POST', '/pulls', {
            title: `Sync upstream client ${candidate.upstreamCommit.slice(0, 12)}${conflicted ? ' (resolve conflicts)' : ''}`,
            head: candidate.branch, base: baseBranch, body, draft: conflicted
        });
        return {...candidate, pullRequestUrl: pull.html_url, compareUrl};
    } catch (failure) {
        return {...candidate, compareUrl, pullRequestBlocked: true, pullRequestError: failure.message};
    }
}

async function record(result, environment) {
    if (environment.GITHUB_OUTPUT) {
        await appendFile(environment.GITHUB_OUTPUT, [
            `status=${result.status}`, `branch=${result.branch || ''}`,
            `candidate_sha=${result.candidateCommit || ''}`,
            `pull_request_url=${result.pullRequestUrl || ''}`, ''
        ].join('\n'));
    }
    const summary = result.status === 'current'
        ? ['## Upstream client is current', '', `Already includes ${result.upstreamCommit}.`, '']
        : ['## Upstream client prepared for review', '',
            result.pullRequestUrl ? `[Open the pull request](${result.pullRequestUrl})` : `[Open a pull request manually](${result.compareUrl})`,
            '', `Status: ${result.status}. Master was preserved.`,
            result.status === 'existing'
                ? 'An open pull request already tracks this upstream revision. Continue reviewing that pull request instead of creating a duplicate.'
                : result.status === 'merged'
                ? 'The candidate browser build runs below. Review its result, then merge the pull request.'
                : 'Resolve the listed conflicts in the draft pull request before merging. No conflicting merge was committed.',
            ...(result.pullRequestBlocked ? ['', `Automatic PR creation was blocked: ${result.pullRequestError}`,
                'Check Settings → Actions → General → Allow GitHub Actions to create and approve pull requests, or use the compare link above.'] : []), ''];
    if (environment.GITHUB_STEP_SUMMARY) await appendFile(environment.GITHUB_STEP_SUMMARY, summary.join('\n'));
    await mkdir('build', {recursive: true});
    await writeFile('build/upstream-sync.json', JSON.stringify(result, null, 2) + '\n');
    console.log(JSON.stringify(result, null, 2));
    if (result.pullRequestBlocked) console.warn('::warning::The sync branch is ready, but automatic pull request creation was blocked. Use the workflow summary compare link.');
}

async function main() {
    const environment = process.env;
    const repository = environment.GITHUB_REPOSITORY;
    if (!repository || !environment.GH_TOKEN) throw new Error('Run this script through the Sync upstream client workflow');
    if (environment.GITHUB_REF !== 'refs/heads/master') throw new Error('Select master when starting the sync workflow');
    const expectedOrigin = `https://github.com/${repository}`;
    const origin = git(process.cwd(), ['remote', 'get-url', 'origin']).stdout;
    if (origin.replace(/\.git$/, '') !== expectedOrigin) throw new Error('The push remote must be this workflow repository');
    const suffix = `${environment.GITHUB_RUN_ID}-${environment.GITHUB_RUN_ATTEMPT || '1'}`;
    const candidate = await prepareCandidate({cwd: process.cwd(), suffix});
    const runUrl = `https://github.com/${repository}/actions/runs/${environment.GITHUB_RUN_ID}`;
    await record(await publishCandidate({cwd: process.cwd(), candidate, repository, token: environment.GH_TOKEN, runUrl}), environment);
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    main().catch(failure => {
        const token = process.env.GH_TOKEN;
        console.error(token ? failure.message.replaceAll(token, '[redacted]') : failure.message);
        process.exitCode = 1;
    });
}
