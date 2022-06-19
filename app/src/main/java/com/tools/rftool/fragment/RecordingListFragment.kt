package com.tools.rftool.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tools.rftool.R
import com.tools.rftool.adapter.RecordingsRecyclerAdapter
import com.tools.rftool.adapter.selection.AdapterItemDetailsLookup
import com.tools.rftool.adapter.selection.AdapterItemKeyProvider
import com.tools.rftool.databinding.FragmentRecordingListBinding
import com.tools.rftool.model.Recording
import com.tools.rftool.service.FileSystemWatcherService
import com.tools.rftool.viewmodel.RecordingsViewModel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.IllegalArgumentException

class RecordingListFragment private constructor() : Fragment(), RecordingsRecyclerAdapter.Listener {

    companion object {
        fun newInstance(): RecordingListFragment {
            return RecordingListFragment()
        }
    }

    private lateinit var binding: FragmentRecordingListBinding
    private lateinit var recordingsRecyclerAdapter: RecordingsRecyclerAdapter
    private val recordingsViewModel by activityViewModels<RecordingsViewModel>()
    private lateinit var tracker: SelectionTracker<Long>
    private var actionMode: ActionMode? = null

    private val fileSystemWatcherBroadcastReceiver = FileSystemWatchBroadcastReceiver()

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

        recordingsRecyclerAdapter = RecordingsRecyclerAdapter(requireContext(), this)
        binding.rvRecordings.adapter = recordingsRecyclerAdapter
        binding.rvRecordings.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        //binding.rvRecordings.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

        recordingsRecyclerAdapter.addAll(recordingsViewModel.getRecordings())

        tracker = SelectionTracker.Builder(
            "recordingSelectionItem",
            binding.rvRecordings,
            AdapterItemKeyProvider(recordingsRecyclerAdapter),
            AdapterItemDetailsLookup<RecordingsRecyclerAdapter.ViewHolder>(binding.rvRecordings),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

        tracker.addObserver(selectionObserver)

        recordingsRecyclerAdapter.setSelectionTracker(tracker)

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            fileSystemWatcherBroadcastReceiver,
            IntentFilter(FileSystemWatcherService.DIR_CHANGE_INTENT)
        )
    }

    private val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionChanged() {
            super.onSelectionChanged()

            if (actionMode == null) {
                actionMode = requireActivity().startActionMode(recordingsActionCallback)
            }

            val selectionSize = tracker.selection.size()
            if (selectionSize > 0) {
                actionMode?.title = "%d selected recordings".format(selectionSize)
                actionMode?.invalidate()
            } else {
                actionMode?.finish()
            }
        }
    }

    override fun onEntryClick(entry: Recording) {
        //RecordingDetailsFragment.newInstance(entry).show(parentFragmentManager, null)
        recordingsViewModel.openRecordingDetails(entry)
    }

    inner class FileSystemWatchBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            recordingsRecyclerAdapter.clear()
            recordingsRecyclerAdapter.addAll(recordingsViewModel.getRecordings())
        }
    }

    private val recordingsActionCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.fragment_recording_list_actionmode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.findItem(R.id.recordings_share).isVisible = tracker.selection.size() == 1
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.recordings_share -> {
                    tracker.selection.firstOrNull()?.also {
                        val entry = recordingsRecyclerAdapter.get(it)
                        if(entry != null) {
                            val file =
                                File("${requireContext().filesDir}/recordings", entry.fileName)
                            try {
                                val fileUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    "com.tools.rftool.fileprovider",
                                    file
                                )
                                if(fileUri != null) {
                                    requireContext().startActivity(Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_STREAM, fileUri)
                                        type = "application/octet-stream"
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    })
                                }
                            } catch (e: IllegalArgumentException) {
                                Toast.makeText(requireContext(), "Can't share the file to external apps", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    true
                }
                R.id.recordings_delete_selection -> {
                    askForDeleteConfirmation(tracker.selection.size()) {
                        val idsToDelete = tracker.selection.toList().toLongArray()
                        val result = idsToDelete.fold(true) { result, id ->
                            val recording = recordingsRecyclerAdapter.get(id)
                            if (recording != null) {
                                if (recordingsViewModel.deleteRecording(recording)) {
                                    tracker.deselect(id)
                                    recordingsRecyclerAdapter.removeId(id)
                                    return@fold result
                                }
                            }
                            return@fold false
                        }
                        if (!result) {
                            Toast.makeText(
                                requireContext(),
                                "Some entries could not be deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        actionMode?.finish()
                    }
                    true
                }
                R.id.recordings_delete_all -> {
                    askForDeleteConfirmation(recordingsRecyclerAdapter.itemCount) {
                        val idsToDelete = recordingsRecyclerAdapter.getAll().map {
                            it.fileName.hashCode().toLong()
                        }.toLongArray()
                        val result = idsToDelete.fold(true) { result, id ->
                            val recording = recordingsRecyclerAdapter.get(id)
                            if (recording != null) {
                                if (recordingsViewModel.deleteRecording(recording)) {
                                    tracker.deselect(id)
                                    recordingsRecyclerAdapter.removeId(id)
                                    return@fold result
                                }
                            }
                            return@fold false
                        }
                        if (!result) {
                            Toast.makeText(
                                requireContext(),
                                "Some entries could not be deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        actionMode?.finish()
                    }
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
        }
    }

    fun askForDeleteConfirmation(size: Int, onDelete: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Deleting %d entries".format(size))
            .setMessage("Are you sure you want to delete the entries?")
            .setNegativeButton("No") { _, _ -> Unit }
            .setPositiveButton("Yes") { _, _ -> onDelete() }
            .show()
    }
}
