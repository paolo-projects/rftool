package com.tools.rftool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tools.rftool.adapter.RecordingsRecyclerAdapter
import com.tools.rftool.databinding.FragmentRecordingsBinding
import com.tools.rftool.model.Recording
import com.tools.rftool.viewmodel.RecordingsViewModel
import kotlinx.coroutines.launch

class RecordingsFragment: Fragment(), RecordingsViewModel.RecordingsListener {

    private lateinit var binding: FragmentRecordingsBinding

    private val recordingsViewModel by activityViewModels<RecordingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.beginTransaction()
            .replace(binding.containerView.id, RecordingListFragment.newInstance(), null)
            .commit()

        lifecycleScope.launch {
            recordingsViewModel.setRecordingsListener(this@RecordingsFragment)
        }
    }

    override fun onOpenDetails(recording: Recording) {
        parentFragmentManager.beginTransaction()
            .replace(binding.containerView.id, RecordingDetailsFragment.newInstance(recording), null)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recordingsViewModel.clearRecordingsListener()
    }
}