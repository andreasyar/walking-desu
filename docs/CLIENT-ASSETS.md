# Внешние ресурсы wand6

Клиентский JAR и публичные ZIP/TAR не содержат изображений. Для wand6
нужен один атлас, перечисленный в `client/required-assets.txt`:
`img/summer.png`. Поместите его в `assets/img/summer.png` распакованного
`wand6-client-4.2.0-dev`, рядом с `assets/required-assets.txt`.

Запускайте `bin/wand6-client localhost <port>` (Windows: `.bat`). Скрипт
добавляет `assets/` в classpath независимо от рабочего каталога. Для прямого
запуска: `java -cp 'lib/*:assets' wand6.client.Wand localhost <port>`;
в Windows разделитель `;`. Без читаемого атласа клиент завершается с понятной
ошибкой до подключения к серверу. Минимальные размеры — 512×672: исторический
renderer вырезает клетки 32×32 вплоть до прямоугольника (448,640,32,32).

## Локальный кандидат

Для этапа 7 использован существующий локальный файл
`recovered/orphan-branch-sprite-review/images/local-img/tiles/summer.png`
размером 512×768. Его копируют в отдельный набор
`.devcontainer/assets/wand6/img/summer.png`. SHA-256 и результаты проверки
приведены в `docs/RECOVERY-STAGE-7.md`.

Это технически пригодный кандидат из локального архива. Точное соответствие
оригинальному историческому атласу и происхождение прав не установлены.
Сборка не скачивает и не публикует этот файл; пользователь предоставляет
свой локальный ресурс. Корректность всех переходов тайлов — задача этапа 8.

## Разработка

`./gradlew assemble` собирает дистрибутивы без ресурсов.
`./gradlew :client:verifyClientAssets` проверяет наличие локального набора;
другой каталог задаётся через `-PclientAssetsDir=/absolute/path/to/assets`.
`./gradlew :client:run --args='localhost <port>'` добавляет этот каталог
в classpath. `:client:publishDev` подготавливает только отдельный локальный
клиент `.devcontainer/artifacts/wand6-client/`, включая внешний `assets/img/`.
Он не заменяет клиент или сервер 3.x. Публичные архивы проверяются задачей
`:client:verifyClientPackaging`. Изображения и PNG проверок остаются вне Git.
