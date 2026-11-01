# Этап 0: сохранение исходных состояний

Дата проверки: 1 ноября 2026 года (Москва).
Задача: [#3](https://github.com/andreasyar/walking-desu/issues/3),
родительский эпик: [#2](https://github.com/andreasyar/walking-desu/issues/2).
Результат: E0.1–E0.4 выполнены.

## Исходное состояние и сохранённая работа

- Рабочая ветка: `codex/gradle-game-3.1`.
- Исходный HEAD: `fe9c7937dc48efe199e789d66e64bd00f036304b`.
- `git status --porcelain=v1` перед началом не вывел ничего: рабочее дерево
  и индекс чистые, неотслеживаемых файлов нет. Игнорируемые локальные ресурсы
  этим утверждением не охватываются и остаются вне Git.
- `origin/codex/gradle-game-3.1` на сервере указывала на тот же HEAD.
- Коммит `afbcaf037788f0cf646e43653be77ce9ae945af4` уже содержал изменения
  README и предварительную диагностику графического окружения в
  `client/src/main/java/client/WalkingDesu.java`. Проверено, что он — предок HEAD.
- Аудит кода и эпик сохранены коммитом `fe9c793`; повторно сохранять
  незакоммиченные пользовательские правки не потребовалось.

Окружение проверки: Fedora Linux 44, Linux `7.2.5-200.fc44.x86_64`,
Git `2.55.0`; команды выполнены на хосте из Bash. JDK и Gradle для этого
этапа не использовались.

## Архив 4.x и восстановление

Опубликована ветка `codex/history/4.x-original`:

- Коммит: `b9d8a8043b0d2f19f42770f4f3a7eceb849c8bfa`.
- Дерево: `f338349b24b9786f619361d979880bd495425d15`.
- Ветка сохраняет исходную историческую 4.x; дальнейшие изменения в неё
  не вносим. Разработка 4.x начнётся в отдельной ветке на этапе 7.

Проверка проведена в новом временном репозитории
`/tmp/walking-desu-e0-restore-f2jo4dg6`, созданном через `git init`.
Объекты получены по HTTPS с GitHub; локальные ссылки на хранилище объектов
(`objects/info/alternates`) отсутствуют. Архив успешно извлечён в рабочее
дерево; каталог `src/wand6` присутствует, рабочее дерево чистое.
`git fsck --full` завершился с кодом 0 без ошибок.

Команды публикации и воспроизводимой проверки (из корня рабочего репозитория):

```sh
git status --porcelain=v1
git rev-parse HEAD
git branch --show-current
git merge-base --is-ancestor afbcaf037788f0cf646e43653be77ce9ae945af4 HEAD
git ls-remote --heads --tags origin
git branch codex/history/4.x-original b9d8a8043b0d2f19f42770f4f3a7eceb849c8bfa
git push origin refs/heads/codex/history/4.x-original:refs/heads/codex/history/4.x-original

# Для повторной проверки использовать новый пустой временный каталог.
restore_dir=$(mktemp -d /tmp/walking-desu-e0-restore-XXXXXX)
git init "$restore_dir"
git -C "$restore_dir" remote add origin https://github.com/andreasyar/walking-desu.git
git -C "$restore_dir" fetch --no-tags origin \
  refs/heads/codex/history/4.x-original:refs/heads/archive \
  refs/heads/codex/gradle-game-3.1:refs/heads/current \
  'refs/tags/*:refs/tags/*'
git -C "$restore_dir" rev-parse archive 'archive^{tree}'
git -C "$restore_dir" checkout --detach archive
git -C "$restore_dir" status --porcelain=v1
git -C "$restore_dir" fsck --full
```

Команды создания и публикации ветки выше описывают выполненное действие;
для повторной проверки уже существующей ветки достаточно блока восстановления.

## Исторические теги

Проверены все 13 тегов: имена, SHA объектов и целевых коммитов совпали
в исходном репозитории, выдаче `git ls-remote --tags origin` и независимом
репозитории восстановления. `01a` — лёгкий тег, остальные — аннотированные.
Для аннотированных тегов отдельно сверены ссылки `^{}` на сервере.

| Тег | SHA объекта ссылки | SHA целевого коммита |
| --- | --- | --- |
| `01a` | `118a09b72ee5f9745523e4a0cc2a0d295c7255e6` | `118a09b72ee5f9745523e4a0cc2a0d295c7255e6` |
| `v1.0.0` | `72c80f2439a25b0ddca64495c2117f817aac80c8` | `94008225074e12801efe1dc08cf93650b55fcbde` |
| `v2.0.0` | `9f538eca7704e43b6870b2fff0551299b3a92269` | `cc0352df6559052863d15eb3146a6bb5e3bc9dc4` |
| `v2.1.0` | `8055b3be5cecbdc3ea1adda87a7ff7f98a5e81e0` | `efd59c007050ea906c3689526d829dba88f7f3dd` |
| `v2.2.0` | `00dd30ebc49074f13b76fcb2ba8145c562284175` | `67addb662f8abcb9a1eb02b8fbc246111891338e` |
| `v2.3.0` | `a64a7f87a9239d2af1771bac738997cc074f9853` | `4245b786e9e76f9325295f1ea67abaac9c3b9690` |
| `v2.4.0` | `51ca3d627d3188bd86c965ef1cf16cc10d6d0731` | `d5351c9fc8d17a765e2d2c881d6b56f0d8719074` |
| `v2.5.0` | `76802c8ec1e30bdd0abf84bc2666ee5fceff063d` | `b67013fe8c133de604429e0de8e440ba36b7aef4` |
| `v2.6.0` | `79905d0305e0e37b7fa78732ed619dc046afe8f5` | `7880e9eb46b34d69469aea85f452b79f786eaceb` |
| `v3.0.0` | `5e1e320fe61e04689a2f615b4416ff46353b22ec` | `3a28dc44ce4fbfa595e26dfc627d69865122cd3d` |
| `v3.1.0` | `3a79a1488a29a8fdbf765c797579f97481c6c97d` | `d2cfc06312873288cbfaaebfc46771f16448a1ca` |
| `v4.0.0` | `15bc337cea48c2efe245352a8c5b3a7b86c20d6e` | `30a8613c0884e6d8aa7ad3ba029f82d46f08f023` |
| `v4.1.0` | `9227f5db5253481e345844724fb250ea9afb1fb1` | `16c3c9d2c8f243e79d2c766f5364e860c65dd46f` |

Для каждого тега использованы команды `git rev-parse refs/tags/<имя>` и
`git rev-parse 'refs/tags/<имя>^{commit}'` в обоих репозиториях.
Существовавшие до начала этапа ссылки веток и тегов на origin после
публикации архива сохранили прежние SHA; добавилась только архивная ветка.

## Документы, ветки и версии

В независимый репозиторий получена также рабочая ветка. Её исходный HEAD
совпал с `fe9c7937dc48efe199e789d66e64bd00f036304b`. Для каждого документа
сверены Git blob SHA исходного HEAD и полученной с origin ветки `current`:

- `README.md`.
- `docs/RECOVERY-EPIC.md`.
- `docs/HISTORICAL-CODE-RECOVERY.md`.
- `docs/GRADLE-MIGRATION.md`.
- `docs/VERSIONING.md`.
- `docs/version-map.tsv`.
- `docs/archive-source-audit/ARCHIVE-CLEANUP.md`.
- `docs/archive-source-audit/HISTORICAL-ASSETS.md`.

Назначение веток и порядок дальнейших слияний описаны в
[эпике](RECOVERY-EPIC.md#ветки-история-и-версии), правила новых выпусков —
в [документе версий](VERSIONING.md#новые-версии-после-восстановления).
`master` остаётся основной веткой, `codex/gradle-game-3.1` — рабочей линией
для будущей 3.2.0. `codex/game-4.x` создаётся от исходной 4.x на этапе 7,
`codex/game-3.x` сохраняет итоговую стабильную 3.x на этапе 14.
История основной ветки не переписывается; при слияниях проверяется итоговое
дерево, исторические теги не перемещаются. Целевая завершённая 4.x — 4.2.0.

Отчёт и обновлённый статус эпика сохраняются отдельным коммитом
«Сохранить исходные состояния и результаты этапа 0» в рабочей ветке.
Его точный SHA и результат публикации фиксируются в issue #3 после push.

## Ограничения

Восстановимость Git-истории подтверждена. Работоспособность исторического
клиента и сервера, графика и игровые сценарии на этом этапе не проверялись;
проверка базы 3.1 выполняется на этапе 1. Локальные графические ресурсы,
артефакты сборки и игнорируемые файлы не являются частью этого архива Git.
Правило неизменности архивной ветки документировано; серверная защита веток
на этом этапе не настраивалась.
