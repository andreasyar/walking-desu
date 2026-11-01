# Окружение экспериментальной линии wand6

JDK 21 и Gradle Wrapper 9.8.2. Откройте проект через Dev Containers либо
выполните `sh .devcontainer/init-host.sh` и запустите сервис `dev` из
`compose.yaml`. Исходники доступны в `/workspace`, кэш Gradle — в
`/workspace/.devcontainer/cache/gradle`.

```sh
./gradlew clean build :client:guiCheckAgent
```

Для wand6 используется отдельный локальный набор
`.devcontainer/assets/wand6/img/summer.png`. `:client:publishDev` готовит
`.devcontainer/artifacts/wand6-client/`; обычная сборка ресурсов не требует.
Графический клиент и `tools/check-wand6-distributions.py` запускаются на хосте
с графической Java. Инструкции: [README](../README.md) и
[ресурсы](../docs/CLIENT-ASSETS.md).

Существующий сервис Compose `server` и `.devcontainer/artifacts/server/server.jar`
относятся к уже опубликованному dev-серверу 3.x. Этап 7 их не обновляет и
не перезапускает. Для wand6 используйте отдельный порт и дистрибутив из
`server/build/distributions/` либо `server/build/libs/wand6-server-*-standalone.jar`.
Ветка сохраняет окружение запуска 3.x, но не предоставляет в нём задачу
публикации сервера wand6. Файлы `.env`, ресурсы, кэши и dev-артефакты остаются вне Git.
