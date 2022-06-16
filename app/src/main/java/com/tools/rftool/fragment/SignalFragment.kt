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
import com.tools.rftool.viewmodel.SdrDeviceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignalFragment: Fragment() {

    private lateinit var binding: FragmentSignalBinding

    private val sdrDeviceViewModel by activityViewModels<SdrDeviceViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentSignalBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            sdrDeviceViewModel.fftSignalMax.collect {
                binding.fftPowerBar.value = it
            }
        }
    }
}