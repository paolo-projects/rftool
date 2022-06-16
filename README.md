# RFTool 4 Android

An Android app for RF frequencies analysis and recording, designed to be interfaced with an USB
RTL SDR device.

## App Structure

This app consists of the Kotlin/Java module which takes care of the "display" part and user interaction,
and the native library taking care of communicating with the RtlSdr USB device, doing the math (FFT),
and updating the graphics such as the spectrogram.

The native library relies on librtlsdr for the USB device setup and communication, which in turn relies
on libusb. The librtlsdr included with this project is a slightly modified version. The modifications
have been necessary to allow libusb communication inside the Android framework, with the mandatory 
permissions in place. Specifically, the modification addresses the `rtlsdr_open` method, that
originally enumerated all the RtlSdr usb devices (which is forbidden by the Android security model) 
and then selected the one at the given index.

The modified method takes an USB file descriptor where the necessary permissions have already been 
granted and the connection secured.

Additionally, the native code uses the KissFFT library to perform the Fourier transform on the received
data, sets up a specific thread for the FFT and spectrogram update. The FFT is also used to detect
a signal above a certain threshold, so to start recording the signal when set up in auto-record mode.

## License

This software is licensed under GNU GPLv3. Read the LICENSE file for additional info.