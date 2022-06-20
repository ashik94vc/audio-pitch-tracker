package com.ashikvetrivelu.pitchtuner.adapter

import com.robinhood.spark.SparkAdapter

class FrequencyVisualizerAdapter(val data: DoubleArray) : SparkAdapter() {

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(index: Int): Any {
        return data[index]
    }

    override fun getY(index: Int): Float {
        return data[index].toFloat()
    }
}