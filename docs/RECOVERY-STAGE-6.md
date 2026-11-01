# Этап 6: выпуск 3.2.0 и основная ветка

Дата: 1 ноября 2026 года. Задача: [#9](https://github.com/andreasyar/walking-desu/issues/9),
эпик: [#2](https://github.com/andreasyar/walking-desu/issues/2).

База этапа: `bd9d0ba429edea66ed5f59d2cbd38719623d19b2`, проверенный
игровой код этапа 5 — `29d15ed09e9021caa0ab76e34153c937218a3590`.
Выпуск продолжает `codex/gradle-game-3.1` и вливается в `master` обычным
merge commit. Архивная 4.x остаётся на
`b9d8a8043b0d2f19f42770f4f3a7eceb849c8bfa`.

## Изменения выпуска

Версия всех модулей/артефактов — `3.2.0`; предварительный прогон выполнен
на `3.2.0-rc1`. Исправлен исторический заголовок окна «Walking Desu 5»:
клиентский JAR записывает Implementation-Version, окно читает эту версию;
при запуске классов без манифеста заголовок остаётся «Walking Desu».
Обновлены README, CHANGELOG, правила версий и окружения.
[Инструкция релиза](RELEASE-3.2.0.md) описывает полный комплект, запуск,
проверку хешей, сборку из тега, совместное обновление и возврат.

## Окружение и предварительный прогон

Сборка: контейнер `walking-desu_devcontainer-dev-1`, Temurin
21.0.13+9-LTS, Gradle Wrapper 9.8.2; изображения `.devcontainer/assets/client`.
Графика: хост Linux, OpenJDK 25.0.4.1, DISPLAY=:0, Wayland/XWayland.
Все серверы проверок изолированы, стартуют с портом `0`.
Работающий dev-сервер на 45000 и опубликованный серверный JAR не заменялись.

```sh
docker exec walking-desu_devcontainer-dev-1 ./gradlew clean build \
  :client:compileChecksJava :server:compileChecksJava :client:publishDev
```

На RC — PASS, код 0, все 29 задач выполнены. Включены inventoryCheck,
regressionCheck (в том числе предметный/визуальный регресс), combatCheck,
itemNetworkCheck и networkSmoke. Сетевая smoke-проверка использовала 32835.
`injected hit failure` в combatCheck — намеренная проверка аварийной задачи;
сохраняется прежнее предупреждение unchecked cast сетевого пакета.

Для RC из корня checkout, classpath клиента
`client/build/classes/java/checks:.devcontainer/artifacts/client/lib/*`:

| Драйвер | Сервер | Порт | Итог |
| --- | --- | --- | --- |
| GraphicalClientCheck | production standalone JAR 3.2.0-rc1 | 44513 | PASS, код 0 |
| GraphicalItemCheck | checks-классы сервера + standalone JAR 3.2.0-rc1 | 45621 | PASS, код 0 |
| GraphicalCombatCheck | production standalone JAR 3.2.0-rc1 | 40411 | PASS, код 0 |

Каждый драйвер запускает два настоящих Swing-клиента. Это автоматизированный
GUI-прогон, без отдельной пользовательской ручной сессии.

## Матрица выпуска

| Сценарий | Проверка и результат |
| --- | --- |
| Сборка/архивы, JDK 21, ресурсы | clean build; все модули и ZIP/TAR/standalone JAR, PASS |
| Отсутствующие обязательные ресурсы | verifyClientAssets с пустым путём: ожидаемый код 1, список файлов и подсказка clientAssetsDir |
| Вход, карта, движение/остановка, текст, два игрока | networkSmoke и GraphicalClientCheck, PASS |
| Спрайты peasant/peon/poring/tower/desu, снаряды и эффекты, смерть монстра, resize | regressionCheck и локальные визуальные фикстуры GraphicalClientCheck, PASS с известной обрезкой UI |
| PvP, HIT у жертвы/наблюдателя, смерть и возрождение | networkSmoke, combatCheck, GraphicalClientCheck/ItemCheck, PASS |
| Предметы/видимость, подбор и объединение, смерть/выпадение/повторный подбор | inventoryCheck, itemNetworkCheck, два GUI: 7+5=12, затем 3+12=15; PASS |
| Гонка подбора, переполнение, подмена ID, одновременные попадания, одна награда | inventoryCheck, itemNetworkCheck, combatCheck; PASS |
| Башня, естественная первая волна, HIT, смерть монстра и золото у обоих | GraphicalCombatCheck, production сервер; PASS |
| Радиус/reuse, усиление, 100 пропусков, сброс и повторная постройка | combatCheck на контроллерах, PASS; полный цикл не ожидается в GUI |
| Отмена до Future, ранний/повторный tick, исчезновение цели/атакующего, очистка | combatCheck и networkSmoke, PASS |
| EOF/reset, повторный вход, неверная команда изолирована | networkSmoke и itemNetworkCheck, PASS |
| Остановка во время accept/handshake, повторный close, остановка executor | combatCheck, PASS |

Просмотрены свежие PNG живых окон: `01-movement-text`,
`03-all-types-effects-gold`, `04-resize-clipping`, `05-monster-death`
из graphical-check; `02-merged-inventory` и `04-repicked-inventory` из
item-graphical-check; `02-wave-tower-hit` и `03-monster-death-gold` из
combat-graphical-check. Видны персонажи, эффекты, золото 12/15, башня и волна.
Снимки получены printAll на EDT, без захвата рабочего стола.
Логи RC и копии изображений сохранены локально в `/tmp/walking-desu-stage6/`.
В Git графика и build-артефакты не включаются.

## Итоговое дерево слияния и публикация

Перед merge требуется получить `refs/pull/<номер>/merge`, сравнить его дерево
с HEAD кандидата и выполнить полную сборку и GUI-прогоны именно на нём.
После merge — повторная сборка `master`, запуск извлечённых дистрибутивов
и сравнение SHA-256 артефактов с проверенным кандидатом.
Тег `v3.2.0` ставится только после этих проверок; прежние теги не передвигаются.

Точные SHA кандидата, проверенного merge ref и итогового master/тега,
команды, порты финального прогона, контрольные суммы и ссылки на PR/релиз
фиксируются в [результате выполнения #9](https://github.com/andreasyar/walking-desu/issues/9).
Это позволяет записать SHA после слияния, не изменяя уже проверенный исходник
и не создавая самоссылку на хеш документа. SHA выпуска также доступен через
`git rev-parse 'v3.2.0^{commit}'` и
[страницу тега](https://github.com/andreasyar/walking-desu/tree/v3.2.0).

Артефакты и SHA256SUMS публикуются в
[релизе 3.2.0](https://github.com/andreasyar/walking-desu/releases/tag/v3.2.0).
Выпуск считается завершённым после публикации доказательств и закрытия #9;
подготовка этого документа сама по себе не означает завершение E6.3–E6.5.

## Ограничения

Длительная нагрузка и Windows/macOS не проверялись. Полный цикл волн
проверен детерминированно, в GUI — естественная первая волна. Клиентский EOF
после PASS при остановке сервера и обрезка элементов при малом размере окна
сохранены как ограничения. Происхождение двух неоднозначных изображений
и внешний набор ресурсов описаны в README. Подробности и возврат к комплекту
этапа 5 — в [инструкции релиза](RELEASE-3.2.0.md).
