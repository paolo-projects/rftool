package com.tools.rftool.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tools.rftool.R
import com.tools.rftool.databinding.FragmentRecordingDetailsBinding
import com.tools.rftool.model.Recording
import com.tools.rftool.repository.AppConfigurationRepository
import com.tools.rftool.task.RecordedSignalLoadTask
import com.tools.rftool.ui.chart.SignalAnalysisAdapter
import com.tools.rftool.util.radio.SignalDecoder
import com.tools.rftool.util.text.DisplayUtils
import com.tools.rftool.viewmodel.RecordingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.*
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.*

@AndroidEntryPoint
class RecordingDetailsFragment private constructor() : BottomSheetDialogFragment(),
    RecordedSignalLoadTask.Listener {

    companion object {
        private const val RECORDING_ARG = "recording_details_recording"

        fun newInstance(recording: Recording): RecordingDetailsFragment {
            val args = Bundle()
            args.putParcelable(RECORDING_ARG, recording)

            val fragment = RecordingDetailsFragment()
            fragment.arguments = args
            return fragment
        }

        private val DATE_FORMATTER_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    }

    private lateinit var binding: FragmentRecordingDetailsBinding
    private lateinit var signalDecoder: SignalDecoder
    private val signalAnalysisAdapter = SignalAnalysisAdapter()
    private lateinit var recordedSignalLoadTask: RecordedSignalLoadTask

    @Inject
    lateinit var appConfiguration: AppConfigurationRepository

    private val recordingsViewModel by activityViewModels<RecordingsViewModel>()

    private lateinit var displayedEntry: Recording

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet: FrameLayout =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
            BottomSheetBehavior.from(bottomSheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }

        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentRecordingDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val entry = arguments!!.getParcelable<Recording>(RECORDING_ARG)
        if (entry != null) {
            recordedSignalLoadTask = RecordedSignalLoadTask(requireContext(), entry, 50.0, this)

            displayedEntry = entry
            binding.tvFileName.text = entry.fileName
            binding.tvTime.text = DATE_FORMATTER_DISPLAY.format(entry.date)
            binding.tvSampleRate.text = DisplayUtils.formatFrequencyHumanReadable(entry.sampleRate)
            binding.tvCenterFrequency.text =
                DisplayUtils.formatFrequencyHumanReadable(entry.centerFrequency)
            binding.tvSampleLength.text =
                DisplayUtils.formatDurationHumanReadable(entry.size.toFloat() / (2 * entry.sampleRate))

            createChart()
            binding.shimmerLayout.hideShimmer()
            binding.chSignal.invalidate()
        } else {
            binding.shimmerLayout.hideShimmer()
        }
    }

    private fun createChart() {
        lifecycleScope.launch(Dispatchers.Default + Job()) {
            signalDecoder =
                SignalDecoder(
                    displayedEntry.sampleRate,
                    appConfiguration.autoRecThreshold.toDouble()
                )

            val data = recordedSignalLoadTask.readFromFile()

            signalAnalysisAdapter.add(data)

            binding.chSignal.adapter = signalAnalysisAdapter

            withContext(Dispatchers.Main) {
                binding.progressBar.progress = 100
                delay(50)
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_recording_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> {
                val file =
                    File("${requireContext().filesDir}/recordings", displayedEntry.fileName)
                try {
                    val fileUri = FileProvider.getUriForFile(
                        requireContext(),
                        getString(R.string.file_provider_authority),
                        file
                    )
                    if (fileUri != null) {
                        requireContext().startActivity(Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            type = "application/octet-stream"
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        })
                    }
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(
                        requireContext(),
                        R.string.error_file_sharing,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            R.id.delete_entry -> {
                askForDeleteConfirm {
                    recordingsViewModel.deleteRecording(displayedEntry)
                    parentFragmentManager.popBackStack()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun askForDeleteConfirm(onDelete: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_recording_title)
            .setMessage(R.string.confirm_delete_recording_message)
            .setNegativeButton(R.string.confirm_no) { _, _ -> }
            .setPositiveButton(R.string.confirm_yes) { _, _ -> onDelete() }
            .show()
    }

    override fun onProgressUpdate(progress: Double) {
        requireActivity().runOnUiThread {
            binding.progressBar.progress = (progress * 100).roundToInt()
        }
    }
}