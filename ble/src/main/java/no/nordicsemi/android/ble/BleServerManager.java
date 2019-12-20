package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import no.nordicsemi.android.ble.utils.ILogger;

/**
 * The manager for local GATT server. To be used with one or more instances of {@link BleManager}
 *
 * @since 2.2
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BleServerManager<E extends BleServerManagerCallbacks> implements ILogger {
	final static UUID CLIENT_USER_DESCRIPTION_DESCRIPTOR_UUID      = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
	final static UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	/** Bluetooth GATT server instance, or null if not opened. */
	private BluetoothGattServer server;

	private final List<BleManager> managers = new ArrayList<>();
	private final Context context;
	private E userCallbacks;

	/**
	 * List of server services returned by {@link #initializeServer()}.
	 * This list is empties when the services are being added one by one to the server.
	 * To get the server services, use {@link BluetoothGattServer#getServices()} instead.
	 */
	private Queue<BluetoothGattService> serverServices;

	private List<BluetoothGattCharacteristic> sharedCharacteristics;
	private List<BluetoothGattDescriptor> sharedDescriptors;

	public BleServerManager(@NonNull final Context context) {
		this.context = context;
	}

	/**
	 * Opens the GATT server and starts initializing services. This method only starts initializing
	 * services. The {@link BleServerManagerCallbacks#onServerReady()} will be called when all
	 * services are done.
	 *
	 * @return true, if the server has been started successfully. If GATT server could not
	 * be started, for example the Bluetooth is disabled, false is returned.
	 * @see #close()
	 */
	public final boolean open() {
		if (server != null)
			return true;

		serverServices = new LinkedList<>(initializeServer());
		final BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		if (bm != null) {
			server = bm.openGattServer(context, gattServerCallback);
		}
		if (server != null) {
			log(Log.INFO, "[Server] Server started successfully");
			try {
				final BluetoothGattService service = serverServices.remove();
				server.addService(service);
			} catch (final NoSuchElementException e) {
				if (userCallbacks != null)
					userCallbacks.onServerReady();
			} catch (final Exception e) {
				close();
				return false;
			}
			return true;
		}
		log(Log.WARN, "GATT server initialization failed");
		serverServices = null;
		return false;
	}

	/**
	 * Closes the GATT server.
	 */
	public final void close() {
		if (server != null) {
			server.close();
			server = null;
		}
		serverServices = null;
		for (BleManager manager: managers) {
			manager.close();
		}
		managers.clear();
	}

	/**
	 * Sets the manager callback listener.
	 *
	 * @param callbacks the callback listener.
	 */
	public final void setManagerCallbacks(@NonNull final E callbacks) {
		userCallbacks = callbacks;
	}

	@Nullable
	final BluetoothGattServer getServer() {
		return server;
	}

	final void addManager(@NonNull final BleManager manager) {
		if (!managers.contains(manager)) {
			managers.add(manager);
		}
	}

	final void removeManager(@NonNull final BleManager manager) {
		managers.remove(manager);
	}

	@Nullable
	private BleManagerHandler getRequestHandler(@NonNull final BluetoothDevice device) {
		for (final BleManager manager : managers) {
			if (device.equals(manager.getBluetoothDevice())) {
				return manager.requestHandler;
			}
		}
		return null;
	}

	@Override
	public void log(final int priority, @NonNull final String message) {
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
	public void log(final int priority, @StringRes final int messageRes,
					@Nullable final Object... params) {
		final String message = context.getString(messageRes, params);
		log(priority, message);
	}

	/**
	 * This method is called once, just after instantiating the {@link BleServerManager}.
	 * It should return a list of server GATT services that will be available for the remote device
	 * to use.
	 * <p>
	 * Server services will be added to the local GATT configuration on the Android device.
	 * The library does not know what services are already set up by other apps or
	 * {@link BleServerManager} instances, so a UUID collision is possible.
	 * The remote device will discover all services set up by all apps.
	 * <p>
	 * In order to enable server callbacks (see {@link android.bluetooth.BluetoothGattServerCallback}),
	 * but without defining own services, return an empty list.
	 *
	 * @since 2.2
	 * @return The list of server GATT services, or null if no services should be created. An
	 * empty array to start the GATT server without any services.
	 */
	@NonNull
	protected abstract List<BluetoothGattService> initializeServer();

	@NonNull
	protected final BluetoothGattService service(@NonNull final UUID uuid, final BluetoothGattCharacteristic... characteristics) {
		final BluetoothGattService service = new BluetoothGattService(uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
		for (BluetoothGattCharacteristic characteristic : characteristics) {
			service.addCharacteristic(characteristic);
		}
		return service;
	}

	@NonNull
	protected final BluetoothGattCharacteristic characteristic(@NonNull final UUID uuid,
															   final int properties, final int permissions,
															   @Nullable final byte[] initialValue,
															   final BluetoothGattDescriptor... descriptors) {
		final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(uuid, properties, permissions);
		for (BluetoothGattDescriptor descriptor: descriptors) {
			characteristic.addDescriptor(descriptor);
		}
		characteristic.setValue(initialValue);
		return characteristic;
	}

	@NonNull
	protected final BluetoothGattCharacteristic characteristic(@NonNull final UUID uuid,
														 	   final int properties, final int permissions,
															   final BluetoothGattDescriptor... descriptors) {
		return characteristic(uuid, properties, permissions, null, descriptors);
	}

	@NonNull
	protected final BluetoothGattCharacteristic sharedCharacteristic(@NonNull final UUID uuid,
																	 final int properties, final int permissions,
																	 @Nullable final byte[] initialValue,
																	 final BluetoothGattDescriptor... descriptors) {
		final BluetoothGattCharacteristic characteristic = characteristic(uuid, properties, permissions, initialValue, descriptors);
		if (sharedCharacteristics == null)
			sharedCharacteristics = new ArrayList<>();
		sharedCharacteristics.add(characteristic);
		return characteristic;
	}

	@NonNull
	protected final BluetoothGattCharacteristic sharedCharacteristic(@NonNull final UUID uuid,
																	 final int properties, final int permissions,
																	 final BluetoothGattDescriptor... descriptors) {
		return characteristic(uuid, properties, permissions, null, descriptors);
	}

	@NonNull
	protected final BluetoothGattDescriptor descriptor(@NonNull final UUID uuid, final int permissions,
													   @Nullable final byte[] initialValue) {
		final BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(uuid, permissions);
		descriptor.setValue(initialValue);
		return descriptor;
	}

	@NonNull
	protected final BluetoothGattDescriptor descriptor(@NonNull final UUID uuid, final int permissions) {
		return descriptor(uuid, permissions, null);
	}

	@NonNull
	protected final BluetoothGattDescriptor sharedDescriptor(@NonNull final UUID uuid,
															 final int permissions,
															 @Nullable final byte[] initialValue) {
		final BluetoothGattDescriptor descriptor = descriptor(uuid, permissions, initialValue);
		if (sharedDescriptors == null)
			sharedDescriptors = new ArrayList<>();
		sharedDescriptors.add(descriptor);
		return descriptor;
	}

	@NonNull
	protected final BluetoothGattDescriptor sharedDescriptor(@NonNull final UUID uuid,
															 final int permissions) {
		return descriptor(uuid, permissions, null);
	}

	/**
	 * This helper method returns a new instance of Client Characteristic Configuration Descriptor
	 * (CCCD) that can be added to a server characteristic in {@link #initializeServer()}.
	 *
	 * @return The CCC descriptor used to enable and disable notifications or indications.
	 */
	@NonNull
	protected final BluetoothGattDescriptor cccd() {
		final BluetoothGattDescriptor cccd = descriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID,
				BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
		cccd.setValue(new byte[] { 0, 0 }); // Notifications and indications disabled initially.
		return cccd;
	}

	/**
	 * This helper method returns a new instance of Client User Description Descriptor
	 * that can be added to a server characteristic in {@link #initializeServer()}.
	 *
	 * @return The User Description descriptor.
	 */
	@NonNull
	protected final BluetoothGattDescriptor description(@NonNull final String description) {
		final BluetoothGattDescriptor cudd = new BluetoothGattDescriptor(CLIENT_USER_DESCRIPTION_DESCRIPTOR_UUID,
				BluetoothGattDescriptor.PERMISSION_READ);
		cudd.setValue(description.getBytes());
		return cudd;
	}

	private final BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {

		@Override
		public void onServiceAdded(final int status, @NonNull final BluetoothGattService service) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				try {
					final BluetoothGattService nextService = serverServices.remove();
					server.addService(nextService);
				} catch (final Exception e) {
					log(Log.INFO, "[Server] All services added successfully");
					if (userCallbacks != null)
						userCallbacks.onServerReady();
					serverServices = null;
				}
			} else {
				log(Log.ERROR, "[Server] Adding service failed with error " + status);
			}
		}

		@Override
		public void onConnectionStateChange(@NonNull final BluetoothDevice device, final int status, final int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
				if (userCallbacks != null)
					userCallbacks.onDeviceConnectedToServer(device);
			} else {
				if (userCallbacks != null)
					userCallbacks.onDeviceDisconnectedFromServer(device);
			}
		}

		@Override
		public void onCharacteristicReadRequest(@NonNull final BluetoothDevice device,
												final int requestId, final int offset,
												@NonNull final BluetoothGattCharacteristic characteristic) {
			log(Log.WARN, "Click offset: " + offset);
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onCharacteristicReadRequest(server, device, requestId, offset, characteristic,
						sharedCharacteristics != null && sharedCharacteristics.contains(characteristic));
			}
		}

		@Override
		public void onCharacteristicWriteRequest(@NonNull final BluetoothDevice device, final int requestId,
												 @NonNull final BluetoothGattCharacteristic characteristic,
												 final boolean preparedWrite, final boolean responseNeeded,
												 final int offset, @NonNull final byte[] value) {
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onCharacteristicWriteRequest(server, device, requestId, characteristic,
						sharedCharacteristics != null && sharedCharacteristics.contains(characteristic),
						preparedWrite, responseNeeded, offset, value);
			}
		}

		@Override
		public void onDescriptorReadRequest(@NonNull final BluetoothDevice device, final int requestId, final int offset,
											@NonNull final BluetoothGattDescriptor descriptor) {
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onDescriptorReadRequest(server, device, requestId, offset, descriptor,
						sharedDescriptors != null && sharedDescriptors.contains(descriptor));
			}
		}

		@Override
		public void onDescriptorWriteRequest(@NonNull final BluetoothDevice device, final int requestId,
											 @NonNull final BluetoothGattDescriptor descriptor,
											 final boolean preparedWrite, final boolean responseNeeded,
											 final int offset, @NonNull final byte[] value) {
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onDescriptorWriteRequest(server, device, requestId, descriptor,
						sharedDescriptors != null && sharedDescriptors.contains(descriptor),
						preparedWrite, responseNeeded, offset, value);
			}
		}

		@Override
		public void onExecuteWrite(@NonNull final BluetoothDevice device, final int requestId,
								   final boolean execute) {
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onExecuteWrite(server, device, requestId, execute);
			}
	}

		@Override
		public void onNotificationSent(@NonNull final BluetoothDevice device, final int status) {
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onNotificationSent(server, device, status);
			}
		}

		@Override
		public void onMtuChanged(@NonNull final BluetoothDevice device, final int mtu) {
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onMtuChanged(server, device, mtu);
			}
		}
	};
}
