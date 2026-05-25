This is a Kotlin Multiplatform project targeting Android, Web, Desktop (JVM).

Описание проекта

Общие сведения MathSolver — это кроссплатформенное программное решение, предназначенное для аналитического решения математических задач. Данная программа реализует самые разнообразные методы, которые могут понадобиться при работе с математикой, включая разнообразные численные методы решения полиномиальный уравнений, общих функциональных уравнений, решения определенного интеграла.

Технологический стек Проект построен на базе современных технологий, обеспечивающих высокую производительность и переиспользование кода: Ядро (Math Engine): C++ — используется для реализации математических алгоритмов, парсинга выражений и символьных вычислений. Frontend & Logic: Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP). Поддерживаемые платформы: Android (mobile), Desktop (JVM), Web (WASM). Интерфейс ввода: Поддержка синтаксиса LaTeX и кастомная математическая клавиатура для быстрого набора.

Ключевые функции Step-by-Step Solution: Многофункциональный ввод: Удобный редактор формул, минимизирующий ошибки при наборе сложных выражений. Native Performance: Благодаря интеграции C++ через механизмы взаимодействия (JNI/Interoperability), сложные вычисления происходят мгновенно на уровне железа. Единый UI: Использование CMP позволяет поддерживать идентичный пользовательский опыт на смартфоне, ПК и в браузере.

Состав команды и роли В разработке принимают участие 4 специалиста, каждый из которых курирует отдельное направление: Участники:
Александр Яцко — Product Lead решает, что делаем в продукте определяет функции (MVP, логика, фичи) принимает продуктовые решения.
Александр Иосипой — Core Developer (C++ Engine) пишет математическое ядро интегралы, логарифмы, производные делает систему пошаговых решений.
Дмитрий Язаджи — Tech Lead / Architect связывает всё вместе (C++ ↔️ Android/Web/Desktop) отвечает за архитектуру проекта Git, ветки, структура проекта, интеграции.
Сергей Кириллин — Разрабатывает Парсер на C++

Архитектурная концепция Проект реализует разделение на Core и UI. Математический движок на C++ является «мозгом» приложения, принимая на вход строковое представление задачи и возвращая ответ.
Слой KMP отвечает за доставку этих данных до пользователя на любой из трех платформ.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

### Build and Run Web Application

To build and run the development version of the web app, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:
- for the Wasm target (faster, modern browsers):
  - on macOS/Linux
    ```shell
    ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
    ```
  - on Windows
    ```shell
    .\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
    ```
- for the JS target (slower, supports older browsers):
  - on macOS/Linux
    ```shell
    ./gradlew :composeApp:jsBrowserDevelopmentRun
    ```
  - on Windows
    ```shell
    .\gradlew.bat :composeApp:jsBrowserDevelopmentRun
    ```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).
