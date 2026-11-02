# Release candidate Wand6 4.2.0-rc1

Дата: 2 ноября 2026 года. Задача: [#19](https://github.com/andreasyar/walking-desu/issues/19).
Ветка: `codex/game-4.x`. Доказательства: [RECOVERY-STAGE-13.md](RECOVERY-STAGE-13.md).

## Комплект

Артефакты собираются локально из ветки (`./gradlew clean check` и dist-задачи):

- `wand6-client-4.2.0-rc1.zip` / `.tar`
- `wand6-server-4.2.0-rc1.zip` / `.tar`
- `wand6-server-4.2.0-rc1-standalone.jar`

Клиентские архивы не содержат изображений. Положите локальный набор в
`wand6-client-4.2.0-rc1/assets/` по [CLIENT-ASSETS.md](CLIENT-ASSETS.md).

```sh
/path/to/wand6-server-4.2.0-rc1/bin/wand6-server 45001
/path/to/wand6-client-4.2.0-rc1/bin/wand6-client localhost 45001
```

Java 21+ для сборки; для GUI на хосте использовалась OpenJDK 25.
Порт `0` выбирает свободный порт и печатает `WAND6_SERVER_PORT=…`.

## Совместное обновление и несовместимость

Протокол wand6 несовместим со стабильной 3.2 и с промежуточными сборками
`4.2.0-dev` до завершения этапа 12. Клиент и сервер RC обновляются только
одним комплектом. Смешивать `walking-desu-*-3.2.0` с `wand6-*-4.2.0-rc1`
нельзя.

## Откат на 3.2

1. Остановите клиенты и сервер wand6.
2. Возьмите архивы [релиза v3.2.0](https://github.com/andreasyar/walking-desu/releases/tag/v3.2.0).
3. Проверьте `SHA256SUMS`, распакуйте, положите `img/` в
   `walking-desu-client-3.2.0/assets/`.
4. Запускайте пару клиента и сервера 3.2 по [RELEASE-3.2.0.md](RELEASE-3.2.0.md).

Стабильный тег `v4.2.0` и перевод `master` относятся к этапу 14.
