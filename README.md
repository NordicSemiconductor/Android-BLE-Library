# Android BLE Library

[ ![Download](https://api.bintray.com/packages/nordic/android/ble-library/images/download.svg?version=1.2.0) ](https://bintray.com/nordic/android/ble-library/1.2.0/link)

This library has been extracted from [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox)
project. It contains classes useful when doing a connection to a Bluetooth LE device.

## Features

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
10. Logging (in nRF Logger)

The library **does not provide support for scanning** for Bluetooth LE devices.
For scanning, we recommend using 
[Android Scanner Compat Library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library)
which brings almost all recent features, introduced in Lollipop and later, to the older platforms. 

## Importing

#### Maven or jcenter

The library may be found on jcenter and Maven Central repository. 
Add it to your project by adding the following dependency:

```grovy
implementation 'no.nordicsemi.android:ble:1.2.0'
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

## Recent changes:

Some things has changed before the lib graduated to 1.x. Those are:
1. Request class has been extracted to a standalone class, importing this class may be required.
2. Current MTU is now available in the BleManager. Use `getMtu()` to get the value. Request change using `requestMtu(int)` as before.

## How to use it

Find the simple example here [Android nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky).

For an example how to use it from an Activity or a Service, check the base Activity and Service 
classes in [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/master/app/src/main/java/no/nordicsemi/android/nrftoolbox/profile).

1. Define your device API by extending `BleManagerCallbacks`:
[example](https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/profile/BlinkyManagerCallbacks.java)
2. Extend `BleManager` class and implement required methods:
[example](https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/profile/BlinkyManager.java)

For examples of BLE Library v1.x please use older branches of above projects.

## Version 2.0

Please, check version 2.0 on *master* branch. Version 2 adds more features (for example synchronous 
requests, and more), is "almost" backwards compatible (a lot of old API has been deprecated).