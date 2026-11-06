"""Small adversarial fixtures for the resource audit; no Android build needed."""
import tempfile
import unittest
from pathlib import Path
from zh_cn_audit import audit, tokens


class AuditTest(unittest.TestCase):
    def fixture(self, source, translation=None):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        root = Path(directory.name)
        parent = root / 'app/example/src/commonMain/composeResources'
        (parent / 'values').mkdir(parents=True)
        (parent / 'values/strings.xml').write_text('<resources>' + source + '</resources>', encoding='utf-8')
        if translation is not None:
            (parent / 'values-zh-rCN').mkdir()
            (parent / 'values-zh-rCN/strings.xml').write_text('<resources>' + translation + '</resources>', encoding='utf-8')
        return root, parent

    def test_creates_missing_locale_and_reports_key(self):
        root, parent = self.fixture('<string name="search">Search</string>')
        report = audit(root)
        self.assertTrue((parent / 'values-zh-rCN').is_dir())
        self.assertEqual(report['totals']['missing_keys'], 1)
        self.assertEqual(report['modules'][0]['missing_keys'], ['search'])

    def test_reordered_or_changed_placeholders_fail(self):
        root, _ = self.fixture('<string name="x">%1$s %2$.1f %%</string>', '<string name="x">%2$.1f %1$s %%</string>')
        self.assertEqual(audit(root)['errors'][0]['kind'], 'placeholders')

    def test_missing_plural_one_fails(self):
        root, _ = self.fixture('<plurals name="x"><item quantity="one">%d item</item><item quantity="other">%d items</item></plurals>', '<plurals name="x"><item quantity="other">%d 项</item></plurals>')
        self.assertEqual(audit(root)['totals']['missing_items'], 1)

    def test_array_length_fails(self):
        root, _ = self.fixture('<string-array name="x"><item>One</item><item>Two</item></string-array>', '<string-array name="x"><item>一</item></string-array>')
        self.assertIn('array_length', [e['kind'] for e in audit(root)['errors']])

    def test_valid_markup_cdata_and_escapes(self):
        source = r'<string name="x"><![CDATA[<b>%1$s</b>\n%d]]></string>'
        translated = r'<string name="x"><![CDATA[<b>%1$s</b>\n%d 项]]></string>'
        root, _ = self.fixture(source, translated)
        self.assertEqual(audit(root)['totals']['errors'], 0)

    def test_changed_markup_and_entity_fail(self):
        root, _ = self.fixture('<string name="x">&amp;<b>Text</b></string>', '<string name="x"><i>文字</i></string>')
        kinds = {e['kind'] for e in audit(root)['errors']}
        self.assertEqual(kinds, {'html_tags', 'entities'})

    def test_invalid_xml_fails(self):
        root, parent = self.fixture('<string name="x">Text</string>')
        (parent / 'values-zh-rCN').mkdir()
        (parent / 'values-zh-rCN/strings.xml').write_text('<resources><string>', encoding='utf-8')
        self.assertEqual(audit(root)['errors'][0]['kind'], 'xml')

    def test_duplicate_keys_fail(self):
        root, _ = self.fixture('<string name="x">A</string>', '<string name="x">甲</string><string name="x">乙</string>')
        self.assertEqual(audit(root)['errors'][0]['kind'], 'xml')

    def test_complete_plural_and_array_pass(self):
        en = '<plurals name="p"><item quantity="one">%d item</item><item quantity="other">%d items</item></plurals><string-array name="a"><item>%s</item><item>Two</item></string-array>'
        zh = '<plurals name="p"><item quantity="one">%d 项</item><item quantity="other">%d 项</item></plurals><string-array name="a"><item>%s</item><item>二</item></string-array>'
        root, _ = self.fixture(en, zh)
        self.assertEqual(audit(root)['totals'], {'modules': 1, 'missing_keys': 0, 'missing_items': 0, 'checked_leaves': 4, 'errors': 0})

    def test_java_format_and_controls(self):
        result = tokens(r'%1$s %d %.1f %2$tY %% %n\n\t\u00b7')
        self.assertEqual(result['placeholders'], ['%1$s', '%d', '%.1f', '%2$tY', '%%', '%n'])
        self.assertEqual(result['escapes'], [r'\n', r'\t', r'\u00b7'])

    def test_grammar_apostrophe_is_strict_by_default(self):
        root, _ = self.fixture(r'<string name="x">Can\'t save\n%s</string>', r'<string name="x">无法保存\n%s</string>')
        self.assertEqual(audit(root)['totals']['errors'], 1)
        allowed = audit(root, allow_grammar_apostrophes=True)
        self.assertEqual(allowed['totals']['errors'], 0)
        self.assertEqual(len(allowed['literal_punctuation_differences']), 1)

    def test_apostrophe_option_does_not_hide_control_errors(self):
        root, _ = self.fixture(r'<string name="x">Can\'t save\n%s</string>', r'<string name="x">无法保存%s</string>')
        self.assertEqual(audit(root, allow_grammar_apostrophes=True)['totals']['errors'], 1)


if __name__ == '__main__':
    unittest.main()
