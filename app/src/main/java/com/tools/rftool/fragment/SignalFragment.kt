package com.tools.rftool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.tools.rftool.R
import com.tools.rftool.databinding.FragmentSignalBinding
import com.tools.rftool.repository.AppConfigurationRepository
import com.tools.rftool.ui.realtimeplot.RealTimePlotAdapter
import com.tools.rftool.viewmodel.SdrDeviceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SignalFragment : Fragment() {

    private lateinit var binding: FragmentSignalBinding

    private val sdrDeviceViewModel by activityViewModels<SdrDeviceViewModel>()
    private lateinit var signalPowerPlotAdapter: RealTimePlotAdapter

    @Inject
    lateinit var appConfigurationRepository: AppConfigurationRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignalBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signalPowerPlotAdapter = RealTimePlotAdapter()
        binding.rtpSignalStrength.adapter = signalPowerPlotAdapter
        binding.rtpSignalStrength.title = getString(R.string.chart_signal_power_title)
        binding.fftPowerBar.treshold = appConfigurationRepository.autoRecThreshold

        lifecycleScope.launch {
            sdrDeviceViewModel.fftSignalMax.collect {
                binding.fftPowerBar.value = it
                signalPowerPlotAdapter.add(it)
            }
        }
        lifecycleScope.launch {
            appConfigurationRepository.autoRecThresholdFlow.collect {
                binding.fftPowerBar.treshold = it
            }
        }
    }
}