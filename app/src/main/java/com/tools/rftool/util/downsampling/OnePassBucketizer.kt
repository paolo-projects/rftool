package com.tools.rftool.util.downsampling

import com.tools.rftool.adapter.SignalDataPoint

internal object OnePassBucketizer {
    fun bucketize(
        input: List<SignalDataPoint>,
        inputSize: Int,
        desiredBuckets: Int
    ): List<Bucket> {
        val middleSize = inputSize - 2
        val bucketSize = middleSize / desiredBuckets
        val lastBucketSize = middleSize % desiredBuckets
        require(bucketSize != 0) { "Can't produce " + desiredBuckets + " buckets from an input series of " + (middleSize + 2) + " elements" }
        val buckets: MutableList<Bucket> = ArrayList()

        // Add first point as the only point in the first bucket
        buckets.add(Bucket.of(input[0]))
        var rest = input.subList(1, input.size - 1)

        // Add middle buckets.
        // Last middle bucket gets the rest of points when inputSize is not a multiple of desiredBuckets
        while (buckets.size < desiredBuckets + 1) {
            val size =
                if (buckets.size == desiredBuckets) bucketSize + lastBucketSize else bucketSize
            buckets.add(Bucket.of(rest.subList(0, size)))
            rest = rest.subList(size, rest.size)
        }

        // Add last point as the only point in the last bucket
        buckets.add(Bucket.of(input[input.size - 1]))
        return buckets
    }
}