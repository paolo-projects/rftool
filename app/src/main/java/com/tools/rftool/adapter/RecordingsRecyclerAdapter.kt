package com.tools.rftool.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.tools.rftool.R
import com.tools.rftool.databinding.RecyclerviewItemRecordingBinding
import com.tools.rftool.model.Recording
import com.tools.rftool.util.text.DisplayUtils
import java.time.format.DateTimeFormatter

class RecordingsRecyclerAdapter(private val context: Context, private val listener: Listener) :
    SelectionRecyclerAdapter<RecordingsRecyclerAdapter.ViewHolder>() {

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
        val vhBinding = RecyclerviewItemRecordingBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(vhBinding.root).apply {
            binding = vhBinding
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = recordingsDataset[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int {
        return recordingsDataset.size
    }

    override fun getItemId(position: Int): Long {
        return recordingsDataset[position].fileName.hashCode().toLong()
    }

    override fun getPosition(id: Long): Int {
        return recordingsDataset.indexOfFirst {
            it.fileName.hashCode().toLong() == id
        }
    }

    fun get(id: Long): Recording? {
        return recordingsDataset.firstOrNull {
            it.fileName.hashCode().toLong() == id
        }
    }

    fun removeId(id: Long) {
        val position = recordingsDataset.indexOfFirst {
            it.fileName.hashCode().toLong() == id
        }
        if(position >= 0) {
            recordingsDataset.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getAll(): List<Recording> {
        return recordingsDataset
    }

    inner class ViewHolder(val itemView: View) : ItemDetailsViewHolder(itemView) {
        lateinit var binding: RecyclerviewItemRecordingBinding

        fun bindItemClickListener(listener: View.OnClickListener) {
        }

        override fun getItem(): ItemDetailsLookup.ItemDetails<Long>  = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey(): Long = getItemId(adapterPosition)
        }

        fun bind(entry: Recording) {
            val fileName = entry.fileName.substring(entry.fileName.lastIndexOf("/") + 1)

            binding.tvRecordingName.text = fileName
            binding.tvRecordingSampleRate.text = DisplayUtils.formatFrequencyHumanReadable(entry.sampleRate)
            binding.tvRecordingCenterFrequency.text = DisplayUtils.formatFrequencyHumanReadable(entry.centerFrequency)
            binding.tvRecordingDate.text = DATE_FORMATTER_DISPLAY.format(entry.date)

            val sampleLengthS = entry.size.toFloat() / (2 * entry.sampleRate)
            binding.tvRecordingLength.text = DisplayUtils.formatDurationHumanReadable(sampleLengthS)

            itemView.setOnClickListener {
                if(!isInActionMode) {
                    listener.onEntryClick(recordingsDataset[adapterPosition])
                }
            }

            tracker?.let {
                if(it.isSelected(getItem().selectionKey)) {
                    binding.contentLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.translucent))
                } else {
                    binding.contentLayout.background = null
                }
            }
        }
    }
}