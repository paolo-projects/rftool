//
// Created by paolo on 21/06/22.
//

#ifndef RFTOOL_THREADSAFEBUCKET_H
#define RFTOOL_THREADSAFEBUCKET_H

#include <mutex>
#include <functional>

template <typename T>
class ThreadSafeBucket {
public:
    ThreadSafeBucket() {}
    ~ThreadSafeBucket() {
        std::unique_lock<std::mutex> lock(mtx);
        delete data;
        data = nullptr;
    }
    void put(T* value) {
        std::unique_lock<std::mutex> lock(mtx);
        delete data;
        data = value;
    }
    void retrieve(std::function<void(T*)> threadSafeExecutor) {
        std::unique_lock<std::mutex> lock(mtx);
        if(data) {
            threadSafeExecutor(data);
        }
        delete data;
        data = nullptr;
    };
    T* claim() {
        std::unique_lock<std::mutex> lock(mtx);
        T* claimedData = data;
        data = nullptr;
        return claimedData;
    }
    bool empty() {
        std::unique_lock<std::mutex> lock(mtx);
        return data == nullptr;
    }
    void clear() {
        std::unique_lock<std::mutex> lock(mtx);
        delete data;
        data = nullptr;
    }
private:
    T* data = nullptr;
    std::mutex mtx;
};

#endif //RFTOOL_THREADSAFEBUCKET_H
