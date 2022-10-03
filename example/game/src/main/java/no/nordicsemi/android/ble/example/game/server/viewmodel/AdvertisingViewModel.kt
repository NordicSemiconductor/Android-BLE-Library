package no.nordicsemi.android.ble.example.game.server.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.ble.example.game.server.data.AdvertisingManager
import javax.inject.Inject

@HiltViewModel
 class AdvertisingViewModel @Inject constructor(
 private val advertiser: AdvertisingManager,
// private val scannerRepository: ScannerRepository,
 ): ViewModel() {

 fun startAdvertising(){
  Log.d("Starting Advertising", "startAdvertising: ")
  advertiser.startAdvertising()
 }


}