package com.tools.rftool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.tools.rftool.adapter.RecordingsRecyclerAdapter
import com.tools.rftool.databinding.FragmentRecordingListBinding
import com.tools.rftool.model.Recording
import com.tools.rftool.viewmodel.RecordingsViewModel

class RecordingListFragment private constructor(): Fragment(), RecordingsRecyclerAdapter.Listener {

    companion object {
        fun newInstance(): RecordingListFragment {
            return RecordingListFragment()
        }
    }

    private lateinit var binding: FragmentRecordingListBinding
    private lateinit var recordingsRecyclerAdapter: RecordingsRecyclerAdapter
    private val recordingsViewModel by activityViewModels<RecordingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordingListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recordingsRecyclerAdapter = RecordingsRecyclerAdapter(this)
        binding.rvRecordings.adapter = recordingsRecyclerAdapter
        binding.rvRecordings.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        recordingsRecyclerAdapter.addAll(recordingsViewModel.getRecordings())
    }

    override fun onEntryClick(entry: Recording) {
        recordingsViewModel.openRecordingDetails(entry)
    }
}
