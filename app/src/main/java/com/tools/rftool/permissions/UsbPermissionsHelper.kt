package com.tools.rftool.permissions

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.tools.rftool.BuildConfig

class UsbPermissionsHelper(
    private val context: Context,
    private val permissionsListener: PermissionResultListener
) : BroadcastReceiver() {
    companion object {
        private const val ACTION_USB_PERMISSION = "${BuildConfig.APPLICATION_ID}.USB_PERMISSION"
    }

    private val usbPermissionsObject = Object()

    interface PermissionResultListener {
        fun onPermissionGranted(device: UsbDevice)
        fun onPermissionRejected()
    }

    fun request(device: UsbDevice) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_MUTABLE
        )
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        context.registerReceiver(this, filter)

        usbManager.requestPermission(device, permissionIntent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_USB_PERMISSION == intent.action) {
            val permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            if (permissionGranted) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                if(device != null) {
                    permissionsListener.onPermissionGranted(device)
                }
            } else {
                permissionsListener.onPermissionRejected()
            }
            context.unregisterReceiver(this)
        }
    }
}