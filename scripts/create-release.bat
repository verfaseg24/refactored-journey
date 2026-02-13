@echo off
REM Скрипт для быстрого создания релиза (Windows)

echo.
echo 🚀 Создание нового релиза кастомного браузера
echo.

REM Проверка git
git rev-parse --git-dir >nul 2>&1
if errorlevel 1 (
    echo ❌ Ошибка: Это не git репозиторий
    exit /b 1
)

REM Получаем текущую версию
for /f "delims=" %%i in ('git describe --tags --abbrev=0 2^>nul') do set CURRENT_VERSION=%%i
if "%CURRENT_VERSION%"=="" set CURRENT_VERSION=v0.0.0

echo 📌 Текущая версия: %CURRENT_VERSION%
echo.

REM Запрашиваем новую версию
set /p NEW_VERSION="Введите новую версию (например, v1.0.0): "

REM Проверка что версия не пустая
if "%NEW_VERSION%"=="" (
    echo ❌ Версия не может быть пустой
    exit /b 1
)

echo.
echo 📝 Создание релиза %NEW_VERSION%
echo.

REM Проверка незакоммиченных изменений
git status --short | findstr /r "." >nul
if not errorlevel 1 (
    echo ⚠️  Обнаружены незакоммиченные изменения:
    git status --short
    echo.
    set /p COMMIT_CHANGES="Закоммитить изменения? (y/n): "
    
    if /i "%COMMIT_CHANGES%"=="y" (
        set /p COMMIT_MESSAGE="Сообщение коммита: "
        git add .
        git commit -m "!COMMIT_MESSAGE!"
        echo ✅ Изменения закоммичены
    ) else (
        echo ❌ Отменено. Сначала закоммитьте изменения.
        exit /b 1
    )
)

REM Создание тега
echo.
echo 🏷️  Создание тега %NEW_VERSION%...
git tag -a "%NEW_VERSION%" -m "Release %NEW_VERSION%"

REM Пуш тега
echo 📤 Отправка тега на GitHub...
git push origin "%NEW_VERSION%"

echo.
echo ✅ Готово!
echo.
echo 📦 GitHub Actions автоматически:
echo    1. Соберет APK
echo    2. Подпишет его (если настроены секреты)
echo    3. Создаст GitHub Release
echo    4. Прикрепит APK к релизу
echo.
echo 🔗 Проверьте статус сборки в GitHub Actions
echo 📥 После завершения скачайте APK из Releases
echo.
pause
