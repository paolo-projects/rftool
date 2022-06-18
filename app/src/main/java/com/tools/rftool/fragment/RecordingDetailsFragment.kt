package com.tools.rftool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.tools.rftool.databinding.FragmentRecordingDetailsBinding
import com.tools.rftool.model.Recording
import com.tools.rftool.util.radio.SignalDecoder
import java.io.*
import kotlin.math.pow
import kotlin.math.sqrt

class RecordingDetailsFragment private constructor() : Fragment() {

    companion object {
        private const val RECORDING_ARG = "recording_details_recording"

        fun newInstance(recording: Recording): RecordingDetailsFragment {
            val args = Bundle()
            args.putParcelable(RECORDING_ARG, recording)

            val fragment = RecordingDetailsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: FragmentRecordingDetailsBinding
    private lateinit var signalDecoder: SignalDecoder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordingDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val entry = arguments!!.getParcelable<Recording>(RECORDING_ARG)
        if (entry != null) {
            createChart(entry)
        }
    }

    private fun readFile(file: File): List<Entry> {
        val data = ArrayList<Entry>()

        val fileBytes = file.readBytes()
        val result = signalDecoder.decode(fileBytes)

        for(i in result.indices) {
            data.add(Entry(i.toFloat(), result[i].toFloat()))
        }

        return data
    }

    private fun realPart(data: ByteArray, index: Int) = (data[index].toInt() and 0xFF) - 127.5f

    private fun magnitude(data: ByteArray, index: Int) = sqrt(
        ((data[index].toInt() and 0xFF) - 127.5f).pow(2) +
                ((data[index + 1].toInt() and 0xFF) - 127.5f).pow(2)
    )

    private fun createChart(recording: Recording) {
        signalDecoder = SignalDecoder(recording.sampleRate)
        val data = readFile(File("${requireContext().filesDir}/recordings", recording.fileName))

        val dataset = LineDataSet(data, "Signal")
        dataset.setDrawCircles(false)
        val chartData = LineData(dataset)

        binding.chSignal.data = chartData
    }
}