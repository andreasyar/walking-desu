# Этап 13. Подготовка стабильной 4.x (RC)

Дата: 2 ноября 2026 года. Задача: [#19](https://github.com/andreasyar/walking-desu/issues/19),
родительский эпик: [#2](https://github.com/andreasyar/walking-desu/issues/2).
E13.1–E13.5 выполнены в `codex/game-4.x`, версия `4.2.0-rc1`.
База этапа: `f29fb0a` (код этапа 12 — `d9089a5079cc334fde9a594b1b00b5f9078192f5`).
Проверенный коммит реализации фиксируется в #19 после сохранения отчёта.

## Цель

Подтвердить функциональную полноту wand6 относительно обязательных сценариев
стабильной 3.2, классифицировать расхождения, собрать RC-дистрибутивы и
зафиксировать ограничения до перевода `master` на этапе 14.

## Окружение

- Сборка: контейнер `devcontainer-dev-1`, Temurin JDK 21.0.13+9-LTS,
  Gradle Wrapper 9.8.2; ресурсы `.devcontainer/assets/wand6`.
- GUI: хост Linux, OpenJDK 25.0.4.1, DISPLAY=:0, WAYLAND_DISPLAY=wayland-0.
- Снимки — содержимое живых Swing-окон через `printAll` на EDT, не desktop capture.

```sh
docker exec -w /workspace devcontainer-dev-1 ./gradlew clean check \
  :client:visualCheck :client:guiCheckAgent :client:multiplayerGuiCheckAgent \
  :client:combatGuiCheckAgent :client:towerDefenceGuiCheckAgent \
  :client:itemGuiCheckAgent :client:distTar :client:distZip \
  :server:distTar :server:distZip
docker exec -w /workspace devcontainer-dev-1 jdeps --print-module-deps \
  server/build/libs/wand6-server-4.2.0-rc1-standalone.jar
python3 tools/check-wand6-distributions.py --assets .devcontainer/assets/wand6
python3 tools/check-wand6-multiplayer.py --assets .devcontainer/assets/wand6
python3 tools/check-wand6-multiplayer.py --assets .devcontainer/assets/wand6 --first two
python3 tools/check-wand6-combat.py --assets .devcontainer/assets/wand6
python3 tools/check-wand6-tower-defence.py --assets .devcontainer/assets/wand6 --first two
python3 tools/check-wand6-items.py --assets .devcontainer/assets/wand6
```

`jdeps` — `java.base`. Публичные ZIP/TAR/standalone содержат только классы
`wand6.*`; изображений, Check и GraphicalAgent нет.

## Матрица сценариев 3.2 → 4.x

Источник ожиданий — матрица выпуска 3.2 ([этап 6](RECOVERY-STAGE-6.md)) и
проверенные отчёты этапов 8–12. Класс: дефект / намеренное изменение /
ограничение.

| Сценарий 3.2 | Результат на 4.2.0-rc1 | Класс |
| --- | --- | --- |
| Сборка JDK 21, ZIP/TAR/standalone без изображений | PASS `clean check`, `verifyClientPackaging` | совпадает |
| Отсутствие обязательных ресурсов / headless / невалидный ник | PASS diagnostics в distribution-check | совпадает |
| Вход, карта, движение/остановка, 8 направлений, фрагмент, resize, гора | PASS GUI distributions port **43003**, каталог `run-hj3_cbka` | совпадает |
| Два игрока, видимость 500, текст, выход/повторный вход | PASS multiplayer GUI **41683** / reverse **33113**; сеть **38573** | совпадает |
| PvP, снаряд, HIT, смерть/возрождение, ответная атака | PASS combat GUI **43745**; сеть **44421** | совпадает |
| Башня, естественные волны, HIT/смерть, пропуски, снос/повтор | PASS TD GUI **33343** (14 PNG); детерминизм/сеть **39949**/**39589** | совпадает |
| Предметы: 7+5=12, подбор, смерть/выпадение, награда 1–99, выход | PASS items GUI **41045**; сеть **34521**; итог прогона 12+43=55 | совпадает (награда случайна) |
| Гонки подбора, 50/51, переполнение, одна награда, RST | PASS itemCheck / inventoryCheck | совпадает |
| 100 пропусков, усиление, сброс, сохранение золота | PASS towerDefenceCheck | совпадает |
| EOF/RST, повторный вход, очистка задач | PASS multiplayer/combat/item checks | совпадает |
| Пакеты/протокол 3.x совместимы с 4.x | Клиент/сервер разных линий несовместимы | намеренное изменение |
| Управление башней F4 / подбор F5 как в 3.2 | В wand6: B+клик и ЛКМ по монетам | намеренное изменение |
| Серверные дальность/reuse атак игрока | В 4.x проверяются (≤500, ≥375 мс); в 3.2 этого не было | намеренное изменение (закрытие дефекта базы) |
| Dummy-движение / красная точка этапа 7 | Удалены на этапе 8; в RC нет | совпадает с целью |
| Автопоиск обхода препятствий | Нет; остановка у непроходимой клетки | ограничение (как 3.2) |
| Постоянный персонаж / БД / reconnect | Нет; новый вход — новый персонаж | ограничение |
| Windows/macOS, длительная нагрузка, публичный хостинг | Не проверялись | ограничение |
| `System.exit(1)` клиента при отсутствии ресурса / ошибке MessageManager | Сохраняется; сервер `System.exit` не вызывает | ограничение |
| Зависимость runtime от собранной 3.x | Нет: только `project(":common")` wand6; `jdeps`=`java.base` | совпадает с целью |

Блокирующих регрессий относительно обязательной полноты 3.2 не обнаружено.
Ограниченный повторный прогон входов/сетей/боя/волн/предметов и `visualCheck`
после RC-сборки — PASS (порты выше в колонке «Результат»).

## RC-дистрибутивы

Версия артефактов: `4.2.0-rc1`. Клиент и сервер обновляются только вместе;
протокол несовместим с 3.2 и с промежуточными `4.2.0-dev` до этапа 12.

Локальные SHA-256 проверенной сборки:

```
e469336feab2776005d5f63948b8e75420bdd0cdd614b0c83eb1ba7dee3236e8  wand6-client-4.2.0-rc1.tar
400ca3fa2a349eff83c0350234048d2d190464a363db872e0567900862edd12d  wand6-client-4.2.0-rc1.zip
c3447a9459bf1e7ac551063fc570f6489466d815814c240752a1c8feabf2e906  wand6-server-4.2.0-rc1.tar
07c46e29cb3e4d0eac471b5774bc6dc94248ce9e78bc5ff4bc370ae399ea476b  wand6-server-4.2.0-rc1.zip
271a3ed4e348e87cb2ef918e321773fd085460300acf13058d95b03b7c965d1e  wand6-server-4.2.0-rc1-standalone.jar
```

Запуск:

```sh
/path/to/wand6-server-4.2.0-rc1/bin/wand6-server 45001
/path/to/wand6-client-4.2.0-rc1/bin/wand6-client localhost 45001
```

Ресурсы — по [CLIENT-ASSETS.md](CLIENT-ASSETS.md); в архивах изображений нет.

## Просмотренные PNG

Просмотрены свежие снимки живых окон (исходное разрешение):

- distributions `run-hj3_cbka`: login/map, walking, mountain-stop;
- multiplayer `run-qlvb4vn0`: both-moving с текстом;
- combat `run-1u14aem0`: projectile/selection;
- tower-defence `run-85pdrc81`: wave-one (башня, poring, два игрока);
- items `run-mblb3moe`: merge (золото 12) и final inventory (золото 55).

Видны заголовок `4.2.0-rc1`, карта, персонажи, HP, чат, снаряды, волны,
башни, счётчик золота. Полные наборы PNG остаются локально в `client/build/*/run-*`.

## Откат на 3.2

Проверен комплект [релиза v3.2.0](https://github.com/andreasyar/walking-desu/releases/tag/v3.2.0):

- `SHA256SUMS` совпал для `walking-desu-client-3.2.0.tar`,
  `walking-desu-server-3.2.0.tar`, `walking-desu-server-3.2.0-standalone.jar`;
- сервер из TAR стартовал на порту **38985** и корректно остановился.

Возврат: остановить wand6, распаковать архивы 3.2, положить локальный `img/`
в `walking-desu-client-3.2.0/assets/`, запускать пару клиента/сервера 3.2.
Смешивать протоколы 3.2 и 4.x нельзя. Подробности — [RELEASE-3.2.0.md](RELEASE-3.2.0.md).

## Изменения этапа

- Версия модулей и артефактов: `4.2.0-dev` → `4.2.0-rc1`.
- Инструменты `tools/check-wand6-*.py` принимают `--version` (по умолчанию RC).
- GUI agent проверяет версию в заголовке окна `4.2.0-rc1`.
- Игровой код этапов 7–12 не менялся в рамках устранения блокеров: блокеров нет.

## Границы

Перевод `master` на 4.x, ветка `codex/game-3.x` и тег `v4.2.0` — этап 14.
Новые механики, БД, публичный хостинг и полная смена графики вне объёма.
Windows/macOS и длительная нагрузка не проверялись. Графика и PNG вне Git.
