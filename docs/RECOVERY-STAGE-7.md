# Этап 7: независимая сборка wand6

Дата: 1 ноября 2026 года. Задача: [#12](https://github.com/andreasyar/walking-desu/issues/12),
эпик: [#2](https://github.com/andreasyar/walking-desu/issues/2).
E7.1–E7.5 выполнены; следующий этап — карта, движение и персонаж 4.x.

## Исходное и проверенное состояние

- Ветка `codex/game-4.x` создана непосредственно от
  `b9d8a8043b0d2f19f42770f4f3a7eceb849c8bfa`.
- Коммит реализации: `ba0a5eb17fdf0ffbfab8316a374588c83ab55a4f`.
  Этот отчёт добавляется следующим документационным коммитом, без изменения
  проверенного игрового кода и конфигурации сборки. Итоговый SHA публикации
  записывается в #12.
- Источник современной инфраструктуры и документации:
  `master` на `41291203a1cf002c62f28bd54285c4a79867be43`.
- Основная ветка 3.2.0, тег `v3.2.0`, архивная
  `codex/history/4.x-original` и существующий dev-сервер не изменялись.
- Версия экспериментальных артефактов — `4.2.0-dev`; новый стабильный тег
  не создавался. Слияние линии 4.x в master относится к этапу 14.

## Разделение исходников и решения

Все 40 исторических Java-файлов wand6 учтены в
[реестре исходников](RECOVERY-STAGE-7-SOURCES.tsv): 27 перенесены с неизменным
содержимым, 13 адаптированы. В трёх main source set теперь 42 Java-файла:
добавлены собственные `LoginDialog` и `ClientAssets`.

| Область | Решение и причина | Проверка |
| --- | --- | --- |
| Gradle/окружение | Адаптировать инфраструктуру master: Wrapper 9.8.2, toolchain/release 21, воспроизводимый порядок архивов, отдельные дистрибутивы. Исходники 3.x в новые модули не импортированы. | Чистая сборка; запуск из извлечённых TAR. |
| Общая модель и протокол | Перенести `wand6.common`, обе стороны `messages` в common. Исторические имена пакетов и поля сообщений сохранены, включая `MAPFRAGMENTREQEST`. Общее `MessageManagerException` перенесено в `wand6.common.exceptions`. | Сервер имеет только common в runtime; `jdeps` показывает только `java.base`; реальный ObjectStream-вход, карта и MOVE. |
| GUI | Убрать неиспользуемые GameField/WanderingMap и заменить старый LoginDialog собственным. Создание окна, обработка ответов и перерисовка выполняются на EDT. Вместо использования исторического бесконечного RedrawTask — Swing Timer; сам класс сохранён. | Production-launcher клиента; вход, карта, resize, версия в заголовке; просмотр двух свежих PNG. |
| Сеть запуска | Для надёжного первого запуска использовать очередь `take`/`flush` вместо потерянного notify; клиент закрывает свой сокет при выходе. Сервер принимает порт аргументом и печатает фактический порт. | Изолированные запуски с портом 0, ограниченные таймауты, закрытие созданных тестами процессов. Это не полная доработка жизненного цикла этапа 9. |
| Ресурсы | Отдельный манифест wand6, проверка читаемости/размеров атласа до подключения. Внешний assets добавляется штатными скриптами; публичная упаковка не включает графику. | JAR/ZIP/TAR проверены; отказ без атласа; сборка с отсутствующим каталогом ресурсов. |
| Старое дерево | Остальные 98 Java-файлов в `src/` сохранены как исторический материал, но исключены из Gradle source set. Это незавершённые старые client/common/newcommon/server; они мешали независимой сборке. NetBeans, JNLP и mk*jar.sh также не используются современной сборкой. | Production-JAR содержат только `wand6/**`; classpath дистрибутивов — только их собственные JAR и внешний assets. |

## Атлас и происхождение

Манифест: `client/required-assets.txt`, единственный обязательный путь —
`img/summer.png`. Для проверки использован локальный кандидат:

```text
recovered/orphan-branch-sprite-review/images/local-img/tiles/summer.png
512×768
SHA-256 c58dac57d84828e9a0efe6840ec332752c34b3853682afab34dcb63352508c63
```

Копия для wand6: `.devcontainer/assets/wand6/img/summer.png`. Размеры покрывают
все исторические вырезки 32×32 (требуется минимум 512×672). Отрисовка суши,
воды и переходов прошла; точное соответствие оригинальному историческому
атласу и происхождение прав не установлены. Полная проверка стыков — этап 8.
Ресурс, скриншоты и dev-артефакты не добавлены в Git и не опубликованы.
Инструкция для собственного ресурса: [CLIENT-ASSETS.md](CLIENT-ASSETS.md).

## Окружение и команды

Сборка: существующий контейнер `walking-desu_devcontainer-dev-1`,
Temurin `21.0.13+9-LTS`, Gradle Wrapper `9.8.2`. GUI: хост Linux,
OpenJDK `25.0.4.1`, `DISPLAY=:0`, XWayland в текущей Wayland-сессии.
GUI-прогон автоматизирован, снимки — содержимое живого Swing-окна через
`printAll` на EDT, без захвата рабочего стола.

```sh
docker exec walking-desu_devcontainer-dev-1 \
  ./gradlew clean build :client:guiCheckAgent :client:verifyClientAssets --console=plain

# На хосте с доступом к дисплею и локальным сокетам:
python3 tools/check-wand6-distributions.py --assets .devcontainer/assets/wand6

# Сервер не требует java.desktop:
docker exec walking-desu_devcontainer-dev-1 \
  jdeps --print-module-deps server/build/libs/wand6-server-4.2.0-dev-standalone.jar

# Сборка без внешних изображений, с принудительным повторным выполнением:
docker exec walking-desu_devcontainer-dev-1 \
  ./gradlew :client:assemble -PclientAssetsDir=/tmp/wand6-stage7-no-assets \
  --rerun-tasks --console=plain
sha256sum -c client/build/stage7-verification/SHA256SUMS
```

Путь `/tmp/wand6-stage7-no-assets` отсутствует. Повторная клиентская сборка
без изображений дала те же SHA-256 JAR/ZIP/TAR, что и проверенный комплект.

## Результаты

| Сценарий | Результат |
| --- | --- |
| Чистый build всех модулей, упаковка, компиляция check-only agent, манифест ресурсов | PASS, код 0. |
| Standalone сервер на JDK 21: HELLO + NAME → WELCOME; запрос (0,0) → карта 32×32; MOVE (300,400) | PASS, порт 42803, код драйвера 0. |
| Извлечённый production-скрипт сервера из постороннего cwd: тот же сетевой smoke | PASS, порт 45369, код драйвера 0. |
| Извлечённый production-скрипт клиента: настоящий GUI, WELCOME, загрузка карты, Dummy MOVE (512,512), resize 1000×720, версия 4.2.0-dev в заголовке | PASS, сервер 45183, клиент 0. |
| Без атласа, без графической сессии (headless), с невалидным testNick | Ожидаемый отказ, код 1; полезная диагностика до сетевого подключения. |
| Клиентские и серверные ZIP/TAR и standalone JAR | PASS: нет изображений, проверочных классов или классов старой игры. |
| jdeps standalone сервера | Только `java.base`. |
| Сборка клиента с отсутствующим каталогом assets | PASS, код 0; контрольные суммы совпадают. |

Для GUI тестовый ник задавался через явные JVM-свойства
`walkingdesu.testMode=true` и `walkingdesu.testNick=stage7-gui`; без testMode
клиент показывает обычный диалог. Драйвер находится только в checks source set
и отдельном agent JAR; он подключён к production-скрипту через `-javaagent`.
Обычный клиентский дистрибутив не содержит драйверов. Тесты убрали из окружения
CLASSPATH/JAVA_OPTS/JAVA_TOOL_OPTIONS/JDK_JAVA_OPTIONS и запускали приложения
из свежего каталога распаковки, без старых классов в runtime.

Свежие доказательства на этой машине (вне Git):

```text
client/build/stage7-verification/build.log
client/build/stage7-verification/distributions.log
client/build/stage7-verification/without-assets.log
client/build/stage7-verification/SHA256SUMS
client/build/distribution-check/run-bwtfvexn/network-smoke.log
client/build/distribution-check/run-bwtfvexn/gui-server.log
client/build/distribution-check/run-bwtfvexn/gui-client.log
client/build/distribution-check/run-bwtfvexn/01-login-map.png
client/build/distribution-check/run-bwtfvexn/02-map-resize.png
```

Оба PNG просмотрены после финальной сборки: начальная карта у границы мира,
после Dummy MOVE и resize — суша, вода и тайловые переходы, поле ввода и
статус незавершённого прототипа. Исключений до PASS в клиентском логе нет.
Все созданные проверки клиентские процессы завершились с 0; свои серверы
драйверы завершили SIGTERM (143 в Java/−15 в Python). Dev-сервер не затронут.

### Контрольные суммы production-артефактов

```text
0724a475fa64bae1c7f8c4f10382aec11bf760d2e3d23b393e3e31108358c62d  client/build/distributions/wand6-client-4.2.0-dev.tar
27317960729986cef2b880ae666767922eac92205e22b6960366304174395c78  client/build/distributions/wand6-client-4.2.0-dev.zip
9bd74c6f4d7c057df082dd822e7b5f9a617aef2f39aba19de08ea160df178d15  client/build/libs/client-4.2.0-dev.jar
85baa298cd15c2137c20b395ee5416f8762a7c00b2e01f7e2a7faaa60fffe0f5  common/build/libs/common-4.2.0-dev.jar
66790c3f7e483afd4b94e23ef320693c89c7fe9770f44bc748b880deb5b53a20  server/build/distributions/wand6-server-4.2.0-dev.tar
b6c036a03a3c3d423b96f4d48fc1b8efa42eda25bd27a0fb00351917ecb084ad  server/build/distributions/wand6-server-4.2.0-dev.zip
4e2e5e3122f9bfc3842eb25525ea0b72a5b60b9f41bc3d2b0521ab31839646cc  server/build/libs/server-4.2.0-dev.jar
56ec03484a982d0d6391baa7846c7c481a2c62c8e52a02fe420849fe4488604f  server/build/libs/wand6-server-4.2.0-dev-standalone.jar
```

## Оставшиеся ограничения

Красная точка и CurveMovement/Dummy остаются историческими заглушками.
Начальные координаты (0,0), пустая область за началом мира, камера, границы,
генерация соседних фрагментов, стыки и полный набор тайлов ещё не проверены
по критериям этапа 8. Сетка и подписи воды — сохранённый debug renderer.

Видимость других игроков не завершена, текст не доставляется и не отображается
как полноценный чат; бой, башни и предметы ещё не перенесены. Сервер сохраняет
исторический неполный жизненный цикл соединений: игроки не удаляются,
поток sender может ждать после отключения; часть ошибочных входов вызывает
System.exit. Это известные ограничения экспериментальной 4.x, а не готовая
многопользовательская игра. Клиент закрывает свой сокет при закрытии окна;
сервер для этапа 7 проверен как отдельный процесс с остановкой по SIGTERM.
Нагрузочные сценарии, Windows/macOS, ручной прогон диалога входа и правильность
всех переходов атласа не заявляются проверенными.

Стабильная 3.2 продолжает использоваться отдельно. Линия wand6 не переводится
в master до завершения следующих этапов и проверки полноты возможностей.
