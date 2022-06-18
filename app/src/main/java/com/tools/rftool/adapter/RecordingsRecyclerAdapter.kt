package com.tools.rftool.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tools.rftool.R
import com.tools.rftool.model.Recording
import com.tools.rftool.util.text.DisplayUtils
import java.time.format.DateTimeFormatter

class RecordingsRecyclerAdapter(private val listener: Listener) :
    RecyclerView.Adapter<RecordingsRecyclerAdapter.ViewHolder>() {

    companion object {
        private val DATE_FORMATTER_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    }

    interface Listener {
        fun onEntryClick(entry: Recording)
    }

    private val recordingsDataset = ArrayList<Recording>()

    fun add(recording: Recording) {
        val previousSize = recordingsDataset.size
        recordingsDataset.add(recording)
        notifyItemRangeInserted(previousSize, 1)
    }

    fun addAll(recordings: List<Recording>) {
        val previousSize = recordingsDataset.size
        recordingsDataset.addAll(recordings)
        notifyItemRangeInserted(previousSize, recordings.size)
    }

    fun clear() {
        val previousCount = recordingsDataset.size
        recordingsDataset.clear()
        notifyItemRangeChanged(0, previousCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val baseView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_recording, parent, false)
        return ViewHolder(baseView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = recordingsDataset[position]
        val fileName = entry.fileName.substring(entry.fileName.lastIndexOf("/") + 1)

        holder.recordingName.text = fileName
        holder.recordingSampleRate.text = "${entry.sampleRate} Hz"
        holder.recordingCenterFrequency.text = "${entry.centerFrequency} Hz"
        holder.recordingDate.text = DATE_FORMATTER_DISPLAY.format(entry.date)

        val sampleLengthS = entry.size.toFloat() / (2 * entry.sampleRate)
        holder.recordingLengthSeconds.text = DisplayUtils.formatDurationHumanReadable(sampleLengthS)

        holder.bindItemClickListener {
            listener.onEntryClick(entry)
        }
    }

    override fun getItemCount(): Int {
        return recordingsDataset.size
    }

    inner class ViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recordingName: TextView = itemView.findViewById(R.id.tv_recording_name)
        val recordingSampleRate: TextView = itemView.findViewById(R.id.tv_recording_sample_rate)
        val recordingCenterFrequency: TextView =
            itemView.findViewById(R.id.tv_recording_center_frequency)
        val recordingDate: TextView = itemView.findViewById(R.id.tv_recording_date)
        val recordingLengthSeconds: TextView = itemView.findViewById(R.id.tv_recording_length)

        fun bindItemClickListener(listener: View.OnClickListener) {
            itemView.setOnClickListener(listener)
        }
    }
}