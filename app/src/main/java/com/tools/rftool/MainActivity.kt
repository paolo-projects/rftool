package com.tools.rftool

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.tools.rftool.adapter.MainActivityPagerAdapter
import com.tools.rftool.databinding.ActivityMainBinding
import com.tools.rftool.permissions.UsbPermissionsHelper
import com.tools.rftool.repository.AppConfigurationRepository
import com.tools.rftool.rtlsdr.Recorder
import com.tools.rftool.service.FileSystemWatcherService
import com.tools.rftool.ui.DepthPageTransformer
import com.tools.rftool.ui.animation.ButtonAnimation
import com.tools.rftool.util.validator.*
import com.tools.rftool.viewmodel.SdrDeviceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.NumberFormatException
import kotlin.math.roundToInt
import kotlin.math.sqrt

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(), UsbPermissionsHelper.PermissionResultListener {
    @Inject
    lateinit var appConfiguration: AppConfigurationRepository

    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentStateAdapter: FragmentStateAdapter

    private val sdrDeviceViewModel by viewModels<SdrDeviceViewModel>()
    private lateinit var usbPermissionsHelper: UsbPermissionsHelper

    private lateinit var autoRecApplyAnimation: ButtonAnimation
    private lateinit var rfApplyAnimation: ButtonAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        fragmentStateAdapter = MainActivityPagerAdapter(this, supportFragmentManager)
        binding.viewPager.adapter = fragmentStateAdapter
        binding.viewPager.isUserInputEnabled = false
        //binding.viewPager.setPageTransformer(DepthPageTransformer())

        binding.bottomNavigation.setOnItemSelectedListener(navigationItemListener)
        binding.viewPager.registerOnPageChangeCallback(onPageChangeCallback)

        usbPermissionsHelper = UsbPermissionsHelper(this, this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)


        val sampleRateValidator = SampleRateInputValidator(this, appConfiguration.sampleRate.value)
        val frequencyValidator =
            FrequencyInputValidator(this, appConfiguration.centerFrequency.value)
        val gainValidator =
            GreaterThanIntegerValidator(0, appConfiguration.gain.value)
        val ppmErrorValidator =
            IntegerValidator(
                appConfiguration.ppmError.value
            )

        lifecycleScope.launch {
            async {
                appConfiguration.sampleRate.collect {
                    binding.navigationViewLayout.tfSampleRate.editText?.setText(it.toString())
                    sampleRateValidator.defaultValue = it
                }
            }
            async {
                appConfiguration.centerFrequency.collect {
                    binding.navigationViewLayout.tfCenterFrequency.editText?.setText(it.toString())
                    frequencyValidator.defaultValue = it
                }
            }
            async {
                appConfiguration.gain.collect {
                    binding.navigationViewLayout.tfGain.editText?.setText(it.toString())
                    gainValidator.defaultValue = it
                }
            }
            async {
                appConfiguration.ppmError.collect {
                    binding.navigationViewLayout.tfPpmError.editText?.setText(it.toString())
                    ppmErrorValidator.defaultValue = it
                }
            }
            async {
                appConfiguration.colorMap.collect {
                    val textView =
                        (binding.navigationViewLayout.tfColorMap.editText!! as MaterialAutoCompleteTextView)
                    textView.setText(
                        textView.adapter.getItem(it) as String, false
                    )
                }
            }
            async {
                appConfiguration.autoRecEnabled.collect {
                    binding.navigationViewLayout.swAutoRec.isChecked = it
                }
            }
            async {
                appConfiguration.autoRecThreshold.collect {
                    binding.navigationViewLayout.tfAutoRecTreshold.editText?.setText(
                        "%.2f".format(it)
                    )
                }
            }
            async {
                appConfiguration.autoRecTimeMs.collect {
                    binding.navigationViewLayout.tfAutoRecTime.editText?.setText(it.toString())
                }
            }
        }

        binding.navigationViewLayout.tfSampleRate.editText!!.setFocusLostValidator(
            sampleRateValidator
        )
        binding.navigationViewLayout.tfCenterFrequency.editText!!.setFocusLostValidator(
            frequencyValidator
        )
        binding.navigationViewLayout.tfGain.editText!!.setFocusLostValidator(
            gainValidator
        )
        binding.navigationViewLayout.tfPpmError.editText!!.setFocusLostValidator(
            ppmErrorValidator
        )

        binding.navigationViewLayout.btnRfApply.setOnClickListener(onSaveNewConfiguration)
        binding.navigationViewLayout.btnAutoRecApply.setOnClickListener(onAutoRecApply)

        autoRecApplyAnimation = ButtonAnimation(binding.navigationViewLayout.btnAutoRecApply)
        rfApplyAnimation = ButtonAnimation(binding.navigationViewLayout.btnRfApply)

        lifecycleScope.launch {
            async {
                sdrDeviceViewModel.deviceConnected.collect {
                    invalidateOptionsMenu()
                }
            }
            async {
                sdrDeviceViewModel.devicePermissionsStatus.collect {
                    invalidateOptionsMenu()
                }
            }
            async {
                sdrDeviceViewModel.recordingEvents.collect {
                    when (it) {
                        Recorder.RecordingEvent.ONGOING -> binding.recordingProgressBar.visibility =
                            View.VISIBLE
                        Recorder.RecordingEvent.FINISHED -> binding.recordingProgressBar.visibility =
                            View.INVISIBLE
                    }
                }
            }
        }

        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

        if (device == null) {
            attemptConnectDevice()
        } else {
            onPermissionGranted(device)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, FileSystemWatcherService::class.java).also {
            bindService(it, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    private val onAutoRecApply = { _: View ->
        appConfiguration.setAutoRecEnabled(binding.navigationViewLayout.swAutoRec.isChecked)

        try {
            var numValue =
                binding.navigationViewLayout.tfAutoRecTreshold.editText!!.text.toString()
                    .replace(",", ".").toFloat()
            if (numValue < 0) {
                numValue = 50f
            }
            binding.navigationViewLayout.tfAutoRecTreshold.editText!!.setText("%.2f".format(numValue))
            appConfiguration.setAutoRecThreshold(numValue)
        } catch (exc: NumberFormatException) {
            binding.navigationViewLayout.tfAutoRecTreshold.editText!!.setText(
                "%.2f".format(
                    appConfiguration.autoRecThreshold
                )
            )
        }

        val minTimeValue = 50
        try {
            var numValue =
                binding.navigationViewLayout.tfAutoRecTime.editText!!.text.toString().toInt()
            if (numValue < minTimeValue) {
                numValue = minTimeValue
            }
            binding.navigationViewLayout.tfAutoRecTime.editText!!.setText(numValue.toString())
            appConfiguration.setAutoRecTimeMs(numValue)
        } catch (exc: NumberFormatException) {
            binding.navigationViewLayout.tfAutoRecTime.editText!!.setText(appConfiguration.autoRecTimeMs.toString())
        }

        autoRecApplyAnimation.animateSuccess()
    }

    private val onSaveNewConfiguration = { v: View ->
        v.requestFocus()
        Handler(Looper.getMainLooper()).post {
            try {
                val sampleRate =
                    Integer.parseInt(binding.navigationViewLayout.tfSampleRate.editText!!.text.toString())
                val centerFrequency =
                    Integer.parseInt(binding.navigationViewLayout.tfCenterFrequency.editText!!.text.toString())
                val gain =
                    Integer.parseInt(binding.navigationViewLayout.tfGain.editText!!.text.toString())
                val ppmError =
                    Integer.parseInt(binding.navigationViewLayout.tfPpmError.editText!!.text.toString())

                val colorMapTextView =
                    binding.navigationViewLayout.tfColorMap.editText!! as MaterialAutoCompleteTextView
                var colorMap = 0
                for (i in 0 until colorMapTextView.adapter.count) {
                    if (colorMapTextView.text.toString() == colorMapTextView.adapter.getItem(i)
                            .toString()
                    ) {
                        colorMap = i
                        break
                    }
                }

                appConfiguration.setSampleRate(sampleRate)
                appConfiguration.setCenterFrequency(centerFrequency)
                appConfiguration.setGain(gain)
                appConfiguration.setPpmError(ppmError)
                appConfiguration.setColorMap(colorMap)

                sdrDeviceViewModel.updateParams(sampleRate, centerFrequency, gain)
                sdrDeviceViewModel.setColorMap(colorMap)

                rfApplyAnimation.animateSuccess()
            } catch (exc: NumberFormatException) {
                Toast.makeText(this, R.string.toast_invalid_parameters, Toast.LENGTH_SHORT).show()
                rfApplyAnimation.animateError()
            }
        }
        Unit
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            binding.bottomNavigation.selectedItemId = when (position) {
                0 -> R.id.bottom_navigation_fft
                1 -> R.id.bottom_navigation_signal_decoder
                2 -> R.id.bottom_navigation_recordings
                else -> 0
            }
        }
    }

    private val navigationItemListener = { item: MenuItem ->
        when (item.itemId) {
            R.id.bottom_navigation_fft -> {
                binding.viewPager.currentItem = 0
                true
            }
            R.id.bottom_navigation_signal_decoder -> {
                binding.viewPager.currentItem = 1
                true
            }
            R.id.bottom_navigation_recordings -> {
                binding.viewPager.currentItem = 2
                true
            }
            else -> false
        }
    }

    override fun onPermissionGranted(device: UsbDevice) {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val connection = usbManager.openDevice(device)
        sdrDeviceViewModel.permissionsGranted()

        sdrDeviceViewModel.initDevice(
            device,
            connection,
            appConfiguration.sampleRate.value,
            appConfiguration.centerFrequency.value,
            appConfiguration.gain.value,
            appConfiguration.ppmError.value,
            appConfiguration.colorMap.value
        )
        invalidateOptionsMenu()
    }

    override fun onPermissionRejected() {
        sdrDeviceViewModel.permissionsRejected()
        Toast.makeText(this, R.string.toast_usb_permission_denied, Toast.LENGTH_SHORT).show()
        invalidateOptionsMenu()
    }

    override fun onDestroy() {
        super.onDestroy()
        sdrDeviceViewModel.closeDevice()
    }

    private fun attemptConnectDevice() {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val device = usbManager.deviceList.values.firstOrNull {
            it.vendorId == resources.getInteger(R.integer.rtl_sdr_vid) && it.productId == resources.getInteger(
                R.integer.rtl_sdr_pid
            )
        }

        if (device != null) {
            usbPermissionsHelper.request(device)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.usb_connect -> {
                attemptConnectDevice()
                true
            }
            R.id.usb_start_stop -> {
                if (sdrDeviceViewModel.deviceConnected.value) {
                    sdrDeviceViewModel.stopReading()
                } else {
                    sdrDeviceViewModel.startReading()
                }
                true
            }
            android.R.id.home -> {
                if (binding.drawerLayout.isOpen) {
                    binding.drawerLayout.close()
                } else {
                    binding.drawerLayout.open()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.usb_connect).apply {
            isEnabled =
                !sdrDeviceViewModel.devicePermissionsStatus.value
            icon.alpha = if (!sdrDeviceViewModel.devicePermissionsStatus.value) 255 else 130
        }
        menu.findItem(R.id.usb_start_stop).apply {
            icon =
                getDrawable(if (sdrDeviceViewModel.deviceConnected.value) R.drawable.ic_baseline_stop_24 else R.drawable.ic_baseline_play_arrow_24)
            title =
                if (sdrDeviceViewModel.deviceConnected.value) getString(R.string.menu_start) else getString(
                    R.string.menu_stop
                )

            isEnabled = sdrDeviceViewModel.devicePermissionsStatus.value
            icon.alpha = if (sdrDeviceViewModel.devicePermissionsStatus.value) 255 else 130
        }
        return super.onPrepareOptionsMenu(menu)
    }
}
