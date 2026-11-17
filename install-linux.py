#!/usr/bin/env python3
# Deprecated: the installer is now a single cross-platform script, install.py.
# This file intentionally does NOT download or run anything.
import sys

print("""
install-linux.py / install-win.py were replaced by install.py.
For security reasons, download it, read it and then run it:

  Linux / macOS:
    curl -sSL -o install.py https://raw.githubusercontent.com/viRafael/arieslinter/main/install.py
    python3 install.py

  Windows (PowerShell or CMD):
    curl.exe -sSL -o install.py https://raw.githubusercontent.com/viRafael/arieslinter/main/install.py
    py install.py
""")
sys.exit(1)
