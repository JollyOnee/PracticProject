#include "MyMath.h"
#include <iostream>
#include <string>
#include <stack>
#include <vector>
#include <string>
#include <cctype>
#include <cstring>
#include <sstream>
#include <list>
struct Token {
    enum Type { NUMBER, OP, FUNC } type;
    std::string value;
};
int priority(char op) {
    if (op == '!' || op == '%') return 4;
    if (op == '^') return 3;
    if (op == '*' || op == '/') return 2;
    if (op == '+' || op == '-') return 1;
    return 0;
}
std::string readBraces(const std::string& s, int& i) {
    std::string result;
    if (i >= s.size() || s[i] != '{') return result;
    i++;
    int depth = 1;
    while (i < s.size() && depth > 0) {
        if (s[i] == '{') {
            depth++;
            result += s[i];
        }
        else if (s[i] == '}') {
            depth--;
            if (depth > 0) result += s[i];
        }
        else {
            result += s[i];
        }
        i++;
    }
    return result;
}
std::string prepareLatex(const std::string& s) {
    std::string result;

    for (int i = 0; i < s.size(); i++) {

        if (s.substr(i, 5) == "\\frac") {
            i += 5;

            std::string top = readBraces(s, i);
            std::string bottom = readBraces(s, i);

            result += "(" + prepareLatex(top) + ")/(" + prepareLatex(bottom) + ")";
            i--;
        }

        else if (s.substr(i, 5) == "\\left") {
            i += 4;
        }

        else if (s.substr(i, 6) == "\\right") {
            i += 5;
        }

        else if (s.substr(i, 6) == "\\times") {
            result += "*";
            i += 5;
        }

        else if (s.substr(i, 5) == "\\cdot") {
            result += "*";
            i += 4;
        }

        else if (s.substr(i, 4) == "\\int") {
            i += 4;

            std::string a;
            std::string b;
            std::string func;

            if (i < s.size() && s[i] == '_') {
                i++;
                a = readBraces(s, i);
            }

            if (i < s.size() && s[i] == '^') {
                i++;
                b = readBraces(s, i);
            }

            if (i < s.size() && s[i] == '{') {
                func = readBraces(s, i);
            }
            else {
                while (i < s.size() &&
                       s[i] != 'd' &&
                       s[i] != '|' &&
                       !isspace(s[i])) {
                    func += s[i];
                    i++;
                }
            }

            result += "int("
                      + prepareLatex(func) + ","
                      + prepareLatex(a) + ","
                      + prepareLatex(b) + ")";

            if (i + 1 < s.size() && s[i] == 'd' && s[i + 1] == 'x') {
                i += 2;
            }

            i--;
        }

        else if (s.substr(i, 4) == "\\sin") {
            i += 4;
            std::string inside = readBraces(s, i);
            result += "sin(" + prepareLatex(inside) + ")";
            i--;
        }

        else if (s.substr(i, 4) == "\\cos") {
            i += 4;
            std::string inside = readBraces(s, i);
            result += "cos(" + prepareLatex(inside) + ")";
            i--;
        }

        else if (s.substr(i, 4) == "\\tan") {
            i += 4;
            std::string inside = readBraces(s, i);
            result += "tan(" + prepareLatex(inside) + ")";
            i--;
        }

        else if (s.substr(i, 3) == "\\ln") {
            i += 3;
            std::string inside = readBraces(s, i);
            result += "ln(" + prepareLatex(inside) + ")";
            i--;
        }

        else if (s.substr(i, 3) == "\\pi") {
            result += "3.1415926";
            i += 2;
        }

        else if ((s[i] == 'A' || s[i] == 'C') && i + 1 < s.size() && s[i + 1] == '_') {
            char func = s[i];
            i += 2;

            std::string bottom = readBraces(s, i);

            if (i < s.size() && s[i] == '^') {
                i++;
            }

            std::string top = readBraces(s, i);

            result += std::string(1, func)
                      + "(" + prepareLatex(bottom)
                      + "," + prepareLatex(top)
                      + ")";

            i--;
        }

        else if (s[i] == '^') {
            result += "^";

            if (i + 1 < s.size() && s[i + 1] == '{') {
                i++;
                std::string power = readBraces(s, i);
                result += prepareLatex(power);
                i--;
            }
        }

        else if (s[i] == '{') {
            result += "(";
        }

        else if (s[i] == '}') {
            result += ")";
        }

        else if (s[i] == 'e') {
            result += "2.7182818";
        }

        else if (s[i] == '\\') {
            continue;
        }

        else {
            result += s[i];
        }
    }

    return result;
}
std::vector<Token> tokenize(const std::string& s) {
    std::vector<Token> tokens;
    bool expectNumber = true;
    for (int i = 0; i < s.size(); i++) {
        if (isspace(s[i])) continue;
        if (s[i] == '-' && expectNumber) {
            std::string num = "-";
            i++;
            while (i < s.size() && (isdigit(s[i]) || s[i] == '.')) {
                num += s[i++];
            }i--;
            tokens.push_back({ Token::NUMBER, num });
            expectNumber = false;
            continue;
        }
        if (isdigit(s[i]) || s[i] == '.') {
            std::string num;

            while (i < s.size() && (isdigit(s[i]) || s[i] == '.')) {
                num += s[i++];
            }
            i--;
            tokens.push_back({ Token::NUMBER, num });
            expectNumber = false;
        }
        else if (s.substr(i, 3) == "sin") {
            tokens.push_back({ Token::FUNC, "sin" });
            i += 2;
            expectNumber = true;
        }
        else if (s.substr(i, 3) == "cos") {
            tokens.push_back({ Token::FUNC, "cos" });
            i += 2;
            expectNumber = true;
        }
        else if (s.substr(i, 3) == "tan") {
            tokens.push_back({ Token::FUNC, "tan" });
            i += 2;
            expectNumber = true;
        }
        else if (s.substr(i, 2) == "ln") {
            tokens.push_back({ Token::FUNC, "ln" });
            i += 1;
            expectNumber = true;
        }
        else if (s.substr(i, 3) == "int") {
            tokens.push_back({ Token::FUNC, "int" });
            i += 2;
            expectNumber = true;
        }
        else if (s[i] == 'A' || s[i] == 'C') {
            tokens.push_back({ Token::FUNC, std::string(1, s[i]) });
            expectNumber = true;
        }
        else if (std::string("+-*/^()!%,").find(s[i]) != std::string::npos) {
            tokens.push_back({ Token::OP, std::string(1, s[i]) });

            if (s[i] == '!' || s[i] == '%') {
                expectNumber = false;
            }
            else {
                expectNumber = (
                        s[i] == '(' ||
                        s[i] == ',' ||
                        s[i] == '+' ||
                        s[i] == '-' ||
                        s[i] == '*' ||
                        s[i] == '/' ||
                        s[i] == '^'
                );
            }
        }
    }

    return tokens;
}
std::vector<Token> toRPN(const std::vector<Token>& tokens) {
    std::vector<Token> output;
    std::stack<Token> ops;
    for (auto& t : tokens) {
        if (t.type == Token::NUMBER) {
            output.push_back(t);
        }
        else if (t.type == Token::FUNC) {
            ops.push(t);
        }
        else {
            char c = t.value[0];
            if (c == '(') {
                ops.push(t);
            }
            else if (c == ',') {
                while (!ops.empty() && ops.top().value != "(") {
                    output.push_back(ops.top());
                    ops.pop();
                }
            }
            else if (c == ')') {
                while (!ops.empty() && ops.top().value != "(") {
                    output.push_back(ops.top());
                    ops.pop();
                }
                if (!ops.empty()) ops.pop();
                if (!ops.empty() && ops.top().type == Token::FUNC) {
                    output.push_back(ops.top());
                    ops.pop();
                }
            }
            else {
                if (c == '!' || c == '%') {
                    output.push_back(t);
                    continue;
                }
                while (!ops.empty() &&
                       ops.top().type == Token::OP &&
                       ops.top().value != "(" &&
                       priority(ops.top().value[0]) >= priority(c)) {
                    output.push_back(ops.top());
                    ops.pop();
                }
                ops.push(t);
            }
        }
    }
    while (!ops.empty()) {
        output.push_back(ops.top());
        ops.pop();
    }
    return output;
}
BigNumber evalRPN(std::vector<Token> rpn) {
    std::stack<BigNumber> st;
    for (auto& t : rpn) {
        if (t.type == Token::NUMBER) {
            std::string val = t.value;
            int sign = 0;
            int point = 0;
            if (!val.empty() && val[0] == '-') {
                sign = 1;
                val = val.substr(1);
            }
            size_t dotPos = val.find('.');
            if (dotPos != std::string::npos) {
                point = val.length() - 1 - dotPos;
                val.erase(dotPos, 1);
            }
            st.push(BigNumber(val, point, sign));
            continue;
        }
        if (t.type == Token::FUNC) {
            BigNumber helper{ "0" };
            if (t.value == "A" || t.value == "C") {
                BigNumber b = st.top(); st.pop();
                BigNumber a = st.top(); st.pop();
                if (t.value == "A") st.push(helper.A(a, b));
                if (t.value == "C") st.push(helper.C(a, b));
                continue;
            }
            if (t.value == "int") {
                BigNumber b = st.top(); st.pop();
                BigNumber a = st.top(); st.pop();
                BigNumber func = st.top(); st.pop();
                Polynom p({ func });
                st.push(p.integral(a, b));
                continue;
            }
            BigNumber a = st.top();
            st.pop();
            Polynom p({ a });
            Polynom result({ BigNumber{"0"} });
            if (t.value == "sin") result = p.sin();
            if (t.value == "cos") result = p.cos();
            if (t.value == "tan") result = p.tan();
            if (t.value == "ln") result = p.logn();
            st.push(result(BigNumber{ "0" }));
            continue;
        }
        if (t.value[0] == '!') {
            BigNumber a = st.top();
            st.pop();
            BigNumber helper{ "0" };
            st.push(helper.factorial(a));
            continue;
        }
        if (t.value[0] == '%') {
            BigNumber a = st.top();
            st.pop();
            BigNumber helper{ "0" };
            st.push(helper.percent(a));
            continue;
        }
        BigNumber b = st.top(); st.pop();
        BigNumber a = st.top(); st.pop();
        char op = t.value[0];
        if (op == '+') st.push(a + b);
        if (op == '-') st.push(a - b);
        if (op == '*') st.push(a * b);
        if (op == '/') st.push(a / b);
        if (op == '^') st.push(a ^ b);
    }
    return st.top();
}
BigNumber makeNumber(std::string val) {
    int sign = 0;
    int point = 0;
    if (!val.empty() && val[0] == '-') {
        sign = 1;
        val = val.substr(1);
    }
    size_t dotPos = val.find('.');
    if (dotPos != std::string::npos) {
        point = val.length() - 1 - dotPos;
        val.erase(dotPos, 1);
    }
    return BigNumber(val, point, sign);
}
Polynom parsePolynomial(std::string s) {
    std::string clean;
    for (char c : s) {
        if (!isspace(c)) clean += c;
    }
    std::vector<BigNumber> coef(20, BigNumber{ "0" });
    for (int i = 0; i < clean.size();) {
        int sign = 0;
        if (clean[i] == '+') i++;
        else if (clean[i] == '-') {
            sign = 1;
            i++;
        }
        std::string term;
        while (i < clean.size() && clean[i] != '+' && clean[i] != '-') {
            term += clean[i];
            i++;
        }
        int degree = 0;
        std::string number = "1";
        size_t xPos = term.find('x');
        if (xPos == std::string::npos) {
            number = term;
            degree = 0;
        }
        else {
            if (xPos == 0) {
                number = "1";
            }
            else {
                number = term.substr(0, xPos);

                if (!number.empty() && number.back() == '*') {
                    number.pop_back();
                }
            }
            size_t powPos = term.find('^');
            if (powPos != std::string::npos) {
                degree = std::stoi(term.substr(powPos + 1));
            }
            else {
                degree = 1;
            }
        }
        BigNumber value = makeNumber(number);

        if (sign == 1) {
            value = -value;
        }
        coef[degree] = coef[degree] + value;
    }
    std::list<BigNumber> result;
    int maxDegree = 0;
    for (int i = 0; i < coef.size(); i++) {
        if (coef[i].getValue() != "0") {
            maxDegree = i;
        }
    }
    for (int i = 0; i <= maxDegree; i++) {
        result.push_back(coef[i]);
    }
    return Polynom(result);
}
std::string keyNumber(BigNumber n) {
    return std::to_string(n.getSign()) + "_" +
           n.getValue() + "_" +
           std::to_string(n.getPoint());
}
void addRootIfUnique(std::vector<BigNumber>& roots, BigNumber root) {
    for (BigNumber r : roots) {
        if (keyNumber(r) == keyNumber(root)) {
            return;
        }
    }
    roots.push_back(root);
}
std::vector<std::string> splitArgs(const std::string& s) {
    std::vector<std::string> args;
    std::string cur;
    int depth = 0;
    for (char c : s) {
        if (c == '(') depth++;
        else if (c == ')') depth--;
        if (c == ',' && depth == 0) {
            args.push_back(cur);
            cur.clear();
        }
        else {
            cur += c;
        }
    }
    args.push_back(cur);
    return args;
}
Polynom constPoly(BigNumber n) {
    return Polynom({ n });
}

Polynom xPoly() {
    return Polynom({ BigNumber{"0"}, BigNumber{"1"} });
}

Polynom powPoly(Polynom p, int n) {
    Polynom result({ BigNumber{"1"} });

    for (int i = 0; i < n; i++) {
        result = result * p;
    }

    return result;
}

Polynom parsePolyExpression(const std::string& s) {
    std::stack<Polynom> values;
    std::stack<char> ops;

    auto applyOp = [&]() {
        char op = ops.top();
        ops.pop();

        Polynom b = values.top();
        values.pop();

        Polynom a = values.top();
        values.pop();

        if (op == '+') values.push(a + b);
        if (op == '-') values.push(a - b);
        if (op == '*') values.push(a * b);
        if (op == '^') {
            // степень должна быть обычным числом
            // пока поддерживаем x^2, x^3, x^4...
            // поэтому b здесь не используем
        }
    };

    auto pr = [](char op) {
        if (op == '^') return 3;
        if (op == '*') return 2;
        if (op == '+' || op == '-') return 1;
        return 0;
    };

    for (int i = 0; i < s.size(); i++) {
        if (isspace(s[i])) continue;

        if (isdigit(s[i])) {
            std::string num;

            while (i < s.size() && (isdigit(s[i]) || s[i] == '.')) {
                num += s[i];
                i++;
            }

            i--;

            values.push(constPoly(makeNumber(num)));
        }
        else if (s[i] == 'x') {
            values.push(xPoly());
        }
        else if (s[i] == '(') {
            ops.push('(');
        }
        else if (s[i] == ')') {
            while (!ops.empty() && ops.top() != '(') {
                applyOp();
            }
            if (!ops.empty()) ops.pop();
        }
        else if (s[i] == '^') {
            i++;

            std::string num;
            while (i < s.size() && isdigit(s[i])) {
                num += s[i];
                i++;
            }
            i--;

            int power = std::stoi(num);

            Polynom a = values.top();
            values.pop();

            values.push(powPoly(a, power));
        }
        else if (s[i] == '+' || s[i] == '-' || s[i] == '*') {
            char op = s[i];

            while (!ops.empty() && ops.top() != '(' && pr(ops.top()) >= pr(op)) {
                applyOp();
            }

            ops.push(op);
        }
    }

    while (!ops.empty()) {
        applyOp();
    }

    return values.top();
}
BigNumber evaluate(const std::string& expr) {
    std::string prepared = prepareLatex(expr);
    if (prepared.substr(0, 4) == "int(" && prepared.back() == ')') {
        std::string inside = prepared.substr(4, prepared.size() - 5);
        std::vector<std::string> args = splitArgs(inside);
        Polynom p = parsePolynomial(args[0]);
        BigNumber a = evaluate(args[1]);
        BigNumber b = evaluate(args[2]);
        return p.integral(a, b);
    }
    // Если выражение содержит x — считаем, что это многочлен
    if (prepared.find('x') != std::string::npos) {
        Polynom p = parsePolyExpression(prepared);
        std::cout << "Polynomial: ";
        p.PolynomPrint();
        std::streambuf* oldCout = std::cout.rdbuf();
        std::ostringstream temp;
        std::cout.rdbuf(temp.rdbuf());
        std::vector<BigNumber> roots = p.Solve();
        std::cout.rdbuf(oldCout);
        std::istringstream debugOutput(temp.str());
        std::string mulStr;
        std::string iStr;
        while (debugOutput >> mulStr >> iStr) {
            BigNumber mul = makeNumber(mulStr);
            if (mul.getValue() == "0") {
                BigNumber x1 = makeNumber(iStr);

                if (p(x1).getValue() == "0") {
                    addRootIfUnique(roots, x1);
                }
                else {
                    BigNumber x2 = x1 + BigNumber{ "1", 1 };

                    if (p(x2).getValue() == "0") {
                        addRootIfUnique(roots, x2);
                    }
                }
            }
        }
        std::cout << "Roots: ";
        for (BigNumber r : roots) {
            r.print(0);
            std::cout << " ";
        }
        std::cout << std::endl;
        return BigNumber{ "0" };
    }
    // Иначе обычное числовое выражение
    auto tokens = tokenize(prepared);
    auto rpn = toRPN(tokens);
    return evalRPN(rpn);
}

int main() {
    /*evaluate("5!").print(1);
    evaluate("50%").print(1);
    evaluate("\\frac{6}{3}").print(1);
    evaluate("\\pi").print(1);
    evaluate("e").print(1);
    evaluate("A_{5}^{2}").print(1);   // 20
    evaluate("A_{6}^{3}").print(1);   // 120
    evaluate("A_{4}^{4}").print(1);   // 24
    evaluate("A_{7}^{0}").print(1);   // 1

    evaluate("C_{5}^{2}").print(1);   // 10
    evaluate("C_{6}^{3}").print(1);   // 20
    evaluate("C_{4}^{4}").print(1);   // 1
    evaluate("C_{7}^{0}").print(1);   // 1
        evaluate("2+3*4").print(1);          // обычные числа
        evaluate("\\frac{6}{3}").print(1);   // обычный LaTeX
        evaluate("x^2-4");                   // многочлен
        evaluate("x^2-5*x+6");               // многочлен
        evaluate("\\frac{1}{1}x^2-9");       // многочлен из LaTeX

    evaluate("C_{2}^{5}").print(1);
    evaluate("\\sin{0}").print(1);
    evaluate("\\cos{0}").print(1);
    evaluate("\\tan{0}").print(1);
    evaluate("\\ln{2}").print(1);
    evaluate("\\int{2}{1}{4}dx").print(1);
    evaluate("\\int{x^10+x^8+1}{0}{1}dx").print(1);
    evaluate("(2+3)*4").print(1);
    evaluate("2+3*4").print(1);
    evaluate("(2+3)^2").print(1);
/*evaluate("\\int{x^2}{0}{1}dx").print(1);
evaluate("\\int{x^2+2*x+1}{0}{1}dx").print(1);
evaluate("\\int{2*x^2-8}{-2}{2}dx").print(1);
evaluate("\\int{1}{0}{5}dx").print(1);
// 5
evaluate("\\int{x}{0}{10}dx").print(1);
// 50
evaluate("\\int{x^2}{0}{1}dx").print(1);
// 0.3333333333333333333333333
evaluate("\\int{2*x}{0}{5}dx").print(1);
// 25
evaluate("\\int{x+1}{0}{3}dx").print(1);
// 7.5
evaluate("\\int{2*x+3}{0}{4}dx").print(1);
// 28
evaluate("\\int{x^2+2*x+1}{0}{1}dx").print(1);
// 2.3333333333333333333333333
evaluate("\\int{x^2-4}{0}{2}dx").print(1);
// -5.3333333333333333333333333
evaluate("\\int{2*x^2-8}{-2}{2}dx").print(1);
// -21.3333333333333333333333333
evaluate("\\int{x^3}{0}{2}dx").print(1);
// 4
evaluate("\\int{x^3+x^2+x+1}{0}{1}dx").print(1);
// 2.0833333333333333333333333
evaluate("\\int{x^4}{0}{1}dx").print(1);
// 0.2
evaluate("\\int{3*x^4-2*x^3+x^2-x+7}{0}{2}dx").print(1);
// 39.0666666666666666666666666
evaluate("\\int{x^5+x^4+x^3+x^2+x+1}{0}{1}dx").print(1);
// 2.45
/*
evaluate("x^2-4");
// roots: -2 2
evaluate("x^2-9");
// roots: -3 3
evaluate("x^2-1");
// roots: -1 1
evaluate("x^2+2*x+1");
// root: -1
evaluate("x^2-5*x+6");
// roots: 2 3
evaluate("x^2+5*x+6");
// roots: -3 -2
evaluate("x^3-x");
// roots: -1 0 1
evaluate("x^3-4*x");
// roots: -2 0 2
evaluate("2*x^2-8");
// roots: -2 2
evaluate("3*x^2-12");
// roots: -2 2
evaluate("x^3-8");
// root: 2
evaluate("x^4-16");
// roots: -2 2
evaluate("x^2");
// root: 0
evaluate("x^2+1");
// no real roots
evaluate("x^3+x^2-x-1");
// roots: -1 1
evaluate("x^3-6*x^2+11*x-6");
// roots: 1 2 3*/                  // roots: -2 2
//evaluate("C_{5}^{2}").print(1);   // 10
//evaluate("C_{6}^{3}").print(1);   // 20
//evaluate("C_{4}^{4}").print(1);   // 1
//evaluate("C_{7}^{0}").print(1);   // 1
//evaluate("A_{5}^{2}").print(1);   // 20
//evaluate("A_{6}^{3}").print(1);   // 120
//evaluate("A_{4}^{4}").print(1);   // 24
//evaluate("A_{7}^{0}").print(1);   // 1
    evaluate("(1+x+x^2)*(4-4*x-5*x^2)");
//evaluate("\\int_{0}^{10}{x}dx").print(1);
    evaluate("C_{10}^{2}").print(1); // 45
    evaluate("C_{5}^{2}").print(1);  // 10
    evaluate("C_{6}^{3}").print(1);  // 20
    evaluate("C_{7}^{0}").print(1);  // 1
    evaluate("\\ln(2000+2x)+1").print(1);
    evaluate("\\left(x+1\\right)\\times\\left(x^{2}-1\\right)");
    return 0;
}