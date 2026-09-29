@echo off
REM ============================================================
REM  重置数据库到初始测试数据
REM  每次录制视频 / 截图演示前执行一次，保证数据干净可复现
REM ============================================================
chcp 65001 >nul
echo 正在重建 ecommerce 数据库...
mysql -uroot -p123456 --default-character-set=utf8mb4 < src\main\resources\db.sql
if %errorlevel% neq 0 (
    echo [失败] 请确认 MySQL 已启动，且密码正确（当前配置：123456）
    pause
    exit /b 1
)
echo [成功] 数据库已重置为初始状态。
pause
