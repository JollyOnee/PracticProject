name: MathSolver
colors:
  surface: '#f4fced'
  surface-dim: '#d4ddce'
  surface-bright: '#f4fced'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eef6e7'
  surface-container: '#e8f0e2'
  surface-container-high: '#e3ebdc'
  surface-container-highest: '#dde5d7'
  on-surface: '#161d15'
  on-surface-variant: '#3e4a3b'
  inverse-surface: '#2b3229'
  inverse-on-surface: '#ebf3e5'
  outline: '#6d7b69'
  outline-variant: '#bccbb6'
  surface-tint: '#006e1c'
  primary: '#006e1c'
  on-primary: '#ffffff'
  primary-container: '#41cb4f'
  on-primary-container: '#005012'
  inverse-primary: '#59e161'
  secondary: '#306a31'
  on-secondary: '#ffffff'
  secondary-container: '#b2f3aa'
  on-secondary-container: '#367136'
  tertiary: '#006685'
  on-tertiary: '#ffffff'
  tertiary-container: '#00c0f7'
  on-tertiary-container: '#004a62'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#76fe7a'
  primary-fixed-dim: '#59e161'
  on-primary-fixed: '#002204'
  on-primary-fixed-variant: '#005313'
  secondary-fixed: '#b2f3aa'
  secondary-fixed-dim: '#97d690'
  on-secondary-fixed: '#002204'
  on-secondary-fixed-variant: '#16521b'
  tertiary-fixed: '#bfe9ff'
  tertiary-fixed-dim: '#6cd2ff'
  on-tertiary-fixed: '#001f2a'
  on-tertiary-fixed-variant: '#004d65'
  background: '#f4fced'
  on-background: '#161d15'
  surface-variant: '#dde5d7'
typography:
  display-math:
    fontFamily: STIX Two Math
    fontSize: 24px
    fontWeight: '400'
    lineHeight: 32px
  headline-lg:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
  button-text:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  baseline: 8px
  container-padding-mobile: 16px
  container-padding-desktop: 32px
  gutter: 16px
  component-gap: 12px

# Design Document: MathSolver Interface & Experience
## 1. Философия дизайна
Дизайн MathSolver базируется на трех принципах: **Ясность**, **Доступность** и **Функциональность**. В отличие от классических калькуляторов, мы фокусируемся на *процессе*, а не только на *результате*. Эстетика приложения вдохновлена современными научными лабораториями — чистые линии, энергичные цвета и высокая концентрация.

## 2. Визуальный язык
* **Цветовая палитра:**
    * *Primary:* Яркий электрический зеленый (#03a830) для кнопок действия и ключевых элементов навигации.
    * *Secondary:* Приглушенный лесной зеленый (#498447) для второстепенных контейнеров и элементов интерфейса.
    * *Tertiary:* Насыщенный голубой (#00c0f7) для выделения специфических математических сущностей или альтернативных путей решения.
    * *Text:* Сбалансированный серый на фоне светлых поверхностей (#f7fbf2) для обеспечения комфортного чтения при длительной работе.
* **Типографика:** Беззасечный шрифт (Inter) для всех элементов интерфейса и специализированный математический шрифт (STIX Two Math) для рендеринга формул в LaTeX.

## 3. Архитектура экранов (UI Flow)
### 3.1. Главный экран (Input Stage)
Центральный элемент — **Smart Input Field**.
* **Keyboard Switcher:** Переключение между стандартной цифровой панелью и расширенной панелью «Линейная алгебра» (матрицы, векторы, операторы).
* **Live Preview:** В процессе ввода данных через кастомную клавиатуру, в верхней части экрана отображается отрендеренная LaTeX-формула, чтобы пользователь видел задачу в привычном математическом виде.

### 3.2. Экран решения (Solution Stage)
После нажатия кнопки «Решить» (Primary color), приложение переходит в режим отображения результата:
* **Final Answer Card:** Выделенный блок с итоговым ответом в самом верху, использующий мягкую заливку цветом Secondary Container.
* **Step-by-Step Breakdown:** Список раскрывающихся карточек (Accordion style). Каждая карточка — это один логический шаг (например, «Нахождение детерминанта»).
* **Explanation:** Текстовое пояснение к каждому шагу (реализуется через строковые ресурсы KMP для поддержки локализации).

## 4. Специфические компоненты интерфейса
### 4.1. Математическая клавиатура (Custom Component)
Поскольку мы не используем камеру, клавиатура — наш главный инструмент.
* **Grid System:** Компактная сетка 5x4 скругленных клавиш (Radius 0.5rem).
* **Long Press:** Дополнительные символы при долгом нажатии.
* **Matrix Builder:** Динамический конструктор, позволяющий пользователю задать размерность матрицы n \times m перед вводом значений.

### 4.2. Адаптивность (Compose Multiplatform)
* **Mobile:** Вертикальный стек, клавиатура занимает нижнюю треть экрана.
* **Desktop/WASM:** Двухпанельный режим. Слева — поле ввода и история, справа — подробное решение с возможностью экспорта в PDF.

## 5. Техническая реализация UI
* **Rendering:** Для отображения формул используется кроссплатформенная библиотека рендеринга LaTeX, совместимая с Compose.
* **Animations:** Плавные переходы (Shared Element Transitions) между вводом и решением для создания ощущения «легкости» приложения.
* **Interaction:** Поддержка горячих клавиш на Desktop-версии (Enter для вычисления, Ctrl+Z для отмены ввода).