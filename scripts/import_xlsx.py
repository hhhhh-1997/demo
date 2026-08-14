#!/usr/bin/env python3
"""读取 data/人员月度绩效_2026-05至07.xlsx，生成 database/seed.sql。

xlsx 结构：sheet1「绩效原始数据」第 1 行为字段名（A~T 共 20 列），
第 2 行为中文注释，第 3 行起为 642 行数据。
关键口径：空单元格 → NULL；含 <v>0</v> 的数值单元格 → 0，二者必须区分。
"""
import re
import zipfile
import xml.etree.ElementTree as ET
from collections import Counter

NS = '{http://schemas.openxmlformats.org/spreadsheetml/2006/main}'

COLUMNS = ["id", "user_id", "realname", "month", "task_finish_rate",
           "work_effect_rate", "work_normativity", "learning_improvement",
           "software_design", "pre_sales_support", "bug_condition",
           "system_design", "code_review", "test_quality", "dept",
           "role", "role_name", "dept_name", "top_dept_id", "top_dept_name"]


def col_index(ref):
    m = re.match(r'([A-Z]+)', ref)
    n = 0
    for ch in m.group(1):
        n = n * 26 + (ord(ch) - 64)
    return n - 1


def parse_sheet(xlsx_path, sheet_path):
    z = zipfile.ZipFile(xlsx_path)
    root = ET.fromstring(z.read(sheet_path))
    rows = []
    for r in root.findall(f'.//{NS}sheetData/{NS}row'):
        cells = {}
        for c in r.findall(f'{NS}c'):
            ref = c.attrib.get('r')
            t = c.attrib.get('t')
            v = c.find(f'{NS}v')
            if t == 'inlineStr':
                is_el = c.find(f'{NS}is')
                val = ''.join(x.text or '' for x in is_el.iter(f'{NS}t'))
            elif v is None:
                val = None
            else:
                val = v.text
            cells[col_index(ref)] = val
        rows.append([cells.get(i) for i in range(20)])
    return rows


def esc(v):
    if v is None:
        return 'NULL'
    return "'" + v.replace("\\", "\\\\").replace("'", "''") + "'"


def main():
    xlsx = "data/人员月度绩效_2026-05至07.xlsx"
    out = "database/seed.sql"
    rows = parse_sheet(xlsx, 'xl/worksheets/sheet1.xml')
    data = rows[2:]  # rows[0] 字段名、rows[1] 中文注释
    assert len(data) == 642, f"expect 642 data rows, got {len(data)}"

    lines = ["-- 绩效月报查询 · 种子数据（由 scripts/import_xlsx.py 自动生成）",
             "USE `default_db`;",
             ""]
    col_sql = ", ".join(f"`{c}`" for c in COLUMNS)
    lines.append(f"INSERT INTO `monthly_performance` ({col_sql}) VALUES")
    lines.append(",\n".join("(" + ", ".join(esc(v) for v in r) + ")" for r in data) + ";")

    with open(out, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

    print(f"wrote {out}: {len(data)} rows")
    months = Counter(r[3] for r in data)
    print("month distribution:", dict(months))
    bug = [r[10] for r in data]
    print("bug_condition NULL:", sum(1 for v in bug if v is None),
          "0:", sum(1 for v in bug if v == '0'))


if __name__ == "__main__":
    main()
