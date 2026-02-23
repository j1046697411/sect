#!/usr/bin/env python3
"""
记忆库索引生成脚本
扫描 docs/memory-bank/ 目录，自动生成 index.md 索引文件
"""

from __future__ import annotations
import os
import re
from pathlib import Path
from collections import defaultdict
from datetime import datetime

SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent.parent.parent.parent
BASE_DIR = PROJECT_ROOT / "docs" / "memory-bank"
OUTPUT_FILE = BASE_DIR / "index.md"

def parse_memory_file(file_path: Path) -> dict:
    """解析记忆文件，提取元数据"""
    content = file_path.read_text(encoding="utf-8")
    
    result = {
        "title": "",
        "category": file_path.parent.name,
        "tags": [],
        "status": "active",
        "created": "",
        "summary": "",
        "file": file_path.name
    }
    
    lines = content.split("\n")
    
    for i, line in enumerate(lines):
        line = line.strip()
        
        if line.startswith("## "):
            result["title"] = line[3:].strip()
        
        elif line.startswith("> 创建时间:"):
            result["created"] = line.split(":", 1)[1].strip()
        
        elif line.startswith("> 分类:"):
            result["category"] = line.split(":", 1)[1].strip()
        
        elif line.startswith("> 状态:"):
            result["status"] = line.split(":", 1)[1].strip()
        
        elif line.startswith("> 标签:"):
            tags_str = line.split(":", 1)[1].strip()
            result["tags"] = [t.strip() for t in tags_str.split() if t.startswith("#")]
        
        elif line.startswith("**问题/背景**:"):
            summary_lines = []
            for j in range(i + 1, min(i + 4, len(lines))):
                if lines[j].startswith("**") or not lines[j].strip():
                    break
                summary_lines.append(lines[j].strip())
            result["summary"] = "".join(summary_lines)[:50]
        
        if result["title"] and result["tags"]:
            break
    
    return result

def scan_memories() -> list[dict]:
    """扫描所有记忆文件"""
    memories = []
    
    for category in ["conventions", "solutions", "lessons", "preferences"]:
        category_dir = BASE_DIR / category
        if not category_dir.exists():
            continue
        
        for file_path in category_dir.glob("*.md"):
            if file_path.name == "index.md":
                continue
            
            memory = parse_memory_file(file_path)
            if memory["title"]:
                memories.append(memory)
    
    return memories

def generate_index(memories: list[dict]) -> str:
    """生成 index.md 内容"""
    
    by_system = defaultdict(list)
    by_category = defaultdict(list)
    
    system_tags = {
        "#角色系统", "#经济系统", "#战斗系统", "#功法系统", 
        "#设施系统", "#可玩性", "#ecs", "#kotlin", "#compose",
        "#测试", "#记忆库", "#规范"
    }
    
    for m in memories:
        by_category[m["category"]].append(m)
        
        for tag in m["tags"]:
            if tag in system_tags:
                by_system[tag].append(m)
                break
    
    lines = [
        "# 记忆库索引",
        "",
        f"> 更新时间: {datetime.now().strftime('%Y-%m-%d')}",
        f"> 总计: {len(memories)} 条记忆",
        "",
        "---",
        ""
    ]
    
    lines.append("## 按系统域检索\n")
    
    for system in sorted(by_system.keys()):
        items = by_system[system]
        lines.append(f"### {system} ({len(items)})")
        lines.append("| 标题 | 分类 | 内容摘要 |")
        lines.append("|------|------|----------|")
        
        for m in sorted(items, key=lambda x: x["title"]):
            rel_path = f"{m['category']}/{m['file']}"
            lines.append(f"| [{m['title']}]({rel_path}) | {m['category']} | {m['summary'][:30]}... |")
        
        lines.append("")
    
    lines.append("---\n")
    lines.append("## 按分类检索\n")
    
    category_names = {
        "conventions": "技术规范与约定",
        "solutions": "问题解决方案",
        "lessons": "错误教训",
        "preferences": "用户偏好"
    }
    
    for category in ["conventions", "solutions", "lessons", "preferences"]:
        items = by_category.get(category, [])
        if not items:
            continue
        
        lines.append(f"### {category} ({len(items)})")
        lines.append(category_names.get(category, ""))
        lines.append("")
        lines.append("| 标题 | 标签 |")
        lines.append("|------|------|")
        
        for m in sorted(items, key=lambda x: x["title"]):
            rel_path = f"{m['category']}/{m['file']}"
            tags_str = " ".join(m["tags"])
            lines.append(f"| [{m['title']}]({rel_path}) | {tags_str} |")
        
        lines.append("")
    
    lines.append("---\n")
    lines.append("## 快速搜索\n")
    lines.append("**输入关键词**：\n")
    
    keyword_map = {
        "弟子/角色": "#角色系统",
        "经济/灵石": "#经济系统",
        "战斗": "#战斗系统",
        "功法": "#功法系统",
        "设施": "#设施系统",
        "ecs": "#ecs",
        "kotlin": "#kotlin",
        "compose": "#compose",
        "bug/错误": "lessons",
    }
    
    for keyword, target in keyword_map.items():
        lines.append(f"- {keyword} → [{target}](#{target.strip('#')})")
    
    return "\n".join(lines)

def main():
    """主函数"""
    print("扫描记忆文件...")
    memories = scan_memories()
    print(f"找到 {len(memories)} 条记忆")
    
    print("生成索引...")
    index_content = generate_index(memories)
    
    print(f"写入 {OUTPUT_FILE}...")
    OUTPUT_FILE.write_text(index_content, encoding="utf-8")
    
    print("完成！")

if __name__ == "__main__":
    main()
