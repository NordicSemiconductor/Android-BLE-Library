# Android BLE Library

[ ![Download](https://api.bintray.com/packages/nordic/android/no.nordicsemi.android%3Able/images/download.svg) ](https://bintray.com/nordic/android/no.nordicsemi.android%3Able/_latestVersion)

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
16. GATT server (since version 2.2.0)

The library **does not provide support for scanning** for Bluetooth LE devices.
For scanning, we recommend using 
[Android Scanner Compat Library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library)
which brings almost all recent features, introduced in Lollipop and later, to the older platforms. 

## Importing

#### Maven Central or jcenter

The library may be found on jcenter and Maven Central repository. 
Add it to your project by adding the following dependency:

```grovy
implementation 'no.nordicsemi.android:ble:2.1.1'
```
The last version not migrated to AndroidX is 2.0.5.

To test the latest features, use the **alpha version**:
```grovy
implementation 'no.nordicsemi.android:ble:2.2.0-alpha08'
```
Features available in version 2.2.0:
1. GATT Server support. This includes setting up the local GATT server on the Android device, new 
   requests for server operations (*wait for read*, *wait for write*, *send notification*, *send indication*,
   *set characteristic value*, *set descriptor value*).
2. New conditional requests: *waif if* and *wait until*.
3. BLE operations are no longer called from the main thread.
4. There's a new option to set a handler for invoking callbacks. A handler can also be set per-callback.
5. Breaking change: some fields in the *BleManager* got rid of the Hungarian Notation. In particular,
   *mCallbacks* was renamed to *callbacks*, and it got deprecated.
6. Breaking change: `BleManager` is no longer a generic class.
7. Breaking change: `setGattCallbacks(BleManagerCallbacks)` has been deprecated. Instead, use new 
   `setDisconnectCallback(DisconnectCallback)` and `setBondingCallback(BondingCallback)`. For other
   callbacks, check out the deprecation messages in `BleManagerCallbacks` interface. 
The API of version 2.2.0 is not finished and may slightly change in the near future.

#### As a library module

Clone this project and add *ble* module as a dependency to your project:

1. In *settings.gradle* file add the following lines:
```groovy
include ':ble'
project(':ble').projectDir = file('../Android-BLE-Library/ble')
```
2. In *app/build.gradle* file add `implementation project(':ble')` inside dependencies.
3. Sync project and build it.

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

The first step is to create your BLE Manager implementation, like below. The manager should
act as API of your remote device, to separate lower BLE layer from the application layer.
```java

class MyBleManager extends BleManager {
	final static UUID SERVICE_UUID = UUID.fromString("XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX");
	final static UUID FIRST_CHAR   = UUID.fromString("XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX");
	final static UUID SECOND_CHAR  = UUID.fromString("XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX");

	// Client characteristics
	private BluetoothGattCharacteristic firstCharacteristic, secondCharacteristic;

	MyBleManager(@NonNull final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BleManagerGattCallback getGattCallback() {
		return new MyManagerGattCallback();
	}

	@Override
	public void log(final int priority, @NonNull final String message) {
		// Please, don't log in production.
		if (Build.DEBUG || priority == Log.ERROR)
			Log.println(priority, "MyBleManager", message);
	}

	/**
	 * BluetoothGatt callbacks object.
	 */
	private class MyManagerGattCallback extends BleManagerGattCallback {

		// This method will be called when the device is connected and services are discovered.
		// You need to obtain references to the characteristics and descriptors that you will use.
		// Return true if all required services are found, false otherwise.
		@Override
		public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(SERVICE_UUID);
			if (service != null) {
				firstCharacteristic = service.getCharacteristic(FIRST_CHAR);
				secondCharacteristic = service.getCharacteristic(SECOND_CHAR);
			}
			// Validate properties
			boolean notify = false;
			if (firstCharacteristic != null) {
				final int properties = dataCharacteristic.getProperties();
				notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
			}
			boolean writeRequest = false;
			if (secondCharacteristic != null) {
				final int properties = controlPointCharacteristic.getProperties();
				writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
				secondCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
			}
			// Return true if all required services have been found
			return firstCharacteristic != null && secondCharacteristic != null
					&& notify && writeRequest;
		}

		// If you have any optional services, allocate them here. Return true only if
		// they are found. 
		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			return super.isOptionalServiceSupported(gatt);
		}

		// Initialize your device here. Often you need to enable notifications and set required
		// MTU or write some initial data. Do it here.
		@Override
		protected void initialize() {
			// You may enqueue multiple operations. A queue ensures that all operations are 
			// performed one after another, but it is not required.
			beginAtomicRequestQueue()
					.add(requestMtu(247) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
						.with((device, mtu) -> log(Log.INFO, "MTU set to " + mtu))
						.fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
					.add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
						.fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
					.add(enableNotifications(firstCharacteristic))
					.done(device -> log(Log.INFO, "Target initialized"))
					.enqueue();			
			// You may easily enqueue more operations here like such:
			writeCharacteristic(secondCharacteristic, "Hello World!".getBytes())
					.done(device -> log(Log.INFO, "Greetings sent"))
					.enqueue();
			// Set a callback for your notifications. You may also use waitForNotification(...).
			// Both callbacks will be called when notification is received.
			setNotificationCallback(firstCharacteristic, callback);
			// If you need to send very long data using Write Without Response, use split()
			// or define your own splitter in split(DataSplitter splitter, WriteProgressCallback cb). 
			writeCharacteristic(secondCharacteristic, "Very, very long data that will no fit into MTU")
					.split()
					.enqueue();
		}

		@Override
		protected void onDeviceDisconnected() {
			// Device disconnected. Release your references here.
			firstCharacteristic = null;
			secondCharacteristic = null;
		}
	};
	
	// Define your API.
	
	private abstract class FluxHandler implements DataReceivedCallback {		
		@Override
		public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			if (data.getByte(0) == 0x01)
				onFluxCapacitorEngaged();
		}
		
		abstract void onFluxCapacitorEngaged();
	}
	
	/** Initialize time machine. */
	public void enableFluxCapacitor(final int year) {
		waitForNotification(firstCharacteristic)
			.trigger(
					writeCharacteristic(secondCharacteristic, new FluxJumpRequest(year))
						.done(device -> log(Log.INDO, "Power on command sent"))
			 )
			.with(new FluxHandler() {
				public void onFluxCapacitorEngaged() {
					log(Log.WARN, "Flux Capacitor enabled! Going back to the future in 3 seconds!");
					callbacks.onFluxCapacitorEngaged();
					
					sleep(3000).enqueue();
					write(secondCharacteristic, "Hold on!".getBytes())
						.done(device -> log(Log.WARN, "It's " + year + "!"))
						.fail((device, status) -> "Not enough flux? (status: " + status + ")")
						.enqueue();
				}
			})
			.enqueue();
	}
	
	/** 
	* Aborts time travel. Call during 3 sec after enabling Flux Capacitor and only if you don't 
	* like 2020. 
	*/
	public void abort() {
		cancelQueue();
	}
}
```
Create the callbacks for your device:
```java
interface FluxCallbacks extends BleManagerCallbacks {
	void onFluxCapacitorEngaged();
}
```

To connect to a Bluetooth LE device using GATT, create a manager instance:

```java
final MyBleManager manager = new MyBleManager(context);
manager.setDisconnectCallback(abortCallback);
manager.connect(device)
	.timeout(100000)
	.retry(3, 100)
	.done(device -> Log.i(TAG, "Device initiated"))
	.enqueue();
```

#### Adding GATT Server support

Starting from version 2.2 you may now define and use the GATT server in the BLE Library.

First, override a `BleServerManager` class and override `initializeServer()` method. Some helper
methods, like `characteristic(...)`, `descriptor(...)` and their shared counterparts were created 
for making the initialization more readable.

```java
public class ServerManager extends BleServerManager {

	ServerManager(@NonNull final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected List<BluetoothGattService> initializeServer() {
		return Collections.singletonList(
				service(SERVICE_UUID,
				characteristic(CHAR_UUID,
						BluetoothGattCharacteristic.PROPERTY_WRITE // properties
								| BluetoothGattCharacteristic.PROPERTY_NOTIFY
								| BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS,
						BluetoothGattCharacteristic.PERMISSION_WRITE, // permissions
						null, // initial data
						cccd(), reliableWrite(), description("Some description", false) // descriptors
				))
		);
	}
}
```
Instantiate the server and set the callback listener:
```java
final ServerManager serverManager = new ServerManager(context);
serverManager.setServerCallback(this);
```
Set the server manager for each client connection:
```java
// e.g. at BleServerManagerCallbacks#onDeviceConnectedToServer(@NonNull final BluetoothDevice device)
final MyBleManager manager = new MyBleManager(context);
manager.setDisconnectCallback(this);
manager.setBondingCallback(this);
// Use the manager with the server
manager.useServer(serverManager);
// Set connected device
manager.connect(device).enqueue()
// [...]

```
The `BleServerManagerCallbacks.onServerReady()` will be invoked when all service were added.
You may initiate your connection there.

In your client manager class, override the following method:
```java
class MyBleManager extends BleManager {
	// [...]	

	// Server characteristics
	private BluetoothGattCharacteristic serverCharacteristic;

	// [...]

	/**
	 * BluetoothGatt callbacks object.
	 */
	private class MyManagerGattCallback extends BleManagerGattCallback {
		// [...]	
	
		@Override
		protected void onServerReady(@NonNull final BluetoothGattServer server) {
			// Obtain your server attributes.
			serverCharacteristic = server
					.getService(SERVICE_UUID)
					.getCharacteristic(CHAR_UUID);
			
			//  set write callback, if you need
			setWriteCallback(serverCharacteristic)
					.with((device, data) ->
						sendNotification(otherCharacteristic, "any data".getBytes())
								.enqueue()
					);
            }
		}
		
		// [...]

		@Override
		protected void onDeviceDisconnected() {
			// [...]
			serverCharacteristic = null;
		}
	};
	
	// [...]
}
``` 

#### How to test it:

The new version is compatible with [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox) 
and [BLE Common Library](https://github.com/NordicSemiconductor/Android-BLE-Common-Library). 
The latter one is a set of useful parsers and callbacks for common Bluetooth SIG adopted profiles.

The libraries are available on jcenter, but if you need to make some changes, clone all 3 projects, 
ensure the path to *:ble* and *:ble-common* modules are correct in *settings.gradle* file, and sync the project.

## Examples

Find the simple example here [Android nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky).

For an example how to use it from an Activity or a Service, check the base Activity and Service 
classes in [nRF Toolbox](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/master/app/src/main/java/no/nordicsemi/android/nrftoolbox/profile).

1. Define your device API by extending `BleManagerCallbacks`:
[example](https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/profile/BlinkyManagerCallbacks.java)
2. Extend `BleManager` class and implement required methods:
[example](https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/profile/BlinkyManager.java)

## Version 1.x

The BLE library v 1.x is no longer supported. Please migrate to 2.x for bug fixing releases.
Find it on [version/1x branch](https://github.com/NordicSemiconductor/Android-BLE-Library/tree/version/1x).

Migration guide is available [here](MIGRATION.md).