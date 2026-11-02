# Окружение экспериментальной линии wand6

JDK 21 и Gradle Wrapper 9.8.2. Откройте проект через Dev Containers либо
выполните `sh .devcontainer/init-host.sh` и запустите сервис `dev` из
`compose.yaml`. Исходники доступны в `/workspace`, кэш Gradle — в
`/workspace/.devcontainer/cache/gradle`.

```sh
./gradlew clean build :client:guiCheckAgent
```

Для wand6 используется отдельный локальный набор
`.devcontainer/assets/wand6/img/summer.png`.

```sh
./gradlew :client:publishDev :server:publishDev
```

- `:client:publishDev` → `.devcontainer/artifacts/wand6-client/` (скрипты, JAR, картинки)
- `:server:publishDev` → `.devcontainer/artifacts/wand6-server/wand6-server.jar` (для Compose)

Сервис Compose `server` (профиль `runtime`) монтирует `artifacts/wand6-server/`
и запускает `wand6-server.jar`. Обычная сборка картинок не требует.
Графический клиент и `tools/check-wand6-distributions.py` запускаются на хосте
с графической Java. Инструкции: [README](../README.md) и
[ресурсы](../docs/CLIENT-ASSETS.md).

Каталоги `artifacts/client/` и `artifacts/server/` — **legacy 3.x**; задачи
publishDev wand6 их не трогают. Подробнее: [artifacts/README.md](artifacts/README.md).
Файлы `.env`, ресурсы, кэши и содержимое wand6-артефактов остаются вне Git.
