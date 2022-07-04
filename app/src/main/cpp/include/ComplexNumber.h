//
// Created by paolo on 24/06/22.
//

#ifndef RFTOOL_COMPLEXNUMBER_H
#define RFTOOL_COMPLEXNUMBER_H

#define sqr(x) ((x)*(x))

template<typename T>
struct ComplexNumber {
    T r;
    T i;

    ComplexNumber<T> operator+(const ComplexNumber<T> &rhs) const {
        return {
                this->r + rhs.r,
                this->i + rhs.i
        };
    }

    ComplexNumber<T> &operator+=(const ComplexNumber<T> &rhs) {
        this->r += rhs.r;
        this->i += rhs.i;
        return *this;
    }

    ComplexNumber<T> operator-() const {
        return {
                -this->r,
                -this->i
        };
    }

    ComplexNumber<T> operator-(const ComplexNumber<T> &rhs) const {
        return *this + (-rhs);
    }

    ComplexNumber<T> operator*(const ComplexNumber<T> &rhs) const {
        return {
                this->r * rhs.r - this->i * rhs.i,
                this->r * rhs.i + this->i * rhs.r
        };
    }

    ComplexNumber<T> &operator*=(const ComplexNumber<T> &rhs) {
        auto result = *this * rhs;
        this->r = result.r;
        this->i = result.i;
        return *this;
    }

    ComplexNumber<T> operator/(const ComplexNumber<T> &rhs) const {
        return {
                (this->r * rhs.r + this->i * rhs.i) / (sqr(rhs.r) + sqr(rhs.i)),
                (this->i * rhs.r - this->r * rhs.i) / (sqr(rhs.r) + sqr(rhs.i))
        };
    }

    ComplexNumber<T> operator/=(const ComplexNumber<T>& rhs) {
        auto result = *this / rhs;
        this->r = rhs.r;
        this->i = rhs.i;
        return *this;
    }

    ComplexNumber<T> operator*(double rhs) const {
        return {
            this->r * rhs,
            this->i * rhs
        };
    }

    ComplexNumber<T>& operator*=(double rhs) {
        this->r *= rhs;
        this->i *= rhs;
        return *this;
    }

    ComplexNumber<T> operator/(double rhs) const {
        return {
                this->r / rhs,
                this->i / rhs
        };
    }

    ComplexNumber<T>& operator/=(double rhs) {
        this->r /= rhs;
        this->i /= rhs;
        return *this;
    }

            T magnitude() const {
        return sqrt(sqr(this->r) + sqr(this->i));
    }

    T phase() const {
        if(this->r != 0) {
            return atan(this->i / this->r);
        } else {
            if(this->i > 0) {
                return M_PI;
            } else if (this->i < 0) {
                return -M_PI;
            } else {
                return 0;
            }
        }
    }
};

#endif //RFTOOL_COMPLEXNUMBER_H
