#ifndef MYMATH_H
#define MYMATH_H
#include <string>
#include <iostream>
#include <list>
#include <vector>

class BigNumber {
private:
    std::string value;
    int point;
    bool sign;
public:
    BigNumber(std::string _value, int _point, bool _sign);
    BigNumber(std::string _value, int _point);
    BigNumber(std::string _value);
    BigNumber(const BigNumber& number);
    BigNumber operator - () const {
        return BigNumber{ value, point, !sign };
    }
    BigNumber operator + (const BigNumber& number) const {
        BigNumber a = *this;
        BigNumber b = number;
        while (a.point < b.point) {
            a.value += '0';
            a.point++;
        }
        while (b.point < a.point) {
            b.value += '0';
            b.point++;
        }
        while (a.value.length() < b.value.length()) {
            a.value.insert(0, "0");
        }
        while (b.value.length() < a.value.length()) {
            b.value.insert(0, "0");
        }
        if (a.sign == b.sign) {
            std::string Sum = "";
            int next = 0;
            for (int i = a.value.length() - 1; i >= 0; i--) {
                int c = (a.value[i] - '0') + (b.value[i] - '0') + next;
                next = c / 10;
                char symbol = (c % 10) + '0';
                Sum.insert(0, std::string(1, symbol));
            }
            if (next) {
                Sum.insert(0, "1");
            }
            return BigNumber{ Sum, a.point, a.sign };
        }
        bool a_bigger = false;
        if (a.value > b.value) {
            a_bigger = true;
        }
        else if (a.value == b.value) {
            return BigNumber{ "0", 0, 0 };
        }
        std::string Sum = "";
        int next = 0;
        if (a_bigger) {
            for (int i = a.value.length() - 1; i >= 0; i--) {
                int c = (a.value[i] - '0') - (b.value[i] - '0') - next;
                if (c < 0) {
                    c += 10;
                    next = 1;
                }
                else {
                    next = 0;
                }
                char symbol = c + '0';
                Sum.insert(0, std::string(1, symbol));
            }
            while (Sum.length() > 1 && Sum[0] == '0') {
                Sum.erase(0, 1);
            }
            return BigNumber{ Sum, a.point, a.sign };
        }
        else {
            for (int i = b.value.length() - 1; i >= 0; i--) {
                int c = (b.value[i] - '0') - (a.value[i] - '0') - next;
                if (c < 0) {
                    c += 10;
                    next = 1;
                }
                else {
                    next = 0;
                }
                char symbol = c + '0';
                Sum.insert(0, std::string(1, symbol));
            }
            while (Sum.length() > 1 && Sum[0] == '0') {
                Sum.erase(0, 1);
            }
            return BigNumber{ Sum, b.point, b.sign };
        }
    }
    BigNumber operator - (const BigNumber& number) const {
        BigNumber temp = number;
        temp.sign = !temp.sign;
        return (*this + temp);
    }
    BigNumber operator*(const BigNumber& number) const {
        BigNumber a = *this;
        BigNumber b = number;
        std::string result(a.value.length() + b.value.length(), '0');
        for (int i = (int)a.value.length() - 1; i >= 0; i--) {
            int carry = 0;
            for (int j = (int)b.value.length() - 1; j >= 0; j--) {
                int cur = (result[i + j + 1] - '0')
                    + (a.value[i] - '0') * (b.value[j] - '0')
                    + carry;
                result[i + j + 1] = (cur % 10) + '0';
                carry = cur / 10;
            }
            int cur = (result[i] - '0') + carry;
            result[i] = (cur % 10) + '0';
        }
        int pos = 0;
        while (pos + 1 < result.size() && result[pos] == '0')
            pos++;
        result.erase(0, pos);
        return BigNumber{ result, a.point + b.point, a.sign != b.sign };
    }
    BigNumber operator / (const BigNumber& number) const {
        if (number.value == "0") return BigNumber{ "99" };
        BigNumber a = *this;
        BigNumber b = number;
        int maxPoint = std::max(a.point, b.point);
        while (a.point < maxPoint) {
            a.value += '0';
            a.point++;
        }
        while (b.point < maxPoint) {
            b.value += '0';
            b.point++;
        }
        a.point = 0;
        b.point = 0;
        auto trim = [](std::string& s) {
            while (s.length() > 1 && s[0] == '0') s.erase(0, 1);
            };
        auto absGreaterOrEqual = [&](const std::string& x, const std::string& y) {
            std::string a = x;
            std::string b = y;
            trim(a);
            trim(b);
            if (a.length() != b.length())
                return a.length() > b.length();
            return a >= b;
            };
        auto sub = [&](std::string x, std::string y) {
            trim(x);
            trim(y);
            while (y.length() < x.length())
                y.insert(0, "0");
            std::string res = "";
            int carry = 0;
            for (int i = x.length() - 1; i >= 0; i--) {
                int c = (x[i] - '0') - (y[i] - '0') - carry;
                if (c < 0) {
                    c += 10;
                    carry = 1;
                }
                else {
                    carry = 0;
                }
                res.insert(0, std::string(1, c + '0'));
            }
            trim(res);
            return res;
            };
        std::string result = "";
        std::string current = "";
        for (int i = 0; i < a.value.length(); i++) {
            current += a.value[i];
            trim(current);
            int digit = 0;
            while (absGreaterOrEqual(current, b.value)) {
                current = sub(current, b.value);
                digit++;
            }
            result += char(digit + '0');
        }
        int precision = 25;
        int pointResult = 0;
        while (precision-- && current != "0") {
            current += '0';
            trim(current);
            int digit = 0;
            while (absGreaterOrEqual(current, b.value)) {
                current = sub(current, b.value);
                digit++;
            }
            result += char(digit + '0');
            pointResult++;
        }
        trim(result);
        while (pointResult > 0 &&
            result.back() == '0') {
            result.pop_back();
            pointResult--;
        }
        bool sign = (a.sign != b.sign);
        return BigNumber{ result, pointResult, sign };
    }
    BigNumber operator ^ (const BigNumber& exp) const {
        if (exp.point != 0) {
            std::cout << "Error";
        }
        if (exp.value == "0") {
            return BigNumber("1", 0, 0);
        }
        BigNumber base = *this;
        BigNumber exponent = exp;
        if (exponent.sign) {
            throw std::runtime_error("Negative exponent not supported");
        }
        BigNumber result("1", 0, 0);
        auto isOdd = [](const std::string& s) {
            return (s.back() - '0') % 2;
            };
        auto div2 = [](std::string s) {
            std::string res;
            int carry = 0;
            for (char c : s) {
                int num = carry * 10 + (c - '0');
                res += (num / 2) + '0';
                carry = num % 2;
            }
            while (res.length() > 1 && res[0] == '0')
                res.erase(0, 1);
            return res;
            };
        auto isZero = [](const std::string& s) {
            for (char c : s)
                if (c != '0')
                    return false;
            return true;
            };
        while (!isZero(exponent.value)) {
            if (isOdd(exponent.value)) {
                result = result * base;
            }
            base = base * base;
            exponent.value = div2(exponent.value);
        }
        bool negativeBase = this->sign;
        if (negativeBase) {
            std::string expCopy = exp.value;
            bool oddExp = isOdd(expCopy);
            result.sign = oddExp;
        }
        else {
            result.sign = 0;
        }
        return result;
    }

    std::string getValue();
    int getPoint();
    BigNumber factorial(BigNumber x);
    BigNumber A(BigNumber x, BigNumber y);
    BigNumber C(BigNumber x, BigNumber y);
    BigNumber rad(BigNumber x);
    BigNumber grade(BigNumber x);
    BigNumber logn(BigNumber n);
    BigNumber percent(BigNumber x);
    bool getSign();
    void setValue(std::string MyValue);
    void setPoint(int MyPoint);
    void setSign(bool MySign);
    void print(bool enter);
    void ParametrsPrint();
    void normalize();
};
class Polynom {
private:
    std::vector<BigNumber> polynom;
public:
    Polynom(const std::list<BigNumber>& numbers);
    Polynom operator - () const {
        std::list<BigNumber> numbers = {};
        BigNumber b{ "1",0,1 };
        for (BigNumber n : polynom) numbers.push_back(n * b);
        return Polynom{ numbers };
    }
    Polynom operator + (const Polynom& number) const {
        int size = polynom.size();
        int size1 = number.polynom.size();
        if (size >= size1) {
            std::list<BigNumber> num = {};
            for (int i = 0; i < size1; i++) {
                num.push_back(number.polynom[i] + polynom[i]);
            }
            for (int i = size1; i < size; i++) {
                num.push_back(polynom[i]);
            }
            return Polynom{ num };
        }
        else {
            std::list<BigNumber> num = {};
            for (int i = 0; i < size; i++) {
                num.push_back(number.polynom[i] + polynom[i]);
            }
            for (int i = size; i < size1; i++) {
                num.push_back(number.polynom[i]);
            }
            return Polynom{ num };
        }
    }
    Polynom operator - (const Polynom& number) const {
        return (*this + (-number));
    }
    Polynom operator * (const Polynom& number) const {
        std::list<BigNumber> multiply = {};
        for (int i = 0; i < polynom.size() + number.polynom.size() - 1;i++) {
            BigNumber s{ "0" };
            for (int j = 0; j < polynom.size(); j++) {
                if (i - j >= 0 && i - j < number.polynom.size()) {
                    s = s + polynom[j] * number.polynom[i - j];
                }
            }
            multiply.push_back(s);
        }
        return Polynom{ multiply };
    }
    BigNumber operator()(BigNumber num) {
        BigNumber finish{ "0" };
        BigNumber pow{ num };
        for (int i = 0; i < polynom.size(); i++) {
            if (i == 0) {
                finish = polynom[0];
                continue;
            }
            finish = finish + (pow * polynom[i]);
            pow = pow * num;
        }
        return finish;
    }
    Polynom Multyply(BigNumber x);
    void PolynomPrint();
    BigNumber max();
    std::vector<BigNumber> Solve();
    Polynom sin();
    void Truncate(int max_degree);
    Polynom cos();
    Polynom del(const Polynom& denom);
    Polynom logn();
    Polynom tan();
    BigNumber integral(BigNumber a, BigNumber b);
    Polynom pow(BigNumber n);
};
#endif
