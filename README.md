# Walking Desu — экспериментальная линия wand6 / 4.x

Ветка `codex/game-4.x` продолжает исторический снимок
`b9d8a8043b0d2f19f42770f4f3a7eceb849c8bfa`. На этапе 7 восстановлена независимая
сборка и запуск wand6. Текущая версия — **4.2.0-dev**, это незавершённый прототип.
Стабильная игра **3.2.0** доступна в `master` и в
[релизе v3.2.0](https://github.com/andreasyar/walking-desu/releases/tag/v3.2.0).

## Сборка

Нужен JDK 21 (для сборки используем Dev Container) и Gradle Wrapper 9.8.2:

```sh
./gradlew clean build :client:guiCheckAgent
```

Внешние библиотеки, старые `classes/` и сборка 3.x не требуются.
`build` проверяет публичную упаковку и выполняет сетевой smoke-тест
на отдельном сервере с автоматически назначенным портом. Изображения для
сборки не нужны. Современные source set включают только:

- `common/src/main/java` — модель и обе стороны сериализованного протокола;
- `client/src/main/java` — графический клиент;
- `server/src/main/java` — сервер без GUI-зависимостей.

Старое `src/`, NetBeans-файлы и `mk*jar.sh` сохранены для истории и **не участвуют
в Gradle-сборке**. Проверочные классы лежат в отдельных `src/checks/java` и
не входят в дистрибутивы. Контракт сообщений несовместим с 3.x: клиент и сервер
нужно брать из одного комплекта wand6.

## Запуск дистрибутивов

Распакуйте ZIP или TAR из `client/build/distributions/` и
`server/build/distributions/`. Предоставьте свой `summer.png` по инструкции
[CLIENT-ASSETS.md](docs/CLIENT-ASSETS.md). Дистрибутивы изображений не содержат.

```sh
/path/to/wand6-server-4.2.0-dev/bin/wand6-server 45001
/path/to/wand6-client-4.2.0-dev/bin/wand6-client localhost 45001
```

Windows: соответствующие `.bat`. По умолчанию используется порт 45000;
для совместного запуска с 3.x указывайте другой свободный порт. Значение `0`
у сервера выбирает свободный порт и печатает `WAND6_SERVER_PORT=<port>`.
Также доступен `java -jar server/build/libs/wand6-server-4.2.0-dev-standalone.jar 45001`.
Клиент требует настоящую графическую сессию и предлагает выбрать ник длиной
3–10 символов. Выход — закрытие окна. Сервер останавливается Ctrl+C.

Для запуска через Gradle:

```sh
./gradlew :server:run --args='45001'
./gradlew :client:run --args='localhost 45001'
```

По умолчанию локальный атлас находится в `.devcontainer/assets/wand6/img/`.
Другой каталог: `-PclientAssetsDir=/absolute/path/to/assets`.
`:client:publishDev` готовит отдельный `.devcontainer/artifacts/wand6-client/`;
сервер и клиент 3.x в `.devcontainer/artifacts/server/` и `client/` не заменяются.

## Проверки и ограничения

Для автоматизированной проверки настоящего GUI **на хосте** с графической Java:

```sh
python3 tools/check-wand6-distributions.py --assets .devcontainer/assets/wand6
```

Сначала выполните сборку с `:client:guiCheckAgent`. Скрипт распаковывает свежие
дистрибутивы, проверяет архивы и диагностику ошибок, запускает production-скрипты
сервера и клиента, проходит вход, карту, Dummy-движение и resize. Check-only Java
agent снимает живое Swing-окно на EDT. Логи и PNG лежат в отдельном каталоге
`client/build/distribution-check/run-*/`; скрипт завершает только свои процессы.
PNG нужно просмотреть; это не захват рабочего стола. Ресурсы и снимки остаются локально.

Работают независимая сборка, вход, загрузка и тайловая отрисовка карты.
Персонаж пока — красная точка; движение — историческая телепортация-заглушка.
Камера у начала мира оставляет пустую область за границами карты. Сетка и
подписи воды у renderer — исторический debug-режим. Стыки фрагментов,
полноценное движение и спрайты относятся к этапу 8. Видимость других игроков,
жизненный цикл серверных соединений, текст, бой, башни и предметы не завершены.
Сервер сохраняет исторические `System.exit` на части ошибок и не предназначен
для публичного размещения на этом этапе. Проверки Windows/macOS не проводились.

[Отчёт этапа 7](docs/RECOVERY-STAGE-7.md),
[эпик #2](https://github.com/andreasyar/walking-desu/issues/2),
[задача #12](https://github.com/andreasyar/walking-desu/issues/12),
[план](docs/RECOVERY-EPIC.md), [исторический аудит](docs/HISTORICAL-CODE-RECOVERY.md).
