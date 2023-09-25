## Changes in version 2.0

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

### Migration guide

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
   
   `ConnectRequest`'s completion callback is called after the initialization is done (without or with errors).

2. Move your callback implementation from `BleManagerGattCallback` to request callbacks.
3. To split logic from parsing, we recommend to extend `DataReceivedCallback` interface in a class 
where your parse your data, and return higher-level values. For a sample, check out nRF Toolbox 
and [Android BLE Common Library](https://github.com/NordicSemiconductor/Android-BLE-Common-Library/). 
If you are depending on a SIG adopted profile, like Heart Rate Monitor, Proximity, etc., 
feel free to include the **BLE Common Library** in your project. 
It has all the parsers implemented. If your profile isn't there, we are happy to accept PRs.
4. `connect()` and `disconnect()` methods also require calling `.enqueue()` in asynchronous use.
5. Replace the `shouldAutoConnect()` method in the manager with `connect(device).useAutConnect(true).enqueue()/await()`.

## Changes in version 2.2

1. `BleManager` is no longer a generic class. The `BleManagerCallbacks` interface, previously used 
   to notify about connection and bond states, battery level (deprecated) and application-level 
   callbacks, has been deprecated, together with `BleManager#setGattCallbacks(...)`. Instead:
   * Use `BlaManager#setConnectionObserver(...)` to get connection state updates.
   * Use `BleManager#setBondingObserver(...)` to get bonding events.
   * If required, manage application-level callbacks in your manager (that extends `BleManager`).
   * To make transition easier, `LegacyBleManager` class was introduced that can be used pretty
     much like the old `BleManager`. It even has `mCallbacks` property.
2. Some fields in the *BleManager* got rid of the Hungarian Notation. In particular,
   *mCallbacks* was renamed to *callbacks* (except in `LegacyBleManager`), and it got deprecated.
3. The protected method `getGattCallback()` in `BleManager` is now called from the 
   constructor, so can't return a final field of a manager, as they are not initialized yet.
   Instead, instantiate the `BleManagerGattCallback` class from there.
   
### Migration guide

#### Quick migration (using deprecated API)

1. To make quick transition from 2.1 to 2.2, change the base class of your `BleManager` implementation
   to `LegacyBleManager` and make sure you return an object (not *null*) from `getGattCallback()`:
      
   ```java
   class MyBleManager extends LegacyBleManager<MyBleManagerCallbacks> {
   
       // [...]
   
       @NonNull
       @Override
       protected BleManagerGattCallback getGattCallback() {
           // Before 2.2 it was allowed to return a class property here, but properties are initiated
           // after the constructor, so they would still be null here. Instead, create a new object:
           return new MyBleManagerGattCallback();
       }
   
       // [...]
   
   }
   ```

#### Proper migration

1. Remove the type parameter from your `BleManager` implementation class:

   ```java
   class MyBleManager extends BleManager {
       // [...]
   }
   ```  

2. Replace `setGattCallbacks(callbacks)` with `setConnectionObserver(observer)` and optionally
   `setBondingObserver(observer)`. If you are using `androidx.lifecycle.LiveData`, consider using
   `no.nordicsemi.android:ble-livedata:$ble-version` dependency in your gradle file. In that case,
   extend `ObservableBleManager` instead of `BleManager` and use `getState()` and `getBondingState()` 
   (or `state` and `bondingState` properties in Kotlin) to get `LiveData` objects. See 
   [nRF Blinky](https://github.com/NordicSemiconductor/Android-nRF-Blinky) for an example.
   
   a) The `ConnectionObserver` no longer has `onServicesDiscovered` method. 

   b) `onLinkLossOccurred` was replaced with `onDeviceDisconnected` with reason 
      `ConnectionObserver#REASON_LINK_LOSS`.
      
   c) `onDeviceNotSupported` was replaced with `onDeviceDisconnected` with reason
      `ConnectionObserver#REASON_NOT_SUPPORTED`.
      
3. In 2.1.x the implementation of `BleManager` had access to user defined callbacks (`mCallbacks`), 
   which could have been used to notify a Service or Activity about incoming notifications, etc. 
   As `BleManager` is no longer a generic type, you'll have to implement this logic on your own.
   E.g., nRF Blinky is exposing LED and Button state using `LiveData`, which are available for the
   Activity through `ViewModel`.

## Changes in version 2.7

1. The Android BLE Library 2.7 was migrated to Java 17 due to minimum Java version in current 
   version of Android Studio.