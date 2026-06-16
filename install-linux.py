#!/usr/bin/env python3
import sys
import os
import subprocess
import urllib.request
import json
import re
import xml.etree.ElementTree as ET
from xml.dom import minidom

# If piped (e.g. curl ... | python3), reconnect stdin to the interactive terminal
try:
    if not sys.stdin.isatty():
        sys.stdin = open('/dev/tty', 'r')
except Exception:
    pass

# ANSI Colors
COLOR_TITLE = "\033[1;38;5;45m"     # Cyan
COLOR_ACCENT = "\033[1;38;5;198m"   # Pink/Magenta
COLOR_SUCCESS = "\033[1;38;5;82m"    # Green
COLOR_WARN = "\033[1;38;5;220m"     # Yellow
COLOR_ERROR = "\033[1;38;5;196m"    # Red
COLOR_INFO = "\033[1;38;5;51m"      # Sky Blue
COLOR_MUTED = "\033[38;5;244m"      # Gray
COLOR_RESET = "\033[0m"

# Icons
ICON_CHECK = "✔"
ICON_CROSS = "✘"
ICON_WARN = "⚠"
ICON_INFO = "ℹ"

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

def print_header():
    os.system('clear')
    print(f"{COLOR_TITLE}┌────────────────────────────────────────────────────────┐")
    print(f"│              {COLOR_ACCENT}Arieslinter Auto-Installer 🚀              {COLOR_TITLE}│")
    print(f"│     Real-Time Static Analysis for Java Test Smells     │")
    print(f"└────────────────────────────────────────────────────────┘{COLOR_RESET}\n")

def check_vscode_extensions():
    required = {
        "vscjava.vscode-java-pack": "Extension Pack for Java (Microsoft)",
        "shengchen.vscode-checkstyle": "Checkstyle for Java"
    }
    missing = dict(required)
    ext_dirs = [
        os.path.expanduser("~/.vscode/extensions"),
        os.path.expanduser("~/.vscode-oss/extensions")
    ]
    for d in ext_dirs:
        if os.path.exists(d):
            try:
                for entry in os.listdir(d):
                    entry_lower = entry.lower()
                    for ext_id in list(missing.keys()):
                        if ext_id.lower() in entry_lower:
                            missing.pop(ext_id, None)
            except Exception:
                pass
    return missing

def install_vscode_extensions(extensions_to_install):
    installed_any = False
    exts_copy = dict(extensions_to_install)
    for cmd in ["code", "code-oss"]:
        # Check if command is available
        try:
            subprocess.run([cmd, "--version"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        except FileNotFoundError:
            continue
            
        for ext_id, ext_name in list(exts_copy.items()):
            print(f"{COLOR_INFO}[{ICON_INFO}] Installing {ext_name} via {cmd}...{COLOR_RESET}")
            res = subprocess.run([cmd, "--install-extension", ext_id], 
                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            if res.returncode == 0:
                print(f"{COLOR_SUCCESS}[{ICON_CHECK}] {ext_name} installed successfully.{COLOR_RESET}")
                exts_copy.pop(ext_id, None)
                installed_any = True
            else:
                print(f"{COLOR_ERROR}[{ICON_CROSS}] Failed to install {ext_name} using {cmd}.{COLOR_RESET}")
    
    if exts_copy:
        print(f"{COLOR_WARN}[{ICON_WARN}] Some extensions could not be installed automatically. Please install them manually in VS Code:")
        for ext_id, ext_name in exts_copy.items():
            print(f"   - {ext_name} ({ext_id})")
    return installed_any

def check_intellij_plugin():
    config_dirs = [
        os.path.expanduser("~/.local/share/JetBrains"),
        os.path.expanduser("~/.config/JetBrains")
    ]
    for base in config_dirs:
        if os.path.exists(base):
            for root, dirs, files in os.walk(base):
                for d in dirs:
                    if "checkstyle-idea" in d.lower():
                        return True
    return False

def download_jar(target_path):
    print(f"{COLOR_INFO}[{ICON_INFO}] Fetching latest release from GitHub...{COLOR_RESET}")
    api_url = "https://api.github.com/repos/viRafael/arieslinter/releases/latest"
    req = urllib.request.Request(api_url, headers={'User-Agent': 'Mozilla/5.0'})
    
    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode('utf-8'))
        
        assets = data.get("assets", [])
        download_url = None
        for asset in assets:
            if asset.get("name", "").endswith(".jar"):
                download_url = asset.get("browser_download_url")
                break
                
        if not download_url:
            print(f"{COLOR_ERROR}[{ICON_CROSS}] Could not find any .jar asset in the latest GitHub Release.{COLOR_RESET}")
            return False
            
        print(f"{COLOR_INFO}[{ICON_INFO}] Downloading JAR: {download_url}{COLOR_RESET}")
        os.makedirs(os.path.dirname(target_path), exist_ok=True)
        
        jar_req = urllib.request.Request(download_url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(jar_req) as response, open(target_path, 'wb') as out_file:
            out_file.write(response.read())
            
        print(f"{COLOR_SUCCESS}[{ICON_CHECK}] Successfully saved JAR to {target_path}{COLOR_RESET}")
        return True
    except Exception as e:
        print(f"{COLOR_ERROR}[{ICON_CROSS}] Error downloading JAR: {e}{COLOR_RESET}")
        return False



def configure_vscode(project_path, jar_path):
    settings_dir = os.path.join(project_path, ".vscode")
    settings_path = os.path.join(settings_dir, "settings.json")
    os.makedirs(settings_dir, exist_ok=True)
    
    data = {}
    if os.path.exists(settings_path):
        try:
            with open(settings_path, 'r', encoding='utf-8') as f:
                content = f.read()
                content_clean = re.sub(r'//.*', '', content)
                content_clean = re.sub(r'/\*.*?\*/', '', content_clean, flags=re.DOTALL)
                data = json.loads(content_clean)
        except Exception:
            pass
            
    data["java.checkstyle.configuration"] = "${workspaceFolder}/checkstyle.xml"
    data["java.checkstyle.autocheck"] = True
    
    modules = data.get("java.checkstyle.modules", [])
    if not isinstance(modules, list):
        modules = []
    if jar_path not in modules:
        modules.append(jar_path)
    data["java.checkstyle.modules"] = modules
    
    with open(settings_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4)
    print(f"{COLOR_SUCCESS}[{ICON_CHECK}] VS Code settings configured.{COLOR_RESET}")

def configure_intellij(project_path, jar_path):
    idea_dir = os.path.join(project_path, ".idea")
    os.makedirs(idea_dir, exist_ok=True)
    xml_path = os.path.join(idea_dir, "checkstyle-idea.xml")
    
    root = None
    if os.path.exists(xml_path):
        try:
            tree = ET.parse(xml_path)
            root = tree.getroot()
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
        "checkstyle-version": "10.12.5",
        "scan-before-checkin": "false",
        "scanscope": "JavaOnly",
        "thirdparty-classpath": jar_path
    }
    
    for key, value in entries.items():
        for entry in map_elem.findall(f"entry[@key='{key}']"):
            map_elem.remove(entry)
        ET.SubElement(map_elem, "entry", key=key, value=value)
        
    rough_string = ET.tostring(root, 'utf-8')
    reparsed = minidom.parseString(rough_string)
    pretty_xml = reparsed.toprettyxml(indent="  ")
    pretty_xml = os.linesep.join([line for line in pretty_xml.splitlines() if line.strip()])
    
    with open(xml_path, "w", encoding='utf-8') as f:
        f.write(pretty_xml)
    print(f"{COLOR_SUCCESS}[{ICON_CHECK}] IntelliJ CheckStyle-IDEA configured.{COLOR_RESET}")

def main():
    print_header()
    
    # Step 1: Environment & Plugin Checks
    print(f"{COLOR_TITLE}1. Analyzing Environment & IDE Plugins...{COLOR_RESET}")
    
    # Python check
    print(f"{COLOR_SUCCESS}[{ICON_CHECK}] Python 3 is installed.{COLOR_RESET}")
    
    # Java check
    try:
        subprocess.run(["java", "-version"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        print(f"{COLOR_SUCCESS}[{ICON_CHECK}] Java Runtime is installed.{COLOR_RESET}")
    except FileNotFoundError:
        print(f"{COLOR_WARN}[{ICON_WARN}] Java is NOT installed in your PATH. Please install Java before using Arieslinter.{COLOR_RESET}")
        
    # VS Code extensions check
    missing_exts = check_vscode_extensions()
    if not missing_exts:
        print(f"{COLOR_SUCCESS}[{ICON_CHECK}] All required VS Code extensions are installed.{COLOR_RESET}")
    else:
        print(f"{COLOR_WARN}[{ICON_WARN}] The following VS Code extensions are missing:")
        for ext_id, ext_name in missing_exts.items():
            print(f"   - {ext_name} ({ext_id})")
        ans = input("   Do you want to install them automatically? [y/N]: ").strip().lower()
        if ans == 'y':
            install_vscode_extensions(missing_exts)
            
    # IntelliJ Checkstyle check
    if check_intellij_plugin():
        print(f"{COLOR_SUCCESS}[{ICON_CHECK}] IntelliJ CheckStyle-IDEA plugin is installed.{COLOR_RESET}")
    else:
        print(f"{COLOR_WARN}[{ICON_WARN}] IntelliJ CheckStyle-IDEA plugin is NOT installed.{COLOR_RESET}")
        print(f"   {COLOR_MUTED}Note: Install the 'CheckStyle-IDEA' plugin via File > Settings > Plugins inside IntelliJ.{COLOR_RESET}")
        
    print("")
    
    # Step 2: Download JAR
    print(f"{COLOR_TITLE}2. Download Arieslinter JAR{COLOR_RESET}")
    jar_target = os.path.expanduser("~/.m2/repository/br/ufba/arieslinter/1.0/arieslinter-1.0.jar")
    print(f"   Target Location: {COLOR_MUTED}{jar_target}{COLOR_RESET}\n")
    
    if not download_jar(jar_target):
        sys.exit(1)
            
    print("")
    
    # Step 3: Project Path & checkstyle.xml Configuration
    print(f"{COLOR_TITLE}3. Target Project Configuration{COLOR_RESET}")
    proj_path = ""
    while not proj_path:
        proj_path = input("   Enter the absolute path where you want to run the AriesLinter [default: current dir]: ").strip()
        if not proj_path:
            proj_path = os.getcwd()
        proj_path = os.path.abspath(os.path.expanduser(proj_path))
        if not os.path.exists(proj_path) or not os.path.isdir(proj_path):
            print(f"{COLOR_ERROR}[{ICON_CROSS}] Directory does not exist. Please enter a valid path.{COLOR_RESET}")
            proj_path = ""
            
    # Write checkstyle.xml
    xml_path = os.path.join(proj_path, "checkstyle.xml")
    try:
        with open(xml_path, 'w', encoding='utf-8') as f:
            f.write(CHECKSTYLE_XML_CONTENT)
        print(f"{COLOR_SUCCESS}[{ICON_CHECK}] Successfully created checkstyle.xml at project root.{COLOR_RESET}")
    except Exception as e:
        print(f"{COLOR_ERROR}[{ICON_CROSS}] Error writing checkstyle.xml: {e}{COLOR_RESET}")
        sys.exit(1)
        
    print("")
    
    # Step 4: IDE configuration
    print(f"{COLOR_TITLE}4. Select IDE to Configure for this Project{COLOR_RESET}")
    print("   [1] VS Code")
    print("   [2] IntelliJ IDEA")
    print("   [3] Both IDEs")
    print("   [4] None (Just write checkstyle.xml)")
    
    ide_choice = ""
    while ide_choice not in ["1", "2", "3", "4"]:
        ide_choice = input("\n   Select option [1-4]: ").strip()
        
    if ide_choice in ["1", "3"]:
        configure_vscode(proj_path, jar_target)
    if ide_choice in ["2", "3"]:
        configure_intellij(proj_path, jar_target)
        
    print(f"\n{COLOR_SUCCESS}========================================================{COLOR_RESET}")
    print(f"{COLOR_SUCCESS}   CONFIGURATION COMPLETE! 🚀{COLOR_RESET}")
    print(f"{COLOR_SUCCESS}========================================================{COLOR_RESET}")
    print("   - checkstyle.xml has been successfully configured.")
    print(f"   Enjoy real-time test smell analysis with {COLOR_ACCENT}Arieslinter{COLOR_RESET}! 🐞\n")

if __name__ == "__main__":
    import signal
    
    def handle_exit(sig=None, frame=None):
        print(f"\n\n{COLOR_WARN}[{ICON_WARN}] Installation aborted by user. Exiting...{COLOR_RESET}\n")
        sys.exit(0)
        
    if hasattr(signal, 'SIGTSTP'):
        signal.signal(signal.SIGTSTP, handle_exit)
        
    try:
        main()
    except (KeyboardInterrupt, EOFError):
        handle_exit()
