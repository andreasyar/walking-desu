# Этап 14. Выпуск 4.2.0 и завершение эпика

Дата: 2 ноября 2026 года. Задача: [#20](https://github.com/andreasyar/walking-desu/issues/20),
эпик: [#2](https://github.com/andreasyar/walking-desu/issues/2).
E14.1–E14.5 выполнены.

База 4.x перед слиянием: `b2a7218` (версия `4.2.0`; RC этапа 13 —
`acc13768ba21d743effc075735e4d910868199bb`). Merge-коммит подготовки —
`caf1a8d`; итоговый `master` / тег `v4.2.0` — `11f0e643af34a874a7933cc9325a271fafd03a6a`
([PR #21](https://github.com/andreasyar/walking-desu/pull/21)).
Дерево `master` совпадает с `codex/game-4.x` (`2b3e3f95…`).

## E14.1. Сохранение 3.x

- Ветка `codex/game-3.x` опубликована от `4129120` (master с упаковкой без графики).
- Тег `v3.2.0` доступен; релиз без клиентских изображений.
- Правила: [RELEASE-3.2.0.md](RELEASE-3.2.0.md).

## E14.2–E14.3. Слияние

Общий предок `b9d8a80` давал rename/delete конфликты между пакетами 3.2 и wand6.
Итоговое дерево взято целиком из `codex/game-4.x`: код 3.x из `master` убран,
история сохранена merge-коммитом и веткой `codex/game-3.x`. Исторические
`src/`/NetBeans wand6 остаются вне Gradle-сборки.

## E14.4. Сборка, проверка, тег, артефакты

Окружение: Dev Container Temurin 21 / Gradle 9.8.2; GUI — OpenJDK 25, DISPLAY=:0.

```sh
./gradlew clean check :client:visualCheck \
  :client:guiCheckAgent :client:multiplayerGuiCheckAgent \
  :client:combatGuiCheckAgent :client:towerDefenceGuiCheckAgent \
  :client:itemGuiCheckAgent :client:distTar :client:distZip \
  :server:distTar :server:distZip
jdeps --print-module-deps server/build/libs/wand6-server-4.2.0-standalone.jar
python3 tools/check-wand6-distributions.py --assets .devcontainer/assets/wand6
python3 tools/check-wand6-multiplayer.py --assets .devcontainer/assets/wand6
python3 tools/check-wand6-combat.py --assets .devcontainer/assets/wand6
python3 tools/check-wand6-tower-defence.py --assets .devcontainer/assets/wand6 --first two
python3 tools/check-wand6-items.py --assets .devcontainer/assets/wand6
```

Все PASS; `jdeps` = `java.base`. GUI порты: distributions **43759**,
multiplayer **34323**, combat **42037**, tower defence **42223**, items **35709**.
Просмотрен `01-login-map.png` (заголовок `4.2.0`).

**Клиентские ZIP/TAR не содержат спрайтов** (`verifyClientPackaging` + ручная
проверка архивов). Пользователь кладёт ресурсы в `assets/` сам
([CLIENT-ASSETS.md](CLIENT-ASSETS.md)), как в 3.2.

Релиз: [v4.2.0](https://github.com/andreasyar/walking-desu/releases/tag/v4.2.0).

```
a530bb52cc3c373925f9ca1671a04f93ea5b9f38738cee2dd669ae54ab2fc937  wand6-client-4.2.0.zip
22ba1ebcb431f18d90783814f3002410c33d445ad506d3fedd54861685eb15c1  wand6-client-4.2.0.tar
cd01def389983a7efc67aecd1208bdc431837f2258c8a67d0224b1264b54c5b3  wand6-server-4.2.0.zip
08482bc135e18ae44287fcf60970955bfe98ebea677d3bb6cceae7d3d1557067  wand6-server-4.2.0.tar
271a3ed4e348e87cb2ef918e321773fd085460300acf13058d95b03b7c965d1e  wand6-server-4.2.0-standalone.jar
```

## E14.5. Доступность линий

| Линия | Где |
| --- | --- |
| Стабильная 4.2 | `master`, `v4.2.0`, [Release](https://github.com/andreasyar/walking-desu/releases/tag/v4.2.0) |
| Стабильная 3.2 | `codex/game-3.x`, `v3.2.0` |
| Исходная 4.x | `codex/history/4.x-original` (`b9d8a80`) |

Инструкция: [RELEASE-4.2.0.md](RELEASE-4.2.0.md). Эпик закрыт по результатам этого этапа.

## Ограничения

Без новых механик относительно RC. Спрайты не коммитятся и не публикуются.
Длительная нагрузка и Windows/macOS не проверялись. Протоколы 3.x/4.x несовместимы.
