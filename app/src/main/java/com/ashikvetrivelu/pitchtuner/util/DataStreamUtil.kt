package com.ashikvetrivelu.pitchtuner.util

import com.ashikvetrivelu.pitchtuner.aspect.PerfLogMonitor
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.pow

object DataStreamUtil {

    val logger = LoggerFactory.getLogger(this.javaClass)

    @PerfLogMonitor
    fun convertBytesToDouble(data: ByteArray, sampleSizeInBits: Int, byteOrder: ByteOrder) : DoubleArray {
        val buffer = ByteBuffer.wrap(data)
        buffer.order(byteOrder)
        val doubleArray = DoubleArray(data.size / sampleSizeInBits)
        for (i in doubleArray.indices) {
            doubleArray[i] = when (sampleSizeInBits) {
                16 -> buffer.short / 32768.0
                else -> throw UnsupportedOperationException("Unknown Sample Size")
            }
        }
        return doubleArray
    }

}