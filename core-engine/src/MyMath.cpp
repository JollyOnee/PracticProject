#include "MyMath.h"
#include <iostream>

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
void BigNumber::print() {
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
    std::cout<<std::endl;
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