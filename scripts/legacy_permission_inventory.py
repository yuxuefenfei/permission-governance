#!/usr/bin/env python3
"""盘点旧 @PreAuthorize 中的权限码（静态线索，不是完整 SpEL 解析器）。

使用：python3 scripts/legacy_permission_inventory.py /path/to/project output.csv
输出中的“疑似权限码”需要人工确认；动态 SpEL 常量、嵌套注解不能靠正则完整识别。
"""
import argparse
import csv
import re
from pathlib import Path

ANNOTATION = re.compile(r"@PreAuthorize\s*\((.{0,1200}?)\)\s*", re.S)
LITERAL = re.compile(r"""['"]([a-zA-Z][a-zA-Z0-9]*(?::[a-zA-Z0-9*]+){2,6})['"]""")


def main() -> None:
    parser = argparse.ArgumentParser(description="提取现有 Java 项目中的权限字符串")
    parser.add_argument("source", type=Path, help="包含 .java 文件的目录")
    parser.add_argument("output", type=Path, help="CSV 输出路径")
    args = parser.parse_args()
    if not args.source.is_dir():
        parser.error("source 必须是存在的目录")
    records = []
    for java in args.source.rglob("*.java"):
        content = java.read_text(encoding="utf-8", errors="replace")
        for m in ANNOTATION.finditer(content):
            line = content.count("\n", 0, m.start()) + 1
            expr = " ".join(m.group(1).split())
            for code in LITERAL.findall(expr):
                records.append((str(java.relative_to(args.source)), line, code, expr))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8-sig", newline="") as stream:
        writer = csv.writer(stream)
        writer.writerow(["source_file", "line", "candidate_permission_code", "expression_preview"])
        writer.writerows(records)
    print(f"已输出 {len(records)} 条候选权限到 {args.output}；动态 SpEL 请人工复核。")


if __name__ == "__main__":
    main()
