#!/usr/bin/env python3
# Deprecated: kept so older links (articles, READMEs) keep working.
# The installer is now a single cross-platform script: install.py
import sys
import urllib.request

URL = "https://raw.githubusercontent.com/viRafael/arieslinter/main/install.py"
print("Note: install-linux.py / install-win.py were replaced by install.py. Fetching it...")
code = urllib.request.urlopen(URL).read().decode("utf-8")
sys.argv[0] = "install.py"
exec(compile(code, "install.py", "exec"), {"__name__": "__main__", "__file__": "install.py"})
