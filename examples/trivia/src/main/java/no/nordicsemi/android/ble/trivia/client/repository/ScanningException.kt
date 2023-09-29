package no.nordicsemi.android.ble.trivia.client.repository

import android.bluetooth.le.ScanCallback

data class ScanningException(val errorCode: Int): Exception() {

    override val message: String?
        get() = when (errorCode) {
            ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "Scan not supported"
            ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "Scan registration failed"
            ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "Internal scanning error"
            ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "Scan already started"
            ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> "Out of hardware resources"
            ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> "Scanning too frequently"
            else -> super.message
        }
}