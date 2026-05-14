#include "MyMath.h"
#include <iostream>
#include <list>
#include <vector>

BigNumber::BigNumber(std::string _value, int _point, bool _sign) {
    point = _point;
    value = _value;
    sign = _sign;
    normalize();
}
BigNumber::BigNumber(std::string _value, int _point) : BigNumber(_value, _point, 0) { }
BigNumber::BigNumber(std::string _value) : BigNumber(_value, 0) { }
BigNumber::BigNumber(const BigNumber &number) {
    value = number.value;
    point = number.point;
    sign = number.sign;
}
int BigNumber::getPoint() {
    return point;
}
std::string BigNumber::getValue() {
    return value;
}
bool BigNumber::getSign() {
    return sign;
}
void BigNumber::setPoint(int MyPoint) {
    point = MyPoint;
}
void BigNumber::setValue(std::string MyValue) {
    value = MyValue;
}
void BigNumber::setSign(bool MySign) {
    sign = MySign;
}
void BigNumber::print(bool enter) {
    normalize();
    if (sign==true) std::cout<<"-";
    if (point==0) {
        for (int i=0; i<value.length(); i++) std::cout<<value[i];
    }
    else if (point>0) {
        if (value.length()>=point+1) {
            int point1 = value.length()-point;
            for (int i=0; i<value.length(); i++) {
                if (point1 == 0) {
                    std::cout<<".";
                    point1--;
                    i--;
                    continue;
                }
                std::cout<<value[i];
                --point1;
            }
        }
        else {
            int point1 = point;
            std::cout<<"0.";
            point1--;
            while (point1+1>value.length()) {
                std::cout<<"0";
                --point1;
            }
            for (int i=0; i<value.length(); i++) std::cout<<value[i];
        }
    }
    if (enter==1) std::cout<<std::endl;
}
void BigNumber::ParametrsPrint() {
    std::cout<<"num: value = "<<value<<", point = "<<point<<", sign = "<<sign<<std::endl;
}
void BigNumber::normalize() {
    while (value.length() > 1 && value[0] == '0')
        value.erase(0, 1);
    if (value == "0") {
        sign = 0;
        point = 0;
    }
    while (point > 0 && value.back() == '0') {
        value.pop_back();
        point--;
    }
}
void Polynom::PolynomPrint() {
    int i=0;
    for (BigNumber n : polynom) {
        if (i!=0 && (n.getSign()==0)) std::cout<<"+";
        if (n.getValue()!="0") n.print(0);
        else (std::cout<<"0");
        std::cout<<"x^"<<i;
        i++;
    }
    std::cout<<std::endl;
}
Polynom::Polynom(const std::list<BigNumber>& numbers) {
    for (BigNumber n : numbers) {
        polynom.push_back(n); 
    }
}
BigNumber Polynom::max() {
    BigNumber max = polynom[0];
    BigNumber minus{"1",0,1};
    std::vector<BigNumber> poly = polynom;
    for (int i=0; i<poly.size(); i++) {
        if (poly[i].getSign()==1) {
            (poly[i] = poly[i]*minus);
        }
        if (BigNumber{poly[i]-max}.getSign()==0) max = poly[i];
    }
    return max;
}
std::vector<BigNumber> Polynom::Solve() {
    BigNumber h{"1",1};
    BigNumber minus{"1",0,1};
    BigNumber one{"1"};
    std::vector<BigNumber> poly = polynom;
    std::list<BigNumber> n = {};
    for (int i = 0; i<poly.size(); i++) {
        n.push_back(poly[i]); 
    }
    Polynom pol = n;
    if (poly[poly.size()-1].getSign()==1) {
            (poly[poly.size()-1] = poly[poly.size()-1]*minus);
    }
    BigNumber left{"1"};
    BigNumber right{"1"};
    for (int i=0; i<poly.size(); i++) {
        if ((poly.size()-1-i) != 0) {
            if (BigNumber{(max() / poly[poly.size()-1-i] + one)-BigNumber{"1000"}}.getSign()==1) left = minus * (max() / poly[poly.size()-1-i] + one);
            else left = BigNumber{"1000",0,1};
            if (BigNumber{(max() / poly[poly.size()-1-i] + one)-BigNumber{"1000"}}.getSign()==1) right = (max() / poly[poly.size()-1-i] + one);
            else right = BigNumber{"1000"};
            break;
        }
    }
    std::vector<BigNumber> sol = {};
    for (BigNumber i=left-h; BigNumber{right-i}.getSign()==0; i = BigNumber{"1",1}+i) {
        BigNumber mul{pol(i)*pol(BigNumber{"1",1}+i)}; mul.print(1); i.print(1);
        if (mul.getSign()==1) {
            BigNumber l = i; BigNumber r = BigNumber{"1",1}+i;
            for (int j=0; j<5; j++) {
                auto fl = pol(l);
                auto fr = pol(r);
                mul = ((l*fr-r*fl)/(fr-fl));
                if (BigNumber{(fr-fl)-BigNumber{"1",4,1}}.getSign()==1 || (BigNumber{(fr-fl)-BigNumber{"1",4}}.getSign()==0 ) && mul.getSign()==0) {
                    r = ((l*fr-r*fl)/(fr-fl));
                    if (j==4) {
                        sol.push_back(r);
                        r.print(1);
                    }
                }
                else {
                    l = ((l*fr-r*fl)/(fr-fl));
                    if (j==4) {
                        sol.push_back(l);
                        l.print(1);
                    }
                }
            }
        } 
    }
    return sol;
}
Polynom Polynom::Multyply(BigNumber x) {
    std::vector<BigNumber> poly = polynom;
    for (int i=0; i<poly.size(); i++) poly[i] = poly[i] * x;
    std::list<BigNumber> sol;
    for (int i=0; i<poly.size(); i++) sol.push_back(poly[i]);
    return Polynom{sol};
}
BigNumber BigNumber::factorial(BigNumber x) {
    BigNumber y{"1"};
    if (x.getSign()==0 && x.getPoint()==0) {
        for (BigNumber i{"1"}; BigNumber{i-x-BigNumber{"1"}}.getSign()==1; i = i+BigNumber{"1"}) y = y * i;
        return y;
    }
    else {
        return BigNumber{"42"};
    }
}
BigNumber BigNumber::A(BigNumber x, BigNumber y) {
    if (x.getSign()==0&&y.getSign()&&x.getPoint()==0&&y.getPoint()==0&&BigNumber{y-x-BigNumber{"1"}}.getSign()==1) return BigNumber{factorial(x)/(factorial(BigNumber{x-y}))};
    else return BigNumber{"42"};
}
BigNumber BigNumber::C(BigNumber x, BigNumber y) {
    if (x.getSign()==0&&y.getSign()&&x.getPoint()==0&&y.getPoint()==0&&BigNumber{y-x-BigNumber{"1"}}.getSign()==1) return BigNumber{factorial(x)/(factorial(y)*factorial(BigNumber{x-y}))};
    else return BigNumber{"42"};
}
Polynom Polynom::sin() {
    std::vector<BigNumber> poly = polynom;
    std::list<BigNumber> poly1;
    for (int i=0; i<poly.size(); i++) poly1.push_back(poly[i]);
    Polynom poly2{poly1};
    Polynom power = poly2;
    Polynom sol({BigNumber{"0"}});
    for (BigNumber i{"1"}; BigNumber{i-BigNumber{"8"}}.getSign()==1; i = i+BigNumber{"2"}) {
        if (BigNumber{(i+BigNumber{"1"})/BigNumber{"4"}}.getPoint()==0) sol = sol + (power.Multyply(BigNumber{"1",0,1}/i.factorial(i)));
        else {
            sol = sol + (power.Multyply(BigNumber{"1"}/i.factorial(i)));
        }
        power = power * poly2 * poly2;
    }
    return sol;
}
Polynom Polynom::cos() {
    std::vector<BigNumber> poly = polynom;
    std::list<BigNumber> poly1;
    for (int i=0; i<poly.size(); i++) poly1.push_back(poly[i]);
    Polynom poly2{poly1};
    Polynom power({BigNumber{"1"}});
    Polynom sol({BigNumber{"0"}});
    for (BigNumber i{"0"}; BigNumber{i-BigNumber{"8"}}.getSign()==1; i = i+BigNumber{"2"}) {
        if (BigNumber{(i)/BigNumber{"4"}}.getPoint()==0) sol = sol + (power.Multyply(BigNumber{"1"}/i.factorial(i)));
        else sol = sol + (power.Multyply(BigNumber{"1",0,1}/i.factorial(i)));
        power = power * poly2 * poly2;
    }
    return sol;
}
Polynom Polynom::del(const Polynom& denom) {
    std::vector<BigNumber> a = polynom;
    std::vector<BigNumber> b = denom.polynom;
    std::vector<BigNumber> r(8, BigNumber{"0"});
    BigNumber b0 = b[0];
    if (BigNumber{b0-BigNumber{"0"}}.getPoint()==0 && BigNumber{b0-BigNumber{"0"}}.getSign()==0&&BigNumber{b0-BigNumber{"1"}}.getSign()==1) {
        return Polynom({BigNumber{"42"}});
    }
    r[0] = a[0] / b0;
    for (int n = 1; n < 8; n++) {
        BigNumber sum{"0"};
        for (int k = 1; k <= n; k++) {
            if (k < b.size() && (n - k) < 8) {
                sum = sum + b[k] * r[n - k];
            }
        }
        BigNumber an = (n < a.size()) ? a[n] : BigNumber{"0"};
        r[n] = (an - sum) / b0;
    }
    std::list<BigNumber> soll{};
    for (int i=0; i<r.size(); i++) soll.push_back(r[i]);
    return Polynom{soll};
}
Polynom Polynom::tan() {
    Polynom s = sin();
    Polynom c = cos();
    BigNumber c0 = c.polynom[0];
    for (auto &x : s.polynom) {
        x = x / c0;
    }
    for (auto &x : c.polynom) {
        x = x / c0;
    }
    return s.del(c);
}
BigNumber BigNumber::rad(BigNumber x) {
    return x * BigNumber{"31415926536",10} / BigNumber{"180"};
}
BigNumber BigNumber::grade(BigNumber x) {
    return x / BigNumber{"31415926536",10} * BigNumber{"180"};
}
void Polynom::Truncate(int max_degree) {
    int i = 0;
    auto it = polynom.begin();
    while (it != polynom.end()) {
        if (i > max_degree) {
            it = polynom.erase(it);
        } 
        else {
            ++it;
        }
        ++i;
    }
    if (polynom.empty()) {
        polynom.push_back(BigNumber{"0"});
    }
}
Polynom Polynom::logn() {
    const int N = 8;
    Polynom one({BigNumber{"1"}});
    Polynom two({BigNumber{"2"}});
    Polynom x = *this;
    Polynom t = (x - one).del(x + one);
    Polynom term = t;
    Polynom result({BigNumber{"0"}});
    for (int n = 0; n < N; n++) {
        int k = 2 * n + 1;
        BigNumber denom = BigNumber{std::to_string(k)};
        BigNumber coeff = BigNumber{"1"} / denom;
        Polynom add = term.Multyply(coeff);
        add.Truncate(8);
        result = result + add;
        result.Truncate(8);
        term = term * t * t;
        term.Truncate(8);
    }
    result = result.Multyply(BigNumber{"2"});
    result.Truncate(8);
    return result;
}
BigNumber Polynom::integral(BigNumber a, BigNumber b) {
    BigNumber h = (b - a) / BigNumber{std::to_string(100)};
    BigNumber sum = (*this)(a) + (*this)(b);
    for (int i = 1; i < 100; i++) {
        BigNumber x = a + (h * BigNumber{std::to_string(i)});
        BigNumber fx = (*this)(x);
        if (i % 2 == 0) {
            sum = sum + fx * BigNumber{"2"};
        } else {
            sum = sum + fx * BigNumber{"4"};
        }
    }
    return (h * sum) / BigNumber{"3"};
}
Polynom Polynom::pow(BigNumber n) {
    const int N = 8;
    Polynom f = *this;
    Polynom ln = Polynom({ n }).logn();
    Polynom A = f*ln;
    Polynom term = Polynom({BigNumber{"1"}});
    Polynom result({BigNumber{"0"}});
    for (BigNumber k{"0"}; BigNumber{k-BigNumber{"8"}}.getSign()==1; k = k + BigNumber{"1"}) {
        BigNumber denom = BigNumber{"1"}.factorial(k);
        BigNumber coeff = BigNumber{"1"} / denom;
        Polynom add = term.Multyply(coeff);
        add.Truncate(8);
        result = result + add;
        result.Truncate(8);
        term = term * A;
        term.Truncate(8);
    }
    return result;
}
BigNumber BigNumber::percent(BigNumber x) {
    return x * BigNumber{"1",2};
}
