#!/bin/bash

# Kover 覆盖率报告生成脚本
# 用法: ./scripts/kover-report.sh [模块名]

MODULE="${1:-libs:lko-ecs}"

echo "================================"
echo "生成 Kover 覆盖率报告"
echo "模块: $MODULE"
echo "================================"
echo ""

# 运行测试并生成报告
echo "1. 运行测试..."
./gradlew :$MODULE:test --quiet

if [ $? -ne 0 ]; then
    echo "❌ 测试失败，请修复后再生成报告"
    exit 1
fi

echo "✅ 测试通过"
echo ""

# 生成 HTML 报告
echo "2. 生成 HTML 报告..."
./gradlew :$MODULE:koverHtmlReportJvm --quiet

if [ $? -eq 0 ]; then
    echo "✅ HTML 报告生成成功"
    echo "   位置: libs/lko-ecs/build/reports/kover/htmlJvm/index.html"
else
    echo "⚠️  HTML 报告生成失败"
fi

echo ""

# 生成 XML 报告
echo "3. 生成 XML 报告..."
./gradlew :$MODULE:koverXmlReportJvm --quiet

if [ $? -eq 0 ]; then
    echo "✅ XML 报告生成成功"
    echo "   位置: libs/lko-ecs/build/reports/kover/reportJvm.xml"
else
    echo "⚠️  XML 报告生成失败"
fi

echo ""
echo "================================"
echo "报告生成完成！"
echo "================================"
echo ""
echo "查看 HTML 报告:"
echo "  open libs/lko-ecs/build/reports/kover/htmlJvm/index.html"
echo ""

# 尝试自动打开报告（macOS）
if command -v open &> /dev/null; then
    if [ -f "libs/lko-ecs/build/reports/kover/htmlJvm/index.html" ]; then
        echo "正在打开报告..."
        open libs/lko-ecs/build/reports/kover/htmlJvm/index.html
    fi
fi
