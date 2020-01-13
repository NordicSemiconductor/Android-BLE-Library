package no.nordicsemi.android.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import no.nordicsemi.android.ble.annotation.ConnectionPriority;
import no.nordicsemi.android.ble.annotation.ConnectionState;
import no.nordicsemi.android.ble.annotation.PhyMask;
import no.nordicsemi.android.ble.annotation.PhyOption;
import no.nordicsemi.android.ble.annotation.PhyValue;
import no.nordicsemi.android.ble.callback.ConnectionPriorityCallback;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.MtuCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.error.GattError;
import no.nordicsemi.android.ble.utils.ParserUtils;

@SuppressWarnings({"WeakerAccess", "DeprecatedIsStillUsed", "unused", "deprecation"})
abstract class BleManagerHandler extends RequestHandler {
	private final static String TAG = "BleManager";

	private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
	private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
	private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
	private final static String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
	private final static String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
	private final static String ERROR_READ_DESCRIPTOR = "Error on reading descriptor";
	private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
	private final static String ERROR_MTU_REQUEST = "Error on mtu request";
	private final static String ERROR_CONNECTION_PRIORITY_REQUEST = "Error on connection priority request";
	private final static String ERROR_READ_RSSI = "Error on RSSI read";
	private final static String ERROR_READ_PHY = "Error on PHY read";
	private final static String ERROR_PHY_UPDATE = "Error on PHY update";
	private final static String ERROR_RELIABLE_WRITE = "Error on Execute Reliable Write";
	private final static String ERROR_NOTIFY = "Error on sending notification/indication";

	private final Object LOCK = new Object();
	private BluetoothDevice bluetoothDevice;
	private BluetoothGatt bluetoothGatt;
	private BleManager manager;
	private BleServerManager serverManager;
	private Handler handler;

	private final Deque<Request> taskQueue = new LinkedBlockingDeque<>();
	private Deque<Request> initQueue;
	private boolean initInProgress;

	/**
	 * A time after which receiving 133 error is considered a timeout, instead of a
	 * different reason.
	 * A {@link BluetoothDevice#connectGatt(Context, boolean, BluetoothGattCallback)} call will
	 * fail after 30 seconds if the device won't be found until then. Other errors happen much
	 * earlier. 20 sec should be OK here.
	 */
	private final static long CONNECTION_TIMEOUT_THRESHOLD = 20000; // ms
	/**
	 * Flag set when services were discovered.
	 */
	private boolean servicesDiscovered;
	/**
	 * Flag set when service discovery was requested.
	 */
	private boolean serviceDiscoveryRequested;
	/**
	 * A timestamp when the last connection attempt was made. This is distinguish two situations
	 * when the 133 error happens during a connection attempt: a timeout (when ~30 sec passed since
	 * connection was requested), or an error (packet collision, packet missed, etc.)
	 */
	private long connectionTime;
	/**
	 * A temporary counter to prevent requesting service discovery for old connection.
	 */
	private int connectionCount = 0;
	/**
	 * Flag set to true when the device is connected.
	 */
	private boolean connected;
	/**
	 * Flag set to true when the initialization queue is complete.
	 */
	private boolean ready;
	/**
	 * A flag indicating that an operation is currently in progress.
	 */
	private boolean operationInProgress;
	/**
	 * This flag is set to false only when the {@link ConnectRequest#shouldAutoConnect()} method
	 * returns true and the device got disconnected without calling {@link BleManager#disconnect()}
	 * method. If {@link ConnectRequest#shouldAutoConnect()} returns false (default) this is always
	 * set to true.
	 */
	private boolean userDisconnected;
	/**
	 * Flag set to true when {@link ConnectRequest#shouldAutoConnect()} method returned true.
	 * The first connection attempt is done with <code>autoConnect</code> flag set to false
	 * (to make the first connection quick) but on connection lost the manager will call
	 * {@link BleManager#connect(BluetoothDevice)} again. This time this method will call
	 * {@link BluetoothGatt#connect()} which always uses <code>autoConnect</code> equal true.
	 */
	private boolean initialConnection;
	/**
	 * The connection state. One of:
	 * {@link BluetoothGatt#STATE_CONNECTING STATE_CONNECTING},
	 * {@link BluetoothGatt#STATE_CONNECTED STATE_CONNECTED},
	 * {@link BluetoothGatt#STATE_DISCONNECTING STATE_DISCONNECTING},
	 * {@link BluetoothGatt#STATE_DISCONNECTED STATE_DISCONNECTED}
	 */
	@ConnectionState
	private int connectionState = BluetoothGatt.STATE_DISCONNECTED;
	/**
	 * This flag is required to resume operations after the connection priority request was made.
	 * It is used only on Android Oreo and newer, as only there there is onConnectionUpdated
	 * callback. However, as this callback is triggered every time the connection parameters
	 * change, even when such request wasn't made, this flag ensures the nextRequest() method
	 * won't be called during another operation.
	 */
	private boolean connectionPriorityOperationInProgress = false;
	/**
	 * A flag indicating that Reliable Write is in progress.
	 */
	private boolean reliableWriteInProgress;
	/**
	 * The current MTU (Maximum Transfer Unit). The maximum number of bytes that can be sent in
	 * a single packet is MTU-3.
	 */
	private int mtu = 23;
	/**
	 * Last received battery value or -1 if value wasn't received.
	 *
	 * @deprecated Battery value should be kept in the profile manager instead. See BatteryManager
	 * class in Android nRF Toolbox app.
	 */
	@IntRange(from = -1, to = 100)
	@Deprecated
	private int batteryValue = -1;
	/** Values of non-shared characteristics. Each connected device has its own copy of such. */
	private Map<BluetoothGattCharacteristic, byte[]> characteristicValues;
	/** Values of non-shared descriptors. Each connected device has its own copy of such. */
	private Map<BluetoothGattDescriptor, byte[]> descriptorValues;
	/**
	 * Temporary values of characteristic to support Reliable Write. The temp value will be
	 * set as valid when the write request is executed, or discarded when aborted.
	 */
	private Deque<Pair<Object /* BluetoothGattCharacteristic of BluetoothGattDescriptor> */, byte[]>> preparedValues;
	private int prepareError;
	/**
	 * The connect request. This is instantiated in {@link BleManager#connect(BluetoothDevice, int)}
	 * and nullified after the device is ready.
	 * <p>
	 * This request has a separate reference, as it is notified when the device becomes ready,
	 * after the initialization requests are done.
	 */
	private ConnectRequest connectRequest;
	/**
	 * Currently performed request or null in idle state.
	 */
	private Request request;
	/**
	 * Currently performer request set, or null if none.
	 */
	private RequestQueue requestQueue;
	/**
	 * A map of {@link ValueChangedCallback}s for handling notifications and indications.
	 */
	@NonNull
	private final HashMap<Object, ValueChangedCallback> notificationCallbacks = new HashMap<>();
	/**
	 * A special handler for Battery Level notifications.
	 */
	@Nullable
	@Deprecated
	private ValueChangedCallback batteryLevelNotificationCallback;
	/**
	 * An instance of a request that waits for a notification or an indication.
	 * There may be only a single instance of such request at a time as this is a blocking request.
	 */
	@Nullable
	private AwaitingRequest awaitingRequest;

	private final BroadcastReceiver bluetoothStateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
			final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

			final String stateString = "[Broadcast] Action received: " + BluetoothAdapter.ACTION_STATE_CHANGED +
					", state changed to " + state2String(state);
			log(Log.DEBUG, stateString);

			switch (state) {
				case BluetoothAdapter.STATE_TURNING_OFF:
				case BluetoothAdapter.STATE_OFF:
					if (previousState != BluetoothAdapter.STATE_TURNING_OFF
							&& previousState != BluetoothAdapter.STATE_OFF) {
						// No more calls are possible
						operationInProgress = true;
						taskQueue.clear();
						initQueue = null;

						final BluetoothDevice device = bluetoothDevice;
						if (device != null) {
							// Signal the current request, if any
							if (request != null && request.type != Request.Type.DISCONNECT) {
								request.notifyFail(device, FailCallback.REASON_BLUETOOTH_DISABLED);
								request = null;
							}
							if (awaitingRequest != null) {
								awaitingRequest.notifyFail(device, FailCallback.REASON_BLUETOOTH_DISABLED);
								awaitingRequest = null;
							}
							if (connectRequest != null) {
								connectRequest.notifyFail(device, FailCallback.REASON_BLUETOOTH_DISABLED);
								connectRequest = null;
							}
						}

						// The connection is killed by the system, no need to disconnect gently.
						userDisconnected = true;
						// Allow new requests when Bluetooth is enabled again. close() doesn't do it.
						// See: https://github.com/NordicSemiconductor/Android-BLE-Library/issues/25
						// and: https://github.com/NordicSemiconductor/Android-BLE-Library/issues/41
						operationInProgress = false;
						// This will call close()
						if (device != null) {
							notifyDeviceDisconnected(device);
						}
					} else {
						// Calling close() will prevent the STATE_OFF event from being logged
						// (this receiver will be unregistered). But it doesn't matter.
						close();
					}
					break;
			}
		}

		private String state2String(final int state) {
			switch (state) {
				case BluetoothAdapter.STATE_TURNING_ON:
					return "TURNING ON";
				case BluetoothAdapter.STATE_ON:
					return "ON";
				case BluetoothAdapter.STATE_TURNING_OFF:
					return "TURNING OFF";
				case BluetoothAdapter.STATE_OFF:
					return "OFF";
				default:
					return "UNKNOWN (" + state + ")";
			}
		}
	};

	private final BroadcastReceiver mBondingBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

			// Skip other devices.
			if (bluetoothDevice == null || device == null
					|| !device.getAddress().equals(bluetoothDevice.getAddress()))
				return;

			log(Log.DEBUG, "[Broadcast] Action received: " +
					BluetoothDevice.ACTION_BOND_STATE_CHANGED +
					", bond state changed to: " + ParserUtils.bondStateToString(bondState) +
					" (" + bondState + ")");

			switch (bondState) {
				case BluetoothDevice.BOND_NONE:
					if (previousBondState == BluetoothDevice.BOND_BONDING) {
						manager.callbacks.onBondingFailed(device);
						log(Log.WARN, "Bonding failed");
						if (request != null) { // CREATE_BOND request
							request.notifyFail(device, FailCallback.REASON_REQUEST_FAILED);
							request = null;
						}
					} else if (previousBondState == BluetoothDevice.BOND_BONDED) {
						if (request != null && request.type == Request.Type.REMOVE_BOND) {
							// The device has already disconnected by now.
							log(Log.INFO, "Bond information removed");
							request.notifySuccess(device);
							request = null;
						}
					}
					break;
				case BluetoothDevice.BOND_BONDING:
					manager.callbacks.onBondingRequired(device);
					return;
				case BluetoothDevice.BOND_BONDED:
					log(Log.INFO, "Device bonded");
					manager.callbacks.onBonded(device);
					if (request != null && request.type == Request.Type.CREATE_BOND) {
						request.notifySuccess(device);
						request = null;
						break;
					}
					// If the device started to pair just after the connection was
					// established the services were not discovered.
					if (!servicesDiscovered && !serviceDiscoveryRequested) {
						serviceDiscoveryRequested = true;
						handler.post(() -> {
							log(Log.VERBOSE, "Discovering services...");
							log(Log.DEBUG, "gatt.discoverServices()");
							bluetoothGatt.discoverServices();
						});
						return;
					}
					// On older Android versions, after executing a command on secured attribute
					// of a device that is not bonded, let's say a write characteristic operation,
					// the system will start bonding. The BOND_BONDING and BOND_BONDED events will
					// be received, but the command will not be repeated automatically.
					//
					// Test results:
					// Devices that require repeating the last task:
					// - Nexus 4 with Android 5.1.1
					// - Samsung S6 with 5.0.1
					// - Samsung S8 with Android 7.0
					// - Nexus 9 with Android 7.1.1
					// Devices that repeat the request automatically:
					// - Pixel 2 with Android 8.1.0
					// - Samsung S8 with Android 8.0.0
					//
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
						if (request != null) {
							// Repeat the last command in that case.
							enqueueFirst(request);
							break;
						}
					}
					// No need to repeat the request.
					return;
			}
			nextRequest(true);
		}
	};

	/**
	 * Initializes the object.
	 *
	 * @param manager The BLE manager.
	 */
	void init(@NonNull final BleManager manager, @NonNull final Handler handler) {
		this.manager = manager;
		this.handler = handler;
	}

	/**
	 * Binds the server with the BLE manager handler. Call with null to unbind the server.
	 *
	 * @param server the server to bind; null to unbind the server.
	 */
	void useServer(@Nullable final BleServerManager server) {
		this.serverManager = server;
	}

	/**
	 * Closes and releases resources.
	 */
	void close() {
		try {
			final Context context = manager.getContext();
			context.unregisterReceiver(bluetoothStateBroadcastReceiver);
			context.unregisterReceiver(mBondingBroadcastReceiver);
		} catch (final Exception e) {
			// the receiver must have been not registered or unregistered before.
		}
		synchronized (LOCK) {
			if (bluetoothGatt != null) {
				if (manager.shouldClearCacheWhenDisconnected()) {
					if (internalRefreshDeviceCache()) {
						log(Log.INFO, "Cache refreshed");
					} else {
						log(Log.WARN, "Refreshing failed");
					}
				}
				log(Log.DEBUG, "gatt.close()");
				try {
					bluetoothGatt.close();
				} catch (final Throwable t) {
					// ignore
				}
				bluetoothGatt = null;
			}
			reliableWriteInProgress = false;
			initialConnection = false;
			notificationCallbacks.clear();
			// close() is called in notifyDeviceDisconnected, which may enqueue new requests.
			// Setting this flag to false would allow to enqueue a new request before the
			// current one ends processing. The following line should not be uncommented.
			// mGattCallback.operationInProgress = false;
			taskQueue.clear();
			initQueue = null;
			bluetoothDevice = null;
		}
	}

	public BluetoothDevice getBluetoothDevice() {
		return bluetoothDevice;
	}

	/**
	 * Returns the value of the server characteristic. For characteristics that are not shared,
	 * the value may be different for each connected device.
	 *
	 * @param serverCharacteristic The characteristic to get value of.
	 * @return The value.
	 */
	@Nullable
	public final byte[] getCharacteristicValue(@NonNull final BluetoothGattCharacteristic serverCharacteristic) {
		if (characteristicValues != null && characteristicValues.containsKey(serverCharacteristic))
			return characteristicValues.get(serverCharacteristic);
		return serverCharacteristic.getValue();
	}

	/**
	 * Returns the value of the server descriptor. For descriptor that are not shared,
	 * the value may be different for each connected device.
	 *
	 * @param serverDescriptor The descriptor to get value of.
	 * @return The value.
	 */
	@Nullable
	public final byte[] getDescriptorValue(@NonNull final BluetoothGattDescriptor serverDescriptor) {
		if (descriptorValues != null && descriptorValues.containsKey(serverDescriptor))
			return descriptorValues.get(serverDescriptor);
		return serverDescriptor.getValue();
	}

	// Requests implementation

	private boolean internalConnect(@NonNull final BluetoothDevice device,
									@Nullable final ConnectRequest connectRequest) {
		final boolean bluetoothEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();
		if (connected || !bluetoothEnabled) {
			final BluetoothDevice currentDevice = bluetoothDevice;
			if (bluetoothEnabled && currentDevice != null && currentDevice.equals(device)) {
				this.connectRequest.notifySuccess(device);
			} else {
				// We can't return false here, as the request would be notified with
				// bluetoothDevice instance instead, and that may be null or a wrong device.
				if (this.connectRequest != null) {
					this.connectRequest.notifyFail(device,
							bluetoothEnabled ?
									FailCallback.REASON_REQUEST_FAILED :
									FailCallback.REASON_BLUETOOTH_DISABLED);
				} // else, the request was already failed by the Bluetooth state receiver
			}
			this.connectRequest = null;
			nextRequest(true);
			return true;
		}

		final Context context = manager.getContext();
		synchronized (LOCK) {
			if (bluetoothGatt != null) {
				// There are 2 ways of reconnecting to the same device:
				// 1. Reusing the same BluetoothGatt object and calling connect() - this will force
				//    the autoConnect flag to true
				// 2. Closing it and reopening a new instance of BluetoothGatt object.
				// The gatt.close() is an asynchronous method. It requires some time before it's
				// finished and device.connectGatt(...) can't be called immediately or service
				// discovery may never finish on some older devices (Nexus 4, Android 5.0.1).
				// If shouldAutoConnect() method returned false we can't call gatt.connect() and
				// have to close gatt and open it again.
				if (!initialConnection) {
					log(Log.DEBUG, "gatt.close()");
					try {
						bluetoothGatt.close();
					} catch (final Throwable t) {
						// ignore
					}
					bluetoothGatt = null;
					try {
						log(Log.DEBUG, "wait(200)");
						Thread.sleep(200); // Is 200 ms enough?
					} catch (final InterruptedException e) {
						// Ignore
					}
				} else {
					// Instead, the gatt.connect() method will be used to reconnect to the same device.
					// This method forces autoConnect = true even if the gatt was created with this
					// flag set to false.
					initialConnection = false;
					connectionTime = 0L; // no timeout possible when autoConnect used
					connectionState = BluetoothGatt.STATE_CONNECTING;
					log(Log.VERBOSE, "Connecting...");
					manager.callbacks.onDeviceConnecting(device);
					log(Log.DEBUG, "gatt.connect()");
					bluetoothGatt.connect();
					return true;
				}
			} else {
				// Register bonding broadcast receiver
				context.registerReceiver(bluetoothStateBroadcastReceiver,
						new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
				context.registerReceiver(mBondingBroadcastReceiver,
						new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
			}
		}

		// This should not happen in normal circumstances, but may, when Bluetooth was turned off
		// when retrying to create a connection.
		if (connectRequest == null)
			return false;
		final boolean shouldAutoConnect = connectRequest.shouldAutoConnect();
		// We will receive Link Loss events only when the device is connected with autoConnect=true.
		userDisconnected = !shouldAutoConnect;
		// The first connection will always be done with autoConnect = false to make the connection quick.
		// If the shouldAutoConnect() method returned true, the manager will automatically try to
		// reconnect to this device on link loss.
		if (shouldAutoConnect) {
			initialConnection = true;
		}
		bluetoothDevice = device;
		log(Log.VERBOSE, connectRequest.isFirstAttempt() ? "Connecting..." : "Retrying...");
		connectionState = BluetoothGatt.STATE_CONNECTING;
		manager.callbacks.onDeviceConnecting(device);
		connectionTime = SystemClock.elapsedRealtime();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// connectRequest will never be null here.
			final int preferredPhy = connectRequest.getPreferredPhy();
			log(Log.DEBUG, "gatt = device.connectGatt(autoConnect = false, TRANSPORT_LE, "
					+ ParserUtils.phyMaskToString(preferredPhy) + ")");
			// A variant of connectGatt with Handled can't be used here.
			// Check https://github.com/NordicSemiconductor/Android-BLE-Library/issues/54
			bluetoothGatt = device.connectGatt(context, false, gattCallback,
					BluetoothDevice.TRANSPORT_LE, preferredPhy/*, handler*/);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			log(Log.DEBUG, "gatt = device.connectGatt(autoConnect = false, TRANSPORT_LE)");
			bluetoothGatt = device.connectGatt(context, false, gattCallback,
					BluetoothDevice.TRANSPORT_LE);
		} else {
			log(Log.DEBUG, "gatt = device.connectGatt(autoConnect = false)");
			bluetoothGatt = device.connectGatt(context, false, gattCallback);
		}
		return true;
	}

	private boolean internalDisconnect() {
		userDisconnected = true;
		initialConnection = false;
		ready = false;

		if (bluetoothGatt != null) {
			connectionState = BluetoothGatt.STATE_DISCONNECTING;
			log(Log.VERBOSE, connected ? "Disconnecting..." : "Cancelling connection...");
			manager.callbacks.onDeviceDisconnecting(bluetoothGatt.getDevice());
			final boolean wasConnected = connected;
			log(Log.DEBUG, "gatt.disconnect()");
			bluetoothGatt.disconnect();

			if (wasConnected)
				return true;

			// If the device wasn't connected, there will be no callback after calling
			// gatt.disconnect(), the connection attempt will be stopped.
			connectionState = BluetoothGatt.STATE_DISCONNECTED;
			log(Log.INFO, "Disconnected");
			manager.callbacks.onDeviceDisconnected(bluetoothGatt.getDevice());
		}
		// request may be of type DISCONNECT or CONNECT (timeout).
		// For the latter, it has already been notified with REASON_TIMEOUT.
		if (request != null && request.type == Request.Type.DISCONNECT) {
			if (bluetoothDevice != null)
				request.notifySuccess(bluetoothDevice);
			else
				request.notifyInvalidRequest();
		}
		nextRequest(true);
		return true;
	}

	private boolean internalCreateBond() {
		final BluetoothDevice device = bluetoothDevice;
		if (device == null)
			return false;

		log(Log.VERBOSE, "Starting pairing...");

		if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
			log(Log.WARN, "Device already bonded");
			request.notifySuccess(device);
			nextRequest(true);
			return true;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			log(Log.DEBUG, "device.createBond()");
			return device.createBond();
		} else {
			/*
			 * There is a createBond() method in BluetoothDevice class but for now it's hidden.
			 * We will call it using reflections. It has been revealed in KitKat (Api19).
			 */
			try {
				final Method createBond = device.getClass().getMethod("createBond");
				log(Log.DEBUG, "device.createBond() (hidden)");
				return (Boolean) createBond.invoke(device);
			} catch (final Exception e) {
				Log.w(TAG, "An exception occurred while creating bond", e);
			}
		}
		return false;
	}

	private boolean internalRemoveBond() {
		final BluetoothDevice device = bluetoothDevice;
		if (device == null)
			return false;

		log(Log.VERBOSE, "Removing bond information...");

		if (device.getBondState() == BluetoothDevice.BOND_NONE) {
			log(Log.WARN, "Device is not bonded");
			request.notifySuccess(device);
			nextRequest(true);
			return true;
		}

		/*
		 * There is a removeBond() method in BluetoothDevice class but for now it's hidden.
		 * We will call it using reflections.
		 */
		try {
			//noinspection JavaReflectionMemberAccess
			final Method removeBond = device.getClass().getMethod("removeBond");
			log(Log.DEBUG, "device.removeBond() (hidden)");
			return (Boolean) removeBond.invoke(device);
		} catch (final Exception e) {
			Log.w(TAG, "An exception occurred while removing bond", e);
		}
		return false;
	}

	/**
	 * When the device is bonded and has the Generic Attribute service and the Service Changed
	 * characteristic this method enables indications on this characteristic.
	 * In case one of the requirements is not fulfilled this method returns <code>false</code>.
	 *
	 * @return <code>true</code> when the request has been sent, <code>false</code> when the device
	 * is not bonded, does not have the Generic Attribute service, the GA service does not have
	 * the Service Changed characteristic or this characteristic does not have the CCCD.
	 */
	private boolean ensureServiceChangedEnabled() {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		// The Service Changed indications have sense only on bonded devices.
		final BluetoothDevice device = gatt.getDevice();
		if (device.getBondState() != BluetoothDevice.BOND_BONDED)
			return false;

		final BluetoothGattService gaService = gatt.getService(BleManager.GENERIC_ATTRIBUTE_SERVICE);
		if (gaService == null)
			return false;

		final BluetoothGattCharacteristic scCharacteristic =
				gaService.getCharacteristic(BleManager.SERVICE_CHANGED_CHARACTERISTIC);
		if (scCharacteristic == null)
			return false;

		log(Log.INFO, "Service Changed characteristic found on a bonded device");
		return internalEnableIndications(scCharacteristic);
	}

	private boolean internalEnableNotifications(@Nullable final BluetoothGattCharacteristic characteristic) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || characteristic == null || !connected)
			return false;

		final BluetoothGattDescriptor descriptor = getCccd(characteristic, BluetoothGattCharacteristic.PROPERTY_NOTIFY);
		if (descriptor != null) {
			log(Log.DEBUG, "gatt.setCharacteristicNotification(" + characteristic.getUuid() + ", true)");
			gatt.setCharacteristicNotification(characteristic, true);

			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			log(Log.VERBOSE, "Enabling notifications for " + characteristic.getUuid());
			log(Log.DEBUG, "gatt.writeDescriptor(" +
					BleManager.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID + ", value=0x01-00)");
			return internalWriteDescriptorWorkaround(descriptor);
		}
		return false;
	}

	private boolean internalDisableNotifications(@Nullable final BluetoothGattCharacteristic characteristic) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || characteristic == null || !connected)
			return false;

		final BluetoothGattDescriptor descriptor = getCccd(characteristic, BluetoothGattCharacteristic.PROPERTY_NOTIFY);
		if (descriptor != null) {
			log(Log.DEBUG, "gatt.setCharacteristicNotification(" + characteristic.getUuid() + ", false)");
			gatt.setCharacteristicNotification(characteristic, false);

			descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			log(Log.VERBOSE, "Disabling notifications and indications for " + characteristic.getUuid());
			log(Log.DEBUG, "gatt.writeDescriptor(" +
					BleManager.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID + ", value=0x00-00)");
			return internalWriteDescriptorWorkaround(descriptor);
		}
		return false;
	}

	private boolean internalEnableIndications(@Nullable final BluetoothGattCharacteristic characteristic) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || characteristic == null || !connected)
			return false;

		final BluetoothGattDescriptor descriptor = getCccd(characteristic, BluetoothGattCharacteristic.PROPERTY_INDICATE);
		if (descriptor != null) {
			log(Log.DEBUG, "gatt.setCharacteristicNotification(" + characteristic.getUuid() + ", true)");
			gatt.setCharacteristicNotification(characteristic, true);

			descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
			log(Log.VERBOSE, "Enabling indications for " + characteristic.getUuid());
			log(Log.DEBUG, "gatt.writeDescriptor(" +
					BleManager.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID + ", value=0x02-00)");
			return internalWriteDescriptorWorkaround(descriptor);
		}
		return false;
	}

	private boolean internalDisableIndications(@Nullable final BluetoothGattCharacteristic characteristic) {
		// This writes exactly the same settings so do not duplicate code.
		return internalDisableNotifications(characteristic);
	}

	private boolean internalSendNotification(@Nullable final BluetoothGattCharacteristic serverCharacteristic,
											 final boolean confirm) {
		if (serverManager == null || serverManager.getServer() == null || serverCharacteristic == null)
			return false;
		final int requiredProperty = confirm ? BluetoothGattCharacteristic.PROPERTY_INDICATE : BluetoothGattCharacteristic.PROPERTY_NOTIFY;
		if ((serverCharacteristic.getProperties() & requiredProperty) == 0)
			return false;
		final BluetoothGattDescriptor cccd = serverCharacteristic.getDescriptor(BleManager.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		if (cccd == null)
			return false;
		// If notifications/indications were enabled, send the notification/indication.
		final byte[] value = descriptorValues.containsKey(cccd) ? descriptorValues.get(cccd) : cccd.getValue();
		if (value != null && value.length == 2 && value[0] != 0) {
			log(Log.VERBOSE, "[Server] Sending " + (confirm ? "indication" : "notification") + " to " + serverCharacteristic.getUuid());
			log(Log.DEBUG, "server.notifyCharacteristicChanged(device, " + serverCharacteristic.getUuid() + ", " + confirm + ")");
			final boolean result = serverManager.getServer().notifyCharacteristicChanged(bluetoothDevice, serverCharacteristic, confirm);
			if (result && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				handler.post(() -> {
					notifyNotificationSent(bluetoothDevice);
					nextRequest(true);
				});
			}
			return result;
		}
		// Otherwise, assume the data was sent. The remote side has not registered for them.
		nextRequest(true);
		return true;
	}

	/**
	 * Returns the Client Characteristic Config Descriptor if the characteristic has the
	 * required property. It may return null if the CCCD is not there.
	 *
	 * @param characteristic   the characteristic to look the CCCD in.
	 * @param requiredProperty the required property: {@link BluetoothGattCharacteristic#PROPERTY_NOTIFY}
	 *                         or {@link BluetoothGattCharacteristic#PROPERTY_INDICATE}.
	 * @return The CCC descriptor or null if characteristic is null, if it doesn't have the
	 * required property, or if the CCCD is missing.
	 */
	private static BluetoothGattDescriptor getCccd(@Nullable final BluetoothGattCharacteristic characteristic,
												   final int requiredProperty) {
		if (characteristic == null)
			return null;

		// Check characteristic property
		final int properties = characteristic.getProperties();
		if ((properties & requiredProperty) == 0)
			return null;

		return characteristic.getDescriptor(BleManager.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
	}

	private boolean internalReadCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || characteristic == null || !connected)
			return false;

		// Check characteristic property.
		final int properties = characteristic.getProperties();
		if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == 0)
			return false;

		log(Log.VERBOSE, "Reading characteristic " + characteristic.getUuid());
		log(Log.DEBUG, "gatt.readCharacteristic(" + characteristic.getUuid() + ")");
		return gatt.readCharacteristic(characteristic);
	}

	private boolean internalWriteCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || characteristic == null || !connected)
			return false;

		// Check characteristic property.
		final int properties = characteristic.getProperties();
		if ((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE |
				BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0)
			return false;

		log(Log.VERBOSE, "Writing characteristic " + characteristic.getUuid() +
				" (" + ParserUtils.writeTypeToString(characteristic.getWriteType()) + ")");
		log(Log.DEBUG, "gatt.writeCharacteristic(" + characteristic.getUuid() + ")");
		return gatt.writeCharacteristic(characteristic);
	}

	private boolean internalReadDescriptor(@Nullable final BluetoothGattDescriptor descriptor) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || descriptor == null || !connected)
			return false;

		log(Log.VERBOSE, "Reading descriptor " + descriptor.getUuid());
		log(Log.DEBUG, "gatt.readDescriptor(" + descriptor.getUuid() + ")");
		return gatt.readDescriptor(descriptor);
	}

	private boolean internalWriteDescriptor(@Nullable final BluetoothGattDescriptor descriptor) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || descriptor == null || !connected)
			return false;

		log(Log.VERBOSE, "Writing descriptor " + descriptor.getUuid());
		log(Log.DEBUG, "gatt.writeDescriptor(" + descriptor.getUuid() + ")");
		return internalWriteDescriptorWorkaround(descriptor);
	}

	/**
	 * There was a bug in Android up to 6.0 where the descriptor was written using parent
	 * characteristic's write type, instead of always Write With Response, as the spec says.
	 * <p>
	 * See: <a href="https://android.googlesource.com/platform/frameworks/base/+/942aebc95924ab1e7ea1e92aaf4e7fc45f695a6c%5E%21/#F0">
	 * https://android.googlesource.com/platform/frameworks/base/+/942aebc95924ab1e7ea1e92aaf4e7fc45f695a6c%5E%21/#F0</a>
	 *
	 * @param descriptor the descriptor to be written
	 * @return the result of {@link BluetoothGatt#writeDescriptor(BluetoothGattDescriptor)}
	 */
	private boolean internalWriteDescriptorWorkaround(@Nullable final BluetoothGattDescriptor descriptor) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || descriptor == null || !connected)
			return false;

		final BluetoothGattCharacteristic parentCharacteristic = descriptor.getCharacteristic();
		final int originalWriteType = parentCharacteristic.getWriteType();
		parentCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
		final boolean result = gatt.writeDescriptor(descriptor);
		parentCharacteristic.setWriteType(originalWriteType);
		return result;
	}

	private boolean internalBeginReliableWrite() {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		// Reliable Write can't be before the old one isn't executed or aborted.
		if (reliableWriteInProgress)
			return true;

		log(Log.VERBOSE, "Beginning reliable write...");
		log(Log.DEBUG, "gatt.beginReliableWrite()");
		return reliableWriteInProgress = gatt.beginReliableWrite();
	}

	private boolean internalExecuteReliableWrite() {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		if (!reliableWriteInProgress)
			return false;

		log(Log.VERBOSE, "Executing reliable write...");
		log(Log.DEBUG, "gatt.executeReliableWrite()");
		return gatt.executeReliableWrite();
	}

	private boolean internalAbortReliableWrite() {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		if (!reliableWriteInProgress)
			return false;

		log(Log.VERBOSE, "Aborting reliable write...");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			log(Log.DEBUG, "gatt.abortReliableWrite()");
			gatt.abortReliableWrite();
		} else {
			log(Log.DEBUG, "gatt.abortReliableWrite(device)");
			gatt.abortReliableWrite(gatt.getDevice());
		}
		return true;
	}

	@Deprecated
	private boolean internalReadBatteryLevel() {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		final BluetoothGattService batteryService = gatt.getService(BleManager.BATTERY_SERVICE);
		if (batteryService == null)
			return false;

		final BluetoothGattCharacteristic batteryLevelCharacteristic =
				batteryService.getCharacteristic(BleManager.BATTERY_LEVEL_CHARACTERISTIC);
		return internalReadCharacteristic(batteryLevelCharacteristic);
	}

	@Deprecated
	private boolean internalSetBatteryNotifications(final boolean enable) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		final BluetoothGattService batteryService = gatt.getService(BleManager.BATTERY_SERVICE);
		if (batteryService == null)
			return false;

		final BluetoothGattCharacteristic batteryLevelCharacteristic =
				batteryService.getCharacteristic(BleManager.BATTERY_LEVEL_CHARACTERISTIC);
		if (enable)
			return internalEnableNotifications(batteryLevelCharacteristic);
		else
			return internalDisableNotifications(batteryLevelCharacteristic);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private boolean internalRequestMtu(@IntRange(from = 23, to = 517) final int mtu) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		log(Log.VERBOSE, "Requesting new MTU...");
		log(Log.DEBUG, "gatt.requestMtu(" + mtu + ")");
		return gatt.requestMtu(mtu);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private boolean internalRequestConnectionPriority(@ConnectionPriority final int priority) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		String text, priorityText;
		switch (priority) {
			case ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH:
				text = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
						"HIGH (11.25–15ms, 0, 20s)" : "HIGH (7.5–10ms, 0, 20s)";
				priorityText = "HIGH";
				break;
			case ConnectionPriorityRequest.CONNECTION_PRIORITY_LOW_POWER:
				text = "LOW POWER (100–125ms, 2, 20s)";
				priorityText = "LOW POWER";
				break;
			default:
			case ConnectionPriorityRequest.CONNECTION_PRIORITY_BALANCED:
				text = "BALANCED (30–50ms, 0, 20s)";
				priorityText = "BALANCED";
				break;
		}
		log(Log.VERBOSE, "Requesting connection priority: " + text + "...");
		log(Log.DEBUG, "gatt.requestConnectionPriority(" + priorityText + ")");
		return gatt.requestConnectionPriority(priority);
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private boolean internalSetPreferredPhy(@PhyMask final int txPhy, @PhyMask final int rxPhy,
											@PhyOption final int phyOptions) {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		log(Log.VERBOSE, "Requesting preferred PHYs...");
		log(Log.DEBUG, "gatt.setPreferredPhy(" + ParserUtils.phyMaskToString(txPhy) + ", "
				+ ParserUtils.phyMaskToString(rxPhy) + ", coding option = "
				+ ParserUtils.phyCodedOptionToString(phyOptions) + ")");
		gatt.setPreferredPhy(txPhy, rxPhy, phyOptions);
		return true;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private boolean internalReadPhy() {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		log(Log.VERBOSE, "Reading PHY...");
		log(Log.DEBUG, "gatt.readPhy()");
		gatt.readPhy();
		return true;
	}

	private boolean internalReadRssi() {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null || !connected)
			return false;

		log(Log.VERBOSE, "Reading remote RSSI...");
		log(Log.DEBUG, "gatt.readRemoteRssi()");
		return gatt.readRemoteRssi();
	}

	/**
	 * Sets and returns notification callback.
	 *
	 * @param characteristic characteristic to bind the callback with. If null, the returned
	 *                       callback will not be null, but will not be used.
	 * @return The callback.
	 */
	@NonNull
	ValueChangedCallback setNotificationCallback(@Nullable final Object characteristic) {
		ValueChangedCallback callback = notificationCallbacks.get(characteristic);
		if (callback == null) {
			callback = new ValueChangedCallback(handler);
			if (characteristic != null) {
				notificationCallbacks.put(characteristic, callback);
			}
		}
		return callback.free();
	}

	@Deprecated
	DataReceivedCallback getBatteryLevelCallback() {
		return (device, data) -> {
			if (data.size() == 1) {
				//noinspection ConstantConditions
				final int batteryLevel = data.getIntValue(Data.FORMAT_UINT8, 0);
				log(Log.INFO, "Battery Level received: " + batteryLevel + "%");
				batteryValue = batteryLevel;
				onBatteryValueReceived(bluetoothGatt, batteryLevel);
				manager.callbacks.onBatteryValueReceived(device, batteryLevel);
			}
		};
	}

	@Deprecated
	void setBatteryLevelNotificationCallback() {
		if (batteryLevelNotificationCallback == null) {
			batteryLevelNotificationCallback = new ValueChangedCallback(handler)
					.with((device, data) -> {
						if (data.size() == 1) {
							//noinspection ConstantConditions
							final int batteryLevel = data.getIntValue(Data.FORMAT_UINT8, 0);
							batteryValue = batteryLevel;
							onBatteryValueReceived(bluetoothGatt, batteryLevel);
							manager.callbacks.onBatteryValueReceived(device, batteryLevel);
						}
					});
		}
	}

	/**
	 * Removes the notifications callback set using
	 * {@link #setNotificationCallback(Object)}.
	 *
	 * @param characteristic characteristic to unbind the callback from.
	 */
	void removeNotificationCallback(@Nullable final Object characteristic) {
		notificationCallbacks.remove(characteristic);
	}

	/**
	 * Clears the device cache.
	 */
	@SuppressWarnings("JavaReflectionMemberAccess")
	private boolean internalRefreshDeviceCache() {
		final BluetoothGatt gatt = bluetoothGatt;
		if (gatt == null) // no need to be connected
			return false;

		log(Log.VERBOSE, "Refreshing device cache...");
		log(Log.DEBUG, "gatt.refresh() (hidden)");
		/*
		 * There is a refresh() method in BluetoothGatt class but for now it's hidden.
		 * We will call it using reflections.
		 */
		try {
			final Method refresh = gatt.getClass().getMethod("refresh");
			return (Boolean) refresh.invoke(gatt);
		} catch (final Exception e) {
			Log.w(TAG, "An exception occurred while refreshing device", e);
			log(Log.WARN, "gatt.refresh() method not found");
		}
		return false;
	}

	// Request Handler methods

	@Override
	final void enqueueFirst(@NonNull final Request request) {
		final Deque<Request> queue = initInProgress ? initQueue : taskQueue;
		queue.addFirst(request);
		request.enqueued = true;
	}

	@Override
	final void enqueue(@NonNull final Request request) {
		final Deque<Request> queue = initInProgress ? initQueue : taskQueue;
		queue.add(request);
		request.enqueued = true;
		nextRequest(false);
	}

	@Override
	final void cancelQueue() {
		taskQueue.clear();
		initQueue = null;
		if (awaitingRequest != null) {
			awaitingRequest.notifyFail(bluetoothDevice, FailCallback.REASON_CANCELLED);
		}
		if (request != null && awaitingRequest != request) {
			request.notifyFail(bluetoothDevice, FailCallback.REASON_CANCELLED);
			request = null;
		}
		awaitingRequest = null;
		if (requestQueue != null) {
			requestQueue.notifyFail(bluetoothDevice, FailCallback.REASON_CANCELLED);
			requestQueue = null;
		}
		if (connectRequest != null) {
			connectRequest.notifyFail(bluetoothDevice, FailCallback.REASON_CANCELLED);
			connectRequest = null;
			internalDisconnect();
		} else {
			nextRequest(true);
		}
	}

	@Override
	final void onRequestTimeout(@NonNull final TimeoutableRequest request) {
		this.request = null;
		awaitingRequest = null;
		if (request.type == Request.Type.CONNECT) {
			connectRequest = null;
			internalDisconnect();
			// The method above will call mGattCallback.nextRequest(true) so we have to return here.
			return;
		}
		if (request.type == Request.Type.DISCONNECT) {
			close();
			return;
		}
		nextRequest(true);
	}

	@Override
	final Handler getHandler() {
		return handler;
	}

	/**
	 * Method returns the connection state:
	 * {@link BluetoothProfile#STATE_CONNECTING STATE_CONNECTING},
	 * {@link BluetoothProfile#STATE_CONNECTED STATE_CONNECTED},
	 * {@link BluetoothProfile#STATE_DISCONNECTING STATE_DISCONNECTING},
	 * {@link BluetoothProfile#STATE_DISCONNECTED STATE_DISCONNECTED}
	 *
	 * @return The connection state.
	 */
	@ConnectionState
	final int getConnectionState() {
		return connectionState;
	}

	/**
	 * This method returns true if the device is connected. Services could have not been
	 * discovered yet.
	 */
	final boolean isConnected() {
		return connected;
	}

	/**
	 * Returns the last received value of Battery Level characteristic, or -1 if such
	 * does not exist, hasn't been read or notification wasn't received yet.
	 */
	@Deprecated
	final int getBatteryValue() {
		return batteryValue;
	}

	/**
	 * Returns true if the device is connected and the initialization has finished,
	 * that is when {@link #onDeviceReady()} was called.
	 */
	final boolean isReady() {
		return ready;
	}

	/**
	 * Returns true if {@link BluetoothGatt#beginReliableWrite()} has been called and
	 * the Reliable Write hasn't been executed nor aborted yet.
	 */
	final boolean isReliableWriteInProgress() {
		return reliableWriteInProgress;
	}

	/**
	 * Returns the current MTU (Maximum Transfer Unit).
	 */
	final int getMtu() {
		return mtu;
	}

	final void overrideMtu(@IntRange(from = 23, to = 517) final int mtu) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			this.mtu = mtu;
		}
	}

	/**
	 * This method should return <code>true</code> when the gatt device supports the
	 * required services.
	 *
	 * @param gatt the gatt device with services discovered
	 * @return <code>True</code> when the device has the required service.
	 */
	protected abstract boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt);

	/**
	 * This method should return <code>true</code> when the gatt device supports the
	 * optional services. The default implementation returns <code>false</code>.
	 *
	 * @param gatt the gatt device with services discovered
	 * @return <code>True</code> when the device has the optional service.
	 */
	protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
		return false;
	}

	/**
	 * This method should return a list of requests needed to initialize the profile.
	 * Enabling Service Change indications for bonded devices and reading the Battery Level
	 * value and enabling Battery Level notifications is handled before executing this queue.
	 * The queue should not have requests that are not available, e.g. should not read an
	 * optional service when it is not supported by the connected device.
	 * <p>
	 * This method is called when the services has been discovered and the device is supported
	 * (has required service).
	 *
	 * @param gatt the gatt device with services discovered
	 * @return The queue of requests.
	 * @deprecated Use {@link #initialize()} instead.
	 */
	@Deprecated
	protected Deque<Request> initGatt(@NonNull final BluetoothGatt gatt) {
		return null;
	}

	/**
	 * This method should set up the request queue needed to initialize the profile.
	 * Enabling Service Change indications for bonded devices is handled before executing this
	 * queue. The queue may have requests that are not available, e.g. read an optional
	 * service when it is not supported by the connected device. Such call will trigger
	 * {@link Request#fail(FailCallback)}.
	 * <p>
	 * This method is called from the main thread when the services has been discovered and
	 * the device is supported (has required service).
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
		// empty initialization queue
	}

	/**
	 * In this method the manager should get references to server characteristics and descriptors
	 * that will use. The method is called after the service discovery of a remote device has
	 * finished and {@link #isRequiredServiceSupported(BluetoothGatt)} returned true.
	 * <p>
	 * The references obtained in this method should be released in {@link #onDeviceDisconnected()}.
	 * <p>
	 * This method is called only when the server was set by
	 * {@link BleManager#useServer(BleServerManager)} and opened using {@link BleServerManager#open()}.
	 *
	 * @param server The GATT Server instance. Use {@link BluetoothGattServer#getService(UUID)} to
	 *               obtain service instance.
	 */
	protected void onServerReady(@NonNull final BluetoothGattServer server) {
		// empty initialization
	}

	/**
	 * Called when the initialization queue is complete.
	 */
	protected void onDeviceReady() {
		// empty
	}

	/**
	 * Called each time the task queue gets cleared.
	 */
	protected void onManagerReady() {
		// empty
	}

	/**
	 * This method should nullify all services and characteristics of the device.
	 * It's called when the device is no longer connected, either due to user action
	 * or a link loss.
	 */
	protected abstract void onDeviceDisconnected();

	private void notifyDeviceDisconnected(@NonNull final BluetoothDevice device) {
		final boolean wasConnected = connected;
		connected = false;
		servicesDiscovered = false;
		serviceDiscoveryRequested = false;
		initInProgress = false;
		connectionState = BluetoothGatt.STATE_DISCONNECTED;
		checkCondition();
		if (!wasConnected) {
			log(Log.WARN, "Connection attempt timed out");
			close();
			manager.callbacks.onDeviceDisconnected(device);
			// ConnectRequest was already notified
		} else if (userDisconnected) {
			log(Log.INFO, "Disconnected");
			close();
			manager.callbacks.onDeviceDisconnected(device);
			final Request request = this.request;
			if (request != null && request.type == Request.Type.DISCONNECT) {
				request.notifySuccess(device);
			}
		} else {
			log(Log.WARN, "Connection lost");
			manager.callbacks.onLinkLossOccurred(device);
			// We are not closing the connection here as the device should try to reconnect
			// automatically.
			// This may be only called when the shouldAutoConnect() method returned true.
		}
		onDeviceDisconnected();
	}

	/**
	 * Callback reporting the result of a characteristic read operation.
	 *
	 * @param gatt           GATT client
	 * @param characteristic Characteristic that was read from the associated remote device.
	 * @deprecated Use {@link ReadRequest#with(DataReceivedCallback)} instead.
	 */
	@Deprecated
	protected void onCharacteristicRead(@NonNull final BluetoothGatt gatt,
										@NonNull final BluetoothGattCharacteristic characteristic) {
		// do nothing
	}

	/**
	 * Callback indicating the result of a characteristic write operation.
	 * <p>If this callback is invoked while a reliable write transaction is
	 * in progress, the value of the characteristic represents the value
	 * reported by the remote device. An application should compare this
	 * value to the desired value to be written. If the values don't match,
	 * the application must abort the reliable write transaction.
	 *
	 * @param gatt           GATT client
	 * @param characteristic Characteristic that was written to the associated remote device.
	 * @deprecated Use {@link WriteRequest#done(SuccessCallback)} instead.
	 */
	@Deprecated
	protected void onCharacteristicWrite(@NonNull final BluetoothGatt gatt,
										 @NonNull final BluetoothGattCharacteristic characteristic) {
		// do nothing
	}

	/**
	 * Callback reporting the result of a descriptor read operation.
	 *
	 * @param gatt       GATT client
	 * @param descriptor Descriptor that was read from the associated remote device.
	 * @deprecated Use {@link ReadRequest#with(DataReceivedCallback)} instead.
	 */
	@Deprecated
	protected void onDescriptorRead(@NonNull final BluetoothGatt gatt,
									@NonNull final BluetoothGattDescriptor descriptor) {
		// do nothing
	}

	/**
	 * Callback indicating the result of a descriptor write operation.
	 * <p>If this callback is invoked while a reliable write transaction is in progress,
	 * the value of the characteristic represents the value reported by the remote device.
	 * An application should compare this value to the desired value to be written.
	 * If the values don't match, the application must abort the reliable write transaction.
	 *
	 * @param gatt       GATT client
	 * @param descriptor Descriptor that was written to the associated remote device.
	 * @deprecated Use {@link WriteRequest} and {@link SuccessCallback} instead.
	 */
	@Deprecated
	protected void onDescriptorWrite(@NonNull final BluetoothGatt gatt,
									 @NonNull final BluetoothGattDescriptor descriptor) {
		// do nothing
	}

	/**
	 * Callback reporting the value of Battery Level characteristic which could have
	 * been received by Read or Notify operations.
	 * <p>
	 * This method will not be called if {@link BleManager#readBatteryLevel()} and
	 * {@link BleManager#enableBatteryLevelNotifications()} were overridden.
	 * </p>
	 *
	 * @param gatt  GATT client
	 * @param value the battery value in percent
	 * @deprecated Use {@link ReadRequest#with(DataReceivedCallback)} and
	 * BatteryLevelDataCallback from BLE-Common-Library instead.
	 */
	@Deprecated
	protected void onBatteryValueReceived(@NonNull final BluetoothGatt gatt,
										  @IntRange(from = 0, to = 100) final int value) {
		// do nothing
	}

	/**
	 * Callback indicating a notification has been received.
	 *
	 * @param gatt           GATT client
	 * @param characteristic Characteristic from which the notification came.
	 * @deprecated Use {@link ReadRequest#with(DataReceivedCallback)} instead.
	 */
	@Deprecated
	protected void onCharacteristicNotified(@NonNull final BluetoothGatt gatt,
											@NonNull final BluetoothGattCharacteristic characteristic) {
		// do nothing
	}

	/**
	 * Callback indicating an indication has been received.
	 *
	 * @param gatt           GATT client
	 * @param characteristic Characteristic from which the indication came.
	 * @deprecated Use {@link ReadRequest#with(DataReceivedCallback)} instead.
	 */
	@Deprecated
	protected void onCharacteristicIndicated(@NonNull final BluetoothGatt gatt,
											 @NonNull final BluetoothGattCharacteristic characteristic) {
		// do nothing
	}

	/**
	 * Method called when the MTU request has finished with success. The MTU value may
	 * be different than requested one.
	 *
	 * @param gatt GATT client
	 * @param mtu  the new MTU (Maximum Transfer Unit)
	 * @deprecated Use {@link MtuRequest#with(MtuCallback)} instead.
	 */
	@Deprecated
	protected void onMtuChanged(@NonNull final BluetoothGatt gatt,
								@IntRange(from = 23, to = 517) final int mtu) {
		// do nothing
	}

	/**
	 * Callback indicating the connection parameters were updated. Works on Android 8+.
	 *
	 * @param gatt     GATT client.
	 * @param interval Connection interval used on this connection, 1.25ms unit.
	 *                 Valid range is from 6 (7.5ms) to 3200 (4000ms).
	 * @param latency  Slave latency for the connection in number of connection events.
	 *                 Valid range is from 0 to 499.
	 * @param timeout  Supervision timeout for this connection, in 10ms unit.
	 *                 Valid range is from 10 (0.1s) to 3200 (32s).
	 * @deprecated Use {@link ConnectionPriorityRequest#with(ConnectionPriorityCallback)} instead.
	 */
	@Deprecated
	@TargetApi(Build.VERSION_CODES.O)
	protected void onConnectionUpdated(@NonNull final BluetoothGatt gatt,
									   @IntRange(from = 6, to = 3200) final int interval,
									   @IntRange(from = 0, to = 499) final int latency,
									   @IntRange(from = 10, to = 3200) final int timeout) {
		// do nothing
	}

	private void onError(final BluetoothDevice device, final String message, final int errorCode) {
		log(Log.ERROR, "Error (0x" + Integer.toHexString(errorCode) + "): "
				+ GattError.parse(errorCode));
		manager.callbacks.onError(device, message, errorCode);
	}

	private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

		@Override
		public final void onConnectionStateChange(@NonNull final BluetoothGatt gatt,
												  final int status, final int newState) {
			log(Log.DEBUG, "[Callback] Connection state changed with status: " +
					status + " and new state: " + newState + " (" + ParserUtils.stateToString(newState) + ")");

			if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
				// Sometimes, when a notification/indication is received after the device got
				// disconnected, the Android calls onConnectionStateChanged again, with state
				// STATE_CONNECTED.
				// See: https://github.com/NordicSemiconductor/Android-BLE-Library/issues/43
				if (bluetoothDevice == null) {
					Log.e(TAG, "Device received notification after disconnection.");
					log(Log.DEBUG, "gatt.close()");
					try {
						gatt.close();
					} catch (final Throwable t) {
						// ignore
					}
					return;
				}

				// Notify the parent activity/service.
				log(Log.INFO, "Connected to " + gatt.getDevice().getAddress());
				connected = true;
				connectionTime = 0L;
				connectionState = BluetoothGatt.STATE_CONNECTED;
				manager.callbacks.onDeviceConnected(gatt.getDevice());

				if (!serviceDiscoveryRequested) {
					final boolean bonded = gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED;
					final int delay = manager.getServiceDiscoveryDelay(bonded);
					if (delay > 0)
						log(Log.DEBUG, "wait(" + delay + ")");

					final int connectionCount = ++BleManagerHandler.this.connectionCount;
					handler.postDelayed(() -> {
						if (connectionCount != BleManagerHandler.this.connectionCount) {
							// Ensure that we will not try to discover services for a lost connection.
							return;
						}
						// Some proximity tags (e.g. nRF PROXIMITY Pebble) initialize bonding
						// automatically when connected. Wait with the discovery until bonding is
						// complete. It will be initiated again in the bond state broadcast receiver
						// on the top of this file.
						if (connected &&
								gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
							serviceDiscoveryRequested = true;
							log(Log.VERBOSE, "Discovering services...");
							log(Log.DEBUG, "gatt.discoverServices()");
							gatt.discoverServices();
						}
					}, delay);
				}
			} else {
				if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					final long now = SystemClock.elapsedRealtime();
					final boolean canTimeout = connectionTime > 0;
					final boolean timeout = canTimeout && now > connectionTime + CONNECTION_TIMEOUT_THRESHOLD;

					if (status != BluetoothGatt.GATT_SUCCESS)
						log(Log.WARN, "Error: (0x" + Integer.toHexString(status) + "): " +
								GattError.parseConnectionError(status));

					// In case of a connection error, retry if required.
					if (status != BluetoothGatt.GATT_SUCCESS && canTimeout && !timeout
							&& connectRequest != null && connectRequest.canRetry()) {
						final int delay = connectRequest.getRetryDelay();
						if (delay > 0)
							log(Log.DEBUG, "wait(" + delay + ")");
						handler.postDelayed(() -> internalConnect(gatt.getDevice(), connectRequest), delay);
						return;
					}

					operationInProgress = true; // no more calls are possible
					taskQueue.clear();
					initQueue = null;
					ready = false;

					// Store the current value of the connected flag...
					final boolean wasConnected = connected;
					// ...because this method sets the connected flag to false.
					notifyDeviceDisconnected(gatt.getDevice()); // this may call close()

					// Signal the current request, if any.
					if (request != null) {
						if (request.type != Request.Type.DISCONNECT && request.type != Request.Type.REMOVE_BOND) {
							// The CONNECT request is notified below.
							// The DISCONNECT request is notified below in
							// notifyDeviceDisconnected(BluetoothDevice).
							// The REMOVE_BOND request will be notified when the bond state changes
							// to BOND_NONE in the broadcast received on the top of this file.
							request.notifyFail(gatt.getDevice(),
									status == BluetoothGatt.GATT_SUCCESS ?
											FailCallback.REASON_DEVICE_DISCONNECTED : status);
							request = null;
						}
					}
					if (awaitingRequest != null) {
						awaitingRequest.notifyFail(gatt.getDevice(), FailCallback.REASON_DEVICE_DISCONNECTED);
						awaitingRequest = null;
					}
					if (connectRequest != null) {
						int reason;
						if (servicesDiscovered)
							reason = FailCallback.REASON_DEVICE_NOT_SUPPORTED;
						else if (status == BluetoothGatt.GATT_SUCCESS)
							reason = FailCallback.REASON_DEVICE_DISCONNECTED;
						else if (status == GattError.GATT_ERROR && timeout)
							reason = FailCallback.REASON_TIMEOUT;
						else
							reason = status;
						connectRequest.notifyFail(gatt.getDevice(), reason);
						connectRequest = null;
					}

					// Reset flag, so the next Connect could be enqueued.
					operationInProgress = false;
					// Try to reconnect if the initial connection was lost because of a link loss,
					// and shouldAutoConnect() returned true during connection attempt.
					// This time it will set the autoConnect flag to true (gatt.connect() forces
					// autoConnect true).
					if (wasConnected && initialConnection) {
						internalConnect(gatt.getDevice(), null);
					} else {
						initialConnection = false;
						nextRequest(false);
					}

					if (wasConnected || status == BluetoothGatt.GATT_SUCCESS)
						return;
				} else {
					if (status != BluetoothGatt.GATT_SUCCESS)
						log(Log.ERROR, "Error (0x" + Integer.toHexString(status) + "): " +
								GattError.parseConnectionError(status));
				}
				manager.callbacks.onError(gatt.getDevice(), ERROR_CONNECTION_STATE_CHANGE, status);
			}
		}

		@Override
		public final void onServicesDiscovered(@NonNull final BluetoothGatt gatt, final int status) {
			serviceDiscoveryRequested = false;
			if (status == BluetoothGatt.GATT_SUCCESS) {
				log(Log.INFO, "Services discovered");
				servicesDiscovered = true;
				if (isRequiredServiceSupported(gatt)) {
					log(Log.VERBOSE, "Primary service found");
					final boolean optionalServicesFound = isOptionalServiceSupported(gatt);
					if (optionalServicesFound)
						log(Log.VERBOSE, "Secondary service found");

					// Notify the parent activity.
					manager.callbacks.onServicesDiscovered(gatt.getDevice(), optionalServicesFound);

					// Initialize server attributes.
					if (serverManager != null) {
						final BluetoothGattServer server = serverManager.getServer();
						if (server != null) {
							for (final BluetoothGattService service: server.getServices()) {
								for (final BluetoothGattCharacteristic characteristic: service.getCharacteristics()) {
									if (!serverManager.isShared(characteristic)) {
										if (characteristicValues == null)
											characteristicValues = new HashMap<>();
										characteristicValues.put(characteristic, characteristic.getValue());
									}
									for (final BluetoothGattDescriptor descriptor: characteristic.getDescriptors()) {
										if (!serverManager.isShared(descriptor)) {
											if (descriptorValues == null)
												descriptorValues = new HashMap<>();
											descriptorValues.put(descriptor, descriptor.getValue());
										}
									}
								}
							}
							onServerReady(server);
						}
					}

					// Obtain the queue of initialization requests.
					// First, let's call the deprecated initGatt(...).
					initInProgress = true;
					operationInProgress = true;
					initQueue = initGatt(gatt);

					final boolean deprecatedApiUsed = initQueue != null;
					if (deprecatedApiUsed) {
						for (final Request request : initQueue) {
							request.enqueued = true;
						}
					}

					if (initQueue == null)
						initQueue = new LinkedBlockingDeque<>();

					// Before we start executing the initialization queue some other tasks
					// need to be done.
					// Note, that operations are added in reverse order to the front of the queue.

					// 1. On devices running Android 4.3-5.x, 8.x and 9.0 the Service Changed
					//    characteristic needs to be enabled by the app (for bonded devices).
					//    The request will be ignored if there is no Service Changed characteristic.
					// This "fix" broke this in Android 8:
					// https://android-review.googlesource.com/c/platform/system/bt/+/239970
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
							|| Build.VERSION.SDK_INT == Build.VERSION_CODES.O
							|| Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1
							|| Build.VERSION.SDK_INT == Build.VERSION_CODES.P)
						enqueueFirst(Request.newEnableServiceChangedIndicationsRequest()
								.setRequestHandler(BleManagerHandler.this));

					// Deprecated:
					if (deprecatedApiUsed) {
						// All Battery Service handling will be removed from BleManager in the future.
						// If you want to read/enable notifications on Battery Level characteristic
						// do this in initialize(...).

						// 2. Read Battery Level characteristic (if such does not exist, this will
						//    be skipped)
						manager.readBatteryLevel();
						// 3. Enable Battery Level notifications if required (if this char. does not
						//    exist, this operation will be skipped)
						if (manager.callbacks.shouldEnableBatteryLevelNotifications(gatt.getDevice()))
							manager.enableBatteryLevelNotifications();
					}
					// End

					initialize();
					initInProgress = false;
					nextRequest(true);
				} else {
					log(Log.WARN, "Device is not supported");
					manager.callbacks.onDeviceNotSupported(gatt.getDevice());
					internalDisconnect();
				}
			} else {
				Log.e(TAG, "onServicesDiscovered error " + status);
				onError(gatt.getDevice(), ERROR_DISCOVERY_SERVICE, status);
				if (connectRequest != null) {
					connectRequest.notifyFail(gatt.getDevice(), FailCallback.REASON_REQUEST_FAILED);
					connectRequest = null;
				}
				internalDisconnect();
			}
		}

		@Override
		public void onCharacteristicRead(final BluetoothGatt gatt,
										 final BluetoothGattCharacteristic characteristic,
										 final int status) {
			final byte[] data = characteristic.getValue();

			if (status == BluetoothGatt.GATT_SUCCESS) {
				log(Log.INFO, "Read Response received from " + characteristic.getUuid() +
						", value: " + ParserUtils.parse(data));

				BleManagerHandler.this.onCharacteristicRead(gatt, characteristic);
				if (request instanceof ReadRequest) {
					final ReadRequest rr = (ReadRequest) request;
					final boolean matches = rr.matches(data);
					if (matches) {
						rr.notifyValueChanged(gatt.getDevice(), data);
					}
					if (!matches || rr.hasMore()) {
						enqueueFirst(rr);
					} else {
						rr.notifySuccess(gatt.getDevice());
					}
				}
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
					|| status == 8 /* GATT INSUF AUTHORIZATION */
					|| status == 137 /* GATT AUTH FAIL */) {
				log(Log.WARN, "Authentication required (" + status + ")");
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					// This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
					Log.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					manager.callbacks.onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
				// The request will be repeated when the bond state changes to BONDED.
				return;
			} else {
				Log.e(TAG, "onCharacteristicRead error " + status);
				if (request instanceof ReadRequest) {
					request.notifyFail(gatt.getDevice(), status);
				}
				awaitingRequest = null;
				onError(gatt.getDevice(), ERROR_READ_CHARACTERISTIC, status);
			}
			checkCondition();
			nextRequest(true);
		}

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt,
										  final BluetoothGattCharacteristic characteristic,
										  final int status) {
			final byte[] data = characteristic.getValue();

			if (status == BluetoothGatt.GATT_SUCCESS) {
				log(Log.INFO, "Data written to " + characteristic.getUuid() +
						", value: " + ParserUtils.parse(data));

				BleManagerHandler.this.onCharacteristicWrite(gatt, characteristic);
				if (request instanceof WriteRequest) {
					final WriteRequest wr = (WriteRequest) request;
					final boolean valid = wr.notifyPacketSent(gatt.getDevice(), data);
					if (!valid && requestQueue instanceof ReliableWriteRequest) {
						wr.notifyFail(gatt.getDevice(), FailCallback.REASON_VALIDATION);
						requestQueue.cancelQueue();
					} else if (wr.hasMore()) {
						enqueueFirst(wr);
					} else {
						wr.notifySuccess(gatt.getDevice());
					}
				}
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
					|| status == 8 /* GATT INSUF AUTHORIZATION */
					|| status == 137 /* GATT AUTH FAIL */) {
				log(Log.WARN, "Authentication required (" + status + ")");
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					// This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
					Log.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					manager.callbacks.onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
				// The request will be repeated when the bond state changes to BONDED.
				return;
			} else {
				Log.e(TAG, "onCharacteristicWrite error " + status);
				if (request instanceof WriteRequest) {
					request.notifyFail(gatt.getDevice(), status);
					// Automatically abort Reliable Write when write error happen
					if (requestQueue instanceof ReliableWriteRequest)
						requestQueue.cancelQueue();
				}
				awaitingRequest = null;
				onError(gatt.getDevice(), ERROR_WRITE_CHARACTERISTIC, status);
			}
			checkCondition();
			nextRequest(true);
		}

		@Override
		public final void onReliableWriteCompleted(@NonNull final BluetoothGatt gatt,
												   final int status) {
			final boolean execute = request.type == Request.Type.EXECUTE_RELIABLE_WRITE;
			reliableWriteInProgress = false;
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (execute) {
					log(Log.INFO, "Reliable Write executed");
					request.notifySuccess(gatt.getDevice());
				} else {
					log(Log.WARN, "Reliable Write aborted");
					request.notifySuccess(gatt.getDevice());
					requestQueue.notifyFail(gatt.getDevice(), FailCallback.REASON_REQUEST_FAILED);
				}
			} else {
				Log.e(TAG, "onReliableWriteCompleted execute " + execute + ", error " + status);
				request.notifyFail(gatt.getDevice(), status);
				onError(gatt.getDevice(), ERROR_RELIABLE_WRITE, status);
			}
			checkCondition();
			nextRequest(true);
		}

		@Override
		public void onDescriptorRead(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
			final byte[] data = descriptor.getValue();

			if (status == BluetoothGatt.GATT_SUCCESS) {
				log(Log.INFO, "Read Response received from descr. " + descriptor.getUuid() +
						", value: " + ParserUtils.parse(data));

				BleManagerHandler.this.onDescriptorRead(gatt, descriptor);
				if (request instanceof ReadRequest) {
					final ReadRequest request = (ReadRequest) BleManagerHandler.this.request;
					request.notifyValueChanged(gatt.getDevice(), data);
					if (request.hasMore()) {
						enqueueFirst(request);
					} else {
						request.notifySuccess(gatt.getDevice());
					}
				}
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
					|| status == 8 /* GATT INSUF AUTHORIZATION */
					|| status == 137 /* GATT AUTH FAIL */) {
				log(Log.WARN, "Authentication required (" + status + ")");
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					// This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
					Log.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					manager.callbacks.onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
				// The request will be repeated when the bond state changes to BONDED.
				return;
			} else {
				Log.e(TAG, "onDescriptorRead error " + status);
				if (request instanceof ReadRequest) {
					request.notifyFail(gatt.getDevice(), status);
				}
				awaitingRequest = null;
				onError(gatt.getDevice(), ERROR_READ_DESCRIPTOR, status);
			}
			checkCondition();
			nextRequest(true);
		}

		@Override
		public void onDescriptorWrite(final BluetoothGatt gatt,
									  final BluetoothGattDescriptor descriptor,
									  final int status) {
			final byte[] data = descriptor.getValue();

			if (status == BluetoothGatt.GATT_SUCCESS) {
				log(Log.INFO, "Data written to descr. " + descriptor.getUuid() +
						", value: " + ParserUtils.parse(data));

				if (isServiceChangedCCCD(descriptor)) {
					log(Log.INFO, "Service Changed notifications enabled");
				} else if (isCCCD(descriptor)) {
					if (data != null && data.length == 2 && data[1] == 0x00) {
						switch (data[0]) {
							case 0x00:
								log(Log.INFO, "Notifications and indications disabled");
								break;
							case 0x01:
								log(Log.INFO, "Notifications enabled");
								break;
							case 0x02:
								log(Log.INFO, "Indications enabled");
								break;
						}
						BleManagerHandler.this.onDescriptorWrite(gatt, descriptor);
					}
				} else {
					BleManagerHandler.this.onDescriptorWrite(gatt, descriptor);
				}
				if (request instanceof WriteRequest) {
					final WriteRequest wr = (WriteRequest) request;
					final boolean valid = wr.notifyPacketSent(gatt.getDevice(), data);
					if (!valid && requestQueue instanceof ReliableWriteRequest) {
						wr.notifyFail(gatt.getDevice(), FailCallback.REASON_VALIDATION);
						requestQueue.cancelQueue();
					} else if (wr.hasMore()) {
						enqueueFirst(wr);
					} else {
						wr.notifySuccess(gatt.getDevice());
					}
				}
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
					|| status == 8 /* GATT INSUF AUTHORIZATION */
					|| status == 137 /* GATT AUTH FAIL */) {
				log(Log.WARN, "Authentication required (" + status + ")");
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					// This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
					Log.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					manager.callbacks.onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
				// The request will be repeated when the bond state changes to BONDED.
				return;
			} else {
				Log.e(TAG, "onDescriptorWrite error " + status);
				if (request instanceof WriteRequest) {
					request.notifyFail(gatt.getDevice(), status);
					// Automatically abort Reliable Write when write error happen
					if (requestQueue instanceof ReliableWriteRequest)
						requestQueue.cancelQueue();
				}
				awaitingRequest = null;
				onError(gatt.getDevice(), ERROR_WRITE_DESCRIPTOR, status);
			}
			checkCondition();
			nextRequest(true);
		}

		@Override
		public void onCharacteristicChanged(final BluetoothGatt gatt,
											final BluetoothGattCharacteristic characteristic) {
			final byte[] data = characteristic.getValue();

			if (isServiceChangedCharacteristic(characteristic)) {
				// TODO this should be tested. Should services be invalidated?
				// Forbid enqueuing more operations.
				operationInProgress = true;
				// Clear queues, services are no longer valid.
				taskQueue.clear();
				initQueue = null;
				log(Log.INFO, "Service Changed indication received");
				log(Log.VERBOSE, "Discovering Services...");
				log(Log.DEBUG, "gatt.discoverServices()");
				gatt.discoverServices();
			} else {
				final BluetoothGattDescriptor cccd =
						characteristic.getDescriptor(BleManager.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
				final boolean notifications = cccd == null || cccd.getValue() == null ||
						cccd.getValue().length != 2 || cccd.getValue()[0] == 0x01;

				final String dataString = ParserUtils.parse(data);
				if (notifications) {
					log(Log.INFO, "Notification received from " +
							characteristic.getUuid() + ", value: " + dataString);
					onCharacteristicNotified(gatt, characteristic);
				} else { // indications
					log(Log.INFO, "Indication received from " +
							characteristic.getUuid() + ", value: " + dataString);
					onCharacteristicIndicated(gatt, characteristic);
				}
				if (batteryLevelNotificationCallback != null && isBatteryLevelCharacteristic(characteristic)) {
					batteryLevelNotificationCallback.notifyValueChanged(gatt.getDevice(), data);
				}
				// Notify the notification registered listener, if set
				final ValueChangedCallback request = notificationCallbacks.get(characteristic);
				if (request != null && request.matches(data)) {
					request.notifyValueChanged(gatt.getDevice(), data);
				}
				// If there is a value change request,
				if (awaitingRequest instanceof WaitForValueChangedRequest
						// registered for this characteristic
						&& awaitingRequest.characteristic == characteristic
						// and didn't have a trigger, or the trigger was started
						// (not necessarily completed)
						&& !awaitingRequest.isTriggerPending()) {
					final WaitForValueChangedRequest valueChangedRequest = (WaitForValueChangedRequest) awaitingRequest;
					if (valueChangedRequest.matches(data)) {
						// notify that new data was received.
						valueChangedRequest.notifyValueChanged(gatt.getDevice(), data);

						// If no more data are expected
						if (!valueChangedRequest.hasMore()) {
							// notify success,
							valueChangedRequest.notifySuccess(gatt.getDevice());
							// and proceed to the next request only if the trigger has completed.
							// Otherwise, the next request will be started when the request's callback
							// will be received.
							awaitingRequest = null;
							if (valueChangedRequest.isTriggerCompleteOrNull()) {
								nextRequest(true);
							}
						}
					}
				}
				if (checkCondition()) {
					nextRequest(true);
				}
			}
		}

		@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
		@Override
		public final void onMtuChanged(@NonNull final BluetoothGatt gatt,
									   @IntRange(from = 23, to = 517) final int mtu,
									   final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				log(Log.INFO, "MTU changed to: " + mtu);
				BleManagerHandler.this.mtu = mtu;
				BleManagerHandler.this.onMtuChanged(gatt, mtu);
				if (request instanceof MtuRequest) {
					((MtuRequest) request).notifyMtuChanged(gatt.getDevice(), mtu);
					request.notifySuccess(gatt.getDevice());
				}
			} else {
				Log.e(TAG, "onMtuChanged error: " + status + ", mtu: " + mtu);
				if (request instanceof MtuRequest) {
					request.notifyFail(gatt.getDevice(), status);
					awaitingRequest = null;
				}
				onError(gatt.getDevice(), ERROR_MTU_REQUEST, status);
			}
			checkCondition();
			nextRequest(true);
		}

		/**
		 * Callback indicating the connection parameters were updated. Works on Android 8+.
		 *
		 * @param gatt     GATT client involved.
		 * @param interval Connection interval used on this connection, 1.25ms unit.
		 *                 Valid range is from 6 (7.5ms) to 3200 (4000ms).
		 * @param latency  Slave latency for the connection in number of connection events.
		 *                 Valid range is from 0 to 499.
		 * @param timeout  Supervision timeout for this connection, in 10ms unit.
		 *                 Valid range is from 10 (0.1s) to 3200 (32s)
		 * @param status   {@link BluetoothGatt#GATT_SUCCESS} if the connection has been updated
		 *                 successfully.
		 */
		@RequiresApi(api = Build.VERSION_CODES.O)
		// @Override
		public final void onConnectionUpdated(@NonNull final BluetoothGatt gatt,
											  @IntRange(from = 6, to = 3200) final int interval,
											  @IntRange(from = 0, to = 499) final int latency,
											  @IntRange(from = 10, to = 3200) final int timeout,
											  final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				log(Log.INFO, "Connection parameters updated " +
						"(interval: " + (interval * 1.25) + "ms," +
						" latency: " + latency + ", timeout: " + (timeout * 10) + "ms)");
				BleManagerHandler.this.onConnectionUpdated(gatt, interval, latency, timeout);

				// This callback may be called af any time, also when some other request is executed
				if (request instanceof ConnectionPriorityRequest) {
					((ConnectionPriorityRequest) request)
							.notifyConnectionPriorityChanged(gatt.getDevice(), interval, latency, timeout);
					request.notifySuccess(gatt.getDevice());
				}
			} else if (status == 0x3b) { // HCI_ERR_UNACCEPT_CONN_INTERVAL
				Log.e(TAG, "onConnectionUpdated received status: Unacceptable connection interval, " +
						"interval: " + interval + ", latency: " + latency + ", timeout: " + timeout);
				log(Log.WARN, "Connection parameters update failed with status: " +
						"UNACCEPT CONN INTERVAL (0x3b) (interval: " + (interval * 1.25) + "ms, " +
						"latency: " + latency + ", timeout: " + (timeout * 10) + "ms)");

				// This callback may be called af any time, also when some other request is executed
				if (request instanceof ConnectionPriorityRequest) {
					request.notifyFail(gatt.getDevice(), status);
					awaitingRequest = null;
				}
			} else {
				Log.e(TAG, "onConnectionUpdated received status: " + status + ", " +
						"interval: " + interval + ", latency: " + latency + ", timeout: " + timeout);
				log(Log.WARN, "Connection parameters update failed with " +
						"status " + status + " (interval: " + (interval * 1.25) + "ms, " +
						"latency: " + latency + ", timeout: " + (timeout * 10) + "ms)");

				// This callback may be called af any time, also when some other request is executed
				if (request instanceof ConnectionPriorityRequest) {
					request.notifyFail(gatt.getDevice(), status);
					awaitingRequest = null;
				}
				manager.callbacks.onError(gatt.getDevice(), ERROR_CONNECTION_PRIORITY_REQUEST, status);
			}
			if (connectionPriorityOperationInProgress) {
				connectionPriorityOperationInProgress = false;
				checkCondition();
				nextRequest(true);
			}
		}

		@RequiresApi(api = Build.VERSION_CODES.O)
		@Override
		public final void onPhyUpdate(@NonNull final BluetoothGatt gatt,
									  @PhyValue final int txPhy, @PhyValue final int rxPhy,
									  final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				log(Log.INFO, "PHY updated (TX: " + ParserUtils.phyToString(txPhy) +
						", RX: " + ParserUtils.phyToString(rxPhy) + ")");
				if (request instanceof PhyRequest) {
					((PhyRequest) request).notifyPhyChanged(gatt.getDevice(), txPhy, rxPhy);
					request.notifySuccess(gatt.getDevice());
				}
			} else {
				log(Log.WARN, "PHY updated failed with status " + status);
				if (request instanceof PhyRequest) {
					request.notifyFail(gatt.getDevice(), status);
					awaitingRequest = null;
				}
				manager.callbacks.onError(gatt.getDevice(), ERROR_PHY_UPDATE, status);
			}
			// PHY update may be requested by the other side, or the Android, without explicitly
			// requesting it. Proceed with the queue only when update was requested.
			if (checkCondition() || request instanceof PhyRequest) {
				nextRequest(true);
			}
		}

		@RequiresApi(api = Build.VERSION_CODES.O)
		@Override
		public final void onPhyRead(@NonNull final BluetoothGatt gatt,
									@PhyValue final int txPhy, @PhyValue final int rxPhy,
									final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				log(Log.INFO, "PHY read (TX: " + ParserUtils.phyToString(txPhy) +
						", RX: " + ParserUtils.phyToString(rxPhy) + ")");
				if (request instanceof PhyRequest) {
					((PhyRequest) request).notifyPhyChanged(gatt.getDevice(), txPhy, rxPhy);
					request.notifySuccess(gatt.getDevice());
				}
			} else {
				log(Log.WARN, "PHY read failed with status " + status);
				if (request instanceof PhyRequest) {
					request.notifyFail(gatt.getDevice(), status);
				}
				awaitingRequest = null;
				manager.callbacks.onError(gatt.getDevice(), ERROR_READ_PHY, status);
			}
			checkCondition();
			nextRequest(true);
		}

		@Override
		public final void onReadRemoteRssi(@NonNull final BluetoothGatt gatt,
										   @IntRange(from = -128, to = 20) final int rssi,
										   final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				log(Log.INFO, "Remote RSSI received: " + rssi + " dBm");
				if (request instanceof ReadRssiRequest) {
					((ReadRssiRequest) request).notifyRssiRead(gatt.getDevice(), rssi);
					request.notifySuccess(gatt.getDevice());
				}
			} else {
				log(Log.WARN, "Reading remote RSSI failed with status " + status);
				if (request instanceof ReadRssiRequest) {
					request.notifyFail(gatt.getDevice(), status);
				}
				awaitingRequest = null;
				manager.callbacks.onError(gatt.getDevice(), ERROR_READ_RSSI, status);
			}
			checkCondition();
			nextRequest(true);
		}
	};

	final void onCharacteristicReadRequest(@NonNull final BluetoothGattServer server,
										   @NonNull final BluetoothDevice device,
										   final int requestId, final int offset,
										   @NonNull final BluetoothGattCharacteristic characteristic) {
		log(Log.DEBUG, "[Server callback] Read request for characteristic " + characteristic.getUuid()
				+ " (requestId=" + requestId + ", offset: " + offset + ")");
		if (offset == 0)
			log(Log.INFO, "[Server] READ request for characteristic " + characteristic.getUuid() + " received");

		byte[] data = characteristicValues == null || !characteristicValues.containsKey(characteristic)
				? characteristic.getValue() : characteristicValues.get(characteristic);

		WaitForReadRequest waitForReadRequest = null;
		// First, try to get the data from the WaitForReadRequest if the request awaits,
		if (awaitingRequest instanceof WaitForReadRequest
				// is registered for this characteristic
				&& awaitingRequest.characteristic == characteristic
				// and didn't have a trigger, or the trigger was started
				// (not necessarily completed)
				&& !awaitingRequest.isTriggerPending()) {
			waitForReadRequest = (WaitForReadRequest) awaitingRequest;
			waitForReadRequest.setDataIfNull(data);
			data = waitForReadRequest.getData(mtu);
		}
		// If data are longer than MTU - 1, cut the array. Only ATT_MTU - 1 bytes can be sent in Long Read.
		if (data != null && data.length > mtu - 1) {
			data = Bytes.copy(data, offset, mtu - 1);
		}

		sendResponse(server, device, BluetoothGatt.GATT_SUCCESS, requestId, offset, data);

		if (waitForReadRequest != null) {
			waitForReadRequest.notifyPacketRead(device, data);

			// If the request is complete, start next one.
			if (!waitForReadRequest.hasMore() && (data == null || data.length < mtu - 1)) {
				waitForReadRequest.notifySuccess(device);
				awaitingRequest = null;
				nextRequest(true);
			}
		} else if (checkCondition()) {
			nextRequest(true);
		}
	}

	final void onCharacteristicWriteRequest(@NonNull final BluetoothGattServer server,
											@NonNull final BluetoothDevice device, final int requestId,
											@NonNull final BluetoothGattCharacteristic characteristic,
											final boolean preparedWrite, final boolean responseNeeded,
											final int offset, @NonNull final byte[] value) {
		log(Log.DEBUG, "[Server callback] Write " + (responseNeeded ? "request" : "command")
				+ " to characteristic " + characteristic.getUuid()
				+ " (requestId=" + requestId + ", prepareWrite=" + preparedWrite + ", responseNeeded="
				+ responseNeeded + ", offset: " + offset + ", value=" + ParserUtils.parseDebug(value) + ")");
		if (offset == 0) {
			final String type = responseNeeded ? "WRITE REQUEST" : "WRITE COMMAND";
			final String option = preparedWrite ? "Prepare " : "";
			log(Log.INFO, "[Server] " + option + type + " for characteristic " + characteristic.getUuid()
					+ " received, value: " + ParserUtils.parse(value));
		}

		if (responseNeeded) {
			sendResponse(server, device, BluetoothGatt.GATT_SUCCESS, requestId, offset, value);
		}

		// If Prepare Write or Long Write is sent, store the data in a temporary queue until it's executed.
		if (preparedWrite) {
			if (preparedValues == null) {
				preparedValues = new LinkedList<>();
			}
			if (offset == 0) {
				// Add new value to the operations.
				preparedValues.offer(new Pair<>(characteristic, value));
			} else {
				// Concatenate the value to the end of previous value, if the previous request was
				// also for the same characteristic.
				final Pair<Object, byte[]> last = preparedValues.peekLast();
				if (last != null && characteristic.equals(last.first)) {
					preparedValues.pollLast();
					preparedValues.offer(new Pair<>(characteristic, Bytes.concat(last.second, value, offset)));
				} else {
					prepareError = BluetoothGatt.GATT_INVALID_OFFSET;
				}
			}
		} else {
			// Otherwise, save the data immediately.
			if (assignAndNotify(device, characteristic, value) || checkCondition()) {
				nextRequest(true);
			}
		}
	}

	final void onDescriptorReadRequest(@NonNull final BluetoothGattServer server,
									   @NonNull final BluetoothDevice device, final int requestId, final int offset,
									   @NonNull final BluetoothGattDescriptor descriptor) {
		log(Log.DEBUG, "[Server callback] Read request for descriptor " + descriptor.getUuid() + " (requestId=" + requestId + ", offset: " + offset + ")");
		if (offset == 0)
			log(Log.INFO, "[Server] READ request for descriptor " + descriptor.getUuid() + " received");

		byte[] data = descriptorValues == null || !descriptorValues.containsKey(descriptor)
				? descriptor.getValue() : descriptorValues.get(descriptor);

		WaitForReadRequest waitForReadRequest = null;
		// First, try to get the data from the WaitForReadRequest if the request awaits,
		if (awaitingRequest instanceof WaitForReadRequest
				// is registered for this descriptor
				&& awaitingRequest.descriptor == descriptor
				// and didn't have a trigger, or the trigger was started
				// (not necessarily completed)
				&& !awaitingRequest.isTriggerPending()) {
			waitForReadRequest = (WaitForReadRequest) awaitingRequest;
			waitForReadRequest.setDataIfNull(data);
			data = waitForReadRequest.getData(mtu);
		}
		// If data are longer than MTU - 1, cut the array. Only ATT_MTU - 1 bytes can be sent in Long Read.
		if (data != null && data.length > mtu - 1) {
			data = Bytes.copy(data, offset, mtu - 1);
		}

		sendResponse(server, device, BluetoothGatt.GATT_SUCCESS, requestId, offset, data);

		if (waitForReadRequest != null) {
			waitForReadRequest.notifyPacketRead(device, data);

			// If the request is complete, start next one.
			if (!waitForReadRequest.hasMore() && (data == null || data.length < mtu - 1)) {
				waitForReadRequest.notifySuccess(device);
				awaitingRequest = null;
				nextRequest(true);
			}
		} else if (checkCondition()) {
			nextRequest(true);
		}
	}

	final void onDescriptorWriteRequest(@NonNull final BluetoothGattServer server,
										@NonNull final BluetoothDevice device, final int requestId,
										@NonNull final BluetoothGattDescriptor descriptor,
										final boolean preparedWrite, final boolean responseNeeded,
										final int offset, @NonNull final byte[] value) {
		log(Log.DEBUG, "[Server callback] Write " + (responseNeeded ? "request" : "command")
				+ " to descriptor " + descriptor.getUuid()
				+ " (requestId=" + requestId + ", prepareWrite=" + preparedWrite + ", responseNeeded="
				+ responseNeeded + ", offset: " + offset + ", value=" + ParserUtils.parseDebug(value) + ")");
		if (offset == 0) {
			final String type = responseNeeded ? "WRITE REQUEST" : "WRITE COMMAND";
			final String option = preparedWrite ? "Prepare " : "";
			log(Log.INFO, "[Server] " + option + type + " request for descriptor " + descriptor.getUuid()
					+ " received, value: " + ParserUtils.parse(value));
		}

		if (responseNeeded) {
			sendResponse(server, device, BluetoothGatt.GATT_SUCCESS, requestId, offset, value);
		}

		// If Prepare Write or Long Write is sent, store the data in a temporary queue until it's executed.
		if (preparedWrite) {
			if (preparedValues == null) {
				preparedValues = new LinkedList<>();
			}
			if (offset == 0) {
				// Add new value to the operations.
				preparedValues.offer(new Pair<>(descriptor, value));
			} else {
				// Concatenate the value to the end of previous value, if the previous request was
				// also for the same descriptor.
				final Pair<Object, byte[]> last = preparedValues.peekLast();
				if (last != null && descriptor.equals(last.first)) {
					preparedValues.pollLast();
					preparedValues.offer(new Pair<>(descriptor, Bytes.concat(last.second, value, offset)));
				} else {
					prepareError = BluetoothGatt.GATT_INVALID_OFFSET;
				}
			}
		} else {
			// Otherwise, save the data immediately.
			if (assignAndNotify(device, descriptor, value) || checkCondition()) {
				nextRequest(true);
			}
		}
	}

	final void onExecuteWrite(@NonNull final BluetoothGattServer server,
							  @NonNull final BluetoothDevice device, final int requestId,
							  final boolean execute) {
		log(Log.DEBUG, "[Server callback] Execute write request (requestId=" + requestId + ", execute=" + execute + ")");
		if (execute) {
			final Deque<Pair<Object, byte[]>> values = preparedValues;
			log(Log.INFO, "[Server] Execute write request received");
			preparedValues = null;
			if (prepareError != 0) {
				sendResponse(server, device, prepareError, requestId, 0, null);
				prepareError = 0;
				return;
			}
			sendResponse(server, device, BluetoothGatt.GATT_SUCCESS, requestId, 0, null);

			if (values == null || values.isEmpty()) {
				return;
			}
			boolean startNextRequest = false;
			for (final Pair<Object, byte[]> value: values) {
				if (value.first instanceof BluetoothGattCharacteristic) {
					final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) value.first;
					startNextRequest = assignAndNotify(device, characteristic, value.second) || startNextRequest;
				} else if (value.first instanceof BluetoothGattDescriptor){
					final BluetoothGattDescriptor descriptor = (BluetoothGattDescriptor) value.first;
					startNextRequest = assignAndNotify(device, descriptor, value.second) || startNextRequest;
				}
			}
			if (checkCondition() || startNextRequest) {
				nextRequest(true);
			}
		} else {
			log(Log.INFO, "[Server] Cancel write request received");
			preparedValues = null;
			sendResponse(server, device, BluetoothGatt.GATT_SUCCESS, requestId, 0, null);
		}
	}

	final void onNotificationSent(@NonNull final BluetoothGattServer server,
								  @NonNull final BluetoothDevice device, final int status) {
		log(Log.DEBUG, "[Server callback] Notification sent (status=" + status + ")");
		if (status == BluetoothGatt.GATT_SUCCESS) {
			notifyNotificationSent(device);
		} else {
			Log.e(TAG, "onNotificationSent error " + status);
			if (request instanceof WriteRequest) {
				request.notifyFail(device, status);
			}
			awaitingRequest = null;
			onError(device, ERROR_NOTIFY, status);
		}
		checkCondition();
		nextRequest(true);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
	final void onMtuChanged(@NonNull final BluetoothGattServer server,
							@NonNull final BluetoothDevice device,
							final int mtu) {
		log(Log.INFO, "[Server] MTU changed to: " + mtu);
		BleManagerHandler.this.mtu = mtu;
		checkCondition();
		nextRequest(false);
	}

	private void notifyNotificationSent(@NonNull final BluetoothDevice device) {
		if (request instanceof WriteRequest) {
			final WriteRequest wr = (WriteRequest) request;
			switch (wr.type) {
				case NOTIFY:
					log(Log.INFO, "[Server] Notification sent");
					break;
				case INDICATE:
					log(Log.INFO, "[Server] Indication sent");
					break;
			}
			wr.notifyPacketSent(device, wr.characteristic.getValue());
			if (wr.hasMore()) {
				enqueueFirst(wr);
			} else {
				wr.notifySuccess(device);
			}
		}
	}

	private boolean assignAndNotify(@NonNull final BluetoothDevice device,
									@NonNull final BluetoothGattCharacteristic characteristic,
									@NonNull final byte[] value) {
		final boolean isShared = characteristicValues == null || !characteristicValues.containsKey(characteristic);
		if (isShared) {
			characteristic.setValue(value);
		} else {
			characteristicValues.put(characteristic, value);
		}
		// Notify listener
		ValueChangedCallback callback;
		if ((callback = notificationCallbacks.get(characteristic)) != null) {
			callback.notifyValueChanged(device, value);
		}

		// Check if a request awaits,
		if (awaitingRequest instanceof WaitForValueChangedRequest
				// is registered for this characteristic
				&& awaitingRequest.characteristic == characteristic
				// and didn't have a trigger, or the trigger was started
				// (not necessarily completed)
				&& !awaitingRequest.isTriggerPending()) {
			final WaitForValueChangedRequest waitForWrite = (WaitForValueChangedRequest) awaitingRequest;
			if (waitForWrite.matches(value)) {
				// notify that new data was received.
				waitForWrite.notifyValueChanged(device, value);

				// If no more data are expected
				if (!waitForWrite.hasMore()) {
					// notify success,
					waitForWrite.notifySuccess(device);
					// and proceed to the next request only if the trigger has completed.
					// Otherwise, the next request will be started when the request's callback
					// will be received.
					awaitingRequest = null;
					return waitForWrite.isTriggerCompleteOrNull();
				}
			}
		}
		return false;
	}

	private boolean assignAndNotify(@NonNull final BluetoothDevice device,
									@NonNull final BluetoothGattDescriptor descriptor,
									@NonNull final byte[] value) {
		final boolean isShared = descriptorValues == null || !descriptorValues.containsKey(descriptor);
		if (isShared) {
			descriptor.setValue(value);
		} else {
			descriptorValues.put(descriptor, value);
		}
		// Notify listener
		ValueChangedCallback callback;
		if ((callback = notificationCallbacks.get(descriptor)) != null) {
			callback.notifyValueChanged(device, value);
		}

		// Check if a request awaits,
		if (awaitingRequest instanceof WaitForValueChangedRequest
				// is registered for this descriptor
				&& awaitingRequest.descriptor == descriptor
				// and didn't have a trigger, or the trigger was started
				// (not necessarily completed)
				&& !awaitingRequest.isTriggerPending()) {
			final WaitForValueChangedRequest waitForWrite = (WaitForValueChangedRequest) awaitingRequest;
			if (waitForWrite.matches(value)) {
				// notify that new data was received.
				waitForWrite.notifyValueChanged(device, value);

				// If no more data are expected
				if (!waitForWrite.hasMore()) {
					// notify success,
					waitForWrite.notifySuccess(device);
					// and proceed to the next request only if the trigger has completed.
					// Otherwise, the next request will be started when the request's callback
					// will be received.
					awaitingRequest = null;
					return waitForWrite.isTriggerCompleteOrNull();
				}
			}
		}
		return false;
	}

	private void sendResponse(@NonNull final BluetoothGattServer server,
							  @NonNull final BluetoothDevice device, final int status,
							  final int requestId, final int offset,
							  @Nullable final byte[] response) {
		String msg;
		switch (status) {
			case BluetoothGatt.GATT_SUCCESS: 				msg = "GATT_SUCCESS"; break;
			case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED: 	msg = "GATT_REQUEST_NOT_SUPPORTED"; break;
			case BluetoothGatt.GATT_INVALID_OFFSET: 		msg = "GATT_INVALID_OFFSET"; break;
			default: throw new InvalidParameterException();
		}
		log(Log.DEBUG, "server.sendResponse(" + msg + ", offset=" + offset + ", value=" + ParserUtils.parseDebug(response) + ")");
		server.sendResponse(device, requestId, status, offset, response);
		log(Log.VERBOSE, "[Server] Response sent");
	}

	private boolean checkCondition() {
		if (awaitingRequest instanceof ConditionalWaitRequest) {
			final ConditionalWaitRequest cwr = (ConditionalWaitRequest) awaitingRequest;
			if (cwr.isFulfilled()) {
				cwr.notifySuccess(bluetoothDevice);
				awaitingRequest = null;
				return true;
			}
		}
		return false;
	}

	/**
	 * Executes the next request. If the last element from the initialization queue has
	 * been executed the {@link #onDeviceReady()} callback is called.
	 */
	@SuppressWarnings("ConstantConditions")
	private synchronized void nextRequest(final boolean force) {
		if (force) {
			operationInProgress = awaitingRequest != null;
		}

		if (operationInProgress) {
			return;
		}

		// Get the first request from the init queue
		Request request = null;
		try {
			// If Request set is present, try taking next request from it
			if (requestQueue != null) {
				if (requestQueue.hasMore()) {
					request = requestQueue.getNext().setRequestHandler(this);
				} else {
					// Set is completed
					requestQueue.notifySuccess(bluetoothDevice);
					requestQueue = null;
				}
			}
			// Request wasn't obtained from the request set? Take next one from the queue.
			if (request == null) {
				request = initQueue != null ? initQueue.poll() : null;
			}
		} catch (final Exception e) {
			// On older Android versions poll() may in some cases throw NoSuchElementException,
			// as it's using removeFirst() internally.
			// See: https://github.com/NordicSemiconductor/Android-BLE-Library/issues/37
			request = null;
		}

		// Are we done with initializing?
		if (request == null) {
			if (initQueue != null) {
				initQueue = null; // release the queue

				// Set the 'operation in progress' flag, so any request made in onDeviceReady()
				// will not start new nextRequest() call.
				operationInProgress = true;
				ready = true;
				onDeviceReady();
				manager.callbacks.onDeviceReady(bluetoothGatt.getDevice());
				if (connectRequest != null) {
					connectRequest.notifySuccess(connectRequest.getDevice());
					connectRequest = null;
				}
			}
			// If so, we can continue with the task queue
			try {
				request = taskQueue.remove();
			} catch (final Exception e) {
				// No more tasks to perform
				operationInProgress = false;
				this.request = null;
				onManagerReady();
				return;
			}
		}

		boolean result = false;
		operationInProgress = true;
		this.request = request;

		if (request instanceof AwaitingRequest) {
			final AwaitingRequest r = (AwaitingRequest) request;

			// The WAIT_FOR_* request types may override the request with a trigger.
			// This is to ensure that the trigger is done after the awaitingRequest was set.
			int requiredProperty = 0;
			switch (request.type) {
				case WAIT_FOR_NOTIFICATION:
					requiredProperty = BluetoothGattCharacteristic.PROPERTY_NOTIFY;
					break;
				case WAIT_FOR_INDICATION:
					requiredProperty = BluetoothGattCharacteristic.PROPERTY_INDICATE;
					break;
				case WAIT_FOR_READ:
					requiredProperty = BluetoothGattCharacteristic.PROPERTY_READ;
					break;
				case WAIT_FOR_WRITE:
					requiredProperty = BluetoothGattCharacteristic.PROPERTY_WRITE
							| BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
							| BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE;
					break;
			}
			result = connected && bluetoothDevice != null
					&& (r.characteristic == null ||
					   (r.characteristic.getProperties() & requiredProperty) != 0);
			if (result) {
				if (r instanceof ConditionalWaitRequest) {
					final ConditionalWaitRequest cwr = (ConditionalWaitRequest) r;
					if (cwr.isFulfilled()) {
						cwr.notifyStarted(bluetoothDevice);
						cwr.notifySuccess(bluetoothDevice);
						nextRequest(true);
						return;
					}
				}
				awaitingRequest = r;

				if (r.getTrigger() != null) {
					// Call notifyStarted for the awaiting request.
					r.notifyStarted(bluetoothDevice);

					// If the request has another request set as a trigger, update the
					// request with the trigger.
					this.request = request = r.getTrigger();
				}
			}
		}
		// Call notifyStarted on the request before it's executed.
		if (request.type == Request.Type.CONNECT) {
			// When the Connect Request is started, the bluetoothDevice is not set yet.
			// It may also be a connect request to a different device, which is an error
			// that is handled in internalConnect()
			final ConnectRequest cr = (ConnectRequest) request;
			cr.notifyStarted(cr.getDevice());
		} else {
			if (bluetoothDevice != null) {
				request.notifyStarted(bluetoothDevice);
			} else {
				// The device wasn't connected before. Target is unknown.
				request.notifyInvalidRequest();

				awaitingRequest = null;
				nextRequest(true);
				return;
			}
		}

		switch (request.type) {
			case CONNECT: {
				final ConnectRequest cr = (ConnectRequest) request;
				connectRequest = cr;
				this.request = null;
				result = internalConnect(cr.getDevice(), cr);
				break;
			}
			case DISCONNECT: {
				result = internalDisconnect();
				break;
			}
			case CREATE_BOND: {
				result = internalCreateBond();
				break;
			}
			case REMOVE_BOND: {
				result = internalRemoveBond();
				break;
			}
			case SET: {
				requestQueue = (RequestQueue) request;
				nextRequest(true);
				return;
			}
			case READ: {
				result = internalReadCharacteristic(request.characteristic);
				break;
			}
			case WRITE: {
				final WriteRequest wr = (WriteRequest) request;
				final BluetoothGattCharacteristic characteristic = request.characteristic;
				if (characteristic != null) {
					characteristic.setValue(wr.getData(mtu));
					characteristic.setWriteType(wr.getWriteType());
				}
				result = internalWriteCharacteristic(characteristic);
				break;
			}
			case READ_DESCRIPTOR: {
				result = internalReadDescriptor(request.descriptor);
				break;
			}
			case WRITE_DESCRIPTOR: {
				final WriteRequest wr = (WriteRequest) request;
				final BluetoothGattDescriptor descriptor = request.descriptor;
				if (descriptor != null) {
					descriptor.setValue(wr.getData(mtu));
				}
				result = internalWriteDescriptor(descriptor);
				break;
			}
			case NOTIFY:
			case INDICATE: {
				final WriteRequest wr = (WriteRequest) request;
				final BluetoothGattCharacteristic characteristic = request.characteristic;
				if (characteristic != null) {
					characteristic.setValue(wr.getData(mtu));
					if (characteristicValues != null && characteristicValues.containsKey(characteristic))
						characteristicValues.put(characteristic, characteristic.getValue());
				}
				result = internalSendNotification(request.characteristic, request.type == Request.Type.INDICATE);
				break;
			}
			case SET_VALUE: {
				final SetValueRequest svr = (SetValueRequest) request;
				if (svr.characteristic != null) {
					if (characteristicValues != null && characteristicValues.containsKey(svr.characteristic))
						characteristicValues.put(svr.characteristic, svr.getData(mtu));
					else
						svr.characteristic.setValue(svr.getData(mtu));
					result = true;
					svr.notifySuccess(bluetoothDevice);
					nextRequest(true);
				}
				break;
			}
			case SET_DESCRIPTOR_VALUE: {
				final SetValueRequest svr = (SetValueRequest) request;
				if (svr.descriptor != null) {
					if (descriptorValues != null && descriptorValues.containsKey(svr.descriptor))
						descriptorValues.put(svr.descriptor, svr.getData(mtu));
					else
						svr.descriptor.setValue(svr.getData(mtu));
					result = true;
					svr.notifySuccess(bluetoothDevice);
					nextRequest(true);
				}
				break;
			}
			case BEGIN_RELIABLE_WRITE: {
				result = internalBeginReliableWrite();
				// There is no callback for begin reliable write request.
				// Notify success and start next request immediately.
				if (result) {
					this.request.notifySuccess(bluetoothDevice);
					nextRequest(true);
					return;
				}
				break;
			}
			case EXECUTE_RELIABLE_WRITE: {
				result = internalExecuteReliableWrite();
				break;
			}
			case ABORT_RELIABLE_WRITE: {
				result = internalAbortReliableWrite();
				break;
			}
			case ENABLE_NOTIFICATIONS: {
				result = internalEnableNotifications(request.characteristic);
				break;
			}
			case ENABLE_INDICATIONS: {
				result = internalEnableIndications(request.characteristic);
				break;
			}
			case DISABLE_NOTIFICATIONS: {
				result = internalDisableNotifications(request.characteristic);
				break;
			}
			case DISABLE_INDICATIONS: {
				result = internalDisableIndications(request.characteristic);
				break;
			}
			case READ_BATTERY_LEVEL: {
				result = internalReadBatteryLevel();
				break;
			}
			case ENABLE_BATTERY_LEVEL_NOTIFICATIONS: {
				result = internalSetBatteryNotifications(true);
				break;
			}
			case DISABLE_BATTERY_LEVEL_NOTIFICATIONS: {
				result = internalSetBatteryNotifications(false);
				break;
			}
			case ENABLE_SERVICE_CHANGED_INDICATIONS: {
				result = ensureServiceChangedEnabled();
				break;
			}
			case REQUEST_MTU: {
				final MtuRequest mr = (MtuRequest) request;
				if (mtu != mr.getRequiredMtu()
						&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					result = internalRequestMtu(mr.getRequiredMtu());
				} else {
					result = connected;
					if (result) {
						mr.notifyMtuChanged(bluetoothDevice, mtu);
						mr.notifySuccess(bluetoothDevice);
						nextRequest(true);
						return;
					}
				}
				break;
			}
			case REQUEST_CONNECTION_PRIORITY: {
				final ConnectionPriorityRequest cpr = (ConnectionPriorityRequest) request;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					connectionPriorityOperationInProgress = true;
					result = internalRequestConnectionPriority(cpr.getRequiredPriority());
				} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					result = internalRequestConnectionPriority(cpr.getRequiredPriority());
					// There is no callback for requestConnectionPriority(...) before Android Oreo.
					// Let's give it some time to finish as the request is an asynchronous operation.
					if (result) {
						final BluetoothDevice device = bluetoothDevice;
						handler.postDelayed(() -> {
							cpr.notifySuccess(device);
							nextRequest(true);
						}, 100);
					}
				}
				break;
			}
			case SET_PREFERRED_PHY: {
				final PhyRequest pr = (PhyRequest) request;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					result = internalSetPreferredPhy(pr.getPreferredTxPhy(),
							pr.getPreferredRxPhy(), pr.getPreferredPhyOptions());
				} else {
					result = connected;
					if (result) {
						pr.notifyLegacyPhy(bluetoothDevice);
						pr.notifySuccess(bluetoothDevice);
						nextRequest(true);
						return;
					}
				}
				break;
			}
			case READ_PHY: {
				final PhyRequest pr = (PhyRequest) request;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					result = internalReadPhy();
				} else {
					result = connected;
					if (result) {
						pr.notifyLegacyPhy(bluetoothDevice);
						pr.notifySuccess(bluetoothDevice);
						nextRequest(true);
						return;
					}
				}
				break;
			}
			case READ_RSSI: {
				result = internalReadRssi();
				break;
			}
			case REFRESH_CACHE: {
				final Request r = request;
				result = internalRefreshDeviceCache();
				if (result) {
					final BluetoothDevice device = bluetoothDevice;
					handler.postDelayed(() -> {
						log(Log.INFO, "Cache refreshed");
						r.notifySuccess(device);
						this.request = null;
						if (awaitingRequest != null) {
							awaitingRequest.notifyFail(device, FailCallback.REASON_NULL_ATTRIBUTE);
							awaitingRequest = null;
						}
						taskQueue.clear();
						initQueue = null;
						if (connected) {
							// Invalidate all services and characteristics
							onDeviceDisconnected();
							// And discover services again
							log(Log.VERBOSE, "Discovering Services...");
							log(Log.DEBUG, "gatt.discoverServices()");
							bluetoothGatt.discoverServices();
						}
					}, 200);
				}
				break;
			}
			case SLEEP: {
				final BluetoothDevice device = bluetoothDevice;
				if (device != null) {
					final SleepRequest sr = (SleepRequest) request;
					log(Log.DEBUG, "sleep(" + sr.getDelay() + ")");
					handler.postDelayed(() -> {
						sr.notifySuccess(device);
						nextRequest(true);
					}, sr.getDelay());
					result = true;
				}
				break;
			}
			case WAIT_FOR_NOTIFICATION:
			case WAIT_FOR_INDICATION:
				// Those were handled before.
				break;
		}
		// The result may be false if given characteristic or descriptor were not found
		// on the device, or the feature is not supported on the Android.
		// In that case, proceed with next operation and ignore the one that failed.
		if (!result) {
			this.request.notifyFail(bluetoothDevice,
					connected ?
							FailCallback.REASON_NULL_ATTRIBUTE :
							BluetoothAdapter.getDefaultAdapter().isEnabled() ?
									FailCallback.REASON_DEVICE_DISCONNECTED :
									FailCallback.REASON_BLUETOOTH_DISABLED);
			awaitingRequest = null;
			connectionPriorityOperationInProgress = false;
			nextRequest(true);
		}
	}

	// Helper methods

	/**
	 * Returns true if this descriptor is from the Service Changed characteristic.
	 *
	 * @param descriptor the descriptor to be checked
	 * @return true if the descriptor belongs to the Service Changed characteristic
	 */
	private boolean isServiceChangedCCCD(@Nullable final BluetoothGattDescriptor descriptor) {
		return descriptor != null &&
				BleManager.SERVICE_CHANGED_CHARACTERISTIC.equals(descriptor.getCharacteristic().getUuid());
	}

	/**
	 * Returns true if this is the Service Changed characteristic.
	 *
	 * @param characteristic the characteristic to be checked
	 * @return true if it is the Service Changed characteristic
	 */
	private boolean isServiceChangedCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic) {
		return characteristic != null &&
				BleManager.SERVICE_CHANGED_CHARACTERISTIC.equals(characteristic.getUuid());
	}

	/**
	 * Returns true if the characteristic is the Battery Level characteristic.
	 *
	 * @param characteristic the characteristic to be checked
	 * @return true if the characteristic is the Battery Level characteristic.
	 */
	@Deprecated
	private boolean isBatteryLevelCharacteristic(@Nullable final BluetoothGattCharacteristic characteristic) {
		return characteristic != null &&
				BleManager.BATTERY_LEVEL_CHARACTERISTIC.equals(characteristic.getUuid());
	}

	/**
	 * Returns true if this descriptor is a Client Characteristic Configuration descriptor (CCCD).
	 *
	 * @param descriptor the descriptor to be checked
	 * @return true if the descriptor is a CCCD
	 */
	private boolean isCCCD(@Nullable final BluetoothGattDescriptor descriptor) {
		return descriptor != null &&
				BleManager.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID.equals(descriptor.getUuid());
	}

//	private boolean isReliableWriteSupported(@Nullable final BluetoothGattCharacteristic characteristic) {
//		if (characteristic == null)
//			return false;
//		final BluetoothGattDescriptor cep = characteristic.getDescriptor(BleServerManager.CHARACTERISTIC_EXTENDED_PROPERTIES_DESCRIPTOR_UUID);
//		return cep != null && cep.getValue() != null && cep.getValue().length >= 2 && (cep.getValue()[0] & 0x01) != 0;
//	}

	private void log(final int priority, @NonNull final String message) {
		manager.log(priority, message);
	}
}