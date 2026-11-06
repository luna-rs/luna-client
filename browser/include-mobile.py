"""
Adds only the separately built mobile runtime to a regular GitHub Pages package.

@author lare96
"""
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import stat
import sys
import tempfile
import urllib.error
import urllib.request
import zipfile
from io import BytesIO

REPOSITORY = "luna-rs/luna-client"
TAG = "v0.5.0-alpha"
NAME = "luna-mobile-web.zip"
MAX_ZIP = 256 * 1024 * 1024
MAX_EXPANDED = 512 * 1024 * 1024
SOURCE_FILES = {
    "index.html", "cache.js", "platform.js", "performance.js", "profiling.js", "client-loader.js",
    "luna-web.css", "luna-web.js", "mobile-boot.js", "mobile-input.js", "mobile.js", "mobile.css",
    "mobile.webmanifest", "js/client.js", "wasm/client.wasm", "wasm/client.wasm-runtime.js",
    "client.properties", "client-config.json", "cache/cache.sha256",
    *[f"assets/mobile/icon-{size}.png" for size in (180, 192, 512)],
    *[f"assets/mobile/tab-{index}.png" for index in range(13)],
    *[f"assets/theme/{name}" for name in ("stoneback.gif", "navbar.gif", "navbar_mainmenu.gif", "navbar_manual.gif", "navbar_rules.gif")],
    *[f"legal/{name}" for name in ("THIRD_PARTY_NOTICES.txt", "teavm-LICENSE.txt", "teavm-NOTICE.txt", "ws-LICENSE.txt")],
    "cache/main_file_cache.dat", *[f"cache/main_file_cache.idx{index}" for index in range(5)],
    *[f"assets/title/{season}/{image}.png" for season in ("christmas", "easter", "halloween") for image in ("background", "logo")],
}
FILES = SOURCE_FILES | {"pages-config.js", ".nojekyll", "release-provenance.json", "mobile.html"}
DIRECTORIES = {str(parent) for name in FILES for parent in PurePosixPath(name).parents if str(parent) != "."}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def digest(data):
    return hashlib.sha256(data).hexdigest()


def install_mobile(website, data, asset):
    require(website.is_dir() and not website.is_symlink(), "The regular website must be a real directory.")
    destination = website / "mobile"
    require(not destination.exists() and not destination.is_symlink(), "The mobile output must not already exist.")
    require(0 < len(data) <= MAX_ZIP and len(data) == asset.get("size")
            and "sha256:" + digest(data) == asset.get("digest"), "The public mobile ZIP failed checksum verification.")
    regular = json.loads((website / "client-config.json").read_bytes())
    with zipfile.ZipFile(BytesIO(data)) as archive:
        members = archive.infolist()
        require(len(members) <= 128 and sum(item.file_size for item in members) <= MAX_EXPANDED,
                "The mobile archive exceeds its extraction limits.")
        seen, files = set(), set()
        for item in members:
            name = item.filename
            require(item.orig_filename == name and name and "\\" not in name and ":" not in name
                    and not name.startswith("/") and not any(ord(character) < 32 for character in name)
                    and all(part not in ("", ".", "..") for part in name.removesuffix("/").split("/")),
                    "The mobile archive contains an unsafe path.")
            require(name.casefold() not in seen, "Duplicate mobile archive paths are not allowed.")
            seen.add(name.casefold())
            kind = stat.S_IFMT(item.external_attr >> 16)
            require(kind in (0, stat.S_IFDIR if item.is_dir() else stat.S_IFREG)
                    and not item.flag_bits & 1 and item.compress_type in (zipfile.ZIP_STORED, zipfile.ZIP_DEFLATED),
                    "The mobile archive contains an unsupported file type.")
            if item.is_dir():
                require(name[:-1] in DIRECTORIES and item.file_size == 0, "Unexpected mobile archive directory.")
            else:
                require(name in FILES and (item.file_size > 0 or name == ".nojekyll"), "Unexpected mobile runtime file.")
                files.add(name)
        require(files == FILES, "The mobile runtime package has missing files.")
        for name in ("release-provenance.json", "client-config.json", "mobile.webmanifest", "pages-config.js"):
            require(archive.getinfo(name).file_size <= 256 * 1024, "Oversized mobile package metadata.")
        provenance = json.loads(archive.read("release-provenance.json"))
        require(re.fullmatch(r"[a-f0-9]{40}", provenance.get("sourceCommit", ""))
                and provenance.get("mobileEntry") == "mobile.html", "The package has no mobile build provenance.")
        hashes = provenance.get("sourceAssets", {})
        require(set(hashes) == SOURCE_FILES, "The mobile runtime hash inventory differs.")
        for name in SOURCE_FILES - {"index.html"}:
            with archive.open(name) as stream:
                require(hashlib.file_digest(stream, "sha256").hexdigest() == hashes[name], "Mobile runtime hash mismatch: " + name)
        require(digest(archive.read("mobile.html")) == provenance.get("generatedAssets", {}).get("mobile.html"),
                "The mobile launcher hash differs.")
        config = json.loads(archive.read("client-config.json"))
        require(config.get("webOrigin") == regular.get("webOrigin") == "https://luna-rs.github.io"
                and config.get("websocketUrl") == regular.get("websocketUrl")
                and isinstance(config.get("websocketUrl"), str) and config["websocketUrl"].startswith("wss://"),
                "The mobile package must use the regular Pages origin and remote secure game gateway.")
        require(digest(archive.read("client.properties")) == config.get("propertiesSha256")
                == provenance.get("clientPropertiesSha256"), "The mobile connection properties checksum differs.")
        pages = archive.read("pages-config.js").decode("utf-8")
        header = re.match(r"window\.LUNA_CLIENT_CONFIG = (.+);\r?\n", pages)
        require(header and json.loads(header.group(1)) == config, "The mobile page connection configuration differs.")
        manifest = json.loads(archive.read("mobile.webmanifest"))
        require(manifest.get("start_url") == "./mobile.html" and manifest.get("scope") == "./"
                and manifest.get("id") == "./?mobile=1" and manifest.get("display") == "standalone"
                and manifest.get("orientation") == "landscape", "The mobile Home Screen launcher settings differ.")
        with tempfile.TemporaryDirectory(prefix=".mobile-site-", dir=website.parent) as staging:
            staged = Path(staging) / "mobile"
            staged.mkdir()
            for name in sorted(FILES):
                output = staged / name
                output.parent.mkdir(parents=True, exist_ok=True)
                with archive.open(name) as source, output.open("xb") as target:
                    shutil.copyfileobj(source, target, 1024 * 1024)
            staged.rename(destination)
    return {"mobile": True, "entry": "mobile/?mobile=1", "sourceCommit": provenance["sourceCommit"], "sha256": digest(data)}


class NoRedirect(urllib.request.HTTPRedirectHandler):
    """
    Keeps the workflow token on GitHub API requests only.

    @author lare96
    """

    def redirect_request(self, request, fp, code, message, headers, url):
        return None


def include_mobile(website):
    headers = {"Accept": "application/vnd.github+json", "User-Agent": "Luna-mobile-Pages-package"}
    if os.environ.get("GITHUB_TOKEN"):
        headers["Authorization"] = "Bearer " + os.environ["GITHUB_TOKEN"]
    url = "https://api.github.com/repos/" + REPOSITORY + "/releases/tags/" + TAG
    try:
        with urllib.request.build_opener(NoRedirect).open(urllib.request.Request(url, headers=headers), timeout=30) as response:
            release = json.loads(response.read(2 * 1024 * 1024))
    except urllib.error.HTTPError as error:
        if error.code == 404:
            return {"mobile": False, "reason": "No mobile release package is published."}
        raise ValueError("Unable to inspect the mobile release (HTTP " + str(error.code) + ").") from None
    require(release.get("tag_name") == TAG and release.get("draft") is False, "Unexpected mobile release metadata.")
    matches = [asset for asset in release.get("assets", []) if asset.get("name") == NAME]
    if not matches:
        return {"mobile": False, "reason": "No mobile release package is attached."}
    require(len(matches) == 1 and matches[0].get("state") == "uploaded", "The mobile release asset is ambiguous.")
    asset = matches[0]
    require(isinstance(asset.get("size"), int) and 0 < asset["size"] <= MAX_ZIP
            and re.fullmatch(r"sha256:[a-f0-9]{64}", asset.get("digest", "")), "Invalid mobile release asset metadata.")
    download = "https://github.com/" + REPOSITORY + "/releases/download/" + TAG + "/" + NAME
    require(asset.get("browser_download_url") == download, "Unexpected mobile download destination.")
    # This public download receives no GitHub token, including across signed asset redirects.
    with urllib.request.urlopen(urllib.request.Request(download, headers={"User-Agent": headers["User-Agent"]}), timeout=60) as response:
        data = response.read(MAX_ZIP + 1)
    return install_mobile(website, data, asset)


if __name__ == "__main__":
    try:
        require(len(sys.argv) == 2, "Usage: python3 browser/include-mobile.py <prepared-regular-website>")
        print(json.dumps(include_mobile(Path(sys.argv[1]).resolve())))
    except Exception:
        print("Mobile Pages packaging failed validation or download; the regular website was not overwritten.", file=sys.stderr)
        raise SystemExit(1) from None
