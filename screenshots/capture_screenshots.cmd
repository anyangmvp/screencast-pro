@echo off
chcp 65001 >nul
echo ==========================================
echo 投屏TV应用截图工具
echo ==========================================
echo.

REM 创建截图目录
if not exist screenshots mkdir screenshots

echo 正在截取TV屏幕...
adb shell screencap -p /sdcard/screenshot_tv.png
adb pull /sdcard/screenshot_tv.png screenshots/Screenshot-TV.png
echo TV屏幕截图已保存到 screenshots/Screenshot-TV.png
echo.

echo ==========================================
echo 截图任务完成！
echo ==========================================
pause
