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
    if (i >= (int)s.size() || s[i] != '{') return result;
    i++;
    int depth = 1;
    while (i < (int)s.size() && depth > 0) {
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

// Разбирает аргумент тригонометрической функции вида \sin{...}
// Если внутри {} есть \left(...\right) — берёт только содержимое скобок как аргумент,
// а остаток возвращает в after (это будет снаружи функции)
std::string parseTrigArg(const std::string& braceContent, std::string& after) {
    after = "";
    size_t leftPos = braceContent.find("\\left");
    size_t rightPos = braceContent.rfind("\\right)");
    if (leftPos != std::string::npos && rightPos != std::string::npos) {
        // пропускаем \left и открывающую скобку
        size_t argStart = leftPos + 5; // после \left
        // пропускаем саму скобку ( или [
        if (argStart < braceContent.size() &&
            (braceContent[argStart] == '(' || braceContent[argStart] == '['))
            argStart++;
        std::string sinArg = braceContent.substr(argStart, rightPos - argStart);
        // всё после \right) остаётся снаружи
        after = braceContent.substr(rightPos + 7);
        return sinArg;
    }
    // нет \left\right — весь контент это аргумент
    return braceContent;
}

std::string prepareLatex(const std::string& s) {
    std::string result;

    for (int i = 0; i < (int)s.size(); i++) {

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
            if (i < (int)s.size() && s[i] == '_') {
                i++;
                a = readBraces(s, i);
            }
            if (i < (int)s.size() && s[i] == '^') {
                i++;
                b = readBraces(s, i);
            }
            if (i < (int)s.size() && s[i] == '{') {
                func = readBraces(s, i);
            }
            else {
                while (i < (int)s.size() &&
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
            if (i + 1 < (int)s.size() && s[i] == 'd' && s[i + 1] == 'x') {
                i += 2;
            }
            i--;
        }
        else if (s.substr(i, 4) == "\\sin") {
            i += 4;
            std::string braceContent = readBraces(s, i);
            std::string after;
            std::string sinArg = parseTrigArg(braceContent, after);
            result += "sin(" + prepareLatex(sinArg) + ")" + prepareLatex(after);
            i--;
        }
        else if (s.substr(i, 4) == "\\cos") {
            i += 4;
            std::string braceContent = readBraces(s, i);
            std::string after;
            std::string cosArg = parseTrigArg(braceContent, after);
            result += "cos(" + prepareLatex(cosArg) + ")" + prepareLatex(after);
            i--;
        }
        else if (s.substr(i, 4) == "\\tan") {
            i += 4;
            std::string braceContent = readBraces(s, i);
            std::string after;
            std::string tanArg = parseTrigArg(braceContent, after);
            result += "tan(" + prepareLatex(tanArg) + ")" + prepareLatex(after);
            i--;
        }
        else if (s.substr(i, 3) == "\\ln") {
            i += 3;

            if (i < (int)s.size() && (int)s.size() - i >= 5 && s.substr(i, 5) == "\\left") {
                i += 5;
            }

            if (i < (int)s.size() && s[i] == '(') {
                i++;
                int start = i;
                int depth = 1;

                while (i < (int)s.size() && depth > 0) {
                    if (s[i] == '(') depth++;
                    else if (s[i] == ')') depth--;
                    i++;
                }

                std::string inside = s.substr(start, i - start - 1);
                result += "ln(" + prepareLatex(inside) + ")";

                if (i < (int)s.size() && (int)s.size() - i >= 6 && s.substr(i, 6) == "\\right") {
                    i += 6;
                }

                i--;
            }
            else {
                std::string braceContent = readBraces(s, i);
                std::string after;
                std::string lnArg = parseTrigArg(braceContent, after);
                result += "ln(" + prepareLatex(lnArg) + ")" + prepareLatex(after);
                i--;
            }
        }
        else if (s.substr(i, 3) == "\\pi") {
            result += "3.1415926";
            i += 2;
        }
        else if ((s[i] == 'A' || s[i] == 'C') && i + 1 < (int)s.size() && s[i + 1] == '_') {
            char func = s[i];
            i += 2;
            std::string bottom = readBraces(s, i);
            if (i < (int)s.size() && s[i] == '^') {
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
            if (i + 1 < (int)s.size() && s[i + 1] == '{') {
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
    for (int i = 0; i < (int)s.size(); i++) {
        if (isspace(s[i])) continue;
        if (s[i] == '-' && expectNumber) {
            std::string num = "-";
            i++;
            while (i < (int)s.size() && (isdigit(s[i]) || s[i] == '.')) {
                num += s[i++];
            }i--;
            tokens.push_back({ Token::NUMBER, num });
            expectNumber = false;
            continue;
        }
        if (isdigit(s[i]) || s[i] == '.') {
            std::string num;
            while (i < (int)s.size() && (isdigit(s[i]) || s[i] == '.')) {
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
    for (int i = 0; i < (int)clean.size();) {
        int sign = 0;
        if (clean[i] == '+') i++;
        else if (clean[i] == '-') {
            sign = 1;
            i++;
        }
        std::string term;
        while (i < (int)clean.size() && clean[i] != '+' && clean[i] != '-') {
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
    for (int i = 0; i < (int)coef.size(); i++) {
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
    };
    auto pr = [](char op) {
        if (op == '^') return 3;
        if (op == '*') return 2;
        if (op == '+' || op == '-') return 1;
        return 0;
    };
    for (int i = 0; i < (int)s.size(); i++) {
        if (isspace(s[i])) continue;
        if (s.substr(i, 2) == "ln") {
            i += 2;
            if (i < (int)s.size() && s[i] == '(') {
                int start = i + 1;
                int depth = 1;
                i++;
                while (i < (int)s.size() && depth > 0) {
                    if (s[i] == '(') depth++;
                    else if (s[i] == ')') depth--;
                    i++;
                }
                std::string inside = s.substr(start, i - start - 1);
                Polynom inner = parsePolyExpression(inside);
                values.push(inner.logn());
                i--;
                continue;
            }
        }
        if (s.substr(i, 3) == "sin") {
            i += 3;
            if (i < (int)s.size() && s[i] == '(') {
                int start = i + 1;
                int depth = 1;
                i++;
                while (i < (int)s.size() && depth > 0) {
                    if (s[i] == '(') depth++;
                    else if (s[i] == ')') depth--;
                    i++;
                }
                std::string inside = s.substr(start, i - start - 1);
                Polynom inner = parsePolyExpression(inside);
                values.push(inner.sin());
                i--;
                continue;
            }
        }
        if (s.substr(i, 3) == "cos") {
            i += 3;
            if (i < (int)s.size() && s[i] == '(') {
                int start = i + 1;
                int depth = 1;
                i++;
                while (i < (int)s.size() && depth > 0) {
                    if (s[i] == '(') depth++;
                    else if (s[i] == ')') depth--;
                    i++;
                }
                std::string inside = s.substr(start, i - start - 1);
                Polynom inner = parsePolyExpression(inside);
                values.push(inner.cos());
                i--;
                continue;
            }
        }
        if (s.substr(i, 3) == "tan") {
            i += 3;
            if (i < (int)s.size() && s[i] == '(') {
                int start = i + 1;
                int depth = 1;
                i++;
                while (i < (int)s.size() && depth > 0) {
                    if (s[i] == '(') depth++;
                    else if (s[i] == ')') depth--;
                    i++;
                }
                std::string inside = s.substr(start, i - start - 1);
                Polynom inner = parsePolyExpression(inside);
                values.push(inner.tan());
                i--;
                continue;
            }
        }
        if (isdigit(s[i])) {
            std::string num;
            while (i < (int)s.size() && (isdigit(s[i]) || s[i] == '.')) {
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
            while (i < (int)s.size() && isdigit(s[i])) {
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
    auto tokens = tokenize(prepared);
    auto rpn = toRPN(tokens);
    return evalRPN(rpn);
}