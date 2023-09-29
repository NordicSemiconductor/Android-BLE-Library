/*
 * Copyright (c) 2020, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.UUID;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import no.nordicsemi.android.ble.annotation.ConnectionPriority;
import no.nordicsemi.android.ble.annotation.ConnectionState;
import no.nordicsemi.android.ble.annotation.LogPriority;
import no.nordicsemi.android.ble.annotation.PairingVariant;
import no.nordicsemi.android.ble.annotation.PhyMask;
import no.nordicsemi.android.ble.annotation.PhyOption;
import no.nordicsemi.android.ble.annotation.WriteType;
import no.nordicsemi.android.ble.callback.ConnectionParametersUpdatedCallback;
import no.nordicsemi.android.ble.data.DataProvider;
import no.nordicsemi.android.ble.observer.BondingObserver;
import no.nordicsemi.android.ble.observer.ConnectionObserver;
import no.nordicsemi.android.ble.callback.ConnectionPriorityCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.MtuCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataMerger;
import no.nordicsemi.android.ble.data.DataSplitter;
import no.nordicsemi.android.ble.error.GattError;
import no.nordicsemi.android.ble.utils.ILogger;
import no.nordicsemi.android.ble.utils.ParserUtils;

/**
 * <p>
 * The BleManager is responsible for managing the low level communication with a Bluetooth LE device.
 * Please see profiles implementation in Android nRF Blinky or Android nRF Toolbox app for an
 * example of use.
 * <p>
 * This base manager has been tested against number of devices and samples from Nordic SDK.
 * <p>
 * The manager handles connection events and initializes the device after establishing the connection.
 * <ol>
 * <li>For bonded devices it ensures that the Service Changed indications, if this characteristic
 * is present, are enabled. Before Android Marshmallow, Android did not enable them by default,
 * leaving this to the developers.</li>
 * <li><strike>The manager tries to read the Battery Level characteristic. No matter the result of
 * this operation (for example the Battery Level characteristic may not have the READ property)
 * it tries to enable Battery Level notifications to get battery updates from the device.</strike>
 * This feature is now deprecated and will not work with the new API. Instead, read or enabledBattery Level
 * notifications just like any other.</li>
 * <li>After connecting and service discovery, the manager initializes the device using given queue
 * of commands. See {@link BleManagerGattCallback#initialize()} method for more details.</li>
 * <li>When initialization complete, the {@link ConnectRequest#done(SuccessCallback)}
 * callback is called.</li>
 * </ol>
 * <p>
 * <strike>The manager also is responsible for parsing the Battery Level values and calling
 * {@link BleManagerCallbacks#onBatteryValueReceived(BluetoothDevice, int)} method.</strike>
 * <p>
 * To get logs, override the {@link #log(int, String)} method.
 * <p>
 * The BleManager should be overridden in your app and all the 'high level' callbacks should
 * be called from there.
 */
@SuppressLint("MissingPermission")
@SuppressWarnings({"WeakerAccess", "unused", "DeprecatedIsStillUsed", "deprecation"})
public abstract class BleManager implements ILogger {
	final static UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	final static UUID BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	final static UUID BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	final static UUID GENERIC_ATTRIBUTE_SERVICE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
	final static UUID SERVICE_CHANGED_CHARACTERISTIC = UUID.fromString("00002A05-0000-1000-8000-00805f9b34fb");

	public static final int PAIRING_VARIANT_PIN = 0;
	public static final int PAIRING_VARIANT_PASSKEY = 1;
	public static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
	public static final int PAIRING_VARIANT_CONSENT = 3;
	public static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
	public static final int PAIRING_VARIANT_DISPLAY_PIN = 5;
	public static final int PAIRING_VARIANT_OOB_CONSENT = 6;

	private final Context context;
	private BleServerManager serverManager;
	@NonNull
	final BleManager.BleManagerGattCallback requestHandler;
	/** Manager callbacks, set using {@link #setGattCallbacks(BleManagerCallbacks)}. */
	@Deprecated
	protected BleManagerCallbacks callbacks;
	@Nullable
	BondingObserver bondingObserver;
	@Nullable
	ConnectionObserver connectionObserver;

	private final BroadcastReceiver mPairingRequestBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			// Skip other devices.
			final BluetoothDevice bluetoothDevice = requestHandler.getBluetoothDevice();
			if (bluetoothDevice == null || device == null
					|| !device.getAddress().equals(bluetoothDevice.getAddress()))
				return;

			// String values are used as the constants are not available for Android 4.3.
			final int variant = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT"/*BluetoothDevice.EXTRA_PAIRING_VARIANT*/, 0);
			final int key = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY"/*BluetoothDevice.PAIRING_KEY*/, -1);
			log(Log.DEBUG, "[Broadcast] Action received: android.bluetooth.device.action.PAIRING_REQUEST"/*BluetoothDevice.ACTION_PAIRING_REQUEST*/ +
					", pairing variant: " + ParserUtils.pairingVariantToString(variant) + " (" + variant + "); key: "+key);

			onPairingRequestReceived(device, variant, key);
		}
	};

	/**
	 * The manager constructor.
	 * <p>
	 * To connect a device, call {@link #connect(BluetoothDevice)}.
	 *
	 * @param context the context.
	 */
	public BleManager(@NonNull final Context context) {
		this(context, new Handler(Looper.getMainLooper()));
	}

	/**
	 * The manager constructor.
	 * <p>
	 * To connect a device, call {@link #connect(BluetoothDevice)}.
	 *
	 * @param context the context.
	 * @param handler the handler used for delaying operations, timeouts and, most of all, the
	 *                request callbacks (done/fail/with, etc).
	 */
	public BleManager(@NonNull final Context context, @NonNull final Handler handler) {
		this.context = context;
		this.requestHandler = getGattCallback();
		this.requestHandler.init(this, handler);

		context.registerReceiver(mPairingRequestBroadcastReceiver,
				// BluetoothDevice.ACTION_PAIRING_REQUEST
				new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST"));
	}

	/**
	 * This method should set up the request queue needed to initialize the profile.
	 * Enabling Service Change indications for bonded devices is handled before executing this
	 * queue. The queue may have requests that are not available, e.g. read an optional
	 * service when it is not supported by the connected device. Such call will trigger
	 * {@link Request#fail(FailCallback)}.
	 * <p>
	 * This method is called when the services has been discovered and the device is supported
	 * (has required service).
	 * <p>
	 * Remember to call {@link Request#enqueue()} for each request.
	 * <p>
	 * A sample initialization should look like this:
	 * <pre>
	 * &#64;Override
	 * protected void initialize() {
	 *    requestMtu(MTU)
	 *       .with((device, mtu) -> {
	 *           ...
	 *       })
	 *       .enqueue();
	 *    setNotificationCallback(characteristic)
	 *       .with((device, data) -> {
	 *           ...
	 *       });
	 *    enableNotifications(characteristic)
	 *       .done(device -> {
	 *           ...
	 *       })
	 *       .fail((device, status) -> {
	 *           ...
	 *       })
	 *       .enqueue();
	 * }
	 * </pre>
	 */
	protected void initialize() {
		// Don't call super.initialize() when overriding this method.
		requestHandler.initialize();
	}

	/**
	 * This method should return <code>true</code> when the gatt device supports the
	 * required services.
	 *
	 * @param gatt the gatt device with services discovered
	 * @return <code>True</code> when the device has the required service.
	 */
	protected boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
		// Don't call super.isRequiredServiceSupported(gatt) when overriding this method.
		return requestHandler.isRequiredServiceSupported(gatt);
	}

	/**
	 * This method should return <code>true</code> when the gatt device supports the
	 * optional services. The default implementation returns <code>false</code>.
	 *
	 * @param gatt the gatt device with services discovered
	 * @return <code>True</code> when the device has the optional service.
	 */
	protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
		// Don't call super.isOptionalServiceSupported(gatt) when overriding this method.
		return requestHandler.isOptionalServiceSupported(gatt);
	}

	/**
	 * In this method the manager should get references to server characteristics and descriptors
	 * that will use. The method is called after the service discovery of a remote device has
	 * finished and {@link #isRequiredServiceSupported(BluetoothGatt)} returned true.
	 * <p>
	 * The references obtained in this method should be released in {@link #onServicesInvalidated()}.
	 * <p>
	 * This method is called only when the server was set by
	 * {@link BleManager#useServer(BleServerManager)} and opened using {@link BleServerManager#open()}.
	 *
	 * @param server The GATT Server instance. Use {@link BluetoothGattServer#getService(UUID)} to
	 *               obtain service instance.
	 */
	protected void onServerReady(@NonNull final BluetoothGattServer server) {
		// Don't call super.onServerReady(server) when overriding this method.
		requestHandler.onServerReady(server);
	}

	/**
	 * This method should nullify all services and characteristics of the device.
	 * <p>
	 * It's called when the services were invalidated and can no longer be used. Most probably the
	 * device has disconnected, Service Changed indication was received, or
	 * {@link #refreshDeviceCache()} request was executed, which has invalidated cached services.
	 */
	protected void onServicesInvalidated() {
		// Don't call super.onServicesInvalidated() when overriding this method.
		requestHandler.onServicesInvalidated();
	}

	/**
	 * Called when the initialization queue is complete.
	 */
	protected void onDeviceReady() {
		// Don't call super.onDeviceReady() when overriding this method.
		requestHandler.onDeviceReady();
	}

	/**
	 * Called each time the task queue gets cleared.
	 */
	protected void onManagerReady() {
		// Don't call super.onManagerReady() when overriding this method.
		requestHandler.onManagerReady();
	}

	/**
	 * Closes and releases resources. This method will be called automatically after
	 * calling {@link #disconnect()}. When the device disconnected with link loss and
	 * {@link ConnectRequest#shouldAutoConnect()} returned true you have to call this method to
	 * close the connection.
	 */
	public void close() {
		try {
			context.unregisterReceiver(mPairingRequestBroadcastReceiver);
		} catch (final Exception e) {
			// The receiver must have been already unregistered before.
		}
		if (serverManager != null) {
			serverManager.removeManager(this);
		}
		requestHandler.close();
	}

	/**
	 * Runs the given runnable using a handler given to the constructor.
	 * If no handler was given, the callbacks will be called on UI thread.
	 *
	 * @param runnable the runnable to be executed.
	 */
	protected void runOnCallbackThread(@NonNull final Runnable runnable) {
		requestHandler.post(runnable);
	}

	/**
	 * Sets the manager callback listener.
	 *
	 * @param callbacks the callback listener.
	 * @deprecated Use per-request callbacks.
	 */
	@Deprecated
	public void setGattCallbacks(@NonNull final BleManagerCallbacks callbacks) {
		this.callbacks = callbacks;
	}

	/**
	 * Sets the connection observer.
	 * This callback will be called using the handler given in {@link BleManager#BleManager(Context, Handler)}.
	 *
	 * @param callback the callback listener.
	 */
	public final void setConnectionObserver(@Nullable final ConnectionObserver callback) {
		this.connectionObserver = callback;
	}

	/**
	 * Returns the current connection observer object.
	 */
	@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
	@Nullable
	public final ConnectionObserver getConnectionObserver() {
		return this.connectionObserver;
	}

	/**
	 * Sets the observer, that will receive events related to bonding.
	 * This callback will be called using the handler given in {@link BleManager#BleManager(Context, Handler)}.
	 *
	 * @param callback the callback.
	 * @see BondingObserver
	 */
	public final void setBondingObserver(@Nullable final BondingObserver callback) {
		this.bondingObserver = callback;
	}

	/**
	 * Returns the current bonding state observer object.
	 */
	@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
	@Nullable
	public final BondingObserver getBondingObserver() {
		return this.bondingObserver;
	}

	/**
	 * This method binds the manager with the give server instance. Apps that allow multiple
	 * simultaneous connections and GATT server should use a single server instance, shared
	 * between all clients.
	 *
	 * @param server the server instance.
	 */
	public final void useServer(@NonNull final BleServerManager server) {
		if (serverManager != null) {
			serverManager.removeManager(this);
		}
		serverManager = server;
		server.addManager(this);
		requestHandler.useServer(server);
	}

	final void closeServer() {
		serverManager = null;
		requestHandler.useServer(null);
	}

	/**
	 * This method will be called if a remote device requires a non-'just works' pairing.
	 * See PAIRING_* constants for possible options.
	 *
	 * @param device  the device.
	 * @param variant pairing variant.
	 * @param key     pairing passkey, if supported by variant. -1 otherwise
	 */
	protected void onPairingRequestReceived(@NonNull final BluetoothDevice device,
											@PairingVariant final int variant,
											final int key) {
		// The API below is available for Android 4.4 or newer.

		// An app may set the PIN here or set pairing confirmation (depending on the variant) using:
		// device.setPin(new byte[] { '1', '2', '3', '4', '5', '6' });
		// device.setPairingConfirmation(true);

		// However, setting the PIN here will not prevent from displaying the default pairing
		// dialog, which is shown by another application (Bluetooth Settings).
	}

	/**
	 * This method returns the GATT callback used by the manager.
	 * <p>
	 * Since version 2.6 this method is private. The manager just can implement all inner methods
	 * directly, without additional object.
	 *
	 * @return The gatt callback object.
	 * @deprecated Implement all methods directly in your manager.
	 */
	@Deprecated
	@NonNull
	protected BleManagerGattCallback getGattCallback() {
		return new BleManagerGattCallback() {
			@Override
			protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
				return false;
			}

			@Override
			protected void onServicesInvalidated() {
				// empty default implementation
			}
		};
	}

	/**
	 * Returns the context that the manager was created with.
	 *
	 * @return The context.
	 */
	@NonNull
	protected final Context getContext() {
		return context;
	}

	/**
	 * Returns the Bluetooth device object used in {@link #connect(BluetoothDevice)}.
	 *
	 * @return The Bluetooth device or null, if {@link #connect(BluetoothDevice)} wasn't called.
	 */
	@Nullable
	// This method is not final, as some Managers may be created with BluetoothDevice in a
	// constructor. Those can return the device object even without calling connect(device).
	public BluetoothDevice getBluetoothDevice() {
		return requestHandler.getBluetoothDevice();
	}

	/**
	 * This method returns true if the device is connected. Services could have not been
	 * discovered yet.
	 */
	public final boolean isConnected() {
		return requestHandler.isConnected();
	}

	/**
	 * Returns true if the device is connected and the initialization has finished,
	 * that is when {@link BleManagerGattCallback#onDeviceReady()} was called.
	 */
	public final boolean isReady() {
		return requestHandler.isReady();
	}

	/**
	 * Returns whether the target device is bonded. The device does not have to be connected,
	 * but must have been set prior to call this method.
	 *
	 * @return True, if the Android has bonds information of the device. This does not mean that
	 * the target device also has such information, or that the link is in fact encrypted.
	 */
	protected final boolean isBonded() {
		final BluetoothDevice bluetoothDevice = requestHandler.getBluetoothDevice();
		return bluetoothDevice != null
				&& bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED;
	}

	/**
	 * Method returns the connection state:
	 * {@link BluetoothGatt#STATE_CONNECTING STATE_CONNECTING},
	 * {@link BluetoothGatt#STATE_CONNECTED STATE_CONNECTED},
	 * {@link BluetoothGatt#STATE_DISCONNECTING STATE_DISCONNECTING},
	 * {@link BluetoothGatt#STATE_DISCONNECTED STATE_DISCONNECTED}
	 *
	 * @return The connection state.
	 */
	@ConnectionState
	public final int getConnectionState() {
		return requestHandler.getConnectionState();
	}

	/**
	 * Returns the last received value of Battery Level characteristic, or -1 if such
	 * does not exist, hasn't been read or notification wasn't received yet.
	 * <p>
	 * The value returned will be invalid if overridden {@link #readBatteryLevel()} and
	 * {@link #enableBatteryLevelNotifications()} were used.
	 *
	 * @return The last battery level value in percent.
	 * @deprecated Keep the battery level in your manager instead.
	 */
	@IntRange(from = -1, to = 100)
	@Deprecated
	public final int getBatteryValue() {
		return requestHandler.getBatteryValue();
	}

	@Override
	@LogPriority
	public int getMinLogPriority() {
		// By default, the library will log entries on INFO and higher priorities.
		// Consider changing to false in production to increase speed and decrease memory allocations.

		// Note: Before version 2.4.0 all logs were logged by default, so this changes previous behavior.
		//       To restore it, return Log.VERBOSE here.
		return Log.INFO;
	}

	@Override
	public void log(@LogPriority final int priority, @NonNull final String message) {
		// Override to log events. Simple log can use Logcat:
		//
		// Log.println(priority, TAG, message);
		//
		// You may also use Timber:
		//
		// Timber.log(priority, message);
		//
		// or nRF Logger:
		//
		// Logger.log(logSession, LogContract.Log.Level.fromPriority(priority), message);
		//
		// Starting from nRF Logger 2.1.3, you may use log-timber and plant nRFLoggerTree.
		// https://github.com/NordicSemiconductor/nRF-Logger-API
	}

	@Override
	public void log(@LogPriority final int priority, @StringRes final int messageRes,
					@Nullable final Object... params) {
		final String message = context.getString(messageRes, params);
		log(priority, message);
	}

	/**
	 * Returns whether to connect to the remote device just once (false) or to add the address to
	 * white list of devices that will be automatically connect as soon as they become available
	 * (true). In the latter case, if Bluetooth adapter is enabled, Android scans periodically
	 * for devices from the white list and if a advertising packet is received from such, it tries
	 * to connect to it. When the connection is lost, the system will keep trying to reconnect to
	 * it in. If true is returned, and the connection to the device is lost the
	 * {@link BleManagerCallbacks#onLinkLossOccurred(BluetoothDevice)} callback is called instead of
	 * {@link BleManagerCallbacks#onDeviceDisconnected(BluetoothDevice)}.
	 * <p>
	 * This feature works much better on newer Android phone models and many not work on older
	 * phones.
	 * <p>
	 * This method should only be used with bonded devices, as otherwise the device may change
	 * it's address. It will however work also with non-bonded devices with private static address.
	 * A connection attempt to a device with private resolvable address will fail.
	 * <p>
	 * The first connection to a device will always be created with autoConnect flag to false
	 * (see {@link BluetoothDevice#connectGatt(Context, boolean, BluetoothGattCallback)}). This is
	 * to make it quick as the user most probably waits for a quick response.
	 * However, if this method returned true during first connection and the link was lost,
	 * the manager will try to reconnect to it using {@link BluetoothGatt#connect()} which forces
	 * autoConnect to true.
	 *
	 * @return The AutoConnect flag value.
	 * @deprecated Use {@link ConnectRequest#useAutoConnect(boolean)} instead.
	 */
	@Deprecated
	protected boolean shouldAutoConnect() {
		return false;
	}

	/**
	 * Returns whether the device cache should be cleared after the device disconnected,
	 * before calling {@link BluetoothGatt#close()}. By default it returns false.
	 * <p>
	 * If the returned value is true, the next time the Android device will connect to
	 * this peripheral the services will be discovered again. If false, the services
	 * will be obtained from the cache.
	 * <p>
	 * Note, that the {@link BluetoothGatt#refresh()} method is not in the public API and it
	 * is not recommended to use this. However, as Android is caching services of all devices,
	 * even if they are not bonded and have Service Changed characteristic, it may necessary to
	 * clear the cache manually.
	 * <p>
	 * On older Android versions clearing device cache helped with connection stability.
	 * It was common to get error 133 on the second and following connections when services were
	 * obtained from the cache. However, full service discovery takes time and consumes peripheral's
	 * battery.
	 *
	 * @return True, if the device cache should be cleared after the device disconnects or false,
	 * (default) if the cached value be used.
	 */
	@SuppressWarnings("JavadocReference")
	protected boolean shouldClearCacheWhenDisconnected() {
		return false;
	}

	/**
	 * The onConnectionStateChange event is triggered just after the Android connects to a device.
	 * In case of bonded devices, the encryption is reestablished AFTER this callback is called.
	 * Moreover, when the device has Service Changed indication enabled, and the list of services
	 * has changed (e.g. using the DFU), the indication is received few hundred milliseconds later,
	 * depending on the connection interval.
	 * When received, Android will start performing a service discovery operation, internally,
	 * and will NOT notify the app that services has changed.
	 * <p>
	 * If the gatt.discoverServices() method would be invoked here with no delay, if would return
	 * cached services, as the SC indication wouldn't be received yet. Therefore, we have to
	 * postpone the service discovery operation until we are (almost, as there is no such callback)
	 * sure, that it has been handled. It should be greater than the time from
	 * LLCP Feature Exchange to ATT Write for Service Change indication.
	 * <p>
	 * If your device does not use Service Change indication (for example does not have DFU)
	 * the delay may be 0.
	 * <p>
	 * Please calculate the proper delay that will work in your solution.
	 * <p>
	 * For devices that are not bonded, but support pairing, a small delay is required on some
	 * older Android versions (Nexus 4 with Android 5.1.1) when the device will send pairing
	 * request just after connection. If so, we want to wait with the service discovery until
	 * bonding is complete.
	 * <p>
	 * The default this implementation returns 1600 ms for bonded and 300 ms when the device is not
	 * bonded to be compatible with older versions of the library.
	 */
	@IntRange(from = 0)
	protected int getServiceDiscoveryDelay(final boolean bonded) {
		return bonded ? 1600 : 300;
	}

	/**
	 * Creates a Connect request that will try to connect to the given Bluetooth LE device.
	 * Call {@link ConnectRequest#enqueue()} or {@link ConnectRequest#await()} in order to execute
	 * the request.
	 * <p>
	 * This method returns a {@link ConnectRequest} which can be used to set completion
	 * and failure callbacks. The completion callback (done) will be called after the initialization
	 * is complete, after {@link BleManagerCallbacks#onDeviceReady(BluetoothDevice)} has been
	 * called.
	 * <p>
	 * Calling {@link ConnectRequest#await()} will make this request
	 * synchronous (the callbacks set will be ignored, instead the synchronous method will
	 * return or throw an exception).
	 * <p>
	 * For asynchronous call usage, {@link ConnectRequest#enqueue()} must be called on the returned
	 * request.
	 *
	 * @param device a device to connect to.
	 * @return The connect request.
	 */
	@NonNull
	public final ConnectRequest connect(@NonNull final BluetoothDevice device) {
		return Request.connect(device)
				.useAutoConnect(shouldAutoConnect())
				.setRequestHandler(requestHandler);
	}

	/**
	 * Creates a Connect request that will try to connect to the given Bluetooth LE device using
	 * preferred PHY. Call {@link ConnectRequest#enqueue()} or {@link ConnectRequest#await()}
	 * in order to execute the request.
	 * <p>
	 * This method returns a {@link ConnectRequest} which can be used to set completion
	 * and failure callbacks. The completion callback will be called after the initialization
	 * is complete, after {@link BleManagerCallbacks#onDeviceReady(BluetoothDevice)} has been
	 * called.
	 * <p>
	 * Calling {@link ConnectRequest#await()} will make this request
	 * synchronous (the callbacks set will be ignored, instead the synchronous method will
	 * return or throw an exception).
	 * <p>
	 * For asynchronous call usage, {@link ConnectRequest#enqueue()} must be called on the returned
	 * request.
	 *
	 * @param device a device to connect to.
	 * @param phy    preferred PHY for connections to remote LE device. Bitwise OR of any of
	 *               {@link PhyRequest#PHY_LE_1M_MASK}, {@link PhyRequest#PHY_LE_2M_MASK},
	 *               and {@link PhyRequest#PHY_LE_CODED_MASK}. This option does not take effect
	 *               if {@code autoConnect} is set to true. PHY 2M and Coded are supported
	 *               on newer devices running Android Oreo or newer.
	 * @return The connect request.
	 * @deprecated Use {@link #connect(BluetoothDevice)} instead and set preferred PHY using
	 * {@link ConnectRequest#usePreferredPhy(int)}.
	 */
	@NonNull
	@Deprecated
	public final ConnectRequest connect(@NonNull final BluetoothDevice device, @PhyMask final int phy) {
		return Request.connect(device)
				.usePreferredPhy(phy)
				.useAutoConnect(shouldAutoConnect())
				.setRequestHandler(requestHandler);
	}

	/**
	 * Disconnects from the device or cancels the pending connection attempt.
	 * Does nothing if device was not connected.
	 *
	 * @return The disconnect request. The completion callback will be called after the device
	 * has disconnected and the connection was closed. If the device was not connected,
	 * the completion callback will be called immediately with device parameter set to null.
	 */
	@NonNull
	public final DisconnectRequest disconnect() {
		return Request.disconnect().setRequestHandler(requestHandler);
	}

	/**
	 * "Server only" alternative to using {@link #connect(BluetoothDevice)} in
	 * {@link no.nordicsemi.android.ble.observer.ServerObserver#onDeviceConnectedToServer(BluetoothDevice) onDeviceConnectedToServer}.
	 * This simply associates the connection to the passed client.
	 */
	public void attachClientConnection(BluetoothDevice client) {
		requestHandler.attachClientConnection(client);
	}

	/**
	 * Returns a request to create bond with the device. The device must be first set using
	 * {@link #connect(BluetoothDevice)} which will try to connect to the device.
	 * If you need to pair with a device before connecting to it you may do it without
	 * the use of BleManager object and connect after bond is established.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 * <p>
	 * <b>Important:</b> This request does NOT guarantee that the link will be encrypted.
	 * If the bond information is present on the phone, but was removed from the peripheral
	 * (or another peripheral is pretending to be the one) this request will succeed, as it
	 * immediately returns if the bond information is present on the Android client.
	 * To make sure no sensitive information is stolen, protect your characteristics and/or
	 * descriptors by assigning them security level. Also, clearly inform user that a device
	 * is being bonded to avoid MITM.
	 *
	 * @return The request.
	 * @deprecated Use {@link #createBondInsecure()} or {@link #ensureBond()} instead.
	 * Deprecated in 2.2.1.
	 * @see #ensureBond()
	 * @see #createBondInsecure()
	 */
	@Deprecated
	@NonNull
	@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
	protected Request createBond() {
		return createBondInsecure();
	}

	/**
	 * Returns a request to create bond with the device. The device must be first set using
	 * {@link #connect(BluetoothDevice)} which will try to connect to the device.
	 * If you need to pair with a device before connecting to it you may do it without
	 * the use of BleManager object and connect after bond is established.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 * <p>
	 * <b>Important:</b> This request does NOT guarantee that the link will be encrypted.
	 * If the bond information is present on the phone, but was removed from the peripheral
	 * (or another peripheral is pretending to be the one) this request will succeed, as it
	 * immediately returns if the bond information is present on the Android client.
	 * To make sure no sensitive information is stolen, protect your characteristics and/or
	 * descriptors by assigning them security level. Also, clearly inform user which device
	 * is being bonded to avoid MITM.
	 * <p>To ensure link encryption, use {@link #ensureBond()}.
	 *
	 * @return The request.
	 * @since 2.2.1
	 * @see #ensureBond()
	 */
	@NonNull
	@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
	protected Request createBondInsecure() {
		return Request.createBond().setRequestHandler(requestHandler);
	}

	/**
	 * Returns a request to ensure the device is bonded and link is encrypted. On Android versions
	 * 4.3-12 (and perhaps later as well) the {@link BluetoothDevice#getBondState()} returns true
	 * even if the link is not encrypted, or the device is not connected at all, checking only
	 * if the bond information is present on Android. Moreover, calling
	 * {@link BluetoothDevice#createBond()} returns false if bond is already present on Android,
	 * despite not being used, giving no trustworthy method to ensure that link is encrypted.
	 * <p>
	 * This method will always call {@link BluetoothDevice#createBond()}. If this method returns
	 * false (e.g. because the bond information is already present on Android), this will remove the
	 * current bond information and call {@link BluetoothDevice#createBond()} again.
	 * <p>
	 * <b>Important:</b> This may fail, if:
	 * <ul>
	 *     <li>The device already has bonding, but encryption wasn't started, and your device
	 *     does not support multiple bondings.</li>
	 *     <li>Someone is pretending to be your device, and advertising with the same MAC. Calling
	 *     this method may remove the valid bond and create a new one against the intruder. Always
	 *     make sure the user bonds to the right devices and indicate it to the user.</li>
	 * </ul>
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @return The request.
	 * @since 2.2.3
	 * @noinspection MismatchedJavadocCode
	 */
	@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
	protected Request ensureBond() {
		return Request.ensureBond().setRequestHandler(requestHandler);
	}

	/**
	 * Enqueues removing bond information. When the device was bonded and the bond
	 * information was successfully removed, the device will disconnect.
	 * Note, that this will not remove the bond information from the connected device!
	 * <p>
	 * The success callback will be called after the device get disconnected,
	 * when the {@link BluetoothDevice#getBondState()} changes to {@link BluetoothDevice#BOND_NONE}.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @return The request.
	 */
	@NonNull
	@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
	protected Request removeBond() {
		return Request.removeBond().setRequestHandler(requestHandler);
	}

	/**
	 * Returns the callback that is registered for value changes (notifications) of given
	 * characteristic. After assigning the notifications callback, notifications must be
	 * enabled using {@link #enableNotifications(BluetoothGattCharacteristic)}.
	 * This applies also when they were already enabled on the remote side.
	 * <p>
	 * To remove the callback, call
	 * {@link #removeNotificationCallback(BluetoothGattCharacteristic)}.
	 *
	 * @param characteristic characteristic to bind the callback with. If null, the returned
	 *                       callback will not be null, but will not be used.
	 * @return The callback.
	 */
	@NonNull
	protected ValueChangedCallback setNotificationCallback(@Nullable final BluetoothGattCharacteristic characteristic) {
		return requestHandler.getValueChangedCallback(characteristic);
	}

	/**
	 * Returns the callback that is registered for value changes (indications) of given
	 * characteristic. After assigning the indication callback, indications must be
	 * enabled using {@link #enableIndications(BluetoothGattCharacteristic)}.
	 * This applies also when they were already enabled on the remote side.
	 * <p>
	 * To remove the callback, call
	 * {@link #removeIndicationCallback(BluetoothGattCharacteristic)}.
	 *
	 * @param characteristic characteristic to bind the callback with. If null, the returned
	 *                       callback will not be null, but will not be used.
	 * @return The callback.
	 */
	@NonNull
	protected ValueChangedCallback setIndicationCallback(@Nullable final BluetoothGattCharacteristic characteristic) {
		return setNotificationCallback(characteristic);
	}

	/**
	 * Returns the callback that is registered for value changes (write command or write request
	 * initiated by the remote device) of given characteristic.
	 * <p>
	 * To remove the callback, call
	 * {@link #removeWriteCallback(BluetoothGattCharacteristic)}.
	 *
	 * @param serverCharacteristic characteristic to bind the callback with. If null, the returned
	 *                       	   callback will not be null, but will not be used.
	 * @return The callback.
	 */
	@NonNull
	protected ValueChangedCallback setWriteCallback(@Nullable final BluetoothGattCharacteristic serverCharacteristic) {
		return requestHandler.getValueChangedCallback(serverCharacteristic);
	}

	/**
	 * Returns the callback that is registered for value changes (write command or write request
	 * initiated by the remote device) of given descriptor.
	 * <p>
	 * To remove the callback, call
	 * {@link #removeWriteCallback(BluetoothGattCharacteristic)}.
	 *
	 * @param serverDescriptor descriptor to bind the callback with. If null, the returned
	 *                         callback will not be null, but will not be used.
	 * @return The callback.
	 */
	@NonNull
	protected ValueChangedCallback setWriteCallback(@Nullable final BluetoothGattDescriptor serverDescriptor) {
		return requestHandler.getValueChangedCallback(serverDescriptor);
	}

	/**
	 * Removes the notifications callback set using
	 * {@link #setNotificationCallback(BluetoothGattCharacteristic)}.
	 *
	 * @param characteristic characteristic to unbind the callback from.
	 */
	protected void removeNotificationCallback(@Nullable final BluetoothGattCharacteristic characteristic) {
		requestHandler.removeValueChangedCallback(characteristic);
	}

	/**
	 * Removes the indications callback set using
	 * {@link #setIndicationCallback(BluetoothGattCharacteristic)}.
	 *
	 * @param characteristic characteristic to unbind the callback from.
	 */
	protected void removeIndicationCallback(@Nullable final BluetoothGattCharacteristic characteristic) {
		removeNotificationCallback(characteristic);
	}

	/**
	 * Removes the write callback set using
	 * {@link #setWriteCallback(BluetoothGattCharacteristic)}.
	 *
	 * @param serverCharacteristic characteristic to unbind the callback from.
	 */
	protected void removeWriteCallback(@Nullable final BluetoothGattCharacteristic serverCharacteristic) {
		requestHandler.removeValueChangedCallback(serverCharacteristic);
	}

	/**
	 * Removes the write callback set using
	 * {@link #setWriteCallback(BluetoothGattCharacteristic)}.
	 *
	 * @param serverDescriptor descriptor to unbind the callback from.
	 */
	protected void removeWriteCallback(@Nullable final BluetoothGattDescriptor serverDescriptor) {
		requestHandler.removeValueChangedCallback(serverDescriptor);
	}

	/**
	 * Sets a one-time callback that will be notified when the value of the given characteristic
	 * changes. This is a blocking request, so the next request will be executed after the
	 * notification was received.
	 * <p>
	 * If {@link WaitForValueChangedRequest#merge(DataMerger)} was used, the whole message will be
	 * completed before the callback is notified.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic that value is expect to change.
	 * @return The request.
	 */
	@NonNull
	protected WaitForValueChangedRequest waitForNotification(@Nullable final BluetoothGattCharacteristic characteristic) {
		return Request.newWaitForNotificationRequest(characteristic)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets a one-time callback that will be notified when the value of the given characteristic
	 * changes. This is a blocking request, so the next request will be executed after the
	 * indication was received.
	 * <p>
	 * If {@link WaitForValueChangedRequest#merge(DataMerger)} was used, the whole message will be
	 * completed before the callback is notified.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic that value is expect to change.
	 * @return The request.
	 */
	@NonNull
	protected WaitForValueChangedRequest waitForIndication(@Nullable final BluetoothGattCharacteristic characteristic) {
		return Request.newWaitForIndicationRequest(characteristic)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets a one-time callback that will be notified when the value of the given characteristic
	 * changes. This is a blocking request, so the next request will be executed after the
	 * write command or write request was received.
	 * <p>
	 * If {@link WaitForValueChangedRequest#merge(DataMerger)} was used, the whole message will be
	 * completed before the callback is notified.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the characteristic that is expected to be written.
	 * @return The request.
	 */
	@NonNull
	protected WaitForValueChangedRequest waitForWrite(@Nullable final BluetoothGattCharacteristic serverCharacteristic) {
		return Request.newWaitForWriteRequest(serverCharacteristic)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets a one-time callback that will be notified when the value of the given descriptor
	 * changes. This is a blocking request, so the next request will be executed after the
	 * write command or write request was received.
	 * <p>
	 * If {@link WaitForValueChangedRequest#merge(DataMerger)} was used, the whole message will be
	 * completed before the callback is notified.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverDescriptor the descriptor that is expected to be written.
	 * @return The request.
	 */
	@NonNull
	protected WaitForValueChangedRequest waitForWrite(@Nullable final BluetoothGattDescriptor serverDescriptor) {
		return Request.newWaitForWriteRequest(serverDescriptor)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Creates a conditional wait request that will wait if the given condition is satisfied.
	 * The condition is checked when the request is executed and each time a new BLE operation is
	 * complete.
	 *
	 * @param condition The condition to examine. If it's satisfied, the manager will wait.
	 * @return The request.
	 */
	@NonNull
	protected ConditionalWaitRequest<Void> waitIf(@NonNull final ConditionalWaitRequest.Condition<Void> condition) {
		return Request.newConditionalWaitRequest(condition, null)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Creates a conditional wait request that will wait if the given condition is satisfied.
	 * The condition is checked when the request is executed and each time a new BLE operation is
	 * complete.
	 *
	 * @param parameter An optional parameter that will be passed to the condition.
	 * @param condition The condition to examine. If it's satisfied, the manager will wait.
	 * @return The request.
	 */
	@NonNull
	protected <T> ConditionalWaitRequest<T> waitIf(@Nullable final T parameter,
												   @NonNull final ConditionalWaitRequest.Condition<T> condition) {
		return Request.newConditionalWaitRequest(condition, parameter)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Creates a conditional wait request that will wait until the given condition is not satisfied.
	 * The condition is checked when the request is executed and each time a new BLE operation is
	 * complete.
	 *
	 * @param condition The condition to examine. If it's not satisfied, the manager will wait.
	 * @return The request.
	 */
	@NonNull
	protected ConditionalWaitRequest<Void> waitUntil(@NonNull final ConditionalWaitRequest.Condition<Void> condition) {
		return waitIf(condition).negate();
	}

	/**
	 * Creates a conditional wait request that will wait until the given condition is not satisfied.
	 * The condition is checked when the request is executed and each time a new BLE operation is
	 * complete.
	 *
	 * @param parameter An optional parameter that will be passed to the condition.
	 * @param condition The condition to examine. If it's not satisfied, the manager will wait.
	 * @return The request.
	 */
	@NonNull
	protected <T> ConditionalWaitRequest<T> waitUntil(@Nullable final T parameter,
													  @NonNull final ConditionalWaitRequest.Condition<T> condition) {
		return waitIf(parameter, condition).negate();
	}

	/**
	 * Creates a request that will wait for enabling notifications. If notifications were
	 * enabled at the time of executing the request, it will complete immediately.
	 *
	 * @param serverCharacteristic the server characteristic with notify property.
	 * @return The request.
	 */
	@NonNull
	protected ConditionalWaitRequest<BluetoothGattCharacteristic> waitUntilNotificationsEnabled(
			@Nullable final BluetoothGattCharacteristic serverCharacteristic) {
		return waitUntil(serverCharacteristic, (characteristic) -> {
			if (characteristic == null)
				return false;
			final BluetoothGattDescriptor cccd = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
			if (cccd == null)
				return false;
			final byte[] value = requestHandler.getDescriptorValue(cccd);
			return value != null && value.length == 2 && (value[0] & 0x01) == 0x01;
		});
	}

	/**
	 * Creates a request that will wait for enabling indications. If indications were
	 * enabled at the time of executing the request, it will complete immediately.
	 *
	 * @param serverCharacteristic the server characteristic with indicate property.
	 * @return The request.
	 */
	@NonNull
	protected ConditionalWaitRequest<BluetoothGattCharacteristic> waitUntilIndicationsEnabled(
			@Nullable final BluetoothGattCharacteristic serverCharacteristic) {
		return waitUntil(serverCharacteristic, (characteristic) -> {
			if (characteristic == null)
				return false;
			final BluetoothGattDescriptor cccd = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
			if (cccd == null)
				return false;
			final byte[] value = requestHandler.getDescriptorValue(cccd);
			return value != null && value.length == 2 && (value[0] & 0x02) == 0x02;
		});
	}

	/**
	 * Waits until the given characteristic is read by the remote device. The data must have been set
	 * to the characteristic before the request is executed.
	 * Use {@link #setCharacteristicValue(BluetoothGattCharacteristic, Data)} to set data,
	 * {@link #waitForRead(BluetoothGattCharacteristic, byte[], int, int)} to set the data immediately,
	 * or {@link #setCharacteristicValue(BluetoothGattCharacteristic, DataProvider)} to set the
	 * data on demand.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the target characteristic to be read.
	 * @return The request.
	 */
	@NonNull
	protected WaitForReadRequest waitForRead(@Nullable final BluetoothGattCharacteristic serverCharacteristic) {
		return Request.newWaitForReadRequest(serverCharacteristic)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets the given data to the readable server characteristic and waits until they are read by the
	 * remote device.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the target characteristic to be read.
	 * @param data                 the data to be sent as read response.
	 * @return The request.
	 */
	@NonNull
	protected WaitForReadRequest waitForRead(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
											 @Nullable final byte[] data) {
		return Request.newWaitForReadRequest(serverCharacteristic, data)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets the given data to the readable server characteristic and waits until they are read by the
	 * remote device.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the target characteristic to be read.
	 * @param data                 the data buffer.
	 * @param offset               index of the first byte to be returned.
	 * @param length               number of bytes to be returned.
	 * @return The request.
	 */
	@NonNull
	protected WaitForReadRequest waitForRead(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
											 @Nullable final byte[] data, final int offset, final int length) {
		return Request.newWaitForReadRequest(serverCharacteristic, data, offset, length)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Waits until the given descriptor is read by the remote device. The data must have been set
	 * to the descriptor before the request is executed.
	 * Use {@link #setDescriptorValue(BluetoothGattDescriptor, byte[])} to set data,
	 * {@link #waitForRead(BluetoothGattDescriptor, byte[], int, int)} to set the data immediately,
	 * or {@link #setDescriptorValue(BluetoothGattDescriptor, DataProvider)} to set the
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverDescriptor the target descriptor to be read.
	 * @return The request.
	 */
	@NonNull
	protected WaitForReadRequest waitForRead(@Nullable final BluetoothGattDescriptor serverDescriptor) {
		return Request.newWaitForReadRequest(serverDescriptor)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets the given data to the readable server descriptor and waits until they are read by the
	 * remote device.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverDescriptor the target descriptor to be read.
	 * @param data             the data to be sent as read response.
	 * @return The request.
	 */
	@NonNull
	protected WaitForReadRequest waitForRead(@Nullable final BluetoothGattDescriptor serverDescriptor,
											 @Nullable final byte[] data) {
		return Request.newWaitForReadRequest(serverDescriptor, data)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets the given data to the readable server descriptor and waits until they are read by the
	 * remote device.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverDescriptor the target descriptor to be read.
	 * @param data             the data buffer.
	 * @param offset           index of the first byte to be returned.
	 * @param length           number of bytes to be returned.
	 * @return The request.
	 */
	@NonNull
	protected WaitForReadRequest waitForRead(@Nullable final BluetoothGattDescriptor serverDescriptor,
											 @Nullable final byte[] data, final int offset, final int length) {
		return Request.newWaitForReadRequest(serverDescriptor, data, offset, length)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets the data provider to the given readable server characteristic.
	 * <p>
	 * The provider will be called when the remote device sends a read command to the given
	 * characteristic. This allows returning current value without the need to update the value
	 * periodically.
	 * <p>
	 * If the provider is not set, the value set using
	 * {@link #setCharacteristicValue(BluetoothGattCharacteristic, Data)} will be returned.
	 *
	 * @param serverCharacteristic the target characteristic to provide data for.
	 * @param provider the data provider for the given characteristic.
	 */
	protected void setCharacteristicValue(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
										  @Nullable final DataProvider provider) {
		requestHandler.setCharacteristicValue(serverCharacteristic, provider);
	}

	/**
	 * Sets the given data to the readable server characteristic.The data will be available to be
	 * read by the remote device.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the target characteristic to set value.
	 * @param data                 data to be set.
	 * @return The request.
	 */
	@NonNull
	protected SetValueRequest setCharacteristicValue(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
													 @Nullable final Data data) {
		return Request.newSetValueRequest(serverCharacteristic, data != null ? data.getValue() : null)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets the given data to the readable server characteristic. The data will be available to be
	 * read by the remote device.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the target characteristic to set value.
	 * @param data                 data to be set.
	 * @return The request.
	 */
	@NonNull
	protected SetValueRequest setCharacteristicValue(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
													 @Nullable final byte[] data) {
		return Request.newSetValueRequest(serverCharacteristic, data)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets the given data to the readable server characteristic. The data will be available to be
	 * read by the remote device until the device writes a new value, or
	 * {@link #sendNotification(BluetoothGattCharacteristic, byte[])} or #sendIn
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the target characteristic to set value.
	 * @param data                 data to be set.
	 * @param offset               index of the first byte to be set.
	 * @param length               number of bytes to be set.
	 * @return The request.
	 */
	@NonNull
	protected SetValueRequest setCharacteristicValue(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
													 @Nullable final byte[] data, final int offset, final int length) {
		return Request.newSetValueRequest(serverCharacteristic, data, offset, length)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets the data provider to the given readable server descriptor.
	 * <p>
	 * The provider will be called when the remote device sends a read command to the given
	 * descriptor. This allows returning current value without the need to update the value
	 * periodically.
	 * <p>
	 * If the provider is not set, the value set using
	 * {@link #setDescriptorValue(BluetoothGattDescriptor, Data)} will be returned.
	 *
	 * @param serverDescriptor the target descriptor to provide data for.
	 * @param provider the data provider for the given characteristic.
	 */
	protected void setDescriptorValue(@Nullable final BluetoothGattDescriptor serverDescriptor,
									  @Nullable final DataProvider provider) {
		requestHandler.setDescriptorValue(serverDescriptor, provider);
	}

	/**
	 * Sets the given data to the readable server descriptor. The data will be available to be
	 * read by the remote device.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverDescriptor the target descriptor to set value.
	 * @param data             data to be set.
	 * @return The request.
	 */
	@NonNull
	protected SetValueRequest setDescriptorValue(@Nullable final BluetoothGattDescriptor serverDescriptor,
												 @Nullable final Data data) {
		return Request.newSetValueRequest(serverDescriptor, data != null ? data.getValue() : null)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets the given data to the readable server descriptor. The data will be available to be
	 * read by the remote device.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverDescriptor the target descriptor to set value.
	 * @param data             data to be set.
	 * @return The request.
	 */
	@NonNull
	protected SetValueRequest setDescriptorValue(@Nullable final BluetoothGattDescriptor serverDescriptor,
												 @Nullable final byte[] data) {
		return Request.newSetValueRequest(serverDescriptor, data)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets the given data to the readable server descriptor. The data will be available to be
	 * read by the remote device.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverDescriptor the target descriptor to set value.
	 * @param data             data to be set.
	 * @param offset           index of the first byte to be set.
	 * @param length           number of bytes to be set.
	 * @return The request.
	 */
	@NonNull
	protected SetValueRequest setDescriptorValue(@Nullable final BluetoothGattDescriptor serverDescriptor,
												 @Nullable final byte[] data, final int offset, final int length) {
		return Request.newSetValueRequest(serverDescriptor, data, offset, length)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Enables notifications on given characteristic.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to be enabled.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest enableNotifications(
			@Nullable final BluetoothGattCharacteristic characteristic) {
		return Request.newEnableNotificationsRequest(characteristic)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Disables notifications on given characteristic.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to be disabled.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest disableNotifications(@Nullable final BluetoothGattCharacteristic characteristic) {
		return Request.newDisableNotificationsRequest(characteristic)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Enables indications on given characteristic.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to be enabled.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest enableIndications(@Nullable final BluetoothGattCharacteristic characteristic) {
		return Request.newEnableIndicationsRequest(characteristic)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Disables indications on given characteristic.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to be disabled.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest disableIndications(@Nullable final BluetoothGattCharacteristic characteristic) {
		return Request.newDisableIndicationsRequest(characteristic)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sends the read request to the given characteristic.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to read.
	 * @return The request.
	 */
	@NonNull
	protected ReadRequest readCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic) {
		return Request.newReadRequest(characteristic)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Writes the given data to the characteristic.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(DataSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to write to.
	 * @param data           data to be written to the characteristic.
	 * @param writeType      the write type which is to be used.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest writeCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic,
											   @Nullable final Data data,
											   @WriteType final int writeType) {
		return Request.newWriteRequest(characteristic, data != null ? data.getValue() : null, writeType)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Writes the given data to the characteristic.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to write to.
	 * @param data           data to be written to the characteristic.
	 * @param writeType      the write type which is to be used.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest writeCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic,
											   @Nullable final byte[] data,
											   @WriteType final int writeType) {
		return Request.newWriteRequest(characteristic, data, writeType)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Writes at most length bytes from offset at given data to the characteristic.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to write to.
	 * @param data           data to be written to the characteristic.
	 * @param offset         index of the first byte to be sent.
	 * @param length         number of bytes to be sent.
	 * @param writeType      the write type which is to be used.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest writeCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic,
											   @Nullable final byte[] data, final int offset, final int length,
											   @WriteType final int writeType) {
		return Request.newWriteRequest(characteristic, data, offset, length, writeType)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Writes the given data to the characteristic. The write type is taken from the characteristic.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(DataSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to write to.
	 * @param data           data to be written to the characteristic.
	 * @return The request.
	 * @deprecated Use {@link #writeCharacteristic(BluetoothGattCharacteristic, Data, int)} instead.
	 */
	@Deprecated
	@NonNull
	protected WriteRequest writeCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic,
											   @Nullable final Data data) {
		return Request.newWriteRequest(characteristic, data != null ? data.getValue() : null)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Writes the given data to the characteristic. The write type is taken from the characteristic.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to write to.
	 * @param data           data to be written to the characteristic.
	 * @return The request.
	 * @deprecated Use {@link #writeCharacteristic(BluetoothGattCharacteristic, byte[], int)} instead.
	 */
	@Deprecated
	@NonNull
	protected WriteRequest writeCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic,
											   @Nullable final byte[] data) {
		return Request.newWriteRequest(characteristic, data)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Writes at most length bytes from offset at given data to the characteristic.
	 * The write type is taken from the characteristic.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param characteristic the characteristic to write to.
	 * @param data           data to be written to the characteristic.
	 * @param offset         index of the first byte to be sent.
	 * @param length         number of bytes to be sent.
	 * @return The request.
	 * @deprecated Use {@link #writeCharacteristic(BluetoothGattCharacteristic, byte[], int, int, int)} instead.
	 */
	@Deprecated
	@NonNull
	protected WriteRequest writeCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic,
											   @Nullable final byte[] data, final int offset, final int length) {
		return Request.newWriteRequest(characteristic, data, offset, length)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sends the read request to the given descriptor.
	 * If the descriptor is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param descriptor the descriptor to read.
	 * @return The request.
	 */
	@NonNull
	protected ReadRequest readDescriptor(@Nullable final BluetoothGattDescriptor descriptor) {
		return Request.newReadRequest(descriptor)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Writes the given data to the descriptor.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the descriptor is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param descriptor the descriptor to write to.
	 * @param data       data to be written to the descriptor.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest writeDescriptor(@Nullable final BluetoothGattDescriptor descriptor,
										   @Nullable final Data data) {
		return Request.newWriteRequest(descriptor, data != null ? data.getValue() : null)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Writes the given data to the descriptor.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the descriptor is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param descriptor the descriptor to write to.
	 * @param data       data to be written to the descriptor.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest writeDescriptor(@Nullable final BluetoothGattDescriptor descriptor,
										   @Nullable final byte[] data) {
		return Request.newWriteRequest(descriptor, data)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Writes at most length bytes from offset at given data to the descriptor.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the descriptor is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param descriptor the descriptor to write to.
	 * @param data       data to be written to the descriptor.
	 * @param offset     index of the first byte to be sent.
	 * @param length     number of bytes to be sent.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest writeDescriptor(@Nullable final BluetoothGattDescriptor descriptor,
										   @Nullable final byte[] data, final int offset,
										   final int length) {
		return Request.newWriteRequest(descriptor, data, offset, length)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sends the notification from the server characteristic. The notifications on this
	 * characteristic must be enabled before the request is executed.
	 *
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the characteristic to notify.
	 * @param data           	   data to be sent to the characteristic.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest sendNotification(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
											@Nullable final Data data) {
		return Request.newNotificationRequest(serverCharacteristic, data != null ? data.getValue() : null)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sends the notification from the server characteristic. The notifications on this
	 * characteristic must be enabled before the request is executed.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the characteristic to notify.
	 * @param data           	   data to be sent to the characteristic.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest sendNotification(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
											@Nullable final byte[] data) {
		return Request.newNotificationRequest(serverCharacteristic, data)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sends the notification with at most length bytes from offset at given data from the server
	 * characteristic. The notifications on this characteristic must be enabled before the request
	 * is executed.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the characteristic to notify.
	 * @param data           	   the data buffer.
	 * @param offset         	   index of the first byte to be sent.
	 * @param length               number of bytes to be sent.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest sendNotification(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
											@Nullable final byte[] data, final int offset, final int length) {
		return Request.newNotificationRequest(serverCharacteristic, data, offset, length)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sends the indication from the server characteristic. The indications on this characteristic
	 * must be enabled before the request is executed.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the characteristic to indicate.
	 * @param data           	   data to be sent.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest sendIndication(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
										  @Nullable final Data data) {
		return Request.newIndicationRequest(serverCharacteristic, data != null ? data.getValue() : null)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sends the indication from the server characteristic. The indications on this characteristic
	 * must be enabled before the request is executed.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the characteristic to indicate.
	 * @param data           	   data to be sent.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest sendIndication(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
										  @Nullable final byte[] data) {
		return Request.newIndicationRequest(serverCharacteristic, data)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sends the indication with at most length bytes from offset at given data from the server
	 * characteristic. The indications on this characteristic must be enabled before the request
	 * is executed.
	 * <p>
	 * Use {@link WriteRequest#split() split()} or
	 * {@link WriteRequest#split(DataSplitter) split(ValueSplitter)} on the returned
	 * {@link WriteRequest} if data should be automatically split into multiple packets.
	 * If the characteristic is null, the {@link Request#fail(FailCallback) fail(FailCallback)}
	 * callback will be called.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param serverCharacteristic the characteristic to indicate.
	 * @param data           	   the data buffer.
	 * @param offset         	   index of the first byte to be sent.
	 * @param length               number of bytes to be sent.
	 * @return The request.
	 */
	@NonNull
	protected WriteRequest sendIndication(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
										  @Nullable final byte[] data, final int offset, final int length) {
		return Request.newIndicationRequest(serverCharacteristic, data, offset, length)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Creates an atomic request queue. The requests from the queue will be executed in order.
	 * This is useful when more then one thread may add requests and you want some of them to
	 * be executed together.
	 *
	 * @return The request.
	 */
	@NonNull
	protected RequestQueue beginAtomicRequestQueue() {
		return new RequestQueue().setRequestHandler(requestHandler);
	}

	/**
	 * Begins the Reliable Write sub-procedure. Requests that need to be performed reliably
	 * should be enqueued with {@link ReliableWriteRequest#add(Operation)} instead of using
	 * {@link Request#enqueue()}. The library will verify all Write operations and will
	 * automatically abort the Reliable Write procedure when the returned data mismatch with the
	 * data sent. When all requests enqueued in the {@link ReliableWriteRequest} were completed,
	 * the Reliable Write will be automatically executed.
	 * <p>
	 * Long Write will not work when Reliable Write is in progress. The library will make sure
	 * that {@link WriteRequest#split()} was called for all {@link WriteRequest} packets, had
	 * they not been assigned other splitter.
	 * <p>
	 * At least one Write operation must be executed before executing or aborting, otherwise the
	 * {@link GattError#GATT_INVALID_OFFSET} error will be reported. Because of that, enqueueing
	 * a {@link ReliableWriteRequest} without any operations does nothing.
	 * <p>
	 * Example of usage:
	 * <pre>
	 *     beginReliableWrite()
	 *           .add(writeCharacteristic(someCharacteristic, someData)
	 *                   .fail(...)
	 *                   .done(...))
	 *           // Non-write requests are also possible
	 *           .add(requestMtu(200))
	 *           // Data will be written in the same order
	 *           .add(writeCharacteristic(someCharacteristic, differentData))
	 *           // This will return the OLD data, not 'differentData', as the RW wasn't executed!
	 *           .add(readCharacteristic(someCharacteristic).with(callback))
	 *           // Multiple characteristics may be written during a single RW
	 *           .add(writeCharacteristic(someOtherCharacteristic, importantData))
	 *        // Finally, enqueue the Reliable Write request in BleManager
	 *     	  .enqueue();
	 * </pre>
	 *
	 * @return The request.
	 */
	@NonNull
	protected ReliableWriteRequest beginReliableWrite() {
		return Request.newReliableWriteRequest()
				.setRequestHandler(requestHandler);
	}

	/**
	 * Returns true if {@link BluetoothGatt#beginReliableWrite()} has been called and
	 * the Reliable Write hasn't been executed nor aborted yet.
	 */
	protected final boolean isReliableWriteInProgress() {
		return requestHandler.isReliableWriteInProgress();
	}

	/**
	 * Reads the battery level from the device.
	 *
	 * @deprecated Use {@link #readCharacteristic(BluetoothGattCharacteristic)} instead.
	 */
	@Deprecated
	protected void readBatteryLevel() {
		Request.newReadBatteryLevelRequest()
				.setRequestHandler(requestHandler)
				.with(requestHandler.getBatteryLevelCallback())
				.enqueue();
	}

	/**
	 * This method enables notifications on the Battery Level characteristic.
	 *
	 * @deprecated Use {@link #setNotificationCallback(BluetoothGattCharacteristic)} and
	 * {@link #enableNotifications(BluetoothGattCharacteristic)} instead.
	 */
	@Deprecated
	protected void enableBatteryLevelNotifications() {
		Request.newEnableBatteryLevelNotificationsRequest()
				.setRequestHandler(requestHandler)
				.before(device -> requestHandler.setBatteryLevelNotificationCallback())
				.done(device -> log(Log.INFO, "Battery Level notifications enabled"))
				.enqueue();
	}

	/**
	 * This method disables notifications on the Battery Level characteristic.
	 *
	 * @deprecated Use {@link #disableNotifications(BluetoothGattCharacteristic)} instead.
	 */
	@Deprecated
	protected void disableBatteryLevelNotifications() {
		Request.newDisableBatteryLevelNotificationsRequest()
				.setRequestHandler(requestHandler)
				.done(device -> log(Log.INFO, "Battery Level notifications disabled"))
				.enqueue();
	}

	/**
	 * Requests new MTU. On Android Lollipop or newer it will send the MTU request to the connected
	 * device. On older versions of Android the
	 * {@link MtuCallback#onMtuChanged(BluetoothDevice, int)} set with
	 * {@link MtuRequest#with(MtuCallback)} will be called with current MTU value.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @return The request.
	 */
	protected MtuRequest requestMtu(@IntRange(from = 23, to = 517) final int mtu) {
		return Request.newMtuRequest(mtu).setRequestHandler(requestHandler);
	}

	/**
	 * Returns the current MTU (Maximum Transfer Unit). MTU specifies the maximum number of bytes
	 * that can be sent in a single write operation. 3 bytes are used for internal purposes,
	 * so the maximum size is MTU-3. The value will changed only if requested with
	 * {@link #requestMtu(int)} and a successful callback is received. If the peripheral requests
	 * MTU change, the {@link BluetoothGattCallback#onMtuChanged(BluetoothGatt, int, int)}
	 * callback is not invoked, therefor the returned MTU value will not be correct.
	 * Use {@link android.bluetooth.BluetoothGattServerCallback#onMtuChanged(BluetoothDevice, int)}
	 * to get the callback with right value requested from the peripheral side.
	 *
	 * @return the current MTU value. Default to 23.
	 */
	@IntRange(from = 23, to = 517)
	protected int getMtu() {
		return requestHandler.getMtu();
	}

	/**
	 * This method overrides the MTU value. Use it only when the peripheral has changed MTU and you
	 * received the
	 * {@link android.bluetooth.BluetoothGattServerCallback#onMtuChanged(BluetoothDevice, int)}
	 * callback. If you want to set MTU as a master, use {@link #requestMtu(int)} instead.
	 *
	 * @param mtu the MTU value set by the peripheral.
	 */
	protected void overrideMtu(@IntRange(from = 23, to = 517) final int mtu) {
		requestHandler.overrideMtu(mtu);
	}

	/**
	 * Requests the new connection priority. Acceptable values are:
	 * <ol>
	 * <li>{@link BluetoothGatt#CONNECTION_PRIORITY_HIGH}
	 * - Interval: 11.25 -15 ms, latency: 0, supervision timeout: 20 sec,</li>
	 * <li>{@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED}
	 * - Interval: 30 - 50 ms, latency: 0, supervision timeout: 20 sec,</li>
	 * <li>{@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}
	 * - Interval: 100 - 125 ms, latency: 2, supervision timeout: 20 sec.</li>
	 * </ol>
	 * Works only on Android Lollipop or newer. On older system versions will cause
	 * {@link Request#fail(FailCallback)} callback or throw
	 * {@link no.nordicsemi.android.ble.exception.RequestFailedException} with
	 * {@link FailCallback#REASON_REQUEST_FAILED} status if called synchronously.
	 * Starting from Android Oreo you may get a callback with the interval, latency and timeout
	 * using {@link ConnectionPriorityRequest#with(ConnectionPriorityCallback)}.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param priority one of: {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH},
	 *                 {@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED},
	 *                 {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.
	 * @return The request.
	 */
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	protected ConnectionPriorityRequest requestConnectionPriority(
			@ConnectionPriority final int priority) {
		return Request.newConnectionPriorityRequest(priority)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Sets connection priority listener.
	 * <p>
	 * This method is only available from Android Oreo, which has added hidden
	 * <a href="https://cs.android.com/android/_/android/platform/packages/modules/Bluetooth/+/b651de2c347368d04dd313f61f719c2f5ae1b92e">onConnectionUpdated</a>
	 * callback to {@link BluetoothGattCallback}.
	 *
	 * @param callback the callback, that will receive all connection parameters updates.
	 * @since 2.5.0
	 */
	@RequiresApi(api = Build.VERSION_CODES.O)
	protected void setConnectionParametersListener(@Nullable final ConnectionParametersUpdatedCallback callback) {
		requestHandler.setConnectionParametersListener(callback);
	}

	/**
	 * Enqueues a request to set the preferred PHY.
	 * <p>
	 * PHY LE 2M and PHY LE Coded are supported only on Android Oreo or newer.
	 * You may safely request other PHYs on older platforms, but  you will get PHY LE 1M
	 * as TX and RX PHY in the callback.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param txPhy      preferred transmitter PHY. Bitwise OR of any of
	 *                   {@link PhyRequest#PHY_LE_1M_MASK}, {@link PhyRequest#PHY_LE_2M_MASK},
	 *                   and {@link PhyRequest#PHY_LE_CODED_MASK}.
	 * @param rxPhy      preferred receiver PHY. Bitwise OR of any of
	 *                   {@link PhyRequest#PHY_LE_1M_MASK}, {@link PhyRequest#PHY_LE_2M_MASK},
	 *                   and {@link PhyRequest#PHY_LE_CODED_MASK}.
	 * @param phyOptions preferred coding to use when transmitting on the LE Coded PHY. Can be one
	 *                   of {@link PhyRequest#PHY_OPTION_NO_PREFERRED},
	 *                   {@link PhyRequest#PHY_OPTION_S2} or {@link PhyRequest#PHY_OPTION_S8}.
	 * @return The request.
	 */
	protected PhyRequest setPreferredPhy(@PhyMask final int txPhy, @PhyMask final int rxPhy,
										 @PhyOption final int phyOptions) {
		return Request.newSetPreferredPhyRequest(txPhy, rxPhy, phyOptions)
				.setRequestHandler(requestHandler);
	}

	/**
	 * Reads the current PHY for this connections.
	 * <p>
	 * PHY LE 2M and PHY LE Coded are supported only on Android Oreo or newer.
	 * You may safely read PHY on older platforms, but you will get PHY LE 1M as TX and RX PHY
	 * in the callback.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @return The request.
	 */
	protected PhyRequest readPhy() {
		return Request.newReadPhyRequest()
				.setRequestHandler(requestHandler);
	}

	/**
	 * Reads the current RSSI (Received Signal Strength Indication).
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @return The request.
	 */
	protected ReadRssiRequest readRssi() {
		return Request.newReadRssiRequest().setRequestHandler(requestHandler);
	}

	/**
	 * Refreshes the device cache. As the {@link BluetoothGatt#refresh()} method is not in the
	 * public API (it's hidden, and on Android P it is on a light gray list) it is called
	 * using reflections and may be removed in some future Android release or on some devices.
	 * <p>
	 * There is no callback indicating when the cache has been cleared. This library assumes
	 * some time and waits. After the delay, it will start service discovery and clear the
	 * task queue. When the service discovery finishes, the
	 * {@link BleManager#isRequiredServiceSupported(BluetoothGatt)} and
	 * {@link BleManager#isOptionalServiceSupported(BluetoothGatt)} will
	 * be called and the initialization will be performed as if the device has just connected.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @return The request.
	 */
	@SuppressWarnings("JavadocReference")
	protected Request refreshDeviceCache() {
		return Request.newRefreshCacheRequest()
				.setRequestHandler(requestHandler);
	}

	/**
	 * Enqueues a sleep operation with given duration. The next request will be performed after
	 * at least given number of milliseconds.
	 * <p>
	 * The returned request must be either enqueued using {@link Request#enqueue()} for
	 * asynchronous use, or awaited using await() in synchronous execution.
	 *
	 * @param delay the delay in milliseconds.
	 * @return The request.
	 */
	protected SleepRequest sleep(@IntRange(from = 0) final long delay) {
		return Request.newSleepRequest(delay).setRequestHandler(requestHandler);
	}

	/**
	 * Enqueues a new request.
	 *
	 * @param request the new request to be added to the end of the queue.
	 * @deprecated This way of enqueueing requests is deprecated, use above methods instead.
	 */
	@Deprecated
	protected final void enqueue(@NonNull final Request request) {
		requestHandler.enqueue(request);
	}

	/**
	 * Removes all enqueued requests from the queue.
	 * The currently executed request will be cancelled and will fail with status
	 * {@link FailCallback#REASON_CANCELLED}.
	 * <p>
	 * If a BLE operation was in progress when the queue was cancelled, enqueueing a next BLE
	 * operation immediately may cause the Bluetooth to behave improperly, as the manager will
	 * try to execute it without waiting for the {@link BluetoothGattCallback callback}. A delay
	 * in such case is recommended.
	 */
	protected final void cancelQueue() {
		requestHandler.cancelQueue();
	}

	/**
	 * The GATT Callback handler. An object of this class must be returned by
	 * {@link #getGattCallback()}. It is responsible for all GATT operations.
	 */
	protected abstract static class BleManagerGattCallback extends BleManagerHandler {
		// All methods defined in the super class.
	}
}
