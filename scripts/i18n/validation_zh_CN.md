# 简体中文验证结果

## 已完成

- 17 组基准资源均有 `values-zh-rCN/strings.xml`。
- 新增 3,269 个资源 key，修订 336 个已有 key；逐条清单见 `changes_zh_CN.md`，机器可读记录见 `translation_changes.json`。
- 缺失 key：0；缺失 plurals quantity / string-array item：0。
- Python 标准库 `xml.etree.ElementTree` 解析全部 17 个中文 XML 成功，未发现重复 key、资源类型变化或数组长度差异。
- 按 string、plurals 每个 quantity、string-array 每个 item 共检查 5,279 个叶节点。
- 格式占位符的编号、类型、精度、数量及顺序全部匹配；HTML 标签、CDATA 边界、XML 实体、换行和 Unicode 转义全部匹配。
- `app_name` 与本次任务开始时的值一致；原有品牌、图标及其他目录的修改未纳入本次编辑。
- 审计脚本的 12 个测试全部通过，覆盖缺失目录、缺失 key、复数项、数组长度、占位符换序、HTML、实体、CDATA、无效 XML、重复 key 和转义检查。
- 没有运行 Gradle，没有提交代码，没有操作桌面。

## 待确认：英文语法单引号

严格审计仍报告 **40 条 `\'` 差异**，它们全部来自英文单词内部的语法单引号，例如 `can\'t`、`device\'s` 和 `you\'ll`。自然中文无需对应的单引号，现有中文也大多已经省略了它们。

原任务明确要求转义逐字符一致，因此这些差异保留为严格审计错误，没有静默豁免，也没有在中文里添加无意义的单引号。已请求确认是否允许仅省略这一类语法标点；未确认前，**不能宣称严格转义检查全部通过**。

逐 key 的严格差异见 `audit_report.json`。`audit_punctuation_review.json` 仅用于评审该例外：开启显式选项后，错误为 0，仍完整记录全部 40 条差异。该报告不代表已经批准放宽要求。

## 复核命令

```powershell
python scripts/i18n/test_zh_cn_audit.py
python scripts/i18n/zh_cn_audit.py --json scripts/i18n/audit_report.json
```

第二条命令当前退出码为 1，只因上述语法单引号。批准例外后，可显式运行：

```powershell
python scripts/i18n/zh_cn_audit.py --allow-grammar-apostrophes --json scripts/i18n/audit_punctuation_review.json
```

审计脚本仅使用 Python 标准库，不需要安装依赖。它会新建缺少的中文目录，但不会写入或改写任何翻译。当前已完成 Python 静态验证，未进行 Android 编译或真机界面测试。
