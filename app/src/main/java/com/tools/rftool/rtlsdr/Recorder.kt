package com.tools.rftool.rtlsdr

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Recorder(private val context: Context) {
    companion object {
        init {
            System.loadLibrary("rftool")
        }
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH:mm:ss")
    }

    class RecorderException(message: String): Error(message)

    var recordingHandler: Handler? = null

    fun record(timeMs: Long, sampleRate: Int, centerFrequency: Int) {
        if(recordingHandler != null) {
            return
        }

        val dateTime = LocalDateTime.now().format(DATE_FORMATTER)
        val fileName =  "${dateTime}_sr_${sampleRate}_f_${centerFrequency}.complex16u"

        if(Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            throw RecorderException("External storage is unavailable")
        }

        val externalDocumentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        val path = "$externalDocumentsDir/$fileName"
        startRecording(path)

        recordingHandler = Handler(Looper.getMainLooper())
        recordingHandler!!.postDelayed({
            stopRecording()
            recordingHandler = null
            Toast.makeText(context, "Signal recording completed", Toast.LENGTH_SHORT).show()
        }, timeMs)
    }

    private external fun startRecording(filePath: String)

    private external fun stopRecording()
}