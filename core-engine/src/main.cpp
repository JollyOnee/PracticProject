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
    BigNumber num1{"123", 2,1};
    BigNumber num2{"234", 3,0};
    BigNumber{BigNumber{"2"}/BigNumber{"2"}}.print(1);
    std::cout<<BigNumber{BigNumber{"4"}/BigNumber{"2"}}.getPoint()<<std::endl;
    num2.print(1); num1.print(1);
    BigNumber{num1+num2}.print(1);
    BigNumber{num1-num2}.print(1);
    BigNumber{num1*num2}.print(1);
    BigNumber{num1/num2}.print(1);
    num1.print(1); num2.print(1);
    Polynom p1({BigNumber{"1",0,1},BigNumber{"2",0},BigNumber{"8",0},BigNumber{"7",0},BigNumber{"6",0}});
    Polynom p2({BigNumber{"133",2},BigNumber{"1245",2,1},BigNumber{"000",2}});
    Polynom p4{p1+p2};
    Polynom p6{p1-p2};
    Polynom p8{p2-p1};
    p1.PolynomPrint(); p2.PolynomPrint(); p4.PolynomPrint(); p6.PolynomPrint(); p8.PolynomPrint();
    std::cout<<std::endl;
    p4 = p2 * p1;
    p4.PolynomPrint();
    BigNumber res1 = p1(BigNumber{"10"});
    BigNumber res2 = p2(BigNumber{"1732",2,1});
    std::cout<<std::endl; p1.PolynomPrint();
    res1.print(1); res2.print(1);
    std::cout<<std::endl;
    BigNumber t = p1.max();
    BigNumber t2 = p2.max();
    t.print(1); t2.print(1);
    std::cout<<std::endl; 
    Polynom p({BigNumber{"188",2},BigNumber{"77972492",6},BigNumber{"0"},BigNumber{"376",2},BigNumber{"0"},BigNumber{"99",0,1}});
    p.PolynomPrint();p1.PolynomPrint();
    p2 = p2 + Polynom({BigNumber{"033",2,1},BigNumber{"1245",2},BigNumber{"1"}}); p2.PolynomPrint(); std::cout<<std::endl;
    std::vector<BigNumber> sol = p.Solve();
    std::vector<BigNumber> sol1 = p1.Solve();
    std::vector<BigNumber> sol2 = p2.Solve();
    for (int i=0; i<sol.size(); i++) {
        sol[i].print(0);
        std::cout<<" ";
    }
    std::cout<<std::endl;
    for (int i=0; i<sol1.size(); i++) {
        sol1[i].print(0);
        std::cout<<" ";
    }
    std::cout<<std::endl;
    for (int i=0; i<sol2.size(); i++) {
        sol2[i].print(0);
        std::cout<<" ";
    }
    std::cout<<std::endl;
    BigNumber{BigNumber{"99"}.factorial(BigNumber{"0"})}.print(1);
    Polynom{Polynom({BigNumber{"0"},BigNumber{"2"}}).cos()}.PolynomPrint();
    Polynom{Polynom({BigNumber{"0"},BigNumber{"2"}}).sin()}.PolynomPrint();
    Polynom{Polynom({BigNumber{"2"},BigNumber{"0"}}).cos()}.PolynomPrint();
    Polynom{Polynom({BigNumber{"2"},BigNumber{"0"}}).sin()}.PolynomPrint();
    std::cout<<std::endl;
    Polynom{Polynom({BigNumber{"2"},BigNumber{"0"}}).tan()}.PolynomPrint();
    Polynom{Polynom({BigNumber{"0"},BigNumber{"2"}}).tan()}.PolynomPrint();
    std::cout<<std::endl;
    Polynom{Polynom({BigNumber{"2"},BigNumber{"0"}}).logn()}.PolynomPrint();
    Polynom{Polynom({BigNumber{"0"},BigNumber{"2"}}).logn()}.PolynomPrint();
    std::cout<<std::endl;
    Polynom({BigNumber{"2"},BigNumber{"0"}}).integral(BigNumber{"123",2,1},BigNumber{"456",1}).print(1);
    Polynom({BigNumber{"0"},BigNumber{"2"}}).integral(BigNumber{"12",1,1},BigNumber{"45",1}).print(1);
    std::cout<<std::endl;
    Polynom{Polynom({BigNumber{"1"}}).del(Polynom({BigNumber{"1"},BigNumber{"1",0,1}}))}.PolynomPrint();
    Polynom{Polynom({BigNumber{"1"}}).del(Polynom({BigNumber{"1"},BigNumber{"1"}}))}.PolynomPrint();
    std::cout<<std::endl;
    Polynom{Polynom({BigNumber{"2"},BigNumber{"0"}}).pow(BigNumber{"2"})}.PolynomPrint();
    Polynom{Polynom({BigNumber{"0"},BigNumber{"2"}}).pow(BigNumber{"2718281828",9})}.PolynomPrint();
    std::cout<<std::endl;
    //BigNumber res = evaluate("155^(3748)");
    //res.print(1);
}
