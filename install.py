#!/usr/bin/env python3
"""Arieslinter auto-installer (Linux, macOS and Windows).

Usage:
    Linux/macOS: curl -sSL https://raw.githubusercontent.com/viRafael/arieslinter/main/install.py | python3
    Windows:     curl -sSL -o install.py https://raw.githubusercontent.com/viRafael/arieslinter/main/install.py && python install.py
"""
import sys
import os
import argparse
import hashlib
import tempfile
import shutil
import subprocess
import urllib.parse
import urllib.request
import json
import re
import xml.etree.ElementTree as ET
from xml.dom import minidom

IS_WIN = sys.platform.startswith("win")
IS_MAC = sys.platform == "darwin"
PLATFORM = "win" if IS_WIN else ("mac" if IS_MAC else "linux")

# If piped (e.g. curl ... | python3), reconnect stdin to the interactive terminal
try:
    if not sys.stdin.isatty():
        sys.stdin = open('CON' if IS_WIN else '/dev/tty', 'r')
except Exception:
    pass

# Make sure unicode output never crashes on legacy Windows consoles
for _stream in (sys.stdout, sys.stderr):
    try:
        _stream.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

# Enable ANSI escape sequences on Windows 10+
if IS_WIN:
    os.system("")

CHECKSTYLE_VERSION = "10.23.1"   # same version the Arieslinter JAR is compiled against (pom.xml)
CHECKSTYLE_SHA256 = "6c5d14f922e356808b4d92db76d172f7c1d9b582bbbb0d337b0da01ae8ac747f"
MIN_JAVA = 11                    # minimum Java required by Checkstyle 10.x

# ANSI Colors
COLOR_TITLE = "\033[1;38;5;45m"     # Cyan
COLOR_ACCENT = "\033[1;38;5;198m"   # Pink/Magenta
COLOR_SUCCESS = "\033[1;38;5;82m"   # Green
COLOR_WARN = "\033[1;38;5;220m"     # Yellow
COLOR_ERROR = "\033[1;38;5;196m"    # Red
COLOR_INFO = "\033[1;38;5;51m"      # Sky Blue
COLOR_MUTED = "\033[38;5;244m"      # Gray
COLOR_RESET = "\033[0m"

# Icons
if IS_WIN:
    ICON_CHECK, ICON_CROSS, ICON_WARN, ICON_INFO = "OK", "ERROR", "WARN", "INFO"
else:
    ICON_CHECK, ICON_CROSS, ICON_WARN, ICON_INFO = "✔", "✘", "⚠", "ℹ"

OK = f"[{ICON_CHECK}]"
ERR = f"[{ICON_CROSS}]"
WARN = f"[{ICON_WARN}]"
INFO = f"[{ICON_INFO}]"

# Default checkstyle.xml contents
CHECKSTYLE_XML_CONTENT = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN"
        "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <module name="TreeWalker">
        <!-- Register Arieslinter custom checks -->
        <module name="br.ufba.arieslinter.checks.AssertionRouletteTestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.ConditionalTestLogicCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.ConstructorInitializationCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.DuplicateAssertCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.EmptyTestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.ExceptionHandlingCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.GeneralFixtureCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.IgnoredTestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.MagicNumberCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.MysteryGuestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.RedundantAssertionCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.RedundantPrintCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.ResourceOptimismCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.SensitiveEqualityCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.SleepyTestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.UnknownTestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.VerboseTestCheck"><property name="severity" value="warning"/></module>
    </module>
</module>
"""

# ---------------------------------------------------------------------------
# IDE table (data driven). To support a new VS Code fork, add one entry here.
# ---------------------------------------------------------------------------
MICROSOFT_MARKETPLACE = "microsoft"
OPEN_VSX = "openvsx"

# Extensions required per marketplace. The Microsoft "Checkstyle for Java"
# extension is NOT published on Open VSX, so forks that use Open VSX need
# a-st.checkstyle-vscode, which is a fork exposing the same java.checkstyle.* settings.
EXTENSIONS_BY_MARKETPLACE = {
    MICROSOFT_MARKETPLACE: {
        "vscjava.vscode-java-pack": "Extension Pack for Java (Microsoft)",
        "shengchen.vscode-checkstyle": "Checkstyle for Java",
    },
    OPEN_VSX: {
        "vscjava.vscode-java-pack": "Extension Pack for Java",
        "a-st.checkstyle-vscode": "Checkstyle for Java (Open VSX)",
    },
}

# Each entry: config dir ("Code" folder name inside the per-OS app data dir),
# home extensions dir, CLI candidates and the default marketplace.
VSCODE_FAMILY = [
    {"id": "code", "name": "VS Code", "cli": ["code"], "config": "Code",
     "ext_dir": "~/.vscode/extensions", "marketplace": MICROSOFT_MARKETPLACE},
    {"id": "code-insiders", "name": "VS Code Insiders", "cli": ["code-insiders"], "config": "Code - Insiders",
     "ext_dir": "~/.vscode-insiders/extensions", "marketplace": MICROSOFT_MARKETPLACE},
    {"id": "code-oss", "name": "Code - OSS", "cli": ["code-oss"], "config": "Code - OSS",
     "ext_dir": "~/.vscode-oss/extensions", "marketplace": OPEN_VSX},
    {"id": "vscodium", "name": "VSCodium", "cli": ["codium", "vscodium"], "config": "VSCodium",
     "ext_dir": "~/.vscode-oss/extensions", "marketplace": OPEN_VSX},
    {"id": "vscodium-insiders", "name": "VSCodium Insiders", "cli": ["codium-insiders"], "config": "VSCodium - Insiders",
     "ext_dir": "~/.vscode-oss-insiders/extensions", "marketplace": OPEN_VSX},
    {"id": "cursor", "name": "Cursor", "cli": ["cursor"], "config": "Cursor",
     "ext_dir": "~/.cursor/extensions", "marketplace": OPEN_VSX},
    {"id": "windsurf", "name": "Windsurf", "cli": ["windsurf"], "config": "Windsurf",
     "ext_dir": "~/.windsurf/extensions", "marketplace": OPEN_VSX},
]

# Candidate roots where a VS Code build keeps resources/app/product.json
# (used to tell whether a given binary is the Microsoft build or a fork).
PRODUCT_JSON_RELATIVE = [
    "resources/app/product.json",
    "../resources/app/product.json",
    "../share/code/resources/app/product.json",
    "../lib/vscode/resources/app/product.json",
    "../../resources/app/product.json",
    "../Resources/app/product.json",
    "../../Resources/app/product.json",
]


KNOWN_APP_ROOTS = ["/usr/share/code", "/opt/visual-studio-code", "/usr/lib/code",
                   "/usr/share/codium", "/opt/vscodium-bin", "/usr/lib/code-oss",
                   "/usr/lib/code-insiders", "/opt/code-oss", "/usr/share/code-oss"]


def app_data_dir():
    """Per-OS directory where VS Code style editors keep their config."""
    if IS_WIN:
        return os.environ.get("APPDATA") or os.path.expanduser("~/AppData/Roaming")
    if IS_MAC:
        return os.path.expanduser("~/Library/Application Support")
    return os.environ.get("XDG_CONFIG_HOME") or os.path.expanduser("~/.config")


def find_cli(ide):
    """Return (command_name, resolved_path) of the first CLI found in PATH."""
    for cmd in ide["cli"]:
        path = shutil.which(cmd)
        if path:
            return cmd, path
    return None, None


def detect_marketplace(ide, cli_cmd, cli_path):
    """Best-effort: read product.json of the installed build to learn which
    extension gallery it uses. Only trusts a product.json whose applicationName
    matches the CLI command. Falls back to the default of the IDE entry."""
    default = ide["marketplace"]
    try:
        bin_dir = os.path.dirname(os.path.realpath(cli_path))
        candidates = [os.path.join(bin_dir, rel) for rel in PRODUCT_JSON_RELATIVE]
        # Wrapper scripts (e.g. /usr/bin/code) live far from the app: try common install roots
        candidates += [os.path.join(r, "resources/app/product.json") for r in KNOWN_APP_ROOTS]
        for cand in candidates:
            if not os.path.isfile(cand):
                continue
            with open(cand, "r", encoding="utf-8") as f:
                product = json.load(f)
            if product.get("applicationName") not in ide["cli"] + [cli_cmd]:
                continue
            url = (product.get("extensionsGallery") or {}).get("serviceUrl", "")
            if "open-vsx" in url:
                return OPEN_VSX
            if "marketplace.visualstudio.com" in url:
                return MICROSOFT_MARKETPLACE
            return default
    except Exception:
        pass
    return default


def detect_vscode_ides():
    """Return the VS Code family editors found on this machine."""
    found = []
    base = app_data_dir()
    for ide in VSCODE_FAMILY:
        cmd, path = find_cli(ide)
        config_dir = os.path.join(base, ide["config"])
        if not path and not os.path.isdir(config_dir):
            continue
        item = dict(ide)
        item["kind"] = "vscode"
        item["cli_cmd"] = cmd            # None when only the config dir exists
        item["cli_path"] = path
        item["config_dir"] = config_dir
        item["marketplace"] = detect_marketplace(ide, cmd, path) if path else ide["marketplace"]
        item["required"] = dict(EXTENSIONS_BY_MARKETPLACE[item["marketplace"]])
        found.append(item)
    return found


def jetbrains_roots():
    if IS_WIN:
        roots = [os.environ.get("APPDATA"), os.environ.get("LOCALAPPDATA")]
        return [os.path.join(r, "JetBrains") for r in roots if r]
    if IS_MAC:
        return [os.path.expanduser("~/Library/Application Support/JetBrains")]
    return [os.path.expanduser("~/.local/share/JetBrains"), os.path.expanduser("~/.config/JetBrains")]


def detect_intellij():
    for base in jetbrains_roots():
        if os.path.isdir(base):
            return {"kind": "intellij", "id": "intellij", "name": "IntelliJ IDEA / JetBrains",
                    "plugin_installed": check_intellij_plugin()}
    return None


def print_header():
    os.system('cls' if IS_WIN else 'clear')
    print(f"{COLOR_TITLE}┌────────────────────────────────────────────────────────┐")
    print(f"│              {COLOR_ACCENT}Arieslinter Auto-Installer 🚀              {COLOR_TITLE}│")
    print(f"│     Real-Time Static Analysis for Java Test Smells     │")
    print(f"└────────────────────────────────────────────────────────┘{COLOR_RESET}\n")


def get_vscode_profile(ide, project_path):
    if not ide.get("config_dir") or not os.path.exists(ide["config_dir"]):
        return None
    proj_uri = "file://" + urllib.parse.quote(project_path.replace("\\", "/"))
    if proj_uri.startswith("file:////"):
        proj_uri = "file:///" + proj_uri[9:]
    elif proj_uri.startswith("file://") and not proj_uri.startswith("file:///"):
        proj_uri = "file:///" + proj_uri[7:]
    storage_json_path = os.path.join(ide["config_dir"], "User", "globalStorage", "storage.json")
    if os.path.exists(storage_json_path):
        try:
            with open(storage_json_path, 'r', encoding='utf-8') as f:
                storage_data = json.load(f)
            workspaces = storage_data.get("profileAssociations", {}).get("workspaces", {})
            for wk_uri, prof_id in workspaces.items():
                if wk_uri.lower() == proj_uri.lower():
                    for p in storage_data.get("userDataProfiles", []):
                        if p.get("location") == prof_id:
                            return p.get("name")
                    return prof_id
        except Exception:
            pass
    return None


def run_cli(ide, extra_args, capture_text=True):
    """Run the IDE CLI. Uses shell on Windows because the CLI is a .cmd wrapper."""
    args = [ide["cli_cmd"]] + extra_args
    if IS_WIN:
        cmd = subprocess.list2cmdline(args)
        return subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=capture_text)
    return subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=capture_text)


def check_vscode_extensions(ide, profile_name=None):
    """Return the required extensions that are missing for this IDE."""
    missing = dict(ide["required"])
    installed = set()
    if ide.get("cli_cmd"):
        try:
            extra = ["--list-extensions"]
            if profile_name:
                extra += ["--profile", profile_name]
            res = run_cli(ide, extra)
            if res.returncode == 0:
                installed = {l.strip().lower() for l in res.stdout.splitlines() if l.strip()}
        except Exception:
            pass

    if installed:
        for ext_id in list(missing):
            if ext_id.lower() in installed:
                missing.pop(ext_id)
        return missing

    # Fallback: scan the extensions directory of this IDE
    d = os.path.expanduser(ide["ext_dir"])
    if os.path.isdir(d):
        try:
            for entry in os.listdir(d):
                for ext_id in list(missing):
                    if ext_id.lower() in entry.lower():
                        missing.pop(ext_id, None)
        except Exception:
            pass
    return missing


def get_disabled_vscode_extensions(ide):
    disabled = set()
    base = ide.get("config_dir")
    if not base or not os.path.exists(base):
        return disabled
    try:
        import sqlite3
        db_paths = [os.path.join(base, "User", "globalStorage", "state.vscdb")]
        profiles_dir = os.path.join(base, "User", "profiles")
        if os.path.isdir(profiles_dir):
            for entry in os.listdir(profiles_dir):
                p_dir = os.path.join(profiles_dir, entry)
                if os.path.isdir(p_dir):
                    db_paths.append(os.path.join(p_dir, "globalStorage", "state.vscdb"))
        for db_path in db_paths:
            if not os.path.exists(db_path):
                continue
            try:
                conn = sqlite3.connect(db_path)
                row = conn.execute("SELECT value FROM ItemTable WHERE key = 'extensionsIdentifiers/disabled'").fetchone()
                if row:
                    for item in json.loads(row[0]):
                        if item.get("id"):
                            disabled.add(item["id"].lower())
                conn.close()
            except Exception:
                pass
    except Exception:
        pass
    return disabled


def print_disabled_extensions_warning(ide_name, installed_disabled):
    if not installed_disabled:
        return
    w = 64
    print(f"\n{COLOR_ERROR}┌" + "─" * w + f"┐{COLOR_RESET}")
    print(f"{COLOR_ERROR}│ " + f"ATTENTION: DISABLED EXTENSION(S) IN {ide_name}".upper().ljust(w - 2) + f" │{COLOR_RESET}")
    print(f"{COLOR_ERROR}├" + "─" * w + f"┤{COLOR_RESET}")
    for ext_id, ext_name in installed_disabled:
        print(f"{COLOR_ERROR}│ " + f"  • {ext_name} ({ext_id})".ljust(w - 2) + f" │{COLOR_RESET}")
    print(f"{COLOR_ERROR}│ " + "".ljust(w - 2) + f" │{COLOR_RESET}")
    print(f"{COLOR_ERROR}│ " + "Open the Extensions tab (Ctrl+Shift+X) and click 'Enable'.".ljust(w - 2) + f" │{COLOR_RESET}")
    print(f"{COLOR_ERROR}└" + "─" * w + f"┘{COLOR_RESET}\n")


def install_vscode_extensions(ide, extensions_to_install, profile_name=None):
    installed_any = False
    remaining = dict(extensions_to_install)
    if not ide.get("cli_cmd"):
        print(f"{COLOR_WARN}{WARN} No '{ide['cli'][0]}' command found in PATH for {ide['name']}; cannot install automatically.{COLOR_RESET}")
    else:
        for ext_id, ext_name in list(remaining.items()):
            print(f"{COLOR_INFO}{INFO} Installing {ext_name} on {ide['name']} via {ide['cli_cmd']}...{COLOR_RESET}")
            extra = ["--install-extension", ext_id]
            if profile_name:
                extra += ["--profile", profile_name]
            res = run_cli(ide, extra)
            if res.returncode == 0:
                print(f"{COLOR_SUCCESS}{OK} {ext_name} installed successfully.{COLOR_RESET}")
                remaining.pop(ext_id, None)
                installed_any = True
            else:
                print(f"{COLOR_ERROR}{ERR} Failed to install {ext_name} on {ide['name']}.{COLOR_RESET}")
                err = (res.stderr or res.stdout or "").strip()
                if err:
                    print(f"{COLOR_MUTED}      {err.splitlines()[-1]}{COLOR_RESET}")
    if remaining:
        print(f"{COLOR_WARN}{WARN} Please install manually in {ide['name']}:{COLOR_RESET}")
        for ext_id, ext_name in remaining.items():
            print(f"   - {ext_name} ({ext_id})")
    return installed_any


def check_intellij_plugin():
    for base in jetbrains_roots():
        if os.path.exists(base):
            for root, dirs, files in os.walk(base):
                for d in dirs:
                    if "checkstyle-idea" in d.lower():
                        return True
    return False


# ---------------------------------------------------------------------------
# Honest reporting: every step records its real outcome here
# ---------------------------------------------------------------------------
REPORT = []   # list of (status, label, detail) where status is ok | warn | fail


def report(status, label, detail=""):
    REPORT.append((status, label, detail))


def aries_home():
    return os.environ.get("ARIESLINTER_HOME") or os.path.expanduser(os.path.join("~", ".arieslinter"))


def aries_jar_path():
    if os.environ.get("ARIESLINTER_HOME"):
        return os.path.join(aries_home(), "arieslinter.jar")
    return os.path.expanduser(os.path.join("~", ".m2", "repository", "br", "ufba", "arieslinter", "1.0", "arieslinter-1.0.jar"))


def checkstyle_jar_path():
    return os.path.join(aries_home(), f"checkstyle-{CHECKSTYLE_VERSION}-all.jar")


# ---------------------------------------------------------------------------
# Java
# ---------------------------------------------------------------------------
def find_java():
    """Return the java executable to use (JAVA_HOME first, then PATH) or None."""
    java_home = os.environ.get("JAVA_HOME")
    if java_home:
        cand = os.path.join(java_home, "bin", "java.exe" if IS_WIN else "java")
        if os.path.isfile(cand):
            return cand
    return shutil.which("java")


def parse_java_major(version_output):
    """'openjdk version "17.0.1"' -> 17 ; '"1.8.0_292"' -> 8 ; '"11"' -> 11. None if unknown."""
    m = re.search(r'version "(\d+)(?:\.(\d+))?', version_output or "")
    if not m:
        return None
    major = int(m.group(1))
    if major == 1 and m.group(2):
        return int(m.group(2))
    return major


def check_java():
    java = find_java()
    if not java:
        print(f"{COLOR_ERROR}{ERR} Java was NOT found (PATH/JAVA_HOME). Install JDK/JRE {MIN_JAVA}+ .{COLOR_RESET}")
        report("fail", "Java", f"not found; install Java {MIN_JAVA} or newer")
        return None
    try:
        res = subprocess.run([java, "-version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=30)
        major = parse_java_major(res.stderr + res.stdout)
    except Exception as e:
        print(f"{COLOR_ERROR}{ERR} Could not run '{java} -version': {e}{COLOR_RESET}")
        report("fail", "Java", f"could not run {java}")
        return None
    if major is None:
        print(f"{COLOR_WARN}{WARN} Could not determine the Java version of {java}.{COLOR_RESET}")
        report("warn", "Java", "version could not be determined")
        return java
    if major < MIN_JAVA:
        print(f"{COLOR_ERROR}{ERR} Java {major} found, but Arieslinter needs Java {MIN_JAVA}+ (Checkstyle {CHECKSTYLE_VERSION}).{COLOR_RESET}")
        report("fail", "Java", f"version {major} is too old (need {MIN_JAVA}+)")
        return None
    print(f"{COLOR_SUCCESS}{OK} Java {major} found ({java}).{COLOR_RESET}")
    report("ok", "Java", f"version {major}")
    return java


# ---------------------------------------------------------------------------
# Downloads (with SHA-256 verification)
# ---------------------------------------------------------------------------
def http_get(url, timeout=60):
    req = urllib.request.Request(url, headers={'User-Agent': 'arieslinter-installer'})
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return resp.read()


def sha256_file(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def download_verified(url, target, expected_sha256=None):
    """Download to target atomically. Reuses an existing file whose hash matches.
    Returns (ok, detail)."""
    if expected_sha256 and os.path.isfile(target) and sha256_file(target) == expected_sha256:
        return True, "already present, checksum OK"
    os.makedirs(os.path.dirname(target), exist_ok=True)
    tmp = target + ".part"
    try:
        data = http_get(url, timeout=120)
        with open(tmp, "wb") as f:
            f.write(data)
        got = hashlib.sha256(data).hexdigest()
        if expected_sha256 and got != expected_sha256:
            os.remove(tmp)
            return False, f"checksum mismatch (expected {expected_sha256[:12]}..., got {got[:12]}...)"
        os.replace(tmp, target)
        return True, "downloaded, checksum OK" if expected_sha256 else "downloaded (no checksum published)"
    except Exception as e:
        try:
            os.remove(tmp)
        except OSError:
            pass
        return False, str(e)


def download_arieslinter_jar(target):
    print(f"{COLOR_INFO}{INFO} Fetching latest Arieslinter release from GitHub...{COLOR_RESET}")
    try:
        data = json.loads(http_get("https://api.github.com/repos/viRafael/arieslinter/releases/latest").decode("utf-8"))
        asset = next((a for a in data.get("assets", []) if a.get("name", "").endswith(".jar")), None)
        if not asset:
            raise RuntimeError("no .jar asset in the latest GitHub release")
        digest = (asset.get("digest") or "")
        sha = digest.split(":", 1)[1] if digest.startswith("sha256:") else None
        print(f"{COLOR_INFO}{INFO} Downloading {asset['name']} ({data.get('tag_name')})...{COLOR_RESET}")
        ok, detail = download_verified(asset["browser_download_url"], target, sha)
    except Exception as e:
        ok, detail = False, str(e)
    if ok:
        print(f"{COLOR_SUCCESS}{OK} Arieslinter JAR: {target} ({detail}){COLOR_RESET}")
        report("ok", "Arieslinter JAR", detail)
    else:
        print(f"{COLOR_ERROR}{ERR} Arieslinter JAR download failed: {detail}{COLOR_RESET}")
        report("fail", "Arieslinter JAR", detail)
    return ok


def download_checkstyle_jar(target):
    url = (f"https://github.com/checkstyle/checkstyle/releases/download/"
           f"checkstyle-{CHECKSTYLE_VERSION}/checkstyle-{CHECKSTYLE_VERSION}-all.jar")
    print(f"{COLOR_INFO}{INFO} Checkstyle {CHECKSTYLE_VERSION} (used for the verification step)...{COLOR_RESET}")
    ok, detail = download_verified(url, target, CHECKSTYLE_SHA256)
    if ok:
        print(f"{COLOR_SUCCESS}{OK} Checkstyle JAR: {target} ({detail}){COLOR_RESET}")
        report("ok", f"Checkstyle {CHECKSTYLE_VERSION} JAR", detail)
    else:
        print(f"{COLOR_ERROR}{ERR} Checkstyle download failed: {detail}{COLOR_RESET}")
        report("fail", f"Checkstyle {CHECKSTYLE_VERSION} JAR", detail)
    return ok


# ---------------------------------------------------------------------------
# Smoke test: run the real engine + Arieslinter checks on a sample file
# ---------------------------------------------------------------------------
SMOKE_SAMPLE = """package smoke;

import org.junit.jupiter.api.Test;

class SmokeTest {
    @Test
    void sleepy() throws Exception {
        Thread.sleep(500);
    }

    @Test
    void empty() {
    }
}
"""
SMOKE_EXPECTED = ["SleepyTest", "EmptyTest"]


def smoke_test(java, aries_jar, cs_jar):
    """Runs Checkstyle from the command line with the Arieslinter checks.
    Proves Java + engine + JAR work together (independent of any IDE)."""
    print(f"{COLOR_INFO}{INFO} Running verification (Checkstyle CLI + Arieslinter on a sample file)...{COLOR_RESET}")
    tmp = tempfile.mkdtemp(prefix="arieslinter-smoke-")
    try:
        sample = os.path.join(tmp, "SmokeTest.java")
        cfg = os.path.join(tmp, "checkstyle.xml")
        with open(sample, "w", encoding="utf-8") as f:
            f.write(SMOKE_SAMPLE)
        with open(cfg, "w", encoding="utf-8") as f:
            f.write(CHECKSTYLE_XML_CONTENT)
        cp = os.pathsep.join([cs_jar, aries_jar])
        res = subprocess.run([java, "-cp", cp, "com.puppycrawl.tools.checkstyle.Main", "-c", cfg, sample],
                             stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=180)
        out = (res.stdout or "") + (res.stderr or "")
        missing = [name for name in SMOKE_EXPECTED if f"[{name}]" not in out]
        if not missing:
            print(f"{COLOR_SUCCESS}{OK} Verified: Arieslinter detected the expected test smells ({', '.join(SMOKE_EXPECTED)}).{COLOR_RESET}")
            report("ok", "Verification (CLI)", "Arieslinter detected the sample test smells")
            return True
        first_error = next((l for l in out.splitlines() if "Exception" in l or "Error" in l or "Unable" in l), out.strip()[:200])
        print(f"{COLOR_ERROR}{ERR} Verification FAILED. Missing detections: {', '.join(missing)}{COLOR_RESET}")
        print(f"{COLOR_MUTED}      {first_error}{COLOR_RESET}")
        report("fail", "Verification (CLI)", f"missing {', '.join(missing)}; {first_error}")
        return False
    except Exception as e:
        print(f"{COLOR_ERROR}{ERR} Verification could not run: {e}{COLOR_RESET}")
        report("fail", "Verification (CLI)", str(e))
        return False
    finally:
        shutil.rmtree(tmp, ignore_errors=True)


# ---------------------------------------------------------------------------
# Project configuration
# ---------------------------------------------------------------------------
def strip_jsonc(text):
    """Remove // and /* */ comments (outside strings) and trailing commas from JSONC."""
    out, i, n = [], 0, len(text)
    in_str = False
    while i < n:
        c = text[i]
        if in_str:
            out.append(c)
            if c == "\\" and i + 1 < n:
                out.append(text[i + 1])
                i += 2
                continue
            if c == '"':
                in_str = False
            i += 1
            continue
        if c == '"':
            in_str = True
            out.append(c)
        elif c == "/" and text[i:i + 2] == "//":
            while i < n and text[i] != "\n":
                i += 1
            continue
        elif c == "/" and text[i:i + 2] == "/*":
            end = text.find("*/", i + 2)
            i = n if end == -1 else end + 2
            continue
        else:
            out.append(c)
        i += 1
    cleaned = "".join(out)
    # trailing commas before } or ] (outside strings is enough here: strings were preserved verbatim,
    # and a ",}" inside a string is vanishingly rare; guard anyway by only touching ", <ws> }|]")
    return re.sub(r',(\s*[}\]])', r'\1', cleaned)


def write_checkstyle_xml(proj_path, force=False, assume_yes=False):
    xml_path = os.path.join(proj_path, "checkstyle.xml")
    if os.path.exists(xml_path):
        with open(xml_path, "r", encoding="utf-8", errors="replace") as f:
            current = f.read()
        if "br.ufba.arieslinter" in current and not force:
            print(f"{COLOR_SUCCESS}{OK} checkstyle.xml already registers the Arieslinter checks; left untouched.{COLOR_RESET}")
            report("ok", "checkstyle.xml", "existing file already has Arieslinter checks (not modified)")
            return True
        overwrite = force
        if not overwrite and not assume_yes:
            print(f"{COLOR_WARN}{WARN} A checkstyle.xml already exists and does NOT contain the Arieslinter checks.{COLOR_RESET}")
            overwrite = input("   Overwrite it? A backup (.bak) will be kept. [y/N]: ").strip().lower() == "y"
        if not overwrite:
            print(f"{COLOR_WARN}{WARN} Kept your checkstyle.xml. Add the Arieslinter <module> entries to it manually (see README).{COLOR_RESET}")
            report("warn", "checkstyle.xml", "existing file kept; Arieslinter checks are NOT registered in it")
            return False
        backup = xml_path + ".bak"
        shutil.copyfile(xml_path, backup)
        print(f"{COLOR_INFO}{INFO} Backup saved to {backup}{COLOR_RESET}")
    try:
        with open(xml_path, "w", encoding="utf-8") as f:
            f.write(CHECKSTYLE_XML_CONTENT)
        print(f"{COLOR_SUCCESS}{OK} checkstyle.xml written at the project root.{COLOR_RESET}")
        report("ok", "checkstyle.xml", "written with all Arieslinter checks")
        return True
    except Exception as e:
        print(f"{COLOR_ERROR}{ERR} Error writing checkstyle.xml: {e}{COLOR_RESET}")
        report("fail", "checkstyle.xml", str(e))
        return False


def configure_vscode(project_path, jar_path):
    """Writes <project>/.vscode/settings.json (shared by VS Code and all its forks)."""
    settings_dir = os.path.join(project_path, ".vscode")
    settings_path = os.path.join(settings_dir, "settings.json")
    os.makedirs(settings_dir, exist_ok=True)

    data = {}
    if os.path.exists(settings_path):
        with open(settings_path, "r", encoding="utf-8") as f:
            raw = f.read()
        if raw.strip():
            try:
                data = json.loads(strip_jsonc(raw))
                if not isinstance(data, dict):
                    raise ValueError("settings.json is not a JSON object")
            except Exception as e:
                # Never overwrite a file we cannot understand
                print(f"{COLOR_ERROR}{ERR} Could not parse .vscode/settings.json ({e}); left untouched.{COLOR_RESET}")
                print(f"   Add these settings manually:\n"
                      f'     "java.checkstyle.configuration": "${{workspaceFolder}}/checkstyle.xml",\n'
                      f'     "java.checkstyle.version": "{CHECKSTYLE_VERSION}",\n'
                      f'     "java.checkstyle.autocheck": true,\n'
                      f'     "java.checkstyle.modules": ["{jar_path.replace(chr(92), "/")}"]')
                report("fail", ".vscode/settings.json", "could not parse existing file; not modified")
                return False
        backup = settings_path + ".bak"
        if not os.path.exists(backup):
            shutil.copyfile(settings_path, backup)

    data["java.checkstyle.configuration"] = "${workspaceFolder}/checkstyle.xml"
    data["java.checkstyle.version"] = CHECKSTYLE_VERSION
    data["java.checkstyle.autocheck"] = True

    modules = data.get("java.checkstyle.modules", [])
    if not isinstance(modules, list):
        modules = []
    formatted_jar_path = jar_path.replace("\\", "/")   # forward slashes work on every OS
    if formatted_jar_path not in modules:
        modules.append(formatted_jar_path)
    data["java.checkstyle.modules"] = modules

    with open(settings_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4)
        f.write("\n")
    print(f"{COLOR_SUCCESS}{OK} .vscode/settings.json configured (VS Code and forks, Checkstyle {CHECKSTYLE_VERSION}).{COLOR_RESET}")
    report("ok", ".vscode/settings.json", f"configured; pinned Checkstyle {CHECKSTYLE_VERSION}")
    report("warn", ".vscode/settings.json", "contains a path specific to this machine; do not commit/share it as-is")
    return True


def configure_intellij(project_path, jar_path):
    idea_dir = os.path.join(project_path, ".idea")
    os.makedirs(idea_dir, exist_ok=True)
    xml_path = os.path.join(idea_dir, "checkstyle-idea.xml")

    root = None
    if os.path.exists(xml_path):
        try:
            root = ET.parse(xml_path).getroot()
        except Exception:
            pass
    if root is None:
        root = ET.Element("project", version="4")

    component = root.find(".//component[@name='CheckStyle-IDEA']")
    if component is None:
        component = ET.SubElement(root, "component", name="CheckStyle-IDEA")
    option = component.find(".//option[@name='configuration']")
    if option is None:
        option = ET.SubElement(component, "option", name="configuration")
    map_elem = option.find("map")
    if map_elem is None:
        map_elem = ET.SubElement(option, "map")

    entries = {
        "active-configuration": "PROJECT_RELATIVE:$PROJECT_DIR$/checkstyle.xml:Arieslinter",
        "checkstyle-version": CHECKSTYLE_VERSION,
        "scan-before-checkin": "false",
        "scanscope": "JavaOnly",
        "thirdparty-classpath": jar_path.replace("\\", "/"),
    }
    for key, value in entries.items():
        for entry in map_elem.findall(f"entry[@key='{key}']"):
            map_elem.remove(entry)
        ET.SubElement(map_elem, "entry", key=key, value=value)

    pretty_xml = minidom.parseString(ET.tostring(root, 'utf-8')).toprettyxml(indent="  ")
    pretty_xml = os.linesep.join([line for line in pretty_xml.splitlines() if line.strip()])
    with open(xml_path, "w", encoding='utf-8') as f:
        f.write(pretty_xml)
    print(f"{COLOR_SUCCESS}{OK} IntelliJ CheckStyle-IDEA configured (Checkstyle {CHECKSTYLE_VERSION}).{COLOR_RESET}")
    report("ok", "IntelliJ project config", f".idea/checkstyle-idea.xml written (Checkstyle {CHECKSTYLE_VERSION})")
    report("warn", "IntelliJ", f"the CheckStyle-IDEA plugin must support Checkstyle {CHECKSTYLE_VERSION}; select it in Settings > Tools > Checkstyle if it does not load")


# ---------------------------------------------------------------------------
# IDE selection and per-IDE preparation
# ---------------------------------------------------------------------------
def choose_ides(vscode_ides, intellij, preselected=None):
    """Interactive multi-select of the IDEs to configure.
    preselected: None (ask), or a string like 'all', 'none', 'code,cursor'."""
    options = list(vscode_ides) + ([intellij] if intellij else [])
    if preselected is not None:
        wanted = {x.strip().lower() for x in preselected.split(",") if x.strip()}
        if wanted & {"none", "0"}:
            return []
        if "all" in wanted:
            return options
        return [o for o in options if o["id"] in wanted]

    print(f"{COLOR_TITLE}   IDEs detected on this machine:{COLOR_RESET}")
    if not options:
        print(f"   {COLOR_WARN}{WARN} No supported IDE was detected automatically.{COLOR_RESET}")
    for i, ide in enumerate(options, 1):
        if ide["kind"] == "vscode":
            how = ide["cli_cmd"] if ide["cli_cmd"] else "no CLI in PATH"
            mk = "Open VSX" if ide["marketplace"] == OPEN_VSX else "Microsoft Marketplace"
            print(f"   [{i}] {ide['name']}  {COLOR_MUTED}({how}; {mk}){COLOR_RESET}")
        else:
            print(f"   [{i}] {ide['name']}")
    n = len(options)
    print("   [a] All detected")
    print("   [0] None (just write checkstyle.xml)")
    while True:
        ans = input("\n   Select IDE(s) (e.g. 1,3), 'a' or '0': ").strip().lower()
        if ans == "0":
            return []
        if ans == "a" and options:
            return options
        try:
            picks = sorted({int(x) for x in re.split(r"[,\s]+", ans) if x})
            if picks and all(1 <= p <= n for p in picks):
                return [options[p - 1] for p in picks]
        except ValueError:
            pass
        print(f"   {COLOR_ERROR}{ERR} Invalid selection.{COLOR_RESET}")


def prepare_vscode_ide(ide, project_path, assume_yes=False, no_install=False):
    """Check/install extensions for one VS Code family IDE and verify the result."""
    print(f"\n{COLOR_TITLE}   → {ide['name']}{COLOR_RESET}")
    label = f"{ide['name']} extensions"
    profile_name = get_vscode_profile(ide, project_path)
    if profile_name:
        print(f"   {COLOR_INFO}{INFO} Active profile for this project: {COLOR_ACCENT}{profile_name}{COLOR_RESET}")

    missing = check_vscode_extensions(ide, profile_name)
    if missing:
        print(f"{COLOR_WARN}{WARN} Missing extensions in {ide['name']}:{COLOR_RESET}")
        for ext_id, ext_name in missing.items():
            print(f"   - {ext_name} ({ext_id})")
        do_install = False
        if not no_install and ide.get("cli_cmd"):
            do_install = assume_yes or input("   Do you want to install them automatically? [y/N]: ").strip().lower() == "y"
        elif not ide.get("cli_cmd"):
            print(f"{COLOR_WARN}{WARN} No '{ide['cli'][0]}' command in PATH for {ide['name']}: install the extensions from its Extensions view.{COLOR_RESET}")
        if do_install:
            install_vscode_extensions(ide, missing, profile_name)
            missing = check_vscode_extensions(ide, profile_name)   # verify, do not assume

    if missing:
        names = ", ".join(f"{n} ({i})" for i, n in missing.items())
        report("fail", label, f"still missing: {names}")
        return

    disabled = get_disabled_vscode_extensions(ide)
    installed_disabled = [(i, n) for i, n in ide["required"].items() if i.lower() in disabled]
    if installed_disabled:
        print_disabled_extensions_warning(ide["name"], installed_disabled)
        report("warn", label, "installed but DISABLED: " + ", ".join(i for i, _ in installed_disabled))
        return
    print(f"{COLOR_SUCCESS}{OK} Required extensions are installed and enabled in {ide['name']}.{COLOR_RESET}")
    report("ok", label, "installed and enabled (confirmed)")
    if ide["marketplace"] == OPEN_VSX:
        report("warn", f"{ide['name']} Java runtime",
               "the Open VSX checkstyle extension may require a newer JDK than 11 to run inside the editor (see its README)")


def print_summary():
    icons = {"ok": (COLOR_SUCCESS, f"[{ICON_CHECK}]"), "warn": (COLOR_WARN, f"[{ICON_WARN}]"), "fail": (COLOR_ERROR, f"[{ICON_CROSS}]")}
    print(f"\n{COLOR_TITLE}========================================================{COLOR_RESET}")
    print(f"{COLOR_TITLE}   SUMMARY{COLOR_RESET}")
    print(f"{COLOR_TITLE}========================================================{COLOR_RESET}")
    for status, label, detail in REPORT:
        color, icon = icons[status]
        print(f"   {color}{icon}{COLOR_RESET} {label}: {detail}")
    fails = sum(1 for s, _, _ in REPORT if s == "fail")
    warns = sum(1 for s, _, _ in REPORT if s == "warn")
    print("")
    if fails:
        print(f"{COLOR_ERROR}   Installation INCOMPLETE: {fails} problem(s) need your attention (see above).{COLOR_RESET}\n")
    elif warns:
        print(f"{COLOR_WARN}   Installed and verified from the command line, with {warns} warning(s) to review.{COLOR_RESET}")
        print(f"   {COLOR_MUTED}Open a Java test file in your IDE to see smells as warnings. In-editor behavior is not verified by this installer.{COLOR_RESET}\n")
    else:
        print(f"{COLOR_SUCCESS}   All checks passed. Open a Java test file in your IDE to see smells as warnings.{COLOR_RESET}\n")
    return 1 if fails else 0


def parse_args(argv=None):
    p = argparse.ArgumentParser(description="Arieslinter installer")
    p.add_argument("--project", help="project directory (skips the prompt)")
    p.add_argument("--ide", help="comma-separated ids to configure: all, none, " +
                   ", ".join([i["id"] for i in VSCODE_FAMILY] + ["intellij"]) + " (skips the prompt)")
    p.add_argument("--yes", "-y", action="store_true", help="install missing IDE extensions without asking")
    p.add_argument("--no-extensions", action="store_true", help="never install IDE extensions (only check them)")
    p.add_argument("--force-xml", action="store_true", help="overwrite an existing checkstyle.xml (a .bak is kept)")
    return p.parse_args(argv)


def main(argv=None):
    args = parse_args(argv)
    if not (args.project and args.ide is not None):
        print_header()

    # Step 1: Target Project Configuration
    print(f"{COLOR_TITLE}1. Target Project Configuration{COLOR_RESET}")
    proj_path = ""
    if args.project:
        proj_path = os.path.abspath(os.path.expanduser(args.project))
        if not os.path.isdir(proj_path):
            print(f"{COLOR_ERROR}{ERR} Directory does not exist: {proj_path}{COLOR_RESET}")
            return 1
        print(f"   Project: {proj_path}")
    while not proj_path:
        proj_path = input("   Enter the absolute path where you want to run the AriesLinter [default: current dir]: ").strip()
        if not proj_path:
            proj_path = os.getcwd()
        proj_path = os.path.abspath(os.path.expanduser(proj_path))
        if not os.path.isdir(proj_path):
            print(f"{COLOR_ERROR}{ERR} Directory does not exist. Please enter a valid path.{COLOR_RESET}")
            proj_path = ""
    print("")

    # Step 2: Environment
    print(f"{COLOR_TITLE}2. Checking environment{COLOR_RESET}")
    java = check_java()
    vscode_ides = detect_vscode_ides()
    intellij = detect_intellij()
    selected = choose_ides(vscode_ides, intellij, args.ide)
    print("")

    # Step 3: Download JARs
    print(f"{COLOR_TITLE}3. Download JARs{COLOR_RESET}")
    jar_target = aries_jar_path()
    cs_target = checkstyle_jar_path()
    aries_ok = download_arieslinter_jar(jar_target)
    cs_ok = download_checkstyle_jar(cs_target) if aries_ok else False
    print("")

    # Step 4: checkstyle.xml
    print(f"{COLOR_TITLE}4. Project configuration{COLOR_RESET}")
    write_checkstyle_xml(proj_path, force=args.force_xml, assume_yes=args.yes)
    print("")

    # Step 5: IDE extensions / config
    print(f"{COLOR_TITLE}5. IDE extensions & configuration{COLOR_RESET}")
    if not selected:
        print(f"   {COLOR_MUTED}No IDE selected; only checkstyle.xml and the JARs were set up.{COLOR_RESET}")
    for ide in selected:
        if ide["kind"] == "vscode":
            prepare_vscode_ide(ide, proj_path, assume_yes=args.yes, no_install=args.no_extensions)
        else:
            print(f"\n{COLOR_TITLE}   → {ide['name']}{COLOR_RESET}")
            if ide["plugin_installed"]:
                print(f"{COLOR_SUCCESS}{OK} CheckStyle-IDEA plugin found.{COLOR_RESET}")
                report("ok", "IntelliJ CheckStyle-IDEA plugin", "found")
            else:
                print(f"{COLOR_WARN}{WARN} CheckStyle-IDEA plugin NOT found. Install it via Settings > Plugins.{COLOR_RESET}")
                report("fail", "IntelliJ CheckStyle-IDEA plugin", "not installed (Settings > Plugins > CheckStyle-IDEA)")
    if aries_ok:
        if any(i["kind"] == "vscode" for i in selected):
            configure_vscode(proj_path, jar_target)
        if any(i["kind"] == "intellij" for i in selected):
            configure_intellij(proj_path, jar_target)
    print("")

    # Step 6: Verification
    print(f"{COLOR_TITLE}6. Verification{COLOR_RESET}")
    if java and aries_ok and cs_ok:
        smoke_test(java, jar_target, cs_target)
    else:
        print(f"{COLOR_WARN}{WARN} Verification skipped (needs Java {MIN_JAVA}+ and both JARs).{COLOR_RESET}")
        report("fail", "Verification (CLI)", "skipped: Java or JARs missing, so nothing could be proven to work")

    return print_summary()


if __name__ == "__main__":
    import signal

    def handle_exit(sig=None, frame=None):
        print(f"\n\n{COLOR_WARN}{WARN} Installation aborted by user. Exiting...{COLOR_RESET}\n")
        sys.exit(130)

    if hasattr(signal, 'SIGTSTP'):
        signal.signal(signal.SIGTSTP, handle_exit)

    try:
        sys.exit(main())
    except (KeyboardInterrupt, EOFError):
        handle_exit()
