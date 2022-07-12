package com.tools.rftool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.slider.Slider
import com.tools.rftool.databinding.FragmentFmPlaybackBinding
import com.tools.rftool.viewmodel.SdrDeviceViewModel

class FmPlaybackFragment: Fragment() {

    private lateinit var binding: FragmentFmPlaybackBinding

    private val sdrDeviceViewModel by activityViewModels<SdrDeviceViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFmPlaybackBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvFmGainValue.text = "30.0 dB"

        binding.fmPlaybackEnable.setOnCheckedChangeListener { v, isChecked ->
            if(sdrDeviceViewModel.deviceConnected.value) {
                if (isChecked) {
                    sdrDeviceViewModel.startFm()
                } else {
                    sdrDeviceViewModel.stopFm()
                }
            } else {
                sdrDeviceViewModel.stopFm();
                v.isChecked = false
            }
        }

        binding.fmGainSlider.addOnChangeListener { _, value, _ ->
            binding.tvFmGainValue.text = "%.1f dB".format(value)
        }

        binding.fmGainSlider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                sdrDeviceViewModel.setDigitalGain(slider.value)
            }
        })
    }

    private val gainSliderTouchListener = object: Slider.OnSliderTouchListener {
        override fun onStartTrackingTouch(slider: Slider) {
        }

        override fun onStopTrackingTouch(slider: Slider) {
        }
    }
}