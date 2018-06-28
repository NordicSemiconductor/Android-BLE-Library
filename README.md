# Android BLE Library

[ ![Download](https://api.bintray.com/packages/nordic/android/ble-library/images/download.svg?version=1.2.0) ](https://bintray.com/nordic/android/ble-library/1.2.0/link)
[ ![Download](https://api.bintray.com/packages/nordic/android/ble-library/images/download.svg) ](https://bintray.com/nordic/android/ble-library/_latestVersion)

This library has been extracted from [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox)
project. It contains classes useful when doing a connection to a Bluetooth LE device.

### Features

**BleManager** class provides the following features:

1. Connection
2. Service discovery
3. Bonding (optional) and removing bond information (using reflections)
4. Automatic handling of Service Changed indications
5. Device initialization
6. Asynchronous and synchronous BLE operations using queue
7. Requesting MTU and connection priority on Android Lollipop or newer
8. Reading and setting preferred PHY on Android Oreo or newer
9. Reading RSSI
10. Refreshing device cache
11. Error handling
12. Logging (in LogCat and optionally nRF Logger)

It may be used for a single connection (see [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox) -> RSC profile) 
or when multiple connections are required (see nRF Toolbox -> Proximity profile), 
from a Service (see nRF Toolbox -> RSC profile), 
ViewModel's repo (see [Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html) and [nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky)),
or as a singleton (not recommended, see nRF Toolbox -> HRM).

**BleManager** exposes a high level API, much more readable then the low level BLE operations. 
If autoConnect option is to be used, the manager also handles initial connection (with autoConnect = false),
which is much faster, and then reconnects in case of link loss with this parameter set to true.
Just return *true* from `shouldAutoConnect()` method in your manager.

### Version 2.x

This library is now in alpha stage. It has been tested in several projects and no issues were found so far.
Feedback is highy appreciated. 

Version 2 has many more features comparing to 1.x:
1. Support for synchronous operations.
2. Operation timeouts.
3. New operations: reading PHY, setting preferred PHY, reading RSSI, clearing device cache, removing bond information.
4. Improved API. Each operation now have its own completion and failure callbacks. Inclding connection and disconnection.
5. Splitting long packets into MTU-3-byte long packets is automatically supported, also merging long packets to single message.
6. All callbacks are guaranteed to be called from the UI thread.
7. Battery Service support has been deprecated and will be removed in some future version.
8. All previously used callbacks in BleManagerGattCallback (for example `onCharacteristicNotified`) will still work, but are now deprecated and may be removed in the future.

#### Maven or jcenter

The library may be found on jcenter and Maven Central repository. Add it to your project by adding the following dependency:

```grovy
implementation 'no.nordicsemi.android:ble:2.0-alpha4'
```

#### Changes:

1. BLE opearation methods (i.e. writeCharacteristic(...), etc.) return the Request class now, instead of boolean.
2. onLinklossOccur callback has been renamed to onLinkLossOccurred.
3. GATT callbacks (for example: onCharacteristicRead, onCharacteristicNotified, etc) inside BleManagerGattCallback has been deprecated. Use Request callbacks instead.
4. Build-in Battery Level support has been deprecated. Request Battery Level as any other value.
5. A new callbacks method: onBondingFailed has been added to BleManagerCallbacks.

#### Migration guide:

1. Replace `initGatt(BluetoothGatt)` with `initialize()`:

Old code:
```java
@Override
protected Deque<Request> initGatt(final BluetoothGatt gatt) {
  final LinkedList<Request> requests = new LinkedList<>();
  requests.add(Request.newEnableNotificationsRequest(mCharacteristic));
  return requests;
}
```
New code:
```java
@Override
protected void initialize() {
  setNotificationCallback(mCharacteristic)
    .with(new DataReceivedCallback() {
      @Override
      public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        ...
      }
    });
  enableNotifications(mCharacteristic)
    .enqueue();
}
```
See changes in [Android nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/) for more examples.

Remember to call `.enqueue()` method for asynchronous operations!

2. Move your callback implementation from BleManagerGattCallback to request callbacks.
3. To split logic from parsing, we recomend to extend DataReceivedCallback interface in a class where your parse your data, and return higher-level values. For a sample, check out nRF Toolbox and [Android BLE Common Library](https://github.com/NordicSemiconductor/Android-BLE-Common-Library/). If you are depending on a SIG adopted profile, like Heart Rate Monitor, Proximity, etc., feel free to include the BLE Common Library in your project. It has all the parsers implemented. If your profile isn't there, we are happy to accept PRs. 
4. Since version 2.0-alpha2 the `connect()` and `disconnect()` methods also require calling `.enqueue()` in asynchronous use.

#### How to test it:

The new version is compatible with [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox) 
and [BLE Common Library](https://github.com/NordicSemiconductor/Android-BLE-Common-Library). The latter one is a set of useful parsers and callbacks for common Bluetooth SIG adopted profiles.

The libraries are available on jcenter, but if you need to make some changes, clone all 3 projects, check out *develop* branches on each of them, ensure the path to *:ble* and *:ble-common* modules are correct in *settings.gradle* file, and sync the project.

### Version 1.x

The BLE library v 1.x is still supported, but will be deprecated when version 2 goes out of alpha stage. It will not reveive any new features.

#### Maven or jcenter

The library may be found on jcenter and Maven Central repository. Add it to your project by adding the following dependency:

```grovy
implementation 'no.nordicsemi.android:ble:1.2.0'
```

#### Changes:

Some things has changed before the lib graduated to 1.0. Those are:
1. Request class has been extracted to a standalone class, importing this class may be required.
2. Current MTU is now available in the BleManager. Use getMtu() to get the value. Request change using requestMtu(int) as before.

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
