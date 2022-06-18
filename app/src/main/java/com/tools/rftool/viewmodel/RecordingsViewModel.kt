package com.tools.rftool.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.tools.rftool.model.Recording
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FilenameFilter
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class RecordingsViewModel @Inject constructor(@ApplicationContext private val context: Context) : ViewModel() {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH:mm:ss")
        private val FILE_NAME_PATTERN =
            Regex("(\\d{2}-\\d{2}-\\d{4}-\\d{2}:\\d{2}:\\d{2})_sr_(\\d+)_f_(\\d+).complex16u")
    }

    interface RecordingsListener {
        fun onOpenDetails(recording: Recording)
    }

    private var recordingsListener: RecordingsListener? = null

    fun setRecordingsListener(listener: RecordingsListener) {
        recordingsListener = listener
    }

    fun clearRecordingsListener() {
        recordingsListener = null
    }

    fun getRecordings(): List<Recording> {
        val recordings = ArrayList<Recording>()
        val recordingsDir = File(context.filesDir, "recordings")

        if(recordingsDir.exists()) {
            val recordingFiles = recordingsDir.listFiles { _, name ->
                FILE_NAME_PATTERN.matches(
                    name
                )
            }
            if (recordingFiles != null) {
                recordings.addAll(recordingFiles.map {
                    val match = FILE_NAME_PATTERN.matchEntire(it.name)
                    val date = DATE_FORMATTER.parse(match!!.groups[1]!!.value)
                    Recording(
                        LocalDateTime.from(date), it.name, match.groups[2]!!.value.toInt(),
                        match.groups[3]!!.value.toInt(), it.length()
                    )
                })
            }
        }

        return recordings
    }

    fun openRecordingDetails(recording: Recording) {
        recordingsListener?.onOpenDetails(recording)
    }
}