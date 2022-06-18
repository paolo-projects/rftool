//
// Created by paolo on 17/06/22.
//

#ifndef RFTOOL_OPTIONAL_H
#define RFTOOL_OPTIONAL_H

#include <type_traits>

template<typename T>
class Optional {
public:
    Optional() : hasValue(false) {}

    Optional(const T &value) : T(value), hasValue(true) {}

    Optional &operator=(const T &rhs) {
        value = T(rhs);
        hasValue = true;
    }

    Optional &operator=(const Optional &rhs) {
        value = rhs.value;
        hasValue = rhs.hasValue;
    }

    const T& get() const {
        return value;
    }

    const T& operator()() const {
        return value;
    }

    void reset() {
        hasValue = false;
        value = T();
    }

    bool isEmpty() const {
        return !hasValue;
    }

private:
    bool hasValue;
    T value;
};

#endif //RFTOOL_OPTIONAL_H
