#!/bin/bash

# Kover 覆盖率报告生成脚本 - 所有模块版本
# 用法: ./scripts/kover-report-all.sh [模块名]

MODULE="${1:-all}"

echo "================================"
echo "生成 Kover 覆盖率报告"
echo "================================"
echo ""

if [ "$MODULE" = "all" ]; then
    echo "为所有可用模块生成报告..."
    echo ""
    ./gradlew allCoverage
    exit 0
fi

# 单个模块处理
echo "模块: $MODULE"
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
    REPORT_PATH="${MODULE//:/\/}"
    echo "   位置: $REPORT_PATH/build/reports/kover/htmlJvm/index.html"
else
    echo "⚠️  HTML 报告生成失败"
fi

echo ""

# 生成 XML 报告
echo "3. 生成 XML 报告..."
./gradlew :$MODULE:koverXmlReportJvm --quiet

if [ $? -eq 0 ]; then
    echo "✅ XML 报告生成成功"
else
    echo "⚠️  XML 报告生成失败"
fi

echo ""
echo "================================"
echo "报告生成完成！"
echo "================================"
