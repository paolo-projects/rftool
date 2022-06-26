package com.tools.rftool.util.usb

import android.content.Context
import android.content.res.XmlResourceParser
import android.util.Log

class UsbDevicesRetriever(context: Context, id: Int) {
    companion object {
        private const val TAG = "UsbDevicesRetriever"
    }

    data class UsbDeviceSpecs(val vid: Int, val pid: Int)

    private val devices = ArrayList<UsbDeviceSpecs>()

    init {
        val xmlResource = context.resources.getXml(id)

        var eventType: Int = -1
        var inResources = false
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            if (xmlResource.name?.lowercase() == "resources" && eventType == XmlResourceParser.START_TAG) {
                inResources = true
            } else if (xmlResource.name?.lowercase() == "resources" && eventType == XmlResourceParser.END_TAG) {
                inResources = false
            } else if (inResources) {
                if (xmlResource.name?.lowercase() == "usb-device") {
                    try {
                        val attrCount = xmlResource.attributeCount
                        var vidVal: String? = null
                        var pidVal: String? = null

                        for(i in 0 until attrCount) {
                            val attrName = xmlResource.getAttributeName(i)
                            if(attrName == "vendor-id") {
                                vidVal = xmlResource.getAttributeValue(i)
                            } else if (attrName == "product-id") {
                                pidVal = xmlResource.getAttributeValue(i)
                            }
                        }
                        val vid = Integer.parseInt(vidVal ?: "")
                        val pid =
                            Integer.parseInt(pidVal ?: "")

                        devices.add(
                            UsbDeviceSpecs(
                                vid, pid
                            )
                        )
                    } catch(exc: NumberFormatException) {
                        /* no-op */
                    }
                }
            }

            eventType = xmlResource.next()
        }
    }

    fun includes(vid: Int, pid: Int): Boolean = devices.firstOrNull { it.vid == vid && it.pid == pid } != null
}