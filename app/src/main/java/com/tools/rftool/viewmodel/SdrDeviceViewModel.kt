package com.tools.rftool.viewmodel

import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import androidx.lifecycle.ViewModel
import com.tools.rftool.fft.Fft
import com.tools.rftool.rtlsdr.IQ
import com.tools.rftool.rtlsdr.RtlSdr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@HiltViewModel
class SdrDeviceViewModel @Inject constructor() : ViewModel() {

    private var rtlSdr: RtlSdr? = null

    private val _deviceData = MutableSharedFlow<DoubleArray>()
    val deviceData = _deviceData.asSharedFlow()

    private val _fftBitmap = MutableSharedFlow<Bitmap>()
    val fftBitmap = _fftBitmap.asSharedFlow()

    private val _devicePermissionsStatus = MutableStateFlow(false)
    val devicePermissionsStatus = _devicePermissionsStatus.asStateFlow()

    private val _deviceConnected = MutableStateFlow(false)
    val deviceConnected = _deviceConnected.asStateFlow()

    private var readThread: Thread? = null
    private var readThreadRunning = false
    private var blockSize = 512 * 20

    private val viewModelScope = CoroutineScope(Dispatchers.IO + Job())

    private var usbDevice: UsbDevice? = null
    private var usbDeviceConnection: UsbDeviceConnection? = null

    private var fftBitmapCollectJob: Job? = null

    private val _sdrConfigChanges = MutableSharedFlow<Unit>()
    val sdrConfigChanges = _sdrConfigChanges.asSharedFlow()

    var sampleRate: Int = 0
        private set
    var centerFrequency: Int = 0
        private set
    var gain: Int = 0
        private set

    fun permissionsGranted() {
        viewModelScope.launch {
            _devicePermissionsStatus.emit(true)
        }
    }

    fun permissionsRejected() {
        viewModelScope.launch {
            _devicePermissionsStatus.emit(false)
        }
    }

    fun initDevice(
        device: UsbDevice,
        connection: UsbDeviceConnection,
        sampleRate: Int,
        centerFrequency: Int,
        gain: Int = 40,
        ppmError: Int = 5,
        blockSize: Int = 512 * 10
    ) {
        usbDevice = device;
        usbDeviceConnection = connection
        rtlSdr = RtlSdr(connection.fileDescriptor, sampleRate, centerFrequency, ppmError)
        this.sampleRate = sampleRate
        this.centerFrequency = centerFrequency
        this.gain = gain

        this.blockSize = blockSize

        fftBitmapCollectJob = viewModelScope.launch {
            rtlSdr!!.bitmap.collect {
                _fftBitmap.emit(it)
            }
        }
    }

    fun startReading() {
        if (readThread == null && rtlSdr != null) {
            readThread = thread(true, block = readTreadRunnable)
        }
    }

    fun updateParams(sampleRate: Int, centerFrequency: Int, gain: Int) {
        viewModelScope.launch {
            _sdrConfigChanges.emit(Unit)
        }

        if (rtlSdr != null) {
            synchronized(rtlSdr!!) {
                rtlSdr?.setDeviceSampleRate(sampleRate)
                rtlSdr?.setDeviceCenterFrequency(centerFrequency)
                rtlSdr?.setDeviceGain(gain)

                this.sampleRate = sampleRate
                this.centerFrequency = centerFrequency
                this.gain = gain
            }
        }
    }

    fun setColorMap(colorMap: Int) {
        rtlSdr?.setFftColorMap(
            colorMap
        )
    }

    fun stopReading() {
        readThreadRunning = false
        readThread?.join()
        readThread = null
    }

    private val readTreadRunnable = {
        readThreadRunning = true

        viewModelScope.launch {
            _deviceConnected.emit(true)
        }

        while (readThreadRunning) {
            synchronized(rtlSdr!!) {
                val data = rtlSdr?.deviceRead(blockSize)

                if (data != null) {
                    viewModelScope.launch {
                        _deviceData.emit(data)
                    }
                } else {
                    readThreadRunning = false
                }
            }
        }

        viewModelScope.launch {
            _deviceConnected.emit(false)
        }
        Unit
    }

    fun closeDevice() {
        stopReading()
        rtlSdr?.closeDevice()
        usbDeviceConnection?.close()
        usbDeviceConnection = null
        usbDevice = null

        fftBitmapCollectJob?.cancel()
        fftBitmapCollectJob = null

        rtlSdr = null
    }

    override fun onCleared() {
        super.onCleared()

        closeDevice()
    }
}