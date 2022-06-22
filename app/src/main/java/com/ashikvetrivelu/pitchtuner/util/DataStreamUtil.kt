package com.ashikvetrivelu.pitchtuner.util

import com.ashikvetrivelu.pitchtuner.aspect.PerfLogMonitor
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.DoubleBuffer
import kotlin.math.pow

object DataStreamUtil {

    val logger = LoggerFactory.getLogger(this.javaClass)

    @PerfLogMonitor
    fun convertBytesToDouble(data: ByteArray, sampleSizeInBits: Int, byteOrder: ByteOrder) : DoubleArray {
        val buffer = ByteBuffer.wrap(data)
        buffer.order(byteOrder)
        val doubleArray = DoubleArray(data.size * 8/ sampleSizeInBits)
        for (i in doubleArray.indices) {
            doubleArray[i] = when (sampleSizeInBits) {
                16 -> buffer.short / 32768.0
                else -> throw UnsupportedOperationException("Unknown Sample Size")
            }
        }
        return doubleArray
    }

    fun convertDoubleToBytes(data: DoubleArray, sampleSizeInBits: Int) : ByteArray {
        val temp = data.map { (it * 32768.0).toInt().toShort()}.toShortArray()
        val buffer = ByteBuffer.allocate(temp.size * sampleSizeInBits / 8)
        for (i: Short in temp) {
            buffer.putShort(i)
        }
        return buffer.array()
    }

    fun convertShortToBytes(data: ShortArray, sampleSizeInBits: Int, byteOrder: ByteOrder): ByteArray {
        val buffer = ByteBuffer.allocate(data.size * sampleSizeInBits / 8)
        buffer.order(byteOrder)
        for(i in data) {
            buffer.putShort(i)
        }
        return buffer.array()
    }
}