// IAudioRecorderInterface.aidl
package com.ashikvetrivelu.pitchtuner.audio.recorder;

import com.ashikvetrivelu.pitchtuner.audio.recorder.IAudioStreamListener;

interface IAudioRecorderInterface {

    int getProcessId();

    void startAudioStream(IAudioStreamListener listener);

}