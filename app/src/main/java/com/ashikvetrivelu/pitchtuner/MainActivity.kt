package com.ashikvetrivelu.pitchtuner

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.*
import android.media.audiofx.NoiseSuppressor
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.ashikvetrivelu.pitchtuner.audio.recorder.AudioRecorderService
import com.ashikvetrivelu.pitchtuner.audio.recorder.IAudioRecorderInterface
import com.ashikvetrivelu.pitchtuner.audio.recorder.IAudioStreamListener
import com.ashikvetrivelu.pitchtuner.databinding.ActivityMainBinding
import com.ashikvetrivelu.pitchtuner.model.PitchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class MainActivity : AppCompatActivity() {

    private val logger = LoggerFactory.getLogger(javaClass)

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var audioRecorderService: IAudioRecorderInterface
    private lateinit var audioTrack: AudioTrack
    private val bufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)


    private val viewModel: PitchViewModel by viewModels()

    var isServiceBound: Boolean = false
    private var isRecordingAvailable = false

    private val permissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted -> if(isGranted) {
            isRecordingAvailable = true
            logger.info("Permission Granted!")
            startRecordingService()
        } else {
            logger.error("Permission Denied!")
            Toast.makeText(this, "Permission denied to record audio!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        audioTrack = AudioTrack.Builder()
            .setAudioFormat(AudioFormat.Builder()
                .setSampleRate(44100)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build())
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(bufferSize)
            .build()

        logger.info("Started Activity")

        setSupportActionBar(binding.toolbar)
    }

    private val serviceConnection  = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            audioRecorderService = IAudioRecorderInterface.Stub.asInterface(service)
            viewModel.processId.value = audioRecorderService.processId.toString()
            val audioStreamListener = object : IAudioStreamListener.Stub() {
                override fun onReceiveData(dataBuffer: ByteArray?) {
                    MainScope().launch {
//                        viewModel.currentAudioByte.value = dataBuffer
                        dataBuffer?.let {
                            if(audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING) {
                                audioTrack.play()
                            }
                            audioTrack.write(it, 0, dataBuffer.size)
                        }
                    }
                }

                override fun onReceiveSampleMetaData(fourierFrequencies: DoubleArray?) {
                    MainScope().launch {
                        logger.info("Data received {}", fourierFrequencies)
                        viewModel.fourierFrequencyData.value = fourierFrequencies
                    }
                }

            }
            audioRecorderService.startAudioStream(audioStreamListener)
            isServiceBound = true
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            isServiceBound = false
            audioTrack.stop()
            audioTrack.release()
        }

    }

    override fun onStart() {
        super.onStart()
        if (isRecordingAvailable || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecordingService()
        } else {
            permissionRequestLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startRecordingService() {
        Intent(this, AudioRecorderService::class.java).also {
            it.putExtra(Intent.EXTRA_RESTRICTIONS_INTENT, isRecordingAvailable)
            bindService(it, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioTrack.stop()
        audioTrack.release()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}