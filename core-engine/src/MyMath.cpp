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
BigNumber BigNumber::round(BigNumber x) {
    std::string a = "";
    int integerSize = x.getValue().size() - x.getPoint();
    for (int i = 0; i < integerSize; i++)
        a += x.getValue()[i];
    if (a == "")
        a = "0";
    BigNumber res{a, 0, x.getSign()};
    if (x.getSign() == 1 && x.getPoint() > 0) {
        bool frac = false;
        for (int i = integerSize; i < x.getValue().size(); i++) {
            if (x.getValue()[i] != '0') {
                frac = true;
                break;
            }
        }
        if (frac)
            res = res - BigNumber{"1"};
    }
    return res;
}
std::vector<BigNumber> Polynom::Solve() {
    std::vector<BigNumber> roots;
    std::list<BigNumber> n;
    for (int i = 0; i < polynom.size(); i++)
        n.push_back(polynom[i]);
    Polynom p = n;
    BigNumber step{"1",2};
    BigNumber eps {"1",6};
    BigNumber left {"30",0,1};
    BigNumber right{"30"};
    auto absVal = [&](const BigNumber& v)
    {
        BigNumber t = v;
        if (t.getSign() == 1)
            t = BigNumber{"0"} - t;
        return t;
    };
    auto isZero = [&](const BigNumber& v)
    {
        return (BigNumber{eps - absVal(v)}).getSign() != 1;
    };
    auto addRoot = [&](const BigNumber& r)
    {
        for (auto &rt : roots)
        {
            BigNumber d = rt - r;
            if (d.getSign() == 1)
                d = BigNumber{"0"} - d;
            if (BigNumber{eps - d}.getSign() != 1)
                return;
        }
        roots.push_back(r);
    };
    BigNumber x1 = left;
    BigNumber f1 = p(x1);
    for (BigNumber x2 = left + step;
         BigNumber{right - x2}.getSign() != 1;
         x2 = x2 + step)
    {
        BigNumber f2 = p(x2);
        if (isZero(f1))
            addRoot(x1);
        if (isZero(f2))
            addRoot(x2);
        BigNumber prod = f1 * f2;
        if (prod.getSign() == 1)
        {
            BigNumber l = x1;
            BigNumber r = x2;
            for (int i = 0; i < 30; i++)
            {
                BigNumber mid = l + (r - l) / BigNumber{"2"};
                BigNumber fm = p(mid);
                if (isZero(fm))
                {
                    l = r = mid;
                    break;
                }
                if (BigNumber{f1 * fm}.getSign() == 1)
                    r = mid;
                else
                    l = mid;
            }
            addRoot(l);
        }
        x1 = x2;
        f1 = f2;
    }
    return roots;
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
    for (int i=0; i<poly.size(); i++) {
        if (i==0 && (BigNumber{poly[i]-BigNumber{"314159265",8}}.getSign()==0||BigNumber{poly[i]+BigNumber{"314159265",8}}.getSign()==1)) {
            poly1.push_back(BigNumber{poly[0]-BigNumber{"2"}*BigNumber{"314159265",8}*BigNumber{BigNumber{poly[0]+BigNumber{"314159265",8}}/BigNumber{BigNumber{"2"}*BigNumber{"314159265",8}}}.round(BigNumber{BigNumber{poly[0]+BigNumber{"314159265",8}}/BigNumber{BigNumber{"2"}*BigNumber{"314159265",8}}})});
            continue;
        }
        poly1.push_back(poly[i]);
    }
    Polynom poly2{poly1};
    Polynom power = poly2;
    Polynom sol({BigNumber{"0"}});
    for (BigNumber i{"1"}; BigNumber{i-BigNumber{"12"}}.getSign()==1; i = i+BigNumber{"2"}) {
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
    for (int i=0; i<poly.size(); i++) {
        if (i==0 && (BigNumber{poly[i]-BigNumber{"314159265",8}}.getSign()==0||BigNumber{poly[i]+BigNumber{"314159265",8}}.getSign()==1)) {
            poly1.push_back(BigNumber{poly[0]-BigNumber{"2"}*BigNumber{"314159265",8}*BigNumber{BigNumber{poly[0]+BigNumber{"314159265",8}}/BigNumber{BigNumber{"2"}*BigNumber{"314159265",8}}}.round(BigNumber{BigNumber{poly[0]+BigNumber{"314159265",8}}/BigNumber{BigNumber{"2"}*BigNumber{"314159265",8}}})});
            continue;
        }
        poly1.push_back(poly[i]);
    }
    Polynom poly2{poly1};
    Polynom power({BigNumber{"1"}});
    Polynom sol({BigNumber{"0"}});
    for (BigNumber i{"0"}; BigNumber{i-BigNumber{"12"}}.getSign()==1; i = i+BigNumber{"2"}) {
        if (BigNumber{(i)/BigNumber{"4"}}.getPoint()==0) sol = sol + (power.Multyply(BigNumber{"1"}/i.factorial(i)));
        else sol = sol + (power.Multyply(BigNumber{"1",0,1}/i.factorial(i)));
        power = power * poly2 * poly2;
    }
    return sol;
}
Polynom Polynom::del(const Polynom& denom) {
    std::vector<BigNumber> a = polynom;
    std::vector<BigNumber> b = denom.polynom;
    std::vector<BigNumber> r(12, BigNumber{"0"});
    BigNumber b0 = b[0];
    if (BigNumber{b0-BigNumber{"0"}}.getPoint()==0 && BigNumber{b0-BigNumber{"0"}}.getSign()==0&&BigNumber{b0-BigNumber{"1"}}.getSign()==1) {
        return Polynom({BigNumber{"42"}});
    }
    r[0] = a[0] / b0;
    for (int n = 1; n < 12; n++) {
        BigNumber sum{"0"};
        for (int k = 1; k <= n; k++) {
            if (k < b.size() && (n - k) < 12) {
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
std::vector<BigNumber> Polynom::getVector() {
    return polynom;
}
BigNumber BigNumber::log() {
    BigNumber ln2 = BigNumber{"069314718056",11};
    BigNumber p = BigNumber{"1"};
    BigNumber i = BigNumber{"0"};
    BigNumber xx = (*this);
    while (BigNumber{p-xx}.getSign()==1) {
        p = p * BigNumber{"2"};
        i = i + BigNumber{"1"};
    }
    BigNumber n = xx/p;
    BigNumber ln_n = (Polynom({n}).logn()).getVector()[0];
    return ln_n+(ln2*i);
}
Polynom Polynom::logn() {
    const int N =12;
    Polynom one({BigNumber{"1"}});
    Polynom zero({BigNumber{"0"}});
    BigNumber ln2 = BigNumber{"069314718056",11};
    bool allZero = true;
    for (size_t i = 0; i < polynom.size(); i++) {
        if (!(polynom[i].getValue() == BigNumber{"0"}.getValue() &&
              polynom[i].getPoint() == BigNumber{"0"}.getPoint() &&
              polynom[i].getSign() == BigNumber{"0"}.getSign())) {
            allZero = false;
            break;
        }
    }
    if (allZero) {
        throw std::runtime_error("log(0)");
    }
    BigNumber c = polynom[0];
    BigNumber p = BigNumber{"1"};
    BigNumber k = BigNumber{"0"};
    while ((p - c).getSign() == 1) {
        p = p * BigNumber{"2"};
        k = k + BigNumber{"1"};
    }
    BigNumber n = c / p;
    BigNumber z =
            (n - BigNumber{"1"}) /
            (n + BigNumber{"1"});
    BigNumber zterm = z;
    BigNumber ln_c{"0"};
    for (int i = 0; i < N; i++) {
        int deg = 2 * i + 1;
        BigNumber coeff =
                BigNumber{"1"} /
                BigNumber{std::to_string(deg)};
        ln_c = ln_c + zterm * coeff;
        zterm = zterm * z * z;
    }
    ln_c = ln_c * BigNumber{"2"};
    ln_c = ln_c + (ln2 * k);
    Polynom Q = this->del(Polynom({c}));
    Polynom t = (Q - one).del(Q + one);
    Polynom term = t;
    Polynom result = zero;
    for (int i = 0; i < N; i++) {
        int deg = 2 * i + 1;
        BigNumber coeff =
                BigNumber{"1"} /
                BigNumber{std::to_string(deg)};
        result = result + term.Multyply(coeff);
        term = term * t * t;
    }
    result = result.Multyply(BigNumber{"2"});
    result.polynom[0] =
            result.polynom[0] + ln_c;
    result.Truncate(12);
    return result;
}
Polynom Polynom::loga(Polynom b) {
    std::vector<BigNumber> poly = polynom;
    std::list<BigNumber> poly1;
    for (int i=0; i<poly.size(); i++) {
        poly1.push_back(poly[i]);
    }
    Polynom poly2{poly1};
    return b.logn().del(poly2.logn());
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
    const int N = 12;
    Polynom f = *this;
    Polynom ln = Polynom({ n }).logn();
    Polynom A = f*ln;
    Polynom term = Polynom({BigNumber{"1"}});
    Polynom result({BigNumber{"0"}});
    for (BigNumber k{"0"}; BigNumber{k-BigNumber{"12"}}.getSign()==1; k = k + BigNumber{"1"}) {
        BigNumber denom = BigNumber{"1"}.factorial(k);
        BigNumber coeff = BigNumber{"1"} / denom;
        Polynom add = term.Multyply(coeff);
        add.Truncate(12);
        result = result + add;
        result.Truncate(12);
        term = term * A;
        term.Truncate(12);
    }
    return result;
}
BigNumber BigNumber::percent(BigNumber x) {
    return x * BigNumber{"1",2};
}