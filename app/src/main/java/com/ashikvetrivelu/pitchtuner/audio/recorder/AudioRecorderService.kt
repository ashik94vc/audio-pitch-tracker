package com.ashikvetrivelu.pitchtuner.audio.recorder

import android.app.Service
import android.content.Intent
import android.media.*
import android.media.audiofx.NoiseSuppressor
import android.os.IBinder
import android.os.Process
import com.ashikvetrivelu.pitchtuner.util.AudioSignalProcessor
import com.ashikvetrivelu.pitchtuner.util.ComplexDouble
import com.ashikvetrivelu.pitchtuner.util.DataStreamUtil
import com.ashikvetrivelu.pitchtuner.util.FastFourierTransform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.nio.ByteOrder

class AudioRecorderService : Service() {

    companion object {
        const val SAMPLE_RATE = 44100
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var audioRecord: AudioRecord
    private lateinit var noiseSuppressor: NoiseSuppressor
    private val BufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
    private var audioStreamListener: IAudioStreamListener? = null

    override fun onBind(intent: Intent): IBinder {
        logger.info("Bound to activity={}", intent.component)
        startRecording()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopRecording()
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        logger.info("Created Service")
    }

    private val binder = object : IAudioRecorderInterface.Stub() {

        override fun getProcessId(): Int {
            logger.info("Process ID: {}", Process.myPid())
            return Process.myPid()
        }

        override fun startAudioStream(listener: IAudioStreamListener?) {
            audioStreamListener = listener
            if (AudioRecord.RECORDSTATE_RECORDING == audioRecord.recordingState) {
                var audioBuffer = ByteArray(BufferSize)

                CoroutineScope(IO).launch {
                    while (true) {
                        audioRecord.read(audioBuffer, 0, BufferSize, AudioRecord.READ_BLOCKING)
                        val fundamentalFrequency = getFundamentalFrequency(audioBuffer)
                        audioStreamListener?.onReceiveData(audioBuffer)
                        audioStreamListener?.onReceiveSampleMetaData(fundamentalFrequency.map {it.abs()}.toDoubleArray())
                    }
                }
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun startRecording() {
        audioRecord = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, BufferSize)
        noiseSuppressor = NoiseSuppressor.create(audioRecord.audioSessionId)
        noiseSuppressor.enabled = true
        audioRecord.startRecording()
        val audioSessionId = audioRecord.audioSessionId;
        MDC.put("sessionId", audioSessionId.toString())
        logger.info("Started Recording! sessionId={}", audioSessionId)
    }

    private fun getFundamentalFrequency(data: ByteArray) : Array<ComplexDouble> {
        val dataDouble = DataStreamUtil.convertBytesToDouble(data, 16, ByteOrder.BIG_ENDIAN)
        val fundamentalArray = FastFourierTransform.performFourierTransform(ComplexDouble.fromRealDoubleArray(dataDouble))
        val sampleCount = fundamentalArray.size
        val fourierFrequency = SAMPLE_RATE / sampleCount

        val index = AudioSignalProcessor.harmonicProductSpectrum(fundamentalArray, 5)

        return fundamentalArray
    }

    private fun stopRecording() {
        noiseSuppressor.release()
        audioRecord.stop()
        audioRecord.release()
    }
}