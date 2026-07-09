#!/bin/bash
# αchat 生产打包脚本：前端构建 → 合并到后端 → 打包可运行JAR
set -e

echo "=== 1/4 构建前端 ==="
cd chat-web-front

# 设置生产 base 路径
sed -i "s|plugins: \[vue()\]|base: '/chat/', plugins: [vue()]|" vite.config.ts
sed -i "s|createWebHistory()|createWebHistory('/chat/')|" src/router/index.ts

npm run build

# 恢复开发配置
sed -i "s|base: '/chat/', plugins: \[vue()\]|plugins: [vue()]|" vite.config.ts
sed -i "s|createWebHistory('/chat/')|createWebHistory()|" src/router/index.ts

echo "=== 2/4 复制前端到后端静态资源 ==="
cd ..
mkdir -p chat-web/src/main/resources/static
cp -r chat-web-front/dist/* chat-web/src/main/resources/static/

echo "=== 3/4 Maven打包 ==="
./mvnw clean package -DskipTests

echo "=== 4/4 完成 ==="
cp chat-web/target/chat-web-0.0.1-SNAPSHOT.jar ./achat.jar
echo "JAR文件: $(pwd)/achat.jar"
echo ""
echo "运行方式: java -jar achat.jar"
echo "浏览器访问: http://localhost:1000/chat/"
