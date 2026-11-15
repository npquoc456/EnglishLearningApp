#!/bin/bash
# Dừng Backend Spring Boot
echo ">>> Đang tắt tiến trình Spring Boot trên cổng 8080..."
fuser -k 8080/tcp 2>/dev/null || pkill -f "demo-0.0.1-SNAPSHOT.jar" || true
echo ">>> Backend đã được tắt thành công!"
