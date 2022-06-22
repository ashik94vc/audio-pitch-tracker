// IAudioStreamListener.aidl
package com.ashikvetrivelu.pitchtuner.audio.recorder;

// Declare any non-default types here with import statements

interface IAudioStreamListener {
    void onReceiveData(in byte[] dataBuffer);
    void onReceiveSampleMetaData(in double[] fourierFrequencies);
    void onReceiveFrequency(double data);
}