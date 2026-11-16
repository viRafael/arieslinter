import importlib.util
import json
import os
import tempfile
import unittest

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
        inst.REPORT.clear()

    def test_merges_and_pins_version(self):
        os.makedirs(os.path.join(self.tmp, ".vscode"))
        path = os.path.join(self.tmp, ".vscode", "settings.json")
        with open(path, "w") as f:
            f.write('{ // keep\n "editor.tabSize": 2, "java.checkstyle.modules": ["/other.jar"], }')
        self.assertTrue(inst.configure_vscode(self.tmp, "C:\\Users\\x\\a.jar"))
        data = json.load(open(path))
        self.assertEqual(data["editor.tabSize"], 2)
        self.assertEqual(data["java.checkstyle.version"], inst.CHECKSTYLE_VERSION)
        self.assertEqual(data["java.checkstyle.modules"], ["/other.jar", "C:/Users/x/a.jar"])
        self.assertTrue(os.path.exists(path + ".bak"))

    def test_never_overwrites_unparsable_file(self):
        os.makedirs(os.path.join(self.tmp, ".vscode"))
        path = os.path.join(self.tmp, ".vscode", "settings.json")
        with open(path, "w") as f:
            f.write("{ broken")
        self.assertFalse(inst.configure_vscode(self.tmp, "/a.jar"))
        self.assertEqual(open(path).read(), "{ broken")


class CheckstyleXmlTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp()
        inst.REPORT.clear()

    def test_existing_foreign_file_is_kept_when_user_declines(self):
        p = os.path.join(self.tmp, "checkstyle.xml")
        open(p, "w").write("<module name='Checker'/>")
        original_input = __builtins__["input"] if isinstance(__builtins__, dict) else __builtins__.input
        try:
            import builtins
            builtins.input = lambda *_: "n"
            self.assertFalse(inst.write_checkstyle_xml(self.tmp))
        finally:
            builtins.input = original_input
        self.assertEqual(open(p).read(), "<module name='Checker'/>")
        self.assertEqual(inst.REPORT[-1][0], "warn")

    def test_force_overwrites_with_backup(self):
        p = os.path.join(self.tmp, "checkstyle.xml")
        open(p, "w").write("<module name='Checker'/>")
        self.assertTrue(inst.write_checkstyle_xml(self.tmp, force=True))
        self.assertTrue(os.path.exists(p + ".bak"))
        self.assertIn("br.ufba.arieslinter", open(p).read())

    def test_existing_arieslinter_file_untouched(self):
        p = os.path.join(self.tmp, "checkstyle.xml")
        open(p, "w").write("<!-- br.ufba.arieslinter -->")
        self.assertTrue(inst.write_checkstyle_xml(self.tmp))
        self.assertEqual(open(p).read(), "<!-- br.ufba.arieslinter -->")


class SelectionTest(unittest.TestCase):
    def test_preselected(self):
        ides = [{"id": "code", "kind": "vscode"}, {"id": "cursor", "kind": "vscode"}]
        self.assertEqual(inst.choose_ides(ides, None, "none"), [])
        self.assertEqual(inst.choose_ides(ides, None, "all"), ides)
        self.assertEqual(inst.choose_ides(ides, None, "cursor"), [ides[1]])


class TableTest(unittest.TestCase):
    def test_open_vsx_forks_do_not_use_microsoft_only_extension(self):
        for ide in inst.VSCODE_FAMILY:
            exts = inst.EXTENSIONS_BY_MARKETPLACE[ide["marketplace"]]
            if ide["marketplace"] == inst.OPEN_VSX:
                self.assertNotIn("shengchen.vscode-checkstyle", exts)


if __name__ == "__main__":
    unittest.main()
