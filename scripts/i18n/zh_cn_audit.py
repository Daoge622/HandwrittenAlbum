#!/usr/bin/env python3
"""Audit all app/**/values/strings.xml against values-zh-rCN (stdlib only).

Run from any directory: python scripts/i18n/zh_cn_audit.py
--json PATH writes the full machine-readable result. Missing locale directories
are created, but resource files and translations are never changed by the audit.
Exit status: 0 if complete and valid, 1 for any validation error.
"""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
KINDS = {'string', 'plurals', 'string-array'}
FORMAT = re.compile(r'%(?:\d+\$)?[-#+ 0,(<]*\d*(?:\.\d+)?(?:[tT][a-zA-Z]|[bBhHsScCdoxXeEfgGaAn%])')
ESCAPE = re.compile(r'\\(?:u[0-9a-fA-F]{4}|.)')
BLOCK = re.compile(r'<(string|plurals|string-array)\b[^>]*\bname="([^"]+)"[^>]*>([\s\S]*?)</\1>')
ITEM = re.compile(r'<item\b([^>]*)>([\s\S]*?)</item>')
TAG = re.compile(r'</?[A-Za-z][^>]*>')
ENTITY = re.compile(r'&(?:#\d+|#x[0-9a-fA-F]+|[a-zA-Z]+);')


def read_resources(path: Path):
    text = path.read_text(encoding='utf-8-sig')
    tree = ET.fromstring(text)
    if tree.tag != 'resources':
        raise ValueError('Root must be <resources>')
    resources = {}
    for element in tree:
        if element.tag not in KINDS:
            continue
        key = element.attrib['name']
        if key in resources:
            raise ValueError(f'Duplicate key: {key}')
        resources[key] = element
    raw = {m[2]: m[3] for m in BLOCK.finditer(text)}
    return resources, raw


def leaves(element, raw):
    if element.tag == 'string':
        return {'text': raw}
    out = {}
    for index, match in enumerate(ITEM.finditer(raw)):
        quantity = re.search(r'quantity="([^"]+)"', match[1])
        key = quantity[1] if quantity else str(index)
        if key in out:
            raise ValueError(f'Duplicate item: {key}')
        out[key] = match[2]
    return out


def tokens(value):
    # CDATA delimiters are structure, not HTML. Contents can be translated.
    plain = value.replace('<![CDATA[', '').replace(']]>', '')
    return {
        'placeholders': FORMAT.findall(value),
        'escapes': ESCAPE.findall(value),
        'html_tags': TAG.findall(plain),
        'entities': ENTITY.findall(value),
        'cdata': re.findall(r'<!\[CDATA\[|\]\]>', value),
    }


def audit(root=ROOT, allow_grammar_apostrophes=False):
    result = {'modules': [], 'errors': [], 'literal_punctuation_differences': []}
    for source in sorted((root / 'app').rglob('values/strings.xml')):
        if 'build' in source.relative_to(root).parts:
            continue
        target = source.parent.parent / 'values-zh-rCN' / 'strings.xml'
        target.parent.mkdir(parents=True, exist_ok=True)
        module = {'source': source.relative_to(root).as_posix(),
                  'target': target.relative_to(root).as_posix(),
                  'missing_keys': [], 'missing_items': [], 'extra_keys': [],
                  'checked_leaves': 0}
        result['modules'].append(module)
        try:
            en, eraw = read_resources(source)
            zh, zraw = read_resources(target) if target.exists() else ({}, {})
            module['source_count'] = len(en)
            module['translated_count'] = len(zh)
            module['missing_keys'] = sorted(en.keys() - zh.keys())
            module['extra_keys'] = sorted(zh.keys() - en.keys())
            for key in sorted(en.keys() & zh.keys()):
                if en[key].tag != zh[key].tag:
                    result['errors'].append({'path': module['target'], 'key': key, 'kind': 'resource_type'})
                    continue
                el, zl = leaves(en[key], eraw.get(key, '')), leaves(zh[key], zraw.get(key, ''))
                if en[key].tag == 'plurals' and 'other' not in zl:
                    result['errors'].append({'path': module['target'], 'key': key, 'kind': 'missing_other'})
                if en[key].tag == 'string-array' and len(el) != len(zl):
                    result['errors'].append({'path': module['target'], 'key': key, 'kind': 'array_length'})
                for item in el:
                    if item not in zl:
                        module['missing_items'].append(key + '/' + item)
                        continue
                    module['checked_leaves'] += 1
                    a, b = tokens(el[item]), tokens(zl[item])
                    for kind in a:
                        if a[kind] != b[kind]:
                            error = {'path': module['target'], 'key': key, 'item': item,
                                     'kind': kind, 'source': a[kind], 'translation': b[kind]}
                            if kind == 'escapes':
                                # This explicit opt-in applies only to escaped
                                # apostrophes inside English words (can't,
                                # device's). All other escapes remain strict.
                                unquoted = re.sub(r"(?<=[A-Za-z])\\'(?=[A-Za-z])", '', el[item])
                                if ESCAPE.findall(unquoted) == b[kind]:
                                    result['literal_punctuation_differences'].append(error)
                                    if allow_grammar_apostrophes:
                                        continue
                            result['errors'].append(error)
        except (ET.ParseError, OSError, ValueError, KeyError) as exc:
            result['errors'].append({'path': module['target'], 'kind': 'xml', 'message': str(exc)})
    result['totals'] = {
        'modules': len(result['modules']),
        'missing_keys': sum(len(m['missing_keys']) for m in result['modules']),
        'missing_items': sum(len(m['missing_items']) for m in result['modules']),
        'checked_leaves': sum(m['checked_leaves'] for m in result['modules']),
        'errors': len(result['errors']),
    }
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--json', type=Path, help='Write detailed JSON report')
    parser.add_argument('--allow-grammar-apostrophes', action='store_true',
                        help="Explicitly allow omission of English grammatical \\' only; record every difference")
    args = parser.parse_args()
    result = audit(allow_grammar_apostrophes=args.allow_grammar_apostrophes)
    result['policy'] = {'allow_grammar_apostrophes': args.allow_grammar_apostrophes}
    for m in result['modules']:
        print(f"{m['target']}: missing={len(m['missing_keys'])}, missing_items={len(m['missing_items'])}, total={m.get('source_count', '?')}")
        for key in m['missing_keys'] + m['missing_items']:
            print('  MISSING ' + key)
    for error in result['errors']:
        print('ERROR ' + json.dumps(error, ensure_ascii=False))
    print(json.dumps(result['totals'], ensure_ascii=False))
    if args.json:
        args.json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return int(bool(result['errors'] or result['totals']['missing_keys'] or result['totals']['missing_items']))


if __name__ == '__main__':
    raise SystemExit(main())
