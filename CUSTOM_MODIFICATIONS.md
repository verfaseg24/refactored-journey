# Кастомные модификации браузера DuckDuckGo

## Внесенные изменения

### 1. Изменение поисковой системы по умолчанию на Google

**Измененные файлы:**
- `app/src/main/java/com/duckduckgo/app/browser/omnibar/QueryUrlConverter.kt`
- `common/common-utils/src/main/java/com/duckduckgo/common/utils/AppUrl.kt`

**Что изменено:**
- Поисковые запросы теперь направляются на `https://www.google.com/search?q=<запрос>` вместо DuckDuckGo
- Домашняя страница изменена на `https://www.google.com`

### 2. Система автоматического прокси

**Новые файлы:**
- `app/src/main/java/com/duckduckgo/app/proxy/ProxyManager.kt`

**Измененные файлы:**
- `app/src/main/java/com/duckduckgo/app/di/NetworkModule.kt`
- `app/src/main/java/com/duckduckgo/app/global/DuckDuckGoApplication.kt`

**Функционал:**
- Автоматическая загрузка списка прокси из: https://github.com/verfaseg24/cuddly-octo-adventure/raw/main/valid_proxies.txt
- Поддержка протоколов: SOCKS, HTTP, HTTPS
- Автоматическая проверка работоспособности прокси перед подключением
- Инициализация при запуске приложения
- Fallback на прямое подключение, если рабочий прокси не найден

**Формат прокси в файле:**
```
http://host:port
https://host:port
socks://host:port
socks5://host:port
host:port  (по умолчанию HTTP)
```

## Сборка проекта

1. Откройте проект в Android Studio
2. Синхронизируйте Gradle: `File -> Sync Project with Gradle Files`
3. Соберите проект: `Build -> Make Project`
4. Запустите на устройстве/эмуляторе: `Run -> Run 'app'`

## Требования

- Android Studio Arctic Fox или новее
- JDK 11 или новее
- Android SDK API 21+
- Gradle 7.0+

## Примечания

- ProxyManager автоматически инициализируется при запуске приложения
- Проверка прокси выполняется с таймаутом 5 секунд
- Если все прокси недоступны, используется прямое подключение
- Логи работы прокси можно увидеть в Logcat с тегом "ProxyManager"

## Отладка

Для просмотра логов работы прокси в Android Studio:
1. Откройте Logcat
2. Фильтр: `tag:ProxyManager`
3. Вы увидите сообщения о загрузке прокси, проверке и выборе рабочего прокси
