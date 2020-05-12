# Android BLE Common Library

[ ![Download](https://api.bintray.com/packages/nordic/android/no.nordicsemi.android%3Able-common/images/download.svg) ](https://bintray.com/nordic/android/no.nordicsemi.android%3Able-common/_latestVersion)

This library is an addon to the BLE Library for Android, 
which provides data parsers and other useful features for some Bluetooth SIG Adopted profiles.

It's compatible with the Android BLE Library starting from version 2.0 and 
used as an example by [Android nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox).

## Services

Currently the following service have been implemented:

- Battery Service
- Date Time
- DST Offset
- Time Zone
- Blood Pressure
- Glucose (with Record Access Control Point)
- Continuous Glucose (with Record Access Control Point)
- Cycling Speed and Cadence
- Heart Rate
- Health Thermometer
- Running Speed and Cadence

We are happy to accept PRs with other parsers.

## Usage

The BLE Library v2 allows to set a callback for BLE operations using `.with(DataReceivedCallback)` method.
It is common, that you application needs to parse the received data. 

Let's have a look at HRM profile. 
The [Heart Rate Measurement](https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.heart_rate_measurement.xml) 
characteristic specifies the structure of data sent by a remote sensor.

Using just the BLE Library, one would have to write code that looks something like this:
```java
class HrmBleManager extends BleManager {
    // [...]

	@NonNull
	@Override
	protected BleManagerGattCallback getGattCallback() {
		return new HrmBleManagerGattCallbacks();
	}
    
    private class HrmBleManagerGattCallbacks extends BleManagerGattCallbacks {

        @Override
        protected void initialize() {
          // ...
          setNotificationCallback(heartRateCharacteristic)
            .with(new DataReceivedCallback() {
              @Override
              void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
                // Read flags
                final int flags = data.getIntValue(Data.FORMAT_UINT8, 0);
                final int hearRateType = (flags & 0x01) == 0 ? Data.FORMAT_UINT8 : Data.FORMAT_UINT16;
                final int sensorContactStatus = (flags & 0x06) >> 1;
        
                // Parsing and validation skipped
                // [...]
        
                // Show received data to the user...
              }
            });
          enableNotifications(heartRateCharacteristic).enqueue();
        }
        
        // [...]

    }
}
```

Parsing the data would have to be implemented and tested in the app.

To make working with standard profiles easier, this library allows to replace the code above with:

```java
class HrmBleManager extends BleManager {
    // [...]

	@NonNull
	@Override
	protected BleManagerGattCallback getGattCallback() {
		return new HrmBleManagerGattCallbacks();
	}
    
    private class HrmBleManagerGattCallbacks extends BleManagerGattCallbacks {
	    
        @Override
        protected void initialize() {
          // ...
          setNotificationCallback(heartRateCharacteristic)
            .with(new HeartRateMeasurementDataCallback() {
              @Override
              public void onHeartRateMeasurementReceived(@NonNull final BluetoothDevice device,
                                                         @IntRange(from = 0) final int heartRate,
                                                         @Nullable final Boolean contactDetected,
                                                         @Nullable final Integer energyExpanded,
                                                         @Nullable final List<Integer> rrIntervals) {
                // Show received data to the user...
              }
        
              @Override
              public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
                // [...]	
              }
            });
          enableNotifications(heartRateCharacteristic).enqueue();
        }
        
        // [...]

    }
}
```

Such code looks simpler, is easier to read, relies on a tested library and marks fields that 
may not be present in the packet with *@Nullable* annotation.

## Testing

All data parsers have unit tests and should work properly. The classes are used by nRF Toolbox 
and were tested against sample apps from Nordic SDK 15.2 on nRF51 and nRF52 DKs.

## Note

In CGMS profile the E2E CRC bytes may be inverted. Unfortunately, I couldn't find any working 
device or test data in order to verify the implementation. The sample from the SDK does not 
support E2E CRC.

## Contribution

Feel free to add more services. Please, follow our coding style. 
The library is and will be based on BSD-3 License. Any feedback is welcome.

Use [Issues](https://github.com/NordicSemiconductor/Android-BLE-Library/issues) to report 
bugs or submit suggestions.
