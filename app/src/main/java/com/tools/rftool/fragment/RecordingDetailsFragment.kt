package com.tools.rftool.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ViewPortHandler
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tools.rftool.R
import com.tools.rftool.databinding.FragmentRecordingDetailsBinding
import com.tools.rftool.model.Recording
import com.tools.rftool.repository.AppConfigurationRepository
import com.tools.rftool.ui.chart.FastLineRenderer
import com.tools.rftool.util.radio.SignalDecoder
import com.tools.rftool.util.text.DisplayUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.*
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@AndroidEntryPoint
class RecordingDetailsFragment private constructor() : BottomSheetDialogFragment() {

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
        private const val SLICE_SIZE = 172000
    }

    private lateinit var binding: FragmentRecordingDetailsBinding
    private lateinit var signalDecoder: SignalDecoder

    @Inject
    lateinit var appConfiguration: AppConfigurationRepository

    lateinit var displayedEntry: Recording
    private var totalSlices: Int = 0
    private var currentSliceIndex = 0

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
            displayedEntry = entry
            binding.tvFileName.text = entry.fileName
            binding.tvTime.text = DATE_FORMATTER_DISPLAY.format(entry.date)
            binding.tvSampleRate.text = DisplayUtils.formatFrequencyHumanReadable(entry.sampleRate)
            binding.tvCenterFrequency.text =
                DisplayUtils.formatFrequencyHumanReadable(entry.centerFrequency)
            binding.tvSampleLength.text =
                DisplayUtils.formatDurationHumanReadable(entry.size.toFloat() / (2 * entry.sampleRate))
            totalSlices = ceil(displayedEntry.size.toFloat() / (2 * SLICE_SIZE)).toInt()
            createChart()
        }

        binding.btnPreviousSlice.setOnClickListener(previousSliceClick)
        binding.btnNextSlice.setOnClickListener(nextSliceClick)
    }

    private fun readFile(file: File, sliceIndex: Int): List<Entry> {
        val data = ArrayList<Entry>()

        val offset: Long = sliceIndex.toLong() * SLICE_SIZE
        val dataSize = min((file.length() - offset).toInt(), SLICE_SIZE)
        val bytes = ByteArray(dataSize)
        FileInputStream(file).use { stream ->
            stream.skip(offset)
            stream.read(bytes)
        }
        val result = signalDecoder.decode(bytes)

        for (i in result.indices) {
            data.add(Entry(i.toFloat(), result[i].toFloat()))
        }

        return data
    }

    private fun realPart(data: ByteArray, index: Int) = (data[index].toInt() and 0xFF) - 127.5f

    private fun magnitude(data: ByteArray, index: Int) = sqrt(
        ((data[index].toInt() and 0xFF) - 127.5f).pow(2) +
                ((data[index + 1].toInt() and 0xFF) - 127.5f).pow(2)
    )

    private fun createChart() {
        signalDecoder =
            SignalDecoder(displayedEntry.sampleRate, appConfiguration.autoRecThreshold.toDouble())
        val file = File("${requireContext().filesDir}/recordings", displayedEntry.fileName)
        val data = readFile(file, currentSliceIndex)

        /*binding.chSignal.renderer = FastLineRenderer(
            binding.chSignal,
            binding.chSignal.animator,
            binding.chSignal.viewPortHandler
        )*/
        val chartData = LineData(LineDataSet(data, "Signal").apply {
            setDrawCircles(false)
        })

        binding.chSignal.data = chartData
        updateSliceButtons()
    }

    private fun updateChart() {
        signalDecoder =
            SignalDecoder(displayedEntry.sampleRate, appConfiguration.autoRecThreshold.toDouble())
        val file = File("${requireContext().filesDir}/recordings", displayedEntry.fileName)
        val data = readFile(file, currentSliceIndex)
        binding.chSignal.data.dataSets.clear()
        binding.chSignal.data.dataSets.add(LineDataSet(data, "Signal").apply {
            setDrawCircles(false)
        })
        binding.chSignal.invalidate()
        updateSliceButtons()
    }

    private val previousSliceClick = { _: View ->
        currentSliceIndex = (currentSliceIndex - 1).coerceAtLeast(0)
        updateChart()
    }

    private val nextSliceClick = { _: View ->
        currentSliceIndex = (currentSliceIndex + 1).coerceAtMost(totalSlices - 1)
        updateChart()
    }

    private fun updateSliceButtons() {
        binding.btnPreviousSlice.isEnabled = currentSliceIndex > 0
        binding.btnNextSlice.isEnabled = currentSliceIndex < totalSlices - 1
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_recording_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.share -> {
                if(displayedEntry != null) {
                    val file =
                        File("${requireContext().filesDir}/recordings", displayedEntry.fileName)
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
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}