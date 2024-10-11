[ ![Download](https://maven-badges.herokuapp.com/maven-central/no.nordicsemi.android/ble/badge.svg?style=plastic) ](https://search.maven.org/artifact/no.nordicsemi.android/ble)

# Android BLE Library

An Android library that solves a lot of Android's Bluetooth Low Energy problems.

> [!Note]
> We're working on a new version of Bluetooth LE library, which eventually will replace this one:
> [Kotlin BLE Library / version 2.0](https://github.com/NordicSemiconductor/Kotlin-BLE-Library/tree/version/2.0).
> Mind, that version 2 is not backward compatible version 1 or with this library.
>
> Currently, the library is in the early development stage. It is not recommended for production use,
> but we encourage you to try it out and provide feedback.
>
> The new library offers the following improvements over this one:
>  * 100% Kotlin code
>  * Scanning for Bluetooth LE devices (scan results as `Flow`)
>  * Support for Bluetooth LE advertising
>  * Support for mocking BLE devices (easy testing)
>  * All BLE operations use `suspend` modifier or `Flow`s
>
> Read more on project's Readme file.

## Importing

The library may be found on Maven Central repository.
Add it to your project by adding the following dependency:

```groovy
implementation 'no.nordicsemi.android:ble:2.9.0-beta03'
```
The last version not migrated to AndroidX is 2.0.5.

BLE library with Kotlin extension is available in:
```groovy
implementation 'no.nordicsemi.android:ble-ktx:2.9.0-beta03'
```

To import the BLE library with set of parsers for common Bluetooth SIG characteristics, use:
```groovy
implementation 'no.nordicsemi.android:ble-common:2.9.0-beta03'
```
For more information, read [this](BLE-COMMON.md).

An extension for easier integration with `LiveData` is available after adding:
```groovy
implementation 'no.nordicsemi.android:ble-livedata:2.9.0-beta03'
```
This extension adds `ObservableBleManager` with `state` and `bondingState` properties, which
notify about connection and bond state using `androidx.lifecycle.LiveData`.

<details>
	<summary>Importing as a module</summary>

Clone this project and add it to your project:

1. In *settings.gradle* file add the following lines:
    ```groovy
    if (file('../Android-BLE-Library').exists()) {
        includeBuild('../Android-BLE-Library')
    }
    ```
2. Sync project and build it.

   The library uses Java 1.18 features. If you're using Android Studio below Giraffe, make sure your
   *build.gradle* includes the following configuration:

   ```groovy
       compileOptions {
           sourceCompatibility JavaVersion.VERSION_1_17
           targetCompatibility JavaVersion.VERSION_1_17
       }
       // For Kotlin projects additionally:
       kotlinOptions {
           jvmTarget = "1.17"
       }
    ```
</details>

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
17. Kotlin support (coroutines, Flow, ...) (since version 2.3)

> Note:
  The library **does not provide support for scanning** for Bluetooth LE devices.
  Instead, we recommend using
  [Android Scanner Compat Library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library)
  which brings almost all recent features, introduced in Lollipop and later, to the older platforms.

### Recent changes

See [Releases](https://github.com/NordicSemiconductor/Android-BLE-Library/releases) for details.
Below is short summary:

<details>
	<summary>Version 2.7</summary>

1. Library has been migrated to Java 17 due to minimum supported version in Android Studio Giraffe.

</details>

<details>
	<summary>Version 2.6</summary>

1. `getGattCallback()` method has been deprecated. Instead, simply move the inner methods to the
   `BleManager` class. See [this PR](https://github.com/NordicSemiconductor/Android-nRF-Blinky/pull/78).
2. Support for *server only* implementation using `attachClientConnection(BluetoothDevice)`. Call it instead of
   `connect(BluetoothDevice)` to use the device as server-only.
3. Data provider for read requests (server side) was added.
4. Cancellation support for flows and suspended methods added to `:ble-kts`.
</details>

<details>
	<summary>Version 2.4</summary>

1. More `:ble-ktx` extensions.
	1. `.suspendForResponse()` and `.suspendForValidResponse()` extension methods added to read and write requests.
	2. `.asResponseFlow()` and `.asValidResponseFlow()` methods added to `ValueChangedCallback`.
	3. `.stateAsFlow()` and `.bondingStateAsFlow()` in `BleManager` now return the same flow when called multiple times.
	4. Progress indications for split outgoing data and merged incoming data can be observed as `Flow` using
	   `splitWithProgressFlow(...)` and `mergeWithProgressFlow(...)`.
2. A new `then(...)` method added to `ValueChangedCallback` which will be called when the callback has been unregistered,
   or the device has invalidated services (i.e. it has disconnected). Useful to release resources.
3. A new option to remove a handler from each `Request` using `setHandler(null)`, which will make the callbacks
   called immediately from the worker looper.
4. Option to filter logs by priority. By default only logs on `Log.INFO` or higher will now be logged. Use
   `getMinLogPriority()` to return a different value if needed. Logs with lower priority will not be produced
   at all, making the library less laggy (parsing incoming data to hex takes notable time).
5. Added support for Big Endian format types with the new `Data.FORMAT_xxx_BE` types. Also, `FORMAT_xxx` have been
   deprecated in favor of `FORMAT_xxx_LE`.
6. All user callbacks (`before`, `with`, `then`, `fail`, ...) are now wrapped in `try-catch` blocks.
</details>

<details>
	<summary>Version 2.3</summary>

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

    When using coroutines, use `.suspend()` method in `Request`, instead of `enqueue()` or `await()`.

    To register to notifications and indications (or incoming write requests for server) use
    ```kotlin
    setNotificationCallback(characteristic)
       .merge(JsonMerger()) // Example of how to use JsonMerger, optional
       .asFlow()
    ```
</details>

<details>
	<summary>Version 2.2</summary>

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

Version 2.2 breaks some API known from version 2.1.1.
Check out [migration guide](MIGRATION.md).
</details>

## Usage

See [Usage](USAGE.md) for more details.

## Examples

#### Trivia game

The [Trivia game](https://github.com/NordicSemiconductor/Android-BLE-Library/tree/main/examples/trivia)
is an example demonstrating client and server.

#### GATT Client Example

Please refer to the `examples/ble-gatt-client folder` for a project that illustrates the GATT
server provided as a foreground service. There's a simple UI with a text field to update
with the value of a characteristic that can be read and subscribed to. This characteristic also
demands encryption as an illustration of best-practice.

You can run this client on one device and a complimenting server on another (see the next section).

> Note:
  This project is not maintained actively. It is provided as an example only and may not be
  migrated to latest version.

#### GATT Server example

Starting from version 2.2 you may now define and use the GATT server in the BLE Library.

Please refer to the `examples/ble-gatt-server folder` for a project that illustrates the GATT
server provided as a foreground service. There's a simple UI with a text field to update
the value of a characteristic that can be read and subscribed to. This characteristic also
demands encryption as an illustration of best-practice.

> Note:
  This project is not maintained actively. It is provided as an example only and may not be
  migrated to latest version.

#### More examples

Find the simple example here [Android nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky).

For an example how to use it from a `ViewModel` or a `Service`, check
[nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox).

## Version 1.x

The BLE library v 1.x is no longer supported. Please migrate to 2.x for bug fixing releases.
Find it on [version/1x branch](https://github.com/NordicSemiconductor/Android-BLE-Library/tree/version/1x).

A migration guide is available [here](MIGRATION.md).