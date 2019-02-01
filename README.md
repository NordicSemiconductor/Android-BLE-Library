# Android BLE Library

[ ![Download](https://api.bintray.com/packages/nordic/android/ble-library/images/download.svg) ](https://bintray.com/nordic/android/ble-library/_latestVersion)

An Android library that solves a lot of Android's Bluetooth Low Energy problems. 
The [BleManager](https://github.com/NordicSemiconductor/Android-BLE-Library/blob/master/ble/src/main/java/no/nordicsemi/android/ble/BleManager.java)
class exposes high level API for connecting and communicating with Bluetooth LE peripherals.
The API is clean and easy to read.

## Features

**BleManager** class provides the following features:

1. Connection, with automatic retires
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

The library **does not provide support for scanning** for Bluetooth LE devices.
For scanning, we recommend using 
[Android Scanner Compat Library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library)
which brings almost all recent features, introduced in Lollipop and later, to the older platforms. 

## Importing

#### Maven or jcenter

The library may be found on jcenter and Maven Central repository. 
Add it to your project by adding the following dependency:

```grovy
implementation 'no.nordicsemi.android:ble:2.0.5'
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

## Usage

`BleManager` may be used for a single connection 
(see [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox) -> RSC profile) 
or when multiple connections are required (see nRF Toolbox -> Proximity profile), 
from a Service (see nRF Toolbox -> RSC profile), ViewModel's repo 
(see [Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html) 
and [nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky)),
or as a singleton (not recommended, see nRF Toolbox -> HRM).

A single `BleManager` instance is responsible for connecting and communicating with a single peripheral.
Multiple manager instances are allowed. Extend `BleManager` with you manager where you define the
high level device's API.

### Changes in version 2.0:

1. BLE operation methods (i.e. `writeCharacteristic(...)`, etc.) return the `Request` class now, 
instead of boolean.
2. `onLinklossOccur` callback has been renamed to `onLinkLossOccurred`.
3. GATT callbacks (for example: `onCharacteristicRead`, `onCharacteristicNotified`, etc.) inside 
`BleManagerGattCallback` has been deprecated. Use `Request` callbacks instead.
4. Build-in Battery Level support has been deprecated. Request Battery Level as any other value.
5. A new callbacks method: `onBondingFailed` has been added to `BleManagerCallbacks`.
6. `shouldAutoConnect()` has ben deprecated, use `useAutoConnect(boolean)` in `ConnectRequest` instead.
7. Timeout is supported for *connect*, *disconnect* and *wait for notification/indication*.
Most BLE operations do not support setting timeout, as receiving the `BluetoothGattCallback` is required
in order to perform the next operation.
8. Atomic `RequestQueue` and `ReliableWriteRequest` are supported.  
9. BLE Library 2.0 uses Java 8. There's no good reason for this except to push the ecosystem to 
having this be a default. As of AGP 3.2 there is no reason not to do this
(via [butterknife](https://github.com/JakeWharton/butterknife)).

#### Migration guide:

1. Replace `initGatt(BluetoothGatt)` with `initialize()`:

Old code:
```java
@Override
protected Deque<Request> initGatt(final BluetoothGatt gatt) {
  final LinkedList<Request> requests = new LinkedList<>();
  requests.add(Request.newEnableNotificationsRequest(characteristic));
  return requests;
}
```
New code:
```java
@Override
protected void initialize() {
  setNotificationCallback(characteristic)
    .with(new DataReceivedCallback() {
      @Override
      public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        ...
      }
    });
  enableNotifications(characteristic)
    .enqueue();
}
```
See changes in [Android nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/) 
and [Android nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky/) for more examples.

Remember to call `.enqueue()` method for initialization requests!

Connect's completion callback is called after the initialization is done (without or with errors).

2. Move your callback implementation from `BleManagerGattCallback` to request callbacks.
3. To split logic from parsing, we recommend to extend `DataReceivedCallback` interface in a class 
where your parse your data, and return higher-level values. For a sample, check out nRF Toolbox 
and [Android BLE Common Library](https://github.com/NordicSemiconductor/Android-BLE-Common-Library/). 
If you are depending on a SIG adopted profile, like Heart Rate Monitor, Proximity, etc., 
feel free to include the **BLE Common Library** in your project. 
It has all the parsers implemented. If your profile isn't there, we are happy to accept PRs.
4. `connect()` and `disconnect()` methods also require calling `.enqueue()` in asynchronous use.
5. Replace the `shouldAutoConnect()` method in the manager with `connect(device).useAutConnect(true).enqueue()/await()`.

#### How to test it:

The new version is compatible with [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox) 
and [BLE Common Library](https://github.com/NordicSemiconductor/Android-BLE-Common-Library). 
The latter one is a set of useful parsers and callbacks for common Bluetooth SIG adopted profiles.

The libraries are available on jcenter, but if you need to make some changes, clone all 3 projects, 
ensure the path to *:ble* and *:ble-common* modules are correct in *settings.gradle* file, and sync the project.

## How to use it

Find the simple example here [Android nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky).

For an example how to use it from an Activity or a Service, check the base Activity and Service 
classes in [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/master/app/src/main/java/no/nordicsemi/android/nrftoolbox/profile).

1. Define your device API by extending `BleManagerCallbacks`:
[example](https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/profile/BlinkyManagerCallbacks.java)
2. Extend `BleManager` class and implement required methods:
[example](https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/profile/BlinkyManager.java)

## Version 1.x

The BLE library v 1.x is no longer supported. Please migrate to 2.0 for bug fixing releases.
Find it on [version/1x branch](https://github.com/NordicSemiconductor/Android-BLE-Library/tree/version/1x).