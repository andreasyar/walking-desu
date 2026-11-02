# Локальные артефакты

## wand6 (актуальная линия 4.x)

| Каталог | Как получить | Назначение |
| --- | --- | --- |
| `wand6-client/` | `./gradlew :client:publishDev` | Локальный клиент со скриптами, JAR и картинками |
| `wand6-server/` | `./gradlew :server:publishDev` | Standalone JAR для Compose-сервиса `server` |

Содержимое этих каталогов в Git не хранится.

## Legacy 3.x (не обновлять из wand6)

| Каталог | Содержимое | Статус |
| --- | --- | --- |
| `client/` | Старый `walking-desu-client` и JAR 3.2.x | Только для сравнения / истории 3.x |
| `server/` | Старый `server.jar` 3.x | Не используется Compose; заменён на `wand6-server/` |

Задачи `:client:publishDev` и `:server:publishDev` **не** пишут в `client/` и `server/`.
Для стабильной 3.2.0 см. ветку `codex/game-3.x` и релиз v3.2.0.
