package com.tools.rftool.service

import android.app.Service
import android.content.Intent
import android.os.FileObserver
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class FileSystemWatcherService: Service() {

    companion object {
        const val DIR_CHANGE_INTENT = "com.tools.rftool.recordings_change"
    }

    private var observer: Observer? = null

    override fun onBind(intent: Intent): IBinder? {
        observer = Observer(File(filesDir, "recordings"))
        observer?.startWatching()
        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        observer?.stopWatching()
        return super.onUnbind(intent)
    }

    private fun onDirectoryEntriesChange() {
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(DIR_CHANGE_INTENT))
    }

    inner class Observer(file: File): FileObserver(file) {
        override fun onEvent(event: Int, path: String?) {
            if(event == CREATE || event == DELETE) {
                onDirectoryEntriesChange()
            }
        }
    }
}