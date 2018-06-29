# Android BLE Library

[ ![Download](https://api.bintray.com/packages/nordic/android/ble-library/images/download.svg?version=1.2.0) ](https://bintray.com/nordic/android/ble-library/1.2.0/link)
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

### Version 1.x

The library is finally on jcenter. See below how to include it in your project.

#### Changes:

Some things has changed before the lib graduated to 1.x. Those are:
1. Request class has been extracted to a standalone class, importing this class may be required.
2. Current MTU is now available in the BleManager. Use `getMtu()` to get the value. Request change using `requestMtu(int)` as before.

### Version 2.0-alpha

Please, check version 2.0 (alpha) on *develop* branch. Version 2 adds more features (for example synchronous requests, and more), is "almost" backwards compatible (a lot of old API has been deprecated).
This version is now well tested and the API should not change. More features may, however, be added.

#### Changes and migration guide:

See the README on develop branch for migration details.

#### How to test it:

The new version is compatible with [nRF Toolbox/develop](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/develop) 
and [BLE Common Library/develop](https://github.com/NordicSemiconductor/Android-BLE-Common-Library/tree/develop). The latter is a set of useful parsers and callbacks for common Bluetooth SIG adopted profiles.

Alpha releases of this and BLE Common Library are available on jcenter and Maven Central.

### How to include it in your project

#### Maven or jcenter

The library may be found on jcenter and Maven Central repository. Add it to your project by adding the following dependency:

```grovy
implementation 'no.nordicsemi.android:ble:1.2.0'
```
 For information how to add the new version, change to *develop* branch.

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