package com.ashikvetrivelu.pitchtuner.util

import com.ashikvetrivelu.pitchtuner.aspect.PerfLogMonitor

object AudioSignalProcessor {

    @PerfLogMonitor
    fun harmonicProductSpectrum(sample: Array<ComplexDouble>, sampleSize: Int): Int {
        val data = Array(sampleSize) {
            Array(sample.size / sampleSize) {
                ComplexDouble(0.0, 0.0)
            }
        }
        for (i in 0 until sampleSize) {
            for (j in 0 until data[0].size)
                data[i][j] = sample[j * (i + 1)]
        }
        val result = Array(sample.size / sampleSize) {
            ComplexDouble(0.0, 0.0)
        }

        for (i in result.indices) {
            var temp = ComplexDouble(1.0, 0.0)
            for (j in 0 until sampleSize) {
                temp = temp.multiply(data[j][i])
            }
            result[i] = temp
        }

        var max = Double.MIN_VALUE
        var maxIdx = -1
        result.map { it.abs() }.forEachIndexed {idx, value -> if (value > max ) {
            maxIdx = idx
            max = value
        }}
        return maxIdx
    }

}