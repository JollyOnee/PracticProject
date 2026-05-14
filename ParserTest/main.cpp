#include "MyMath.h"
#include <iostream>
#include <string>
#include <stack>
#include <vector>
#include <string>
#include <cctype>
#include <cstring>
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
        else if (s.substr(i, 4) == "\\int") {
            i += 4;
            std::string func = readBraces(s, i);
            std::string a = readBraces(s, i);
            std::string b = readBraces(s, i);
            result += "int(" + prepareLatex(func) + "," + prepareLatex(a) + "," + prepareLatex(b) + ")";
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
            result += std::string(1, func) + "(" + prepareLatex(top) + "," + prepareLatex(bottom) + ")";
            i--;
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
BigNumber evaluate(const std::string& expr) {
    if (expr.substr(0, 4) == "\\int") {
        int i = 4;
        std::string func = readBraces(expr, i);
        std::string left = readBraces(expr, i);
        std::string right = readBraces(expr, i);
        Polynom p = parsePolynomial(func);
        BigNumber a = evaluate(left);
        BigNumber b = evaluate(right);
        return p.integral(a, b);
    }
    std::string prepared = prepareLatex(expr);
    auto tokens = tokenize(prepared);
    auto rpn = toRPN(tokens);
    return evalRPN(rpn);
}
int main() {
    /*BigNumber num1{"123", 2,1};
    BigNumber num2{ "234", 3,0 };
    BigNumber{ BigNumber{"2"} / BigNumber{"2"} }.print(1);
    std::cout << BigNumber{ BigNumber{"4"} / BigNumber{"2"} }.getPoint() << std::endl;
    num2.print(1); num1.print(1);
    BigNumber{ num1 + num2 }.print(1);
    BigNumber{ num1 - num2 }.print(1);
    BigNumber{ num1 * num2 }.print(1);
    BigNumber{ num1 / num2 }.print(1);
    num1.print(1); num2.print(1);
    Polynom p1({ BigNumber{"1",0,1},BigNumber{"2",0},BigNumber{"8",0},BigNumber{"7",0},BigNumber{"6",0} });
    Polynom p2({ BigNumber{"133",2},BigNumber{"1245",2,1},BigNumber{"000",2} });
    Polynom p4{ p1 + p2 };
    Polynom p6{ p1 - p2 };
    Polynom p8{ p2 - p1 };
    p1.PolynomPrint(); p2.PolynomPrint(); p4.PolynomPrint(); p6.PolynomPrint(); p8.PolynomPrint();
    std::cout << std::endl;
    p4 = p2 * p1;
    p4.PolynomPrint();
    BigNumber res1 = p1(BigNumber{ "10" });
    BigNumber res2 = p2(BigNumber{ "1732",2,1 });
    std::cout << std::endl; p1.PolynomPrint();
    res1.print(1); res2.print(1);
    std::cout << std::endl;
    BigNumber t = p1.max();
    BigNumber t2 = p2.max();
    t.print(1); t2.print(1);
    std::cout << std::endl;
    Polynom p({ BigNumber{"188",2},BigNumber{"77972492",6},BigNumber{"0"},BigNumber{"376",2},BigNumber{"0"},BigNumber{"99",0,1} });
    p.PolynomPrint();p1.PolynomPrint();
    p2 = p2 + Polynom({ BigNumber{"033",2,1},BigNumber{"1245",2},BigNumber{"1"} }); p2.PolynomPrint(); std::cout << std::endl;
    std::vector<BigNumber> sol = p.Solve();
    std::vector<BigNumber> sol1 = p1.Solve();
    std::vector<BigNumber> sol2 = p2.Solve();
    for (int i = 0; i < sol.size(); i++) {
        sol[i].print(0);
        std::cout << " ";
    }
    std::cout << std::endl;
    for (int i = 0; i < sol1.size(); i++) {
        sol1[i].print(0);
        std::cout << " ";
    }
    std::cout << std::endl;
    for (int i = 0; i < sol2.size(); i++) {
        sol2[i].print(0);
        std::cout << " ";
    }
    std::cout << std::endl;
    BigNumber{ BigNumber{"99"}.factorial(BigNumber{"0"}) }.print(1);
    Polynom{ Polynom({BigNumber{"0"},BigNumber{"2"}}).cos() }.PolynomPrint();
    Polynom{ Polynom({BigNumber{"0"},BigNumber{"2"}}).sin() }.PolynomPrint();
    Polynom{ Polynom({BigNumber{"2"},BigNumber{"0"}}).cos() }.PolynomPrint();
    Polynom{ Polynom({BigNumber{"2"},BigNumber{"0"}}).sin() }.PolynomPrint();
    std::cout << std::endl;
    Polynom{ Polynom({BigNumber{"2"},BigNumber{"0"}}).tan() }.PolynomPrint();
    Polynom{ Polynom({BigNumber{"0"},BigNumber{"2"}}).tan() }.PolynomPrint();
    std::cout << std::endl;
    Polynom{ Polynom({BigNumber{"2"},BigNumber{"0"}}).logn() }.PolynomPrint();
    Polynom{ Polynom({BigNumber{"0"},BigNumber{"2"}}).logn() }.PolynomPrint();
    std::cout << std::endl;
    Polynom({ BigNumber{"2"},BigNumber{"0"} }).integral(BigNumber{ "123",2,1 }, BigNumber{ "456",1 }).print(1);
    Polynom({ BigNumber{"0"},BigNumber{"2"} }).integral(BigNumber{ "12",1,1 }, BigNumber{ "45",1 }).print(1);
    std::cout << std::endl;
    Polynom{ Polynom({BigNumber{"1"}}).del(Polynom({BigNumber{"1"},BigNumber{"1",0,1}})) }.PolynomPrint();
    Polynom{ Polynom({BigNumber{"1"}}).del(Polynom({BigNumber{"1"},BigNumber{"1"}})) }.PolynomPrint();
    std::cout << std::endl;
    Polynom{ Polynom({BigNumber{"2"},BigNumber{"0"}}).pow(BigNumber{"3"}) }.PolynomPrint();
    Polynom{ Polynom({BigNumber{"0"},BigNumber{"2"}}).pow(BigNumber{"27",1}) }.PolynomPrint();
    std::cout << std::endl;
    Polynom p44 = Polynom{ Polynom({BigNumber{"0"},BigNumber{"1"}}).sin() } + Polynom({ BigNumber{"1"},BigNumber{"1"} });
    p44.PolynomPrint();
    std::vector<BigNumber> solv = p44.Solve();
    for (int i = 0; i < solv.size(); i++) {
        solv[i].print(0);
        std::cout << " ";
    }*/
    evaluate("5!").print(1);
    evaluate("50%").print(1);
    evaluate("\\frac{6}{3}").print(1);
    evaluate("\\pi").print(1);
    evaluate("e").print(1);
    /*evaluate("A_{2}^{5}").print(1);
    evaluate("C_{2}^{5}").print(1);*/
    evaluate("\\sin{0}").print(1);
    evaluate("\\cos{0}").print(1);
    evaluate("\\tan{0}").print(1);
    evaluate("\\ln{2}").print(1);
    evaluate("\\int{2}{1}{4}dx").print(1);
    evaluate("\\int{x^10+x^8+1}{0}{1}dx").print(1);
    evaluate("(2+3)*4").print(1);
    evaluate("2+3*4").print(1);
    evaluate("(2+3)^2").print(1);
    return 0;
}