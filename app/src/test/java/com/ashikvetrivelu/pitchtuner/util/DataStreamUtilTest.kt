package com.ashikvetrivelu.pitchtuner.util

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.ByteOrder

internal class DataStreamUtilTest {


    @Test
    fun testByteDoubleConversion() {

        val byteArray = ByteArray(1024)
        val doubleArray = DataStreamUtil.convertBytesToDouble(byteArray, 16, ByteOrder.nativeOrder())
        val newByteArray = DataStreamUtil.convertDoubleToBytes(doubleArray, 16)
        assertArrayEquals(newByteArray, byteArray)
    }

}