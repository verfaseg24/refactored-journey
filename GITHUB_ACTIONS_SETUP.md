# Настройка GitHub Actions для автоматической сборки

## Созданные workflows

### 1. `build-custom-debug.yml` - Автоматическая сборка Debug APK
**Когда запускается:**
- При каждом push в ветки `main` или `develop`
- Можно запустить вручную через GitHub UI

**Что делает:**
- Собирает debug версию APK
- Загружает APK как артефакт (доступен 30 дней)

**Как скачать:**
1. Зайдите в репозиторий на GitHub
2. Перейдите в `Actions`
3. Выберите последний успешный запуск
4. Скачайте артефакт `custom-browser-debug`

### 2. `build-custom-release.yml` - Сборка Release APK с подписью
**Когда запускается:**
- При создании тега (например, `v1.0.0`)
- Можно запустить вручную

**Что делает:**
- Собирает release версию APK
- Подписывает APK вашим ключом
- Создает GitHub Release
- Прикрепляет подписанный APK к релизу

## Быстрый старт (Debug версия)

Самый простой способ - использовать debug версию без подписи:

1. **Запушьте код в GitHub:**
```bash
git add .
git commit -m "Add custom modifications"
git push origin main
```

2. **Дождитесь сборки:**
   - Зайдите в `Actions` на GitHub
   - Дождитесь завершения workflow `Build Custom Debug APK`

3. **Скачайте APK:**
   - Откройте успешный запуск
   - Скачайте артефакт `custom-browser-debug`
   - Распакуйте ZIP и установите APK

## Настройка Release версии (с подписью)

Для создания подписанной release версии нужно настроить секреты:

### Шаг 1: Создайте keystore (если нет)

```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

### Шаг 2: Конвертируйте keystore в base64

```bash
# Linux/Mac
base64 my-release-key.jks | tr -d '\n' > keystore.txt

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("my-release-key.jks")) > keystore.txt
```

### Шаг 3: Добавьте секреты в GitHub

1. Зайдите в `Settings` → `Secrets and variables` → `Actions`
2. Нажмите `New repository secret`
3. Добавьте следующие секреты:

| Имя секрета | Значение |
|-------------|----------|
| `SIGNING_KEY` | Содержимое файла `keystore.txt` (base64) |
| `KEY_ALIAS` | Алиас ключа (например, `my-key-alias`) |
| `KEY_STORE_PASSWORD` | Пароль keystore |
| `KEY_PASSWORD` | Пароль ключа |

### Шаг 4: Создайте релиз

```bash
# Создайте тег
git tag v1.0.0
git push origin v1.0.0
```

Workflow автоматически:
- Соберет APK
- Подпишет его
- Создаст GitHub Release
- Прикрепит APK к релизу

## Ручной запуск workflow

1. Зайдите в `Actions`
2. Выберите нужный workflow
3. Нажмите `Run workflow`
4. Выберите ветку и нажмите `Run workflow`

## Структура файлов

```
.github/
└── workflows/
    ├── build-custom-debug.yml    # Debug сборка (простая)
    └── build-custom-release.yml  # Release сборка (с подписью)
```

## Troubleshooting

### Ошибка: "Gradle build failed"
- Проверьте, что все зависимости доступны
- Убедитесь, что код компилируется локально

### Ошибка: "Signing failed"
- Проверьте правильность секретов
- Убедитесь, что base64 строка не содержит переносов строк

### APK не устанавливается
- Debug APK: Разрешите установку из неизвестных источников
- Release APK: Убедитесь, что APK правильно подписан

## Рекомендации

1. **Для разработки:** Используйте `build-custom-debug.yml`
2. **Для публикации:** Используйте `build-custom-release.yml` с подписью
3. **Версионирование:** Используйте семантическое версионирование (v1.0.0, v1.1.0, и т.д.)

## Альтернатива: Сборка без GitHub Actions

Если не хотите настраивать GitHub Actions, можете собрать локально:

```bash
# Debug версия
./gradlew assembleInternalDebug

# Release версия (нужен keystore)
./gradlew assemblePlayRelease
```

APK будет в `app/build/outputs/apk/`
