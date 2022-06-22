package com.ashikvetrivelu.pitchtuner

import android.os.Bundle
import android.os.IBinder
import android.os.Process
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ashikvetrivelu.pitchtuner.adapter.FrequencyVisualizerAdapter
import com.ashikvetrivelu.pitchtuner.audio.recorder.IAudioStreamListener
import com.ashikvetrivelu.pitchtuner.databinding.FragmentPitchMeterBinding
import com.ashikvetrivelu.pitchtuner.model.PitchViewModel
import org.slf4j.LoggerFactory

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PitchMeterFragment : Fragment() {

    private var _binding: FragmentPitchMeterBinding? = null
    private val logger = LoggerFactory.getLogger(javaClass)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val processModel: PitchViewModel by activityViewModels()


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {


        _binding = FragmentPitchMeterBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        processModel.fourierFrequencyData.observe(viewLifecycleOwner) {
            binding.audioData.adapter = FrequencyVisualizerAdapter(it)
        }

        processModel.frequency.observe(viewLifecycleOwner) {
            binding.pitchData.text = it.toString()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}