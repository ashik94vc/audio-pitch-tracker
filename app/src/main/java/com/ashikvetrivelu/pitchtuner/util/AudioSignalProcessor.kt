package com.ashikvetrivelu.pitchtuner.util

import com.ashikvetrivelu.pitchtuner.aspect.PerfLogMonitor
import java.nio.ByteOrder
import kotlin.math.sqrt

object AudioSignalProcessor {

    private fun findRMSAverage(data: DoubleArray): Double {
        val sum = data.sumOf { it * it }
        val meanSquare = sum/data.size
        return sqrt(meanSquare)
    }

    fun normalizeData(data: DoubleArray): DoubleArray {
        val rmsThreshold = findRMSAverage(data)
        return data.map { if (it < rmsThreshold) 0.0 else it }.toDoubleArray()
    }

    fun noiseReduce(data: ByteArray, sampleRate: Int, byteOrder: ByteOrder): ByteArray {
        var doubleArray = DataStreamUtil.convertBytesToDouble(data, sampleRate, byteOrder)
        doubleArray = normalizeData(doubleArray)
        return DataStreamUtil.convertDoubleToBytes(doubleArray, sampleRate)
    }

    @PerfLogMonitor
    fun harmonicProductSpectrum(sample: DoubleArray, order: Int): DoubleArray {
        val hps = DoubleArray(sample.size)
        val hpsLength = sample.size / (order + 1)
        for (i in sample.indices) {
            if ( i < hpsLength) {
                hps[i] = sample[i]
            } else {
                hps[i] = Double.NEGATIVE_INFINITY
            }
        }

        for (harmonic in 1..order) {
            val downSamplingFactor = harmonic + 1
            for (index in 0 until hpsLength) {
                var avg: Double = 0.0
                for (i in 0 until downSamplingFactor) {
                    avg += sample[index*downSamplingFactor + i]
                }
                hps[index] += avg / downSamplingFactor
            }
        }
        return hps
    }

}