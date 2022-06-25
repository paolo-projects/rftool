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
    /**
     * Put a value into the bucket. This action is thread-safe
     * @param value The value to put
     */
    void put(T* value) {
        T* previousData;
        {
            std::unique_lock<std::mutex> lock(mtx);
            previousData = data;
            data = value;
        }
        delete previousData;
    }
    /**
     * Collect the current value from the bucket, run the provided function
     * and then delete it. This action is thread-safe
     * @param threadSafeExecutor The function that will be called with the current object as argument if there's any
     */
    void collect(std::function<void(T*)> threadSafeExecutor) {
        std::unique_lock<std::mutex> lock(mtx);
        if(data) {
            threadSafeExecutor(data);
        }
        delete data;
        data = nullptr;
    };
    /**
     * Claim the current object, returning it and emptying the bucket. This action is thread-safe
     * @return The current object in the bucket, or nullptr
     */
    T* claim() {
        std::unique_lock<std::mutex> lock(mtx);
        T* claimedData = data;
        data = nullptr;
        return claimedData;
    }
    /**
     * Returns true if the bucket is empty. This action is thread-safe
     * @return true or false
     */
    bool empty() {
        std::unique_lock<std::mutex> lock(mtx);
        return data == nullptr;
    }
    /**
     * Clear the contents of the bucket, deleting the current object if any. This action is thread-safe
     */
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
