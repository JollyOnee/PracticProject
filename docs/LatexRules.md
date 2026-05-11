# LaTeX Syntax Rules for MathSolver

This document outlines the LaTeX formatting rules used in the project to ensure proper rendering and interaction.

## 1. Basic Operators
*   **Addition:** `+`
*   **Subtraction:** `-`
*   **Multiplication:** `*` (rendered as $\times$ or $\cdot$)
*   **Division:** `/` (for simple inline division)

## 2. Functions & Commands
All LaTeX commands must start with a backslash `\`.
*   **Absolute Value:** `\left| x \right|` (automatic scaling for tall content)
*   **Trigonometry:** `\sin`, `\cos`, `\tan`, `\arcsin`, `\arccos`, `\arctan`
*   **Hyperbolic:** `\sinh`, `\cosh`, `\tanh`
*   **Logarithms:** 
    *   Common: `\log_{10}{x}`
    *   Natural: `\ln{x}`
    *   Custom Base: `\log_{base}{value}`
*   **Roots:**
    *   Square root: `\sqrt{x}`
    *   n-th root: `\sqrt[n]{x}`

## 3. Fractions
Fractions use the `\frac{numerator}{denominator}` structure.
*   Example: `\frac{1}{2}` renders as $\frac{1}{2}$
*   Nested fractions: `\frac{1}{\frac{2}{3}}`

## 4. Exponents and Subscripts
*   **Exponents:** Use `^`. For multiple characters, wrap in braces: `e^{i\pi}`.
*   **Subscripts:** Use `_`. Example: `x_{n+1}`.

## 5. Braces and Grouping
*   **Curly Braces `{}`:** Used to group mandatory arguments for commands. These are invisible in the final render.
*   **Square Brackets `[]`:** Used for optional parameters (e.g., the degree of a root `\sqrt[3]{x}`). Visible if not part of a command.
*   **Parentheses `()`:** Used for mathematical grouping. Rendered as visible brackets.
*   **Escaping:** To show a literal percentage sign, use `\%`.

## 6. Constants and Symbols
*   **Pi:** `\pi`
*   **Euler's Number:** `e`
*   **Infinity:** `\infty`
*   **Degree:** `^{\circ}`

---
*Note: The `MathViewModel` includes smart navigation that automatically skips command names (e.g., `\sin`) and jumps into empty braces `{}` for faster editing.*
