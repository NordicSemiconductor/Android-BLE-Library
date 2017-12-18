# Android BLE Library

This library has been extracted from [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox)
project. It contains classes useful when doing a connection to a Bluetooth LE device.

### Features

**BleManager** class provides the following features:

1. Connection
2. Service discovery
3. Bonding (optional)
4. Enabling Service Changed indications
5. Device initialization
6. Async BLE operations using queue
7. Reading Battery Level value
8. Requesting MTU and connection priority
9. Error handling
10. Logging (in LogCat and optionally nRF Logger)

It may be used for a single connection (see [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox) -> RSC profile) 
or when multiple connections are required (see nRF Toolbox -> Proximity profile), 
from a Service (see nRF Toolbox -> RSC profile), 
ViewModel's repo (see [Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html) and [nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky)),
or as a singleton (not recommended, see nRF Toolbox -> HRM).

**BleManager** exposes a high level API instead of low level BLE operations. 
If autoConnect option is to be used, the manager also handles initial connection (with autoConnect = false),
which is much faster, and then reconnects in case of link loss with this parameter set to true.
Just return *true* from `shouldAutoConnect()` method in your manager.

### How to include it in your project

Clone this project and add *ble* module as a dependency to your project:

1. In *settings.gradle* file add the following lines:
```groovy
include ':ble'
project(':ble').projectDir = file('../Android-BLE-Library/ble')
```
2. In *app/build.gradle* file add `implementation project(':ble')` inside dependencies.
3. Sync project and build it

See example projects listed below.

### How to use it

1. Define your device API by extending BleManagerCallbacks: [example](https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/profile/BlinkyManagerCallbacks.java)
2. Extend BleManager class and implement required methods: [example](https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/profile/BlinkyManager.java)

### Example

Find the simple example here [Android nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky).

For an example how to use it from an Activity or a Service, check the base Activity and Service classes in [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/master/app/src/main/java/no/nordicsemi/android/nrftoolbox/profile).