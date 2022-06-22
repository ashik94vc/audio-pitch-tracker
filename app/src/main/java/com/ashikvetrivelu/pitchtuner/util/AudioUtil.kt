package com.ashikvetrivelu.pitchtuner.util

import kotlin.math.ceil
import kotlin.math.log
import kotlin.math.pow

object AudioUtil {

    fun findBestBufferSize(sampleSize: Int): Int {
        return 2.0.pow(ceil(log(sampleSize.toDouble(), 2.0))).toInt()
    }

}