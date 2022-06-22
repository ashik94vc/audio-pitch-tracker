package com.ashikvetrivelu.pitchtuner.model
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PitchViewModel : ViewModel() {

    val processId = MutableLiveData<String>()
    val fourierFrequencyData = MutableLiveData<DoubleArray>()
    val frequency = MutableLiveData<Double>()

    init {
        processId.value = "Process Not Attached!"
    }

}