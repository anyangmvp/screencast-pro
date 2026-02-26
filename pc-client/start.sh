#!/bin/bash

echo "============================================"
echo "    Screen Cast Pro - 电脑投屏到安卓TV"
echo "============================================"
echo ""

# 检查 Java
if [ -z "$JAVA_HOME" ]; then
    echo "错误: 未设置 JAVA_HOME 环境变量"
    echo "请确保已安装 Java 17 或更高版本"
    exit 1
fi

echo "使用 Java: $JAVA_HOME"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIB_DIR="$SCRIPT_DIR/target/lib"
JAR_FILE="$SCRIPT_DIR/target/pc-client-1.0.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "错误: 找不到主程序文件 $JAR_FILE"
    echo "请先运行: mvn clean package"
    exit 1
fi

if [ ! -d "$LIB_DIR" ]; then
    echo "错误: 找不到依赖库目录 $LIB_DIR"
    echo "请先运行: mvn clean package"
    exit 1
fi

echo "启动程序..."
echo ""

java -jar "$JAR_FILE"

if [ $? -ne 0 ]; then
    echo ""
    echo "程序异常退出"
    read -p "按回车键继续..."
fi
