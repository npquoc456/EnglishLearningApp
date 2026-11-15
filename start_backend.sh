#!/bin/bash
# Script khởi động Backend Spring Boot ở chế độ background

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$PROJECT_DIR/laptrinhungdungdidong"
JAR_FILE="$BACKEND_DIR/build/libs/demo-0.0.1-SNAPSHOT.jar"
LOG_FILE="$BACKEND_DIR/backend_runtime.log"

if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo ">>> [Backend] Spring Boot đang chạy sẵn trên cổng 8080."
else
    echo ">>> [Backend] Đang khởi động Spring Boot..."
    if [ ! -f "$JAR_FILE" ]; then
        echo ">>> [Backend] Đang đóng gói file JAR..."
        cd "$BACKEND_DIR" && ./gradlew bootJar -x test
    fi
    nohup java -jar "$JAR_FILE" >> "$LOG_FILE" 2>&1 &
    
    echo ">>> [Backend] Đang chờ backend sẵn sàng..."
    for i in {1..20}; do
        if nc -z localhost 8080; then
            echo ">>> [Backend] Spring Boot đã sẵn sàng trên http://localhost:8080 !"
            break
        fi
        sleep 0.5
    done
fi

# Chuyển tiếp cổng 8080 cho máy ảo/thiết bị Android nếu adb đang kết nối
if command -v adb &> /dev/null; then
    adb reverse tcp:8080 tcp:8080 2>/dev/null && echo ">>> [ADB] Đã liên kết reverse tcp:8080 tcp:8080."
fi
