package com.ashikvetrivelu.pitchtuner.audio.recorder

import android.app.Service
import android.content.Intent
import android.media.*
import android.media.audiofx.NoiseSuppressor
import android.os.IBinder
import android.os.Process
import com.ashikvetrivelu.pitchtuner.util.*
import com.google.common.util.concurrent.AtomicDouble
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.*

class AudioRecorderService : Service() {

    companion object {
        const val SAMPLE_RATE = 44100
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var audioRecord: AudioRecord
    private lateinit var noiseSuppressor: NoiseSuppressor
    private val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
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
                var audioBuffer = ByteArray(SAMPLE_RATE)
                val increment = (2 * PI * 940.0 / SAMPLE_RATE).toFloat()
                val shortArray = ShortArray(SAMPLE_RATE/2)
                val floatArray = FloatArray(SAMPLE_RATE/2)
                CoroutineScope(IO).launch {
                    while (true) {
                        audioRecord.read(audioBuffer, 0, SAMPLE_RATE, AudioRecord.READ_BLOCKING)
//                        audioBuffer = generateSineWave(increment, shortArray, floatArray)
                        val fundamentalFrequency = getFundamentalFrequency(audioBuffer)
                        logger.info("frequency={}", fundamentalFrequency)
                        audioStreamListener?.onReceiveData(audioBuffer)
                        audioStreamListener?.onReceiveFrequency(fundamentalFrequency)
                    }
                }
            }
        }
    }


    @SuppressWarnings("MissingPermission")
    private fun startRecording() {
        audioRecord = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        noiseSuppressor = NoiseSuppressor.create(audioRecord.audioSessionId)
        noiseSuppressor.enabled = true
        audioRecord.startRecording()
        val audioSessionId = audioRecord.audioSessionId;
        MDC.put("sessionId", audioSessionId.toString())
        logger.info("Started Recording! sessionId={} bufferSize={}", audioSessionId,bufferSize)
    }

    private fun generateSineWave(increment: Float, buffer: ShortArray, sample: FloatArray): ByteArray {
        for (i in sample.indices) {
            sample[i] = sin(increment * i)
            buffer[i] = (sample[i] * Short.MAX_VALUE).toInt().toShort()
        }
        return DataStreamUtil.convertShortToBytes(buffer, 16, byteOrder = ByteOrder.LITTLE_ENDIAN)
    }

    private fun getFundamentalFrequency(data: ByteArray) : Double {
        var dataDouble = DataStreamUtil.convertBytesToDouble(data, 16, ByteOrder.LITTLE_ENDIAN)

        val sampleSize = dataDouble.size
        val desiredSampleSize = AudioUtil.findBestBufferSize(sampleSize)
        dataDouble = DoubleArray(desiredSampleSize - sampleSize){0.0}.plus(dataDouble)
        val fourierData = ComplexDouble.fromRealDoubleArray(dataDouble)
        FastFourierTransform.calcAndApplyBlackmanWindow(fourierData)
        val fundamentalArray = FastFourierTransform.performFourierTransform(fourierData)

        val sampleCount = fundamentalArray.size
        val fourierFrequency: Double = SAMPLE_RATE.toDouble() / (sampleCount.toDouble())

        val magnitude = DoubleArray(sampleCount)

        for (i in fundamentalArray.indices) {
            magnitude[i] = log10(fundamentalArray[i].abs())
        }

        audioStreamListener?.onReceiveSampleMetaData(magnitude)


        //Bandpass 50 - 18000Hz
        for (i in 0 until ceil(50/fourierFrequency).toInt()) {
            magnitude[i] = Double.NEGATIVE_INFINITY
        }
        for (i in (18000/fourierFrequency).toInt() until sampleCount) {
            magnitude[i] = Double.NEGATIVE_INFINITY
        }


        val hps = AudioSignalProcessor.harmonicProductSpectrum(magnitude, 0)
        var maxIndex = 0
        var maxValue = Double.MIN_VALUE
        for ( i in hps.indices) {
            if (maxValue < hps[i]) {
                maxValue = hps[i]
                maxIndex = i
            }
        }

        logger.info("fundamentalIndex={} fourierFrequency={} sampleCount={} sampleSize={}", maxIndex, fourierFrequency, sampleCount, sampleSize)
        return fourierFrequency * maxIndex
    }

    private fun stopRecording() {
        noiseSuppressor.release()
        audioRecord.stop()
        audioRecord.release()
    }
}