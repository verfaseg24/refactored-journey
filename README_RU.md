# Модифицированный браузер DuckDuckGo для Android

## Что изменено

### ✅ 1. Поиск по умолчанию - Google
Все поисковые запросы теперь идут через Google вместо DuckDuckGo.

### ✅ 2. Автоматическая система прокси
- При запуске браузер автоматически загружает список прокси
- Проверяет каждый прокси на работоспособность
- Подключается к первому рабочему прокси
- Поддерживает: SOCKS, HTTP, HTTPS
- Источник прокси: https://github.com/verfaseg24/cuddly-octo-adventure/raw/main/valid_proxies.txt

## Автоматическая сборка через GitHub Actions

### Настройка GitHub Actions

1. **Форкните репозиторий** на свой GitHub аккаунт

2. **Настройте Secrets (опционально):**
   - Перейдите в `Settings -> Secrets and variables -> Actions`
   - Добавьте секреты (если есть):
     - `DEBUG_PROPERTIES` - base64 закодированный файл свойств
     - `DEBUG_KEY` - base64 закодированный keystore
     - `MALICIOUS_SITE_PROTECTION_AUTH_TOKEN` - токен защиты

3. **Запуск сборки:**

   **Вариант A: Debug сборка (автоматически при push)**
   ```bash
   git add .
   git commit -m "Update custom browser"
   git push origin main
   ```
   - Workflow: `.github/workflows/build-custom-debug.yml`
   - Запускается автоматически при push в main/develop
   - Создает debug APK
   - Доступен в разделе Actions -> Artifacts

   **Вариант B: Release сборка (через тег)**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
   - Workflow: `.github/workflows/build-custom-release.yml`
   - Создает release APK
   - Автоматически создает GitHub Release
   - APK доступен в разделе Releases

   **Вариант C: Ручной запуск**
   - Перейдите в `Actions` на GitHub
   - Выберите workflow (Debug или Release)
   - Нажмите `Run workflow`
   - Выберите ветку и запустите

4. **Скачивание APK:**
   - **Debug:** `Actions -> выберите workflow -> Artifacts -> скачайте custom-browser-debug`
   - **Release:** `Releases -> Latest release -> скачайте APK`

### Структура Workflows

**build-custom-debug.yml** - Debug сборка
- Запускается при push в main/develop
- Можно запустить вручную
- Создает debug APK для тестирования
- Использует dummy keystore если секреты не настроены

**build-custom-release.yml** - Release сборка
- Запускается при создании тега (v*)
- Можно запустить вручную
- Создает release APK
- Автоматически публикует в GitHub Releases

### Требования для сборки

Workflows автоматически устанавливают:
- ✅ JDK (из `.github/.java-version`)
- ✅ Go 1.18.3
- ✅ Android NDK 21.4.7075529
- ✅ Gradle с кешированием
- ✅ Все зависимости проекта

## Локальная сборка

### Как собрать локально

1. Установите Android Studio
2. Откройте проект
3. Нажмите `Build -> Make Project`
4. Запустите на устройстве: `Run -> Run 'app'`

### Сборка через командную строку

```bash
# Debug сборка
./gradlew assembleInternalDebug

# Release сборка
./gradlew assembleInternalRelease

# APK будет в: app/build/outputs/apk/
```

## Технические детали

**Измененные файлы:**
- `QueryUrlConverter.kt` - изменен URL поиска на Google
- `AppUrl.kt` - изменена домашняя страница на Google
- `ProxyManager.kt` - новый класс для управления прокси
- `NetworkModule.kt` - интеграция прокси в сетевой слой
- `DuckDuckGoApplication.kt` - инициализация прокси при запуске

**Как работает прокси:**
1. При запуске приложения загружается список прокси
2. Каждый прокси проверяется подключением к google.com (таймаут 5 сек)
3. Выбирается первый рабочий прокси
4. Если все прокси не работают - используется прямое подключение

**Логи:**
Откройте Logcat в Android Studio и фильтруйте по тегу `ProxyManager` чтобы видеть работу системы прокси.

## Требования

- Android Studio Arctic Fox+
- JDK 11+
- Android SDK API 21+
- Android NDK 21.4.7075529

## Примечание

Это модификация официального браузера DuckDuckGo. Все изменения документированы в файле `CUSTOM_MODIFICATIONS.md`.
