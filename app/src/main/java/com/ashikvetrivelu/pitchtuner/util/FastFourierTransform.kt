package com.ashikvetrivelu.pitchtuner.util

import org.slf4j.LoggerFactory
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object FastFourierTransform {

    fun calcAndApplyBlackmanWindow(data: Array<ComplexDouble>) {
        val sampleSize = data.size
        val window = DoubleArray(sampleSize)
        for (i in window.indices) {
            window[i] = (0.42 - (0.5 * cos(2 * PI * i / (sampleSize - 1)))
                    + (0.08 * cos(4 * PI * i / (sampleSize - 1))))
            data[i].real = window[i] * data[i].real
            data[i].imaginary = window[i] * data[i].imaginary
        }
    }

    fun performFourierTransform(data: Array<ComplexDouble>): Array<ComplexDouble> {

        val size = data.size

        if (size == 0) {
            return emptyArray()
        }
        if (size == 1) {
            return arrayOf(data[0])
        }

        val evenComplex: MutableList<ComplexDouble> = emptyList<ComplexDouble>().toMutableList()
        val oddComplex: MutableList<ComplexDouble> = emptyList<ComplexDouble>().toMutableList()

        for (i in data.indices) {
            when(i%2) {
                0 -> evenComplex.add(data[i])
                1 -> oddComplex.add(data[i])
            }
        }

        val evenFFT = performFourierTransform(evenComplex.toTypedArray())
        val oddFFT = performFourierTransform(oddComplex.toTypedArray())

        val result: Array<ComplexDouble> = Array(size) { ComplexDouble(0.0, 0.0) }
        for (i in 0 until (size/2)) {
            val kth = -2 * i * Math.PI / size
            val wk = ComplexDouble(cos(kth), sin(kth))
            result[i] = evenFFT[i].plus(wk.multiply(oddFFT[i]))
            result[i + size/2] = evenFFT[i].minus(wk.multiply(oddFFT[i]))
        }
        return result
    }

}