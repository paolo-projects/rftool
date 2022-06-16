package com.tools.rftool.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.tools.rftool.R
import com.tools.rftool.databinding.FragmentFftBinding
import com.tools.rftool.fft.Fft
import com.tools.rftool.repository.AppConfigurationRepository
import com.tools.rftool.rtlsdr.IQ
import com.tools.rftool.spectrogram.FftTimeSeriesSpectrogramAdapter
import com.tools.rftool.viewmodel.SdrDeviceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class FftFragment : Fragment() {

    companion object {
        private const val TAG = "FftFragment"
    }

    private lateinit var binding: FragmentFftBinding

    @Inject lateinit var appConfiguration: AppConfigurationRepository
    private val sdrDeviceViewModel by activityViewModels<SdrDeviceViewModel>()
    private val spectrogramAdapter = FftTimeSeriesSpectrogramAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFftBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.spectrogram.adapter = spectrogramAdapter
        binding.spectrogram.sampleRate = appConfiguration.sampleRate
        binding.spectrogram.centerFrequency = appConfiguration.centerFrequency

        lifecycleScope.launch {
            async {
                sdrDeviceViewModel.deviceConnected.collect(deviceStatusChanged)
            }
            async {
                sdrDeviceViewModel.fftBitmap.collect {
                    spectrogramAdapter.setBitmap(it)
                }
            }
            async {
                sdrDeviceViewModel.sdrConfigChanges.collect {
                    binding.spectrogram.sampleRate = sdrDeviceViewModel.sampleRate
                    binding.spectrogram.centerFrequency = sdrDeviceViewModel.centerFrequency
                }
            }
        }
    }

    private val deviceStatusChanged = FlowCollector { connected: Boolean ->
        if (connected) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}