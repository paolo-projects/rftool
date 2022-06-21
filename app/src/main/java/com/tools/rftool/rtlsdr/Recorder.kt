package com.tools.rftool.rtlsdr

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Recorder(private val context: Context, private val listener: RecorderListener) {
    companion object {
        init {
            System.loadLibrary("rftool")
        }
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH:mm:ss")
    }

    enum class RecordingEvent {
        ONGOING, FINISHED
    }

    interface RecorderListener {
        fun onRecordingStatus(status: RecordingEvent)
    }

    class RecorderException(message: String): Error(message)

    var recordingHandler: Handler? = null

    fun record(timeMs: Int, sampleRate: Int, centerFrequency: Int) {
        if(recordingHandler != null) {
            return
        }

        val dateTime = LocalDateTime.now().format(DATE_FORMATTER)
        val fileName =  "${dateTime}_sr_${sampleRate}_f_${centerFrequency}.complex16u"

        val documentsDir = File(context.filesDir, "/recordings")
        if(!documentsDir.exists()) {
            documentsDir.mkdir()
        }

        val path = "$documentsDir/$fileName"
        startRecordingTimed(path, timeMs)
    }

    private external fun startRecordingTimed(filePath: String, durationMs: Int)
    private external fun startRecording(filePath: String)

    private external fun stopRecording()

    // Called from JNI
    private fun onRecordingStarted() {
        listener.onRecordingStatus(RecordingEvent.ONGOING)
    }

    // Called from JNI
    private fun onRecordingCompleted() {
        listener.onRecordingStatus(RecordingEvent.FINISHED)
    }
}