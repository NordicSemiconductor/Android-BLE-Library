# Android BLE Library

[ ![Download](https://api.bintray.com/packages/nordic/android/ble-library/images/download.svg) ](https://bintray.com/nordic/android/ble-library/_latestVersion)

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

### Version 1.0

The library is finally on jcenter. See below how to include it in your project.

#### Changes:

Some things has changed before the lib graduated to 1.0. Those are:
1. Request class has been extracted to a standalone class, importing this class may be required.
2. Current MTU is now available in the BleManager. Use getMtu() to get the value. Request change using requestMtu(int) as before.

### Version 2.0-alpha

Please, check version 2.0 (alpha) on *develop* branch. Version 2 adds more features (for example synchronous requests, and more), is "almost" backwards compatible (a lot of API has been deprecated).
This version is under constant development and API may change any time without a notice.

#### Changes and migration guide:

1. BLE opearation methods (i.e. writeCharacteristic(...), etc.) return the Request class now, instead of boolean.
2. onLinklossOccur callback has been renamed to onLinkLossOccurred.
3. GATT callbacks (for example: onCharacteristicRead, onCharacteristicNotified, etc) inside BleManagerGattCallback has been deprecated. Use Request callbacks instead.
4. Build-in Battery Level support has been deprecated. Request Battery Level as any other value.
5. A new callbacks method: onBondingFailed has been added to BleManagerCallbacks.

#### To be added:

1. Requesting PHY (for Android Oreo devices that support it).
2. More.

#### How to test it:

The new version is compatible with [nRF Toolbox/develop](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/develop) 
and [BLE Common Library/develop](https://github.com/NordicSemiconductor/Android-BLE-Common-Library/tree/develop). The latter is a set of useful parsers and callbacks for common Bluetooth SIG adopted profiles.

Clone all 3 projects, check out *develop* branches on each of them, ensure the path to :ble and :ble-common modules are correct in *settings.gradle* file, and sync the project.

### How to include it in your project

#### Maven or jcenter

The library may be found on jcenter and Maven Central repository. Add it to your project by adding the following dependency:

```grovy
compile 'no.nordicsemi.android:ble:1.0.0'
```

#### Manual

Clone this project and add *ble* module as a dependency to your project:

1. In *settings.gradle* file add the following lines:
```groovy
include ':ble'
project(':ble').projectDir = file('../Android-BLE-Library/ble')
```
2. In *app/build.gradle* file add `implementation project(':ble')` inside dependencies.
3. Sync project and build it.

See example projects listed below.

### How to use it

1. Define your device API by extending BleManagerCallbacks: [example](https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/profile/BlinkyManagerCallbacks.java)
2. Extend BleManager class and implement required methods: [example](https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/profile/BlinkyManager.java)

### Example

Find the simple example here [Android nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky).

For an example how to use it from an Activity or a Service, check the base Activity and Service classes in [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/master/app/src/main/java/no/nordicsemi/android/nrftoolbox/profile).