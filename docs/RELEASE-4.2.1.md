# Выпуск Walking Desu / wand6 4.2.1

Дата: 2 ноября 2026 года.
Исходники выпуска: тег `v4.2.1`; основная ветка — `master`.
Предыдущий стабильный выпуск: [4.2.0](RELEASE-4.2.0.md).

## Что изменилось

Патч плавности клиента на Linux/Wayland (XWayland):

- кадр рисуется через `paintImmediately`, затем `Toolkit.sync()` сбрасывает
  буфер в дисплей;
- флаг занятости кадра держится до конца реальной отрисовки;
- целевая частота перерисовки ~60 fps вместо 25;
- мягкий `TIMESYNC` (`ServerTime.reconcile`) и диагностика `max`/`sync` в HUD
  сохранены как сопутствующие правки.

Подробности: [CLIENT-SMOOTHNESS.md](CLIENT-SMOOTHNESS.md).

Протокол сообщений относительно 4.2.0 не менялся: клиент и сервер 4.2.0/4.2.1
совместимы по сети. Для исправления дёрганья нужен **клиент 4.2.1**.

## Комплект и запуск

В GitHub Release публикуются клиентский и серверный ZIP/TAR, standalone JAR
сервера и `SHA256SUMS`. **Клиентский архив не содержит изображений.** Перед
запуском скопируйте свой локальный набор спрайтов в
`wand6-client-4.2.1/assets/` по путям из [CLIENT-ASSETS.md](CLIENT-ASSETS.md)
(тот же порядок, что для 4.2.0 / 3.2: ресурсы вне Git и вне публичных
дистрибутивов).

```sh
sha256sum -c SHA256SUMS
tar -xf wand6-client-4.2.1.tar
tar -xf wand6-server-4.2.1.tar
# положите img/summer.png, peasant/poring/tower/canon и coin_stacks_gold.png
# в wand6-client-4.2.1/assets/ согласно CLIENT-ASSETS.md
wand6-server-4.2.1/bin/wand6-server 45001
wand6-client-4.2.1/bin/wand6-client localhost 45001
```

Windows: ZIP и `.bat` из `bin/`. Java 21+; клиенту нужна GUI Java и графическая
сессия. Порт `0` у сервера выбирает свободный порт и печатает
`WAND6_SERVER_PORT=<port>`. Также:
`java -jar wand6-server-4.2.1-standalone.jar 45001`.

## Сборка из тега

```sh
git checkout --detach v4.2.1
./gradlew clean build -PclientAssetsDir=/absolute/path/to/wand6-assets
```

Публичные архивы собираются без изображений (`verifyClientPackaging`).
Локальные GUI-проверки требуют отдельно предоставленный каталог ресурсов.

## Контрольные суммы

```
cdf46a7974e1c221f3d21a51efa57d3673606c71d5f734f7540e1acdb150cd0c  wand6-client-4.2.1.zip
0b1451429d4f42ef6ec364b74e8846854ee678aa51a96f54c0fb4956386f5ff5  wand6-client-4.2.1.tar
31833d87082ac396da136a44ed5e6bb1330ec602bb215d479cbe9b087aeb890b  wand6-server-4.2.1.zip
54fde8c8a483d10ccd086a08cbaa9e5ab4d9ecf6d3f4191b1a34238170438e13  wand6-server-4.2.1.tar
b7d2296a7c0fae927b1d62e0b0ef2e9aae877c4ee3450a4bbe449eaa03c4c57a  wand6-server-4.2.1-standalone.jar
```

## Ограничения

Те же, что у [4.2.0](RELEASE-4.2.0.md): прототип без БД/авторизации/reconnect,
без автообхода пути; длительная нагрузка и Windows/macOS в этом патче отдельно
не прогонялись. Спрайты не коммитятся и не публикуются.
