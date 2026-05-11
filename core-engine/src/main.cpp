#include "MyMath.h"
#include <iostream>
#include <string>
#include <stack>
#include <vector>
#include <string>
#include <cctype>
#include <stdexcept>
#include <cstring>

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
        if (isspace(s[i]))
            continue;
        if (s[i] == '-' && expectNumber) {
            std::string num = "-";
            i++;
            while (i < s.size() &&
                  (isdigit(s[i]) || s[i] == '.')) {
                num += s[i++];
            }
            i--;
            tokens.push_back({Token::NUMBER, num});
            expectNumber = false;
            continue;
        }
        if (isdigit(s[i]) || s[i] == '.') {
            std::string num;
            while (i < s.size() &&
                  (isdigit(s[i]) || s[i] == '.')) {
                num += s[i++];
            }
            i--;
            tokens.push_back({Token::NUMBER, num});
            expectNumber = false;
        }
        else if (std::string("+-*/^()").find(s[i]) != std::string::npos) {
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
        }
        else {
            char c = t.value[0];
            if (c == '(') {
                ops.push(c);
            }
            else if (c == ')') {
                while (!ops.empty() && ops.top() != '(') {
                    output.push_back({Token::OP, std::string(1, ops.top())});
                    ops.pop();
                }
                if (!ops.empty()) ops.pop();
            }
            else {
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
        }
        else {
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
int main() {
    BigNumber num1{"123", 2};
    BigNumber num2{"234", 3};
    for (int i=-5; i<7; i++) {
        BigNumber num{"123",i};
        num.ParametrsPrint(); num.print();
    }
    for (int i=-5; i<7; i++) {
        BigNumber num{"1232",i,1};
        num.ParametrsPrint(); num.print();
    }
    num1.setPoint(2); num1.setValue("3754"); num1.setSign(0);
    num1.ParametrsPrint(); num1.print();
    num2 = -num1;
    num2.ParametrsPrint();
    num2.print();
    num1.setPoint(2); num1.setValue("1234"); num1.setSign(1);
    num2.setPoint(2); num2.setValue("998"); num2.setSign(1);
    num1.print(); num2.print();
    BigNumber number = num1 + num2;
    for (int i=0; i<4; i++) {
        for(int j=0; j<4; j++) {
            BigNumber nu{"999",i}, nu1{"88",j};
            nu.print(); nu1.print();
            BigNumber num = nu + nu1;
            num.ParametrsPrint(); num.print();
            std::cout<<std::endl;
        }
    }
    std::cout<<std::endl;
    number.print();
    num1.setSign(0);
    num2.setSign(0);
    num1.print();
    num2.print();
    BigNumber numb = num1 - num2;
    numb.print();
    BigNumber numb1 = num1 + num2;
    numb1.print();
    BigNumber numb2 = num1 * num2;
    numb2.print();
    BigNumber numb3 = num1 / num2;
    numb3.print();
    BigNumber res = evaluate("1.2+3*(-4)");
    res.print();
}