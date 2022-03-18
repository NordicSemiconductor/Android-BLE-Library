# Android BLE Library

[ ![Download](https://maven-badges.herokuapp.com/maven-central/no.nordicsemi.android/ble/badge.svg?style=plastic) ](https://search.maven.org/artifact/no.nordicsemi.android/ble)


An Android library that solves a lot of Android's Bluetooth Low Energy problems. 
The [BleManager](https://github.com/NordicSemiconductor/Android-BLE-Library/blob/master/ble/src/main/java/no/nordicsemi/android/ble/BleManager.java)
class exposes high level API for connecting and communicating with Bluetooth LE peripherals.
The API is clean and easy to read.

## Features

**BleManager** class provides the following features:

1. Connection, with automatic retries
2. Service discovery
3. Bonding (optional) and removing bond information (using reflections)
4. Automatic handling of Service Changed indications
5. Device initialization
6. Asynchronous and synchronous BLE operations using queue
7. Splitting and merging long packets when writing and reading characteristics and descriptors
8. Requesting MTU and connection priority (on Android Lollipop or newer)
9. Reading and setting preferred PHY (on Android Oreo or newer)
10. Reading RSSI
11. Refreshing device cache (using reflections)
12. Reliable Write support
13. Operation timeouts (for *connect*, *disconnect* and *wait for notification* requests)
14. Error handling
15. Logging
16. GATT server (since version 2.2)

The library **does not provide support for scanning** for Bluetooth LE devices.
For scanning, we recommend using 
[Android Scanner Compat Library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library)
which brings almost all recent features, introduced in Lollipop and later, to the older platforms. 

### Version 2.3

New features added in version 2.3:

1. `:ble-ktx` module added with support for coroutines and Flow.
    1. `.suspend()` methods added in `Request`s.
    2. `asFlow()` method added to `ValueChangedCallback`.
    3. Connection and bonding state available as Flow.
    4. New helper methods to get a `BluetoothGattCharacteristic` with given required properties 
       and instance id added to `BluetoothGattService`.
2. `JsonMerger` class added, which should help with use cases when a device sends a JSON file in multiple
    packets.
3. `:ble-livedata` migrated to Java with some API changes, as sealed classes are no longer available.
4. Support for new `onServicesChanged()` callback, added in API 31 (Android 12).
5. Option to cancel pending Bluetooth LE connection using `ConnectRequest.cancelPendingConnection()`.

When using coroutines use `.suspend()` method in `Request`, instead of `enqueue()` or `await()`.

To register to notifications and indications (or incoming write requests for server) use 
```kotlin
setNotificationCallback(characteristic)
   .merge(JsonMerger()) // Example of how to use JsonMerger, optional
   .asFlow()
```
This is still experimental feature and any feedback is welcome.

### Version 2.2

New features added in version 2.2:

1. GATT Server support. This includes setting up the local GATT server on the Android device, new 
   requests for server operations: 
   * *wait for read*, 
   * *wait for write*, 
   * *send notification*, 
   * *send indication*,
   * *set characteristic value*,
   * *set descriptor value*.
2. New conditional requests: 
   * *wait if*,
   * *wait until*.
3. BLE operations are no longer called from the main thread.
4. There's a new option to set a handler for invoking callbacks. A handler can also be set per-callback.

### Migration to version 2.2

Version 2.2 breaks some API known from version 2.1.1.
Check out [migration guide](MIGRATION.md).

## Importing

#### Maven Central

The library may be found on Maven Central repository. 
Add it to your project by adding the following dependency:

```grovy
implementation 'no.nordicsemi.android:ble:2.4.0-beta03'
```
The last version not migrated to AndroidX is 2.0.5.

BLE library with Kotlin extension is available in:
```grovy
implementation 'no.nordicsemi.android:ble-ktx:2.4.0-beta03'
``` 

To import the BLE library with set of parsers for common Bluetooth SIG characteristics, use:
```grovy
implementation 'no.nordicsemi.android:ble-common:2.4.0-beta03'
```
For more information, read [this](BLE-COMMON.md).

An extension for easier integration with `LiveData` is available after adding:
```grovy
implementation 'no.nordicsemi.android:ble-livedata:2.4.0-beta03'
```
This extension adds `ObservableBleManager` with `state` and `bondingState` properties, which 
notify about connection and bond state using `androidx.lifecycle.LiveData`.

#### As a library module

Clone this project and add *ble* module as a dependency to your project:

1. In *settings.gradle* file add the following lines:
```groovy
include ':ble'
project(':ble').projectDir = file('../Android-BLE-Library/ble')
```
2. In *app/build.gradle* file add `implementation project(':ble')` inside dependencies.
3. Sync project and build it.

You may do the same with other modules available in this project. Keep in mind, that
*ble-livedata* module requires Kotlin, but no special changes are required in the app.

#### Setting up

The library uses Java 1.8 features. If you're using Android Studio below 4.2, make sure your 
*build.gradle* includes the following configuration:

```groovy
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    // For Kotlin projects additionally:
    kotlinOptions {
        jvmTarget = "1.8"
    }
```

## Usage

A `BleManager` instance is responsible for connecting and communicating with a single peripheral.
Multiple manager instances are allowed. Extend `BleManager` with you manager where you define the
high level device's API.

`BleManager` may be used in different ways:
1. In a Service, for a single connection - see [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox) -> RSC profile, 
2. In a Service with multiple connections - see nRF Toolbox -> Proximity profile, 
3. From ViewModel's repo - see [Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html) 
and [nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky),
4. As a singleton - not recommended, see nRF Toolbox -> HRM.

Please refer to the `examples/ble-gatt-client folder` for a project that illustrates the GATT
server provided as a foreground service. There's a simple UI with a text field to update
with the value of a characteristic that can be read and subscribed to. This characteristic also
demands encryption as an illustration of best-practice.

You can run this client on one device and a complimenting server on another (see the next section).

#### Adding GATT Server support

Starting from version 2.2 you may now define and use the GATT server in the BLE Library.

Please refer to the `examples/ble-gatt-server folder` for a project that illustrates the GATT
server provided as a foreground service. There's a simple UI with a text field to update
the value of a characteristic that can be read and subscribed to. This characteristic also
demands encryption as an illustration of best-practice.

## More examples

Find the simple example here [Android nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky).

For an example how to use it from an Activity or a Service, check the base Activity and Service 
classes in [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/master/app/src/main/java/no/nordicsemi/android/nrftoolbox/profile).

## Version 1.x

The BLE library v 1.x is no longer supported. Please migrate to 2.x for bug fixing releases.
Find it on [version/1x branch](https://github.com/NordicSemiconductor/Android-BLE-Library/tree/version/1x).

A migration guide is available [here](MIGRATION.md).