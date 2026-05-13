#include "MyMath.h"
#include <iostream>
#include <stack>
#include <vector>
#include <cctype>
#include <stdexcept>

// ===== BigNumber methods =====

BigNumber::BigNumber(std::string _value, int _point, bool _sign) {
    point = _point;
    value = _value;
    sign = _sign;
    normalize();
}
BigNumber::BigNumber(std::string _value, int _point) : BigNumber(_value, _point, 0) {}
BigNumber::BigNumber(std::string _value) : BigNumber(_value, 0) {}
BigNumber::BigNumber(const BigNumber &number) {
    value = number.value;
    point = number.point;
    sign = number.sign;
}

int BigNumber::getPoint() { return point; }
std::string BigNumber::getValue() { return value; }
bool BigNumber::getSign() { return sign; }
void BigNumber::setPoint(int MyPoint) { point = MyPoint; }
void BigNumber::setValue(std::string MyValue) { value = MyValue; }
void BigNumber::setSign(bool MySign) { sign = MySign; }

void BigNumber::print() {
    normalize();
    if (sign == true) std::cout << "-";
    if (point == 0) {
        for (int i = 0; i < value.length(); i++) std::cout << value[i];
    } else if (point > 0) {
        if (value.length() >= point + 1) {
            int point1 = value.length() - point;
            for (int i = 0; i < value.length(); i++) {
                if (point1 == 0) { std::cout << "."; point1--; i--; continue; }
                std::cout << value[i];
                --point1;
            }
        } else {
            int point1 = point;
            std::cout << "0.";
            point1--;
            while (point1 + 1 > value.length()) { std::cout << "0"; --point1; }
            for (int i = 0; i < value.length(); i++) std::cout << value[i];
        }
    }
    std::cout << std::endl;
}

void BigNumber::ParametrsPrint() {
    std::cout << "num: value = " << value << ", point = " << point << ", sign = " << sign << std::endl;
}

void BigNumber::normalize() {
    while (value.length() > 1 && value[0] == '0')
        value.erase(0, 1);
    if (value == "0") { sign = 0; point = 0; }
    while (point > 0 && value.back() == '0') {
        value.pop_back();
        point--;
    }
}

// ===== Calculator logic =====

struct Token {
    enum Type { NUMBER, OP } type;
    std::string value;
};

int priority(char op) {
    if (op == '^') return 3;
    if (op == '*' || op == '/') return 2;
    if (op == '+' || op == '-') return 1;
    return 0;
}

std::vector<Token> tokenize(const std::string& s) {
    std::vector<Token> tokens;
    bool expectNumber = true;
    for (int i = 0; i < s.size(); i++) {
        if (isspace(s[i])) continue;
        if (s[i] == '-' && expectNumber) {
            std::string num = "-";
            i++;
            while (i < s.size() && (isdigit(s[i]) || s[i] == '.')) num += s[i++];
            i--;
            tokens.push_back({Token::NUMBER, num});
            expectNumber = false;
            continue;
        }
        if (isdigit(s[i]) || s[i] == '.') {
            std::string num;
            while (i < s.size() && (isdigit(s[i]) || s[i] == '.')) num += s[i++];
            i--;
            tokens.push_back({Token::NUMBER, num});
            expectNumber = false;
        } else if (std::string("+-*/^()").find(s[i]) != std::string::npos) {
            tokens.push_back({Token::OP, std::string(1, s[i])});
            expectNumber = (s[i] == '(' || s[i] == '+' || s[i] == '-' || s[i] == '*' || s[i] == '/' || s[i] == '^');
        }
    }
    return tokens;
}

std::vector<Token> toRPN(const std::vector<Token>& tokens) {
    std::vector<Token> output;
    std::stack<char> ops;
    for (auto& t : tokens) {
        if (t.type == Token::NUMBER) {
            output.push_back(t);
        } else {
            char c = t.value[0];
            if (c == '(') {
                ops.push(c);
            } else if (c == ')') {
                while (!ops.empty() && ops.top() != '(') {
                    output.push_back({Token::OP, std::string(1, ops.top())});
                    ops.pop();
                }
                if (!ops.empty()) ops.pop();
            } else {
                while (!ops.empty() && priority(ops.top()) >= priority(c)) {
                    output.push_back({Token::OP, std::string(1, ops.top())});
                    ops.pop();
                }
                ops.push(c);
            }
        }
    }
    while (!ops.empty()) {
        output.push_back({Token::OP, std::string(1, ops.top())});
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
            if (!val.empty() && val[0] == '-') { sign = 1; val = val.substr(1); }
            size_t dotPos = val.find('.');
            if (dotPos != std::string::npos) {
                point = val.length() - 1 - dotPos;
                val.erase(dotPos, 1);
            }
            st.push(BigNumber(val, point, sign));
        } else {
            BigNumber b = st.top(); st.pop();
            BigNumber a = st.top(); st.pop();
            char op = t.value[0];
            if (op == '+') st.push(a + b);
            if (op == '-') st.push(a - b);
            if (op == '*') st.push(a * b);
            if (op == '/') st.push(a / b);
            if (op == '^') st.push(a ^ b);
        }
    }
    return st.top();
}

BigNumber evaluate(const std::string& expr) {
    auto tokens = tokenize(expr);
    auto rpn = toRPN(tokens);
    return evalRPN(rpn);
}