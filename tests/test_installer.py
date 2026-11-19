import builtins
import glob
import hashlib
import importlib.util
import json
import os
import tempfile
import unittest
from unittest import mock

HERE = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("install", os.path.join(HERE, "..", "install.py"))
inst = importlib.util.module_from_spec(spec)
spec.loader.exec_module(inst)


class JavaVersionTest(unittest.TestCase):
    def test_parse(self):
        self.assertEqual(inst.parse_java_major('openjdk version "17.0.1" 2021-10-19'), 17)
        self.assertEqual(inst.parse_java_major('java version "1.8.0_292"'), 8)
        self.assertEqual(inst.parse_java_major('openjdk version "11" 2018-09-25'), 11)
        self.assertEqual(inst.parse_java_major('openjdk version "25.0.1" 2026'), 25)
        self.assertIsNone(inst.parse_java_major("garbage"))

    def test_probe_rejects_failing_java_stub(self):
        # e.g. the macOS /usr/bin/java stub: exits non-zero when no JDK is installed
        fake = mock.Mock(returncode=1, stderr="Unable to locate a Java Runtime.", stdout="")
        with mock.patch.object(inst.subprocess, "run", return_value=fake):
            self.assertIsNone(inst.probe_java("/usr/bin/java"))

    def test_probe_accepts_working_java(self):
        fake = mock.Mock(returncode=0, stderr='openjdk version "17.0.1"', stdout="")
        with mock.patch.object(inst.subprocess, "run", return_value=fake):
            self.assertEqual(inst.probe_java("/x/java"), 17)


class JsoncTest(unittest.TestCase):
    def test_comments_and_trailing_commas(self):
        raw = '{\n // c\n "a": 1, /* x */ "u": "http://x.com/a//b",\n "l": [1, 2,],\n}'
        self.assertEqual(json.loads(inst.strip_jsonc(raw)),
                         {"a": 1, "u": "http://x.com/a//b", "l": [1, 2]})

    def test_escaped_quote_in_string(self):
        raw = '{"a": "he said \\"//no\\" ok"}'
        self.assertEqual(json.loads(inst.strip_jsonc(raw))["a"], 'he said "//no" ok')


class ConfigureVscodeTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp()
        self.settings = os.path.join(self.tmp, ".vscode", "settings.json")
        inst.REPORT.clear()

    def _write(self, text):
        os.makedirs(os.path.dirname(self.settings), exist_ok=True)
        with open(self.settings, "w") as f:
            f.write(text)

    def test_merges_pins_version_and_backs_up(self):
        self._write('{ // keep\n "editor.tabSize": 2, "java.checkstyle.modules": ["/other.jar"], }')
        self.assertTrue(inst.configure_vscode(self.tmp, "C:\\Users\\x\\arieslinter-1.0.jar"))
        data = json.load(open(self.settings))
        self.assertEqual(data["editor.tabSize"], 2)
        self.assertEqual(data["java.checkstyle.version"], inst.CHECKSTYLE_VERSION)
        self.assertEqual(data["java.checkstyle.modules"], ["/other.jar", "C:/Users/x/arieslinter-1.0.jar"])
        self.assertEqual(len(glob.glob(self.settings + ".bak-*")), 1)

    def test_replaces_stale_arieslinter_jar_path(self):
        self._write('{"java.checkstyle.modules": ["/old/.m2/arieslinter-1.0.jar", "/other.jar"]}')
        inst.configure_vscode(self.tmp, "/new/arieslinter-1.0.jar")
        data = json.load(open(self.settings))
        self.assertEqual(data["java.checkstyle.modules"], ["/other.jar", "/new/arieslinter-1.0.jar"])

    def test_never_overwrites_unparsable_file(self):
        self._write("{ broken")
        self.assertFalse(inst.configure_vscode(self.tmp, "/a.jar"))
        self.assertEqual(open(self.settings).read(), "{ broken")

    def test_refuses_symlinked_settings(self):
        target = os.path.join(self.tmp, "victim.txt")
        open(target, "w").write("precious")
        os.makedirs(os.path.dirname(self.settings))
        os.symlink(target, self.settings)
        self.assertFalse(inst.configure_vscode(self.tmp, "/a.jar"))
        self.assertEqual(open(target).read(), "precious")

    def test_refuses_symlinked_vscode_dir(self):
        outside = tempfile.mkdtemp()
        os.symlink(outside, os.path.join(self.tmp, ".vscode"))
        self.assertFalse(inst.configure_vscode(self.tmp, "/a.jar"))
        self.assertEqual(os.listdir(outside), [])


class ConfigureIntellijTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp()
        inst.REPORT.clear()

    def test_writes_pinned_version(self):
        self.assertTrue(inst.configure_intellij(self.tmp, "C:\\j\\arieslinter-1.0.jar"))
        text = open(os.path.join(self.tmp, ".idea", "checkstyle-idea.xml")).read()
        self.assertIn(f'value="{inst.CHECKSTYLE_VERSION}"', text)
        self.assertIn("C:/j/arieslinter-1.0.jar", text)

    def test_unparsable_file_is_not_overwritten(self):
        os.makedirs(os.path.join(self.tmp, ".idea"))
        p = os.path.join(self.tmp, ".idea", "checkstyle-idea.xml")
        open(p, "w").write("<not xml")
        self.assertFalse(inst.configure_intellij(self.tmp, "/a.jar"))
        self.assertEqual(open(p).read(), "<not xml")


class CheckstyleXmlTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp()
        self.xml = os.path.join(self.tmp, "checkstyle.xml")
        inst.REPORT.clear()

    def test_existing_foreign_file_is_kept_when_user_declines(self):
        open(self.xml, "w").write("<module name='Checker'/>")
        with mock.patch.object(inst, "INTERACTIVE", True), mock.patch.object(builtins, "input", lambda *_: "n"):
            self.assertFalse(inst.write_checkstyle_xml(self.tmp))
        self.assertEqual(open(self.xml).read(), "<module name='Checker'/>")
        self.assertEqual(inst.REPORT[-1][0], "warn")

    def test_force_overwrites_with_backup(self):
        open(self.xml, "w").write("<module name='Checker'/>")
        self.assertTrue(inst.write_checkstyle_xml(self.tmp, force=True))
        self.assertEqual(len(glob.glob(self.xml + ".bak-*")), 1)
        self.assertIn("br.ufba.arieslinter", open(self.xml).read())

    def test_existing_arieslinter_file_untouched(self):
        open(self.xml, "w").write("<!-- br.ufba.arieslinter -->")
        self.assertTrue(inst.write_checkstyle_xml(self.tmp))
        self.assertEqual(open(self.xml).read(), "<!-- br.ufba.arieslinter -->")

    def test_symlink_is_refused(self):
        target = os.path.join(self.tmp, "victim")
        open(target, "w").write("precious")
        os.symlink(target, self.xml)
        self.assertFalse(inst.write_checkstyle_xml(self.tmp, force=True))
        self.assertEqual(open(target).read(), "precious")


class DownloadTest(unittest.TestCase):
    """The checksum is the security boundary: it must fail closed."""

    def setUp(self):
        self.tmp = tempfile.mkdtemp()
        self.target = os.path.join(self.tmp, "x.jar")
        self.data = b"jar-bytes"
        self.sha = hashlib.sha256(self.data).hexdigest()

    def test_requires_a_pinned_checksum(self):
        with mock.patch.object(inst, "http_get", return_value=self.data) as get:
            ok, detail = inst.download_verified("https://github.com/x", self.target, None)
        self.assertFalse(ok)
        get.assert_not_called()
        self.assertFalse(os.path.exists(self.target))

    def test_wrong_checksum_discards_file(self):
        with mock.patch.object(inst, "http_get", return_value=b"tampered"):
            ok, detail = inst.download_verified("https://github.com/x", self.target, self.sha)
        self.assertFalse(ok)
        self.assertIn("mismatch", detail)
        self.assertEqual(os.listdir(self.tmp), [])           # no target, no .part left behind

    def test_good_checksum_installs_and_is_reused(self):
        with mock.patch.object(inst, "http_get", return_value=self.data):
            ok, _ = inst.download_verified("https://github.com/x", self.target, self.sha)
        self.assertTrue(ok)
        self.assertEqual(open(self.target, "rb").read(), self.data)
        with mock.patch.object(inst, "http_get", side_effect=AssertionError("should not download")):
            ok, detail = inst.download_verified("https://github.com/x", self.target, self.sha)
        self.assertTrue(ok)
        self.assertIn("already present", detail)

    def test_refuses_symlink_target(self):
        victim = os.path.join(self.tmp, "victim")
        open(victim, "w").write("precious")
        os.symlink(victim, self.target)
        with mock.patch.object(inst, "http_get", return_value=self.data):
            ok, _ = inst.download_verified("https://github.com/x", self.target, self.sha)
        self.assertFalse(ok)
        self.assertEqual(open(victim).read(), "precious")

    def test_pins_are_present(self):
        self.assertRegex(inst.ARIES_SHA256, r"^[0-9a-f]{64}$")
        self.assertRegex(inst.CHECKSTYLE_SHA256, r"^[0-9a-f]{64}$")
        self.assertTrue(inst.ARIES_JAR_URL.startswith("https://github.com/viRafael/arieslinter/releases/download/"))


class UrlAllowListTest(unittest.TestCase):
    def test_allows_github(self):
        inst.check_url("https://github.com/viRafael/arieslinter/releases/download/1.5/a.jar")
        inst.check_url("https://objects.githubusercontent.com/abc")

    def test_rejects_other_hosts_and_schemes(self):
        for bad in ["http://github.com/x", "https://evil.example.com/x", "https://github.com.evil.com/x",
                    "https://notgithub.com/x", "file:///etc/passwd"]:
            with self.assertRaises(ValueError, msg=bad):
                inst.check_url(bad)

    def test_redirect_to_untrusted_host_is_blocked(self):
        handler = inst._SafeRedirect()
        with self.assertRaises(ValueError):
            handler.redirect_request(mock.Mock(), None, 302, "Found", {}, "https://evil.example.com/x")


class ProfileNameTest(unittest.TestCase):
    def test_valid(self):
        for ok in ["Work", "My Profile", "java-dev", "a_b (1)"]:
            self.assertTrue(inst.valid_profile(ok), ok)

    def test_rejects_shell_metacharacters_and_option_injection(self):
        for bad in ["x&calc", "a|b", "%PATH%", "^x", "--install-extension", "-x", "a;b", "", None, "a" * 100]:
            self.assertFalse(inst.valid_profile(bad), repr(bad))


class SanitizeTest(unittest.TestCase):
    def test_strips_escape_sequences(self):
        self.assertNotIn("\x1b", inst.sanitize("evil\x1b[2Jtext\n"))


class SelectionTest(unittest.TestCase):
    def test_preselected(self):
        ides = [{"id": "code", "kind": "vscode"}, {"id": "cursor", "kind": "vscode"}]
        self.assertEqual(inst.choose_ides(ides, None, "none"), [])
        self.assertEqual(inst.choose_ides(ides, None, "all"), ides)
        self.assertEqual(inst.choose_ides(ides, None, "cursor"), [ides[1]])

    def test_prompt_without_terminal_raises(self):
        with mock.patch.object(inst, "INTERACTIVE", False):
            with self.assertRaises(inst.NoTerminal):
                inst.choose_ides([], None, None)


class CustomIdeTest(unittest.TestCase):
    def _fake_cli(self, name="antigravity", ok=True):
        d = tempfile.mkdtemp()
        path = os.path.join(d, name)
        with open(path, "w") as f:
            f.write("#!/bin/sh\n" + ('echo 1.150.0\n' if ok else "exit 3\n"))
        os.chmod(path, 0o755)
        return path

    def test_builds_entry_from_full_path(self):
        ide = inst.make_custom_ide(self._fake_cli())
        self.assertEqual(ide["kind"], "vscode")
        self.assertEqual(ide["id"], "custom")
        self.assertEqual(ide["marketplace"], inst.OPEN_VSX)
        self.assertIn("a-st.checkstyle-vscode", ide["required"])

    def test_missing_or_broken_command_returns_none(self):
        self.assertIsNone(inst.make_custom_ide("definitely-not-a-real-editor-xyz"))
        self.assertIsNone(inst.make_custom_ide(self._fake_cli(ok=False)))
        self.assertIsNone(inst.make_custom_ide(""))

    def test_enabled_state_is_unknown_for_custom_editors(self):
        ide = inst.make_custom_ide(self._fake_cli())
        self.assertIsNone(inst.get_disabled_vscode_extensions(ide))

    def test_antigravity_is_registered_with_open_vsx(self):
        entry = next(i for i in inst.VSCODE_FAMILY if i["id"] == "antigravity")
        self.assertEqual(entry["marketplace"], inst.OPEN_VSX)

    def test_preselect_custom(self):
        ide = inst.make_custom_ide(self._fake_cli())
        self.assertEqual(inst.choose_ides([ide], None, "custom"), [ide])


class MenuInputTest(unittest.TestCase):
    def _run(self, options, answers):
        it = iter(answers)
        with mock.patch.object(inst, "INTERACTIVE", True), mock.patch.object(builtins, "input", lambda *_: next(it)):
            return inst.choose_ides(options, None, None)

    def test_a_with_no_ide_detected_explains_and_reprompts(self):
        # used to be reported as "Invalid selection"; must keep asking instead of failing
        self.assertEqual(self._run([], ["a", "0"]), [])

    def test_a_selects_everything_when_ides_exist(self):
        ides = [{"id": "code", "kind": "vscode", "cli_cmd": "code", "marketplace": inst.MICROSOFT_MARKETPLACE, "name": "VS Code"}]
        self.assertEqual(self._run(ides, ["a"]), ides)

    def test_c_adds_a_custom_editor_then_it_can_be_selected(self):
        cli = CustomIdeTest()._fake_cli()
        chosen = self._run([], ["c", cli, "1"])
        self.assertEqual(len(chosen), 1)
        self.assertEqual(chosen[0]["id"], "custom")

    def test_c_with_bad_command_asks_again(self):
        self.assertEqual(self._run([], ["c", "nope-nope-xyz", "0"]), [])


class TableTest(unittest.TestCase):
    def test_open_vsx_forks_do_not_use_microsoft_only_extension(self):
        for ide in inst.VSCODE_FAMILY:
            exts = inst.EXTENSIONS_BY_MARKETPLACE[ide["marketplace"]]
            if ide["marketplace"] == inst.OPEN_VSX:
                self.assertNotIn("shengchen.vscode-checkstyle", exts)

    def test_editor_version_parse(self):
        self.assertEqual(inst.parse_editor_version("1.144.2\nabc\nx64"), (1, 144, 2))
        self.assertIsNone(inst.parse_editor_version("nope"))


class MarketplaceDetectionTest(unittest.TestCase):
    def _make(self, app_name, gallery):
        root = tempfile.mkdtemp()
        os.makedirs(os.path.join(root, "bin"))
        os.makedirs(os.path.join(root, "resources", "app"))
        cli = os.path.join(root, "bin", "code")
        open(cli, "w").write("")
        json.dump({"applicationName": app_name, "extensionsGallery": {"serviceUrl": gallery}},
                  open(os.path.join(root, "resources", "app", "product.json"), "w"))
        return cli

    def test_code_oss_named_code_uses_open_vsx(self):
        ide = inst.VSCODE_FAMILY[0]   # "code"
        cli = self._make("code-oss", "https://open-vsx.org/vscode/gallery")
        self.assertEqual(inst.detect_marketplace(ide, "code", cli), inst.OPEN_VSX)

    def test_microsoft_build(self):
        ide = inst.VSCODE_FAMILY[0]
        cli = self._make("code", "https://marketplace.visualstudio.com/_apis/public/gallery")
        self.assertEqual(inst.detect_marketplace(ide, "code", cli), inst.MICROSOFT_MARKETPLACE)


class UninstallTest(unittest.TestCase):
    def test_removes_only_what_we_added(self):
        tmp = tempfile.mkdtemp()
        os.makedirs(os.path.join(tmp, ".vscode"))
        p = os.path.join(tmp, ".vscode", "settings.json")
        json.dump({"editor.tabSize": 2,
                   "java.checkstyle.configuration": "${workspaceFolder}/checkstyle.xml",
                   "java.checkstyle.version": inst.CHECKSTYLE_VERSION,
                   "java.checkstyle.modules": ["/o.jar", "/h/arieslinter-1.0.jar"]}, open(p, "w"))
        with open(os.path.join(tmp, "checkstyle.xml"), "w") as f:
            f.write(inst.CHECKSTYLE_XML_CONTENT)
        self.assertEqual(inst.uninstall(tmp), 0)
        data = json.load(open(p))
        self.assertEqual(data, {"editor.tabSize": 2, "java.checkstyle.modules": ["/o.jar"]})
        self.assertFalse(os.path.exists(os.path.join(tmp, "checkstyle.xml")))


if __name__ == "__main__":
    unittest.main()
