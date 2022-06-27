package com.tools.rftool.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import androidx.lifecycle.ViewModel
import com.tools.rftool.repository.AppConfigurationRepository
import com.tools.rftool.rtlsdr.Recorder
import com.tools.rftool.rtlsdr.RtlSdr
import com.tools.rftool.util.usb.UsbDevicesRetriever
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.thread

@HiltViewModel
class SdrDeviceViewModel @Inject constructor(@ApplicationContext private val context: Context) :
    ViewModel(), RtlSdr.RtlSdrListener, Recorder.RecorderListener {

    private var rtlSdr: RtlSdr? = null

    private val _deviceData = MutableSharedFlow<DoubleArray>()
    val deviceData = _deviceData.asSharedFlow()

    private val _fftBitmap = MutableSharedFlow<Bitmap>()
    val fftBitmap = _fftBitmap.asSharedFlow()

    private val _devicePermissionsStatus = MutableStateFlow(false)
    val devicePermissionsStatus = _devicePermissionsStatus.asStateFlow()

    private val _deviceConnected = MutableStateFlow(false)
    val deviceConnected = _deviceConnected.asStateFlow()

    private val _fftSignalMax = MutableSharedFlow<Double>()
    val fftSignalMax = _fftSignalMax.asSharedFlow()

    private val _recordingEvents = MutableStateFlow(Recorder.RecordingEvent.FINISHED)
    val recordingEvents = _recordingEvents.asStateFlow()

    private val _currentDeviceSpecs = MutableStateFlow<UsbDevicesRetriever.UsbDeviceSpecs?>(null)
    val currentDeviceSpecs = _currentDeviceSpecs.asStateFlow()

    @Inject
    lateinit var appConfigurationRepository: AppConfigurationRepository

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

    private val recorder = Recorder(context, this)

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
        deviceSpecs: UsbDevicesRetriever.UsbDeviceSpecs,
        connection: UsbDeviceConnection,
        sampleRate: Int,
        centerFrequency: Int,
        gain: Int = 40,
        ppmError: Int = 5,
        colorMap: Int = 0,
        blockSize: Int = 512 * 10
    ) {
        usbDevice = device
        usbDeviceConnection = connection
        rtlSdr =
            RtlSdr(
                connection.fileDescriptor,
                this,
                sampleRate,
                centerFrequency,
                ppmError,
                gain,
                colorMap
            )
        this.sampleRate = sampleRate
        this.centerFrequency = centerFrequency
        this.gain = gain

        this.blockSize = blockSize

        fftBitmapCollectJob = viewModelScope.launch {
            rtlSdr!!.bitmap.collect {
                _fftBitmap.emit(it)
            }
        }
        viewModelScope.launch {
            _currentDeviceSpecs.emit(deviceSpecs)
        }
    }

    fun startReading() {
        if (rtlSdr != null) {
            rtlSdr?.startDeviceDataCollection(blockSize)

            viewModelScope.launch {
                _deviceConnected.emit(true)
            }
        }
    }

    fun updateParams(sampleRate: Int, centerFrequency: Int, gain: Int) {
        viewModelScope.launch {
            _sdrConfigChanges.emit(Unit)
        }

        if (rtlSdr != null) {
            rtlSdr?.setDeviceSampleRate(sampleRate)
            rtlSdr?.setDeviceCenterFrequency(centerFrequency)
            rtlSdr?.setDeviceGain(gain)

            this.sampleRate = sampleRate
            this.centerFrequency = centerFrequency
            this.gain = gain
        }
    }

    fun setColorMap(colorMap: Int) {
        rtlSdr?.setFftColorMap(
            colorMap
        )
    }

    fun stopReading() {
        rtlSdr?.stopDeviceDataCollection()

        viewModelScope.launch {
            _deviceConnected.emit(false)
        }
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

    override fun onFftMax(fftMax: Double) {
        if (appConfigurationRepository.autoRecEnabled.value && fftMax > appConfigurationRepository.autoRecThreshold.value) {
            recorder.record(
                appConfigurationRepository.autoRecTimeMs.value,
                sampleRate,
                centerFrequency
            )
        }
        viewModelScope.launch {
            _fftSignalMax.emit(fftMax)
        }
    }

    override fun onRecordingStatus(status: Recorder.RecordingEvent) {
        viewModelScope.launch {
            _recordingEvents.emit(status)
        }
    }
}