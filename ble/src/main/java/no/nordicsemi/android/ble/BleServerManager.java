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
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import no.nordicsemi.android.ble.annotation.CharacteristicPermissions;
import no.nordicsemi.android.ble.annotation.CharacteristicProperties;
import no.nordicsemi.android.ble.annotation.DescriptorPermissions;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.utils.ILogger;

/**
 * The manager for local GATT server. To be used with one or more instances of {@link BleManager}
 *
 * @since 2.2
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BleServerManager implements ILogger {
	private final static UUID CHARACTERISTIC_EXTENDED_PROPERTIES_DESCRIPTOR_UUID = UUID.fromString("00002900-0000-1000-8000-00805f9b34fb");
	private final static UUID CLIENT_USER_DESCRIPTION_DESCRIPTOR_UUID            = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
	private final static UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID       = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	/** Bluetooth GATT server instance, or null if not opened. */
	private BluetoothGattServer server;

	private final List<BleManager> managers = new ArrayList<>();
	private final Context context;
	private BleServerManagerCallbacks callbacks;

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
				if (callbacks != null)
					callbacks.onServerReady();
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
			// closeServer() must be called before close(). Otherwise close() would remove
			// the manager from managers list while iterating this loop.
			manager.closeServer();
			manager.close();
		}
		managers.clear();
	}

	/**
	 * Sets the manager callback listener.
	 *
	 * @param callbacks the callback listener.
	 */
	public final void setManagerCallbacks(@NonNull final BleServerManagerCallbacks callbacks) {
		this.callbacks = callbacks;
	}

	/**
	 * Returns the {@link BluetoothGattServer} instance.
	 */
	@Nullable
	final BluetoothGattServer getServer() {
		return server;
	}

	/**
	 * Adds the BLE Manager to be handled.
	 * @param manager the Ble Manager.
	 */
	final void addManager(@NonNull final BleManager manager) {
		if (!managers.contains(manager)) {
			managers.add(manager);
		}
	}

	/**
	 * Removes the manager. Callbacks will no longer be passed to it.
	 * @param manager the manager to be removed.
	 */
	final void removeManager(@NonNull final BleManager manager) {
		managers.remove(manager);
	}

	final boolean isShared(@NonNull final BluetoothGattCharacteristic characteristic) {
		return sharedCharacteristics != null && sharedCharacteristics.contains(characteristic);
	}

	final boolean isShared(@NonNull final BluetoothGattDescriptor descriptor) {
		return sharedDescriptors != null && sharedDescriptors.contains(descriptor);
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
	 * to use. You may use {@link #service(UUID, BluetoothGattCharacteristic...)} to easily
	 * instantiate a service.
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

	/**
	 * A helper method for creating a primary service with given UUID and list of characteristics.
	 * This method can be called from {@link #initializeServer()}.
	 *
	 * @param uuid The service UUID.
	 * @param characteristics The optional list of characteristics.
	 * @return The new service.
	 */
	@NonNull
	protected final BluetoothGattService service(@NonNull final UUID uuid, final BluetoothGattCharacteristic... characteristics) {
		final BluetoothGattService service = new BluetoothGattService(uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
		for (BluetoothGattCharacteristic characteristic : characteristics) {
			service.addCharacteristic(characteristic);
		}
		return service;
	}

	/**
	 * A helper method that creates a characteristic with given UUID, properties and permissions.
	 * Optionally, an initial value and a list of descriptors may be set.
	 * <p>
	 * The Client Characteristic Configuration Descriptor (CCCD) will be added automatically if
	 * {@link BluetoothGattCharacteristic#PROPERTY_NOTIFY} or {@link BluetoothGattCharacteristic#PROPERTY_INDICATE}
	 * was set, if not added explicitly in the descriptors list.
	 * <p>
	 * If {@link #reliableWrite()} was added as one of the descriptors or the Characteristic User
	 * Description descriptor was created with any of write permissions
	 * (see {@link #description(String, boolean)}) the
	 * {@link BluetoothGattCharacteristic#PROPERTY_EXTENDED_PROPS} property will be added automatically.
	 * <p>
	 * The value of the characteristic will NOT be shared between clients. Each client will write
	 * and read its own copy. To create a shared characteristic, use
	 * {@link #sharedCharacteristic(UUID, int, int, byte[], BluetoothGattDescriptor...)} instead.
	 *
	 * @param uuid The characteristic UUID.
	 * @param properties The bit mask of characteristic properties. See {@link BluetoothGattCharacteristic}
	 *                   for details.
	 * @param permissions The bit mask or characteristic permissions. See {@link BluetoothGattCharacteristic}
	 *                    for details.
	 * @param initialValue The optional initial value of the characteristic.
	 * @param descriptors The optional list of descriptors.
	 * @return The characteristic.
	 */
	@NonNull
	protected final BluetoothGattCharacteristic characteristic(@NonNull final UUID uuid,
															   @CharacteristicProperties int properties,
															   @CharacteristicPermissions final int permissions,
															   @Nullable final byte[] initialValue,
															   final BluetoothGattDescriptor... descriptors) {
		// Look for Client Characteristic Configuration descriptor,
		// Characteristic User Description descriptor and Characteristic Extended Properties descriptor.
		boolean writableAuxiliaries = false;
		boolean cccdFound = false;
		boolean cepdFound = false;
		BluetoothGattDescriptor cepd = null;
		for (final BluetoothGattDescriptor descriptor : descriptors) {
			if (CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID.equals(descriptor.getUuid())) {
				cccdFound = true;
			} else if (CLIENT_USER_DESCRIPTION_DESCRIPTOR_UUID.equals(descriptor.getUuid())
					&& 0 != (descriptor.getPermissions() & (
							  BluetoothGattDescriptor.PERMISSION_WRITE
							| BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED
							| BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM))) {
				writableAuxiliaries = true;
			} else if (CHARACTERISTIC_EXTENDED_PROPERTIES_DESCRIPTOR_UUID.equals(descriptor.getUuid())) {
				cepd = descriptor;
				cepdFound = true;
			}
		}

		if (writableAuxiliaries) {
			if (cepd == null) {
				cepd = new BluetoothGattDescriptor(CHARACTERISTIC_EXTENDED_PROPERTIES_DESCRIPTOR_UUID,
						BluetoothGattDescriptor.PERMISSION_READ);
				cepd.setValue(new byte[]{0x02, 0x00});
			} else {
				if (cepd.getValue() != null && cepd.getValue().length == 2) {
					cepd.getValue()[0] |= 0x02;
				} else {
					cepd.setValue(new byte[]{0x02, 0x00});
				}
			}
		}

		final boolean cccdRequired = (properties & (BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_INDICATE)) != 0;
		final boolean reliableWrite = cepd != null && cepd.getValue() != null
				&& cepd.getValue().length == 2 && (cepd.getValue()[0] & 0x01) != 0;
		if (writableAuxiliaries || reliableWrite) {
			properties |= BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS;
		}
		if ((properties & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) != 0 && cepd == null) {
			cepd = new BluetoothGattDescriptor(CHARACTERISTIC_EXTENDED_PROPERTIES_DESCRIPTOR_UUID, BluetoothGattDescriptor.PERMISSION_READ);
			cepd.setValue(new byte[] { 0, 0 });
		}


		final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(uuid, properties, permissions);
		if (cccdRequired && !cccdFound) {
			characteristic.addDescriptor(cccd());
		}
		for (BluetoothGattDescriptor descriptor: descriptors) {
			characteristic.addDescriptor(descriptor);
		}
		if (cepd != null && !cepdFound) {
			characteristic.addDescriptor(cepd);
		}
		characteristic.setValue(initialValue);
		return characteristic;
	}

	/**
	 * A helper method that creates a characteristic with given UUID, properties and permissions.
	 * Optionally, an initial value and a list of descriptors may be set.
	 * <p>
	 * The Client Characteristic Configuration Descriptor (CCCD) will be added automatically if
	 * {@link BluetoothGattCharacteristic#PROPERTY_NOTIFY} or {@link BluetoothGattCharacteristic#PROPERTY_INDICATE}
	 * was set, if not added explicitly in the descriptors list.
	 * <p>
	 * If {@link #reliableWrite()} was added as one of the descriptors or the Characteristic User
	 * Description descriptor was created with any of write permissions
	 * (see {@link #description(String, boolean)}) the
	 * {@link BluetoothGattCharacteristic#PROPERTY_EXTENDED_PROPS} property will be added automatically.
	 * <p>
	 * The value of the characteristic will NOT be shared between clients. Each client will write
	 * and read its own copy. To create a shared characteristic, use
	 * {@link #sharedCharacteristic(UUID, int, int, byte[], BluetoothGattDescriptor...)} instead.
	 *
	 * @param uuid The characteristic UUID.
	 * @param properties The bit mask of characteristic properties. See {@link BluetoothGattCharacteristic}
	 *                   for details.
	 * @param permissions The bit mask or characteristic permissions. See {@link BluetoothGattCharacteristic}
	 *                    for details.
	 * @param initialValue The optional initial value of the characteristic.
	 * @param descriptors The optional list of descriptors.
	 * @return The characteristic.
	 */
	@NonNull
	protected final BluetoothGattCharacteristic characteristic(@NonNull final UUID uuid,
															   @CharacteristicProperties final int properties,
															   @CharacteristicPermissions final int permissions,
															   @Nullable final Data initialValue,
															   final BluetoothGattDescriptor... descriptors) {
		return characteristic(uuid, properties, permissions, initialValue != null ? initialValue.getValue() : null, descriptors);
	}

	/**
	 * A helper method that creates a characteristic with given UUID, properties and permissions.
	 * Optionally, a list of descriptors may be set.
	 * <p>
	 * The Client Characteristic Configuration Descriptor (CCCD) will be added automatically if
	 * {@link BluetoothGattCharacteristic#PROPERTY_NOTIFY} or {@link BluetoothGattCharacteristic#PROPERTY_INDICATE}
	 * was set, if not added explicitly in the descriptors list.
	 * <p>
	 * If {@link #reliableWrite()} was added as one of the descriptors or the Characteristic User
	 * Description descriptor was created with any of write permissions
	 * (see {@link #description(String, boolean)}) the
	 * {@link BluetoothGattCharacteristic#PROPERTY_EXTENDED_PROPS} property will be added automatically.
	 * <p>
	 * The value of the characteristic will NOT be shared between clients. Each client will write
	 * and read its own copy. To create a shared characteristic, use
	 * {@link #sharedCharacteristic(UUID, int, int, byte[], BluetoothGattDescriptor...)} instead.
	 *
	 * @param uuid The characteristic UUID.
	 * @param properties The bit mask of characteristic properties. See {@link BluetoothGattCharacteristic}
	 *                   for details.
	 * @param permissions The bit mask or characteristic permissions. See {@link BluetoothGattCharacteristic}
	 *                    for details.
	 * @param descriptors The optional list of descriptors.
	 * @return The characteristic.
	 */
	@NonNull
	protected final BluetoothGattCharacteristic characteristic(@NonNull final UUID uuid,
															   @CharacteristicProperties final int properties,
															   @CharacteristicPermissions final int permissions,
															   final BluetoothGattDescriptor... descriptors) {
		return characteristic(uuid, properties, permissions, (byte[]) null, descriptors);
	}

	/**
	 * A helper method that creates a characteristic with given UUID, properties and permissions.
	 * Optionally, an initial value and a list of descriptors may be set.
	 * <p>
	 * The Client Characteristic Configuration Descriptor (CCCD) will be added automatically if
	 * {@link BluetoothGattCharacteristic#PROPERTY_NOTIFY} or {@link BluetoothGattCharacteristic#PROPERTY_INDICATE}
	 * was set, if not added explicitly in the descriptors list.
	 * <p>
	 * If {@link #reliableWrite()} was added as one of the descriptors or the Characteristic User
	 * Description descriptor was created with any of write permissions
	 * (see {@link #description(String, boolean)}) the
	 * {@link BluetoothGattCharacteristic#PROPERTY_EXTENDED_PROPS} property will be added automatically.
	 * <p>
	 * The value of the characteristic is shared between clients. A value written by one of the
	 * connected clients will be available for all other clients. To create a sandboxed characteristic,
	 * use {@link #characteristic(UUID, int, int, byte[], BluetoothGattDescriptor...)} instead.
	 *
	 * @param uuid The characteristic UUID.
	 * @param properties The bit mask of characteristic properties. See {@link BluetoothGattCharacteristic}
	 *                   for details.
	 * @param permissions The bit mask or characteristic permissions. See {@link BluetoothGattCharacteristic}
	 *                    for details.
	 * @param initialValue The optional initial value of the characteristic.
	 * @param descriptors The optional list of descriptors.
	 * @return The characteristic.
	 */
	@NonNull
	protected final BluetoothGattCharacteristic sharedCharacteristic(@NonNull final UUID uuid,
																	 @CharacteristicProperties final int properties,
																	 @CharacteristicPermissions final int permissions,
																	 @Nullable final byte[] initialValue,
																	 final BluetoothGattDescriptor... descriptors) {
		final BluetoothGattCharacteristic characteristic = characteristic(uuid, properties, permissions, initialValue, descriptors);
		if (sharedCharacteristics == null)
			sharedCharacteristics = new ArrayList<>();
		sharedCharacteristics.add(characteristic);
		return characteristic;
	}

	/**
	 * A helper method that creates a characteristic with given UUID, properties and permissions.
	 * Optionally, an initial value and a list of descriptors may be set.
	 * <p>
	 * The Client Characteristic Configuration Descriptor (CCCD) will be added automatically if
	 * {@link BluetoothGattCharacteristic#PROPERTY_NOTIFY} or {@link BluetoothGattCharacteristic#PROPERTY_INDICATE}
	 * was set, if not added explicitly in the descriptors list.
	 * <p>
	 * If {@link #reliableWrite()} was added as one of the descriptors or the Characteristic User
	 * Description descriptor was created with any of write permissions
	 * (see {@link #description(String, boolean)}) the
	 * {@link BluetoothGattCharacteristic#PROPERTY_EXTENDED_PROPS} property will be added automatically.
	 * <p>
	 * The value of the characteristic is shared between clients. A value written by one of the
	 * connected clients will be available for all other clients. To create a sandboxed characteristic,
	 * use {@link #characteristic(UUID, int, int, byte[], BluetoothGattDescriptor...)} instead.
	 *
	 * @param uuid The characteristic UUID.
	 * @param properties The bit mask of characteristic properties. See {@link BluetoothGattCharacteristic}
	 *                   for details.
	 * @param permissions The bit mask or characteristic permissions. See {@link BluetoothGattCharacteristic}
	 *                    for details.
	 * @param initialValue The optional initial value of the characteristic.
	 * @param descriptors The optional list of descriptors.
	 * @return The characteristic.
	 */
	@NonNull
	protected final BluetoothGattCharacteristic sharedCharacteristic(@NonNull final UUID uuid,
																	 @CharacteristicProperties final int properties,
																	 @CharacteristicPermissions final int permissions,
																	 @Nullable final Data initialValue,
																	 final BluetoothGattDescriptor... descriptors) {
		return sharedCharacteristic(uuid, properties, permissions, initialValue != null ? initialValue.getValue() : null, descriptors);
	}

	/**
	 * A helper method that creates a characteristic with given UUID, properties and permissions.
	 * Optionally, a list of descriptors may be set.
	 * <p>
	 * The Client Characteristic Configuration Descriptor (CCCD) will be added automatically if
	 * {@link BluetoothGattCharacteristic#PROPERTY_NOTIFY} or {@link BluetoothGattCharacteristic#PROPERTY_INDICATE}
	 * was set, if not added explicitly in the descriptors list.
	 * <p>
	 * If {@link #reliableWrite()} was added as one of the descriptors or the Characteristic User
	 * Description descriptor was created with any of write permissions
	 * (see {@link #description(String, boolean)}) the
	 * {@link BluetoothGattCharacteristic#PROPERTY_EXTENDED_PROPS} property will be added automatically.
	 * <p>
	 * The value of the characteristic is shared between clients. A value written by one of the
	 * connected clients will be available for all other clients. To create a sandboxed characteristic,
	 * use {@link #characteristic(UUID, int, int, byte[], BluetoothGattDescriptor...)} instead.
	 *
	 * @param uuid The characteristic UUID.
	 * @param properties The bit mask of characteristic properties. See {@link BluetoothGattCharacteristic}
	 *                   for details.
	 * @param permissions The bit mask or characteristic permissions. See {@link BluetoothGattCharacteristic}
	 *                    for details.
	 * @param descriptors The optional list of descriptors.
	 * @return The characteristic.
	 */
	@NonNull
	protected final BluetoothGattCharacteristic sharedCharacteristic(@NonNull final UUID uuid,
																	 @CharacteristicProperties final int properties,
																	 @CharacteristicPermissions final int permissions,
																	 final BluetoothGattDescriptor... descriptors) {
		return sharedCharacteristic(uuid, properties, permissions, (byte[]) null, descriptors);
	}

	/**
	 * A helper method that creates a descriptor with given UUID and permissions.
	 * Optionally, an initial value may be set.
	 * <p>
	 * The value of the descriptor will NOT be shared between clients. Each client will write
	 * and read its own copy. To create a shared descriptor, use
	 * {@link #sharedDescriptor(UUID, int, byte[])} instead.
	 *
	 * @param uuid The characteristic UUID.
	 * @param permissions The bit mask or descriptor permissions. See {@link BluetoothGattCharacteristic}
	 *                    for details.
	 * @param initialValue The optional initial value of the descriptor.
	 * @return The characteristic.
	 */
	@NonNull
	protected final BluetoothGattDescriptor descriptor(@NonNull final UUID uuid,
													   @DescriptorPermissions final int permissions,
													   @Nullable final byte[] initialValue) {
		final BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(uuid, permissions);
		descriptor.setValue(initialValue);
		return descriptor;
	}

	/**
	 * A helper method that creates a descriptor with given UUID and permissions.
	 * Optionally, an initial value may be set.
	 * <p>
	 * The value of the descriptor will NOT be shared between clients. Each client will write
	 * and read its own copy. To create a shared descriptor, use
	 * {@link #sharedDescriptor(UUID, int, byte[])} instead.
	 *
	 * @param uuid The characteristic UUID.
	 * @param permissions The bit mask or descriptor permissions. See {@link BluetoothGattCharacteristic}
	 *                    for details.
	 * @param initialValue The optional initial value of the descriptor.
	 * @return The characteristic.
	 */
	@NonNull
	protected final BluetoothGattDescriptor descriptor(@NonNull final UUID uuid,
													   @DescriptorPermissions final int permissions,
													   @Nullable final Data initialValue) {
		return descriptor(uuid, permissions, initialValue != null ? initialValue.getValue() : null);
	}

	/**
	 * A helper method that creates a descriptor with given UUID and permissions.
	 * Optionally, an initial value may be set.
	 * <p>
	 * The value of the characteristic is shared between clients. A value written by one of the
	 * connected clients will be available for all other clients. To create a sandboxed characteristic,
	 * use {@link #characteristic(UUID, int, int, byte[], BluetoothGattDescriptor...)} instead.
	 *
	 * @param uuid The characteristic UUID.
	 * @param permissions The bit mask or characteristic permissions. See {@link BluetoothGattCharacteristic}
	 *                    for details.
	 * @param initialValue The optional initial value of the characteristic.
	 * @return The characteristic.
	 */
	@NonNull
	protected final BluetoothGattDescriptor sharedDescriptor(@NonNull final UUID uuid,
															 @DescriptorPermissions final int permissions,
															 @Nullable final byte[] initialValue) {
		final BluetoothGattDescriptor descriptor = descriptor(uuid, permissions, initialValue);
		if (sharedDescriptors == null)
			sharedDescriptors = new ArrayList<>();
		sharedDescriptors.add(descriptor);
		return descriptor;
	}

	/**
	 * A helper method that creates a descriptor with given UUID and permissions.
	 * Optionally, an initial value may be set.
	 * <p>
	 * The value of the characteristic is shared between clients. A value written by one of the
	 * connected clients will be available for all other clients. To create a sandboxed characteristic,
	 * use {@link #characteristic(UUID, int, int, byte[], BluetoothGattDescriptor...)} instead.
	 *
	 * @param uuid The characteristic UUID.
	 * @param permissions The bit mask or characteristic permissions. See {@link BluetoothGattCharacteristic}
	 *                    for details.
	 * @param initialValue The optional initial value of the characteristic.
	 * @return The characteristic.
	 */
	@NonNull
	protected final BluetoothGattDescriptor sharedDescriptor(@NonNull final UUID uuid,
															 @DescriptorPermissions final int permissions,
															 @Nullable final Data initialValue) {
		return sharedDescriptor(uuid, permissions, initialValue != null ? initialValue.getValue() : null);
	}

	/**
	 * This helper method returns a new instance of Client Characteristic Configuration Descriptor
	 * (CCCD) that can be added to a server characteristic in {@link #initializeServer()}.
	 *
	 * @return The CCC descriptor used to enable and disable notifications or indications.
	 */
	@NonNull
	protected final BluetoothGattDescriptor cccd() {
		return descriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID,
				BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE,
				new byte[] { 0, 0 });
	}

	/**
	 * This helper method returns a new instance of Client Characteristic Configuration Descriptor
	 * (CCCD) that can be added to a server characteristic in {@link #initializeServer()}.
	 *
	 * @return The CCC descriptor used to enable and disable notifications or indications.
	 */
	@NonNull
	protected final BluetoothGattDescriptor sharedCccd() {
		return sharedDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID,
				BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE,
				new byte[] { 0, 0 });
	}

	/**
	 * This helper method returns a new instance of Characteristic Extended Properties descriptor
	 * that can be added to a server characteristic.
	 * This descriptor should be added it {@link BluetoothGattCharacteristic#PROPERTY_EXTENDED_PROPS}
	 * property is set.
	 * @return The CEP descriptor with Reliable Write bit set.
	 */
	@NonNull
	protected final BluetoothGattDescriptor reliableWrite() {
		return sharedDescriptor(CHARACTERISTIC_EXTENDED_PROPERTIES_DESCRIPTOR_UUID,
				BluetoothGattDescriptor.PERMISSION_READ,
				new byte[] { 1, 0 });
	}

	/**
	 * This helper method returns a new instance of Client User Description Descriptor
	 * that can be added to a server characteristic in {@link #initializeServer()}.
	 *
	 * @param description the UTF-8 string that is a user textual description of the characteristic.
	 * @param writableAuxiliaries if true, the descriptor will be writable and the Writable Auxiliaries
	 *                            bit in Characteristic Extended Properties descriptor will be set.
	 *                            See Vol. 3, Part F, Section 3.3.3.2 in Bluetooth Core specification 5.1.
	 * @return The User Description descriptor.
	 */
	@NonNull
	protected final BluetoothGattDescriptor description(@Nullable final String description, final boolean writableAuxiliaries) {
		final BluetoothGattDescriptor cud = descriptor(CLIENT_USER_DESCRIPTION_DESCRIPTOR_UUID,
				BluetoothGattDescriptor.PERMISSION_READ | (writableAuxiliaries ? BluetoothGattDescriptor.PERMISSION_WRITE : 0),
				description != null ? description.getBytes() : null);
		if (!writableAuxiliaries)
			sharedDescriptors.add(cud);
		return cud;
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
					if (callbacks != null)
						callbacks.onServerReady();
					serverServices = null;
				}
			} else {
				log(Log.ERROR, "[Server] Adding service failed with error " + status);
			}
		}

		@Override
		public void onConnectionStateChange(@NonNull final BluetoothDevice device, final int status, final int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
				log(Log.INFO, "[Server] " + device.getAddress() + " is now connected");
				if (callbacks != null)
					callbacks.onDeviceConnectedToServer(device);
			} else {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					log(Log.INFO, "[Server] " + device.getAddress() + " is disconnected");
				} else {
					log(Log.WARN, "[Server] " + device.getAddress() + " has disconnected connected with status: " + status);
				}
				if (callbacks != null)
					callbacks.onDeviceDisconnectedFromServer(device);
			}
		}

		@Override
		public void onCharacteristicReadRequest(@NonNull final BluetoothDevice device,
												final int requestId, final int offset,
												@NonNull final BluetoothGattCharacteristic characteristic) {
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onCharacteristicReadRequest(server, device, requestId, offset, characteristic);
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
						preparedWrite, responseNeeded, offset, value);
			}
		}

		@Override
		public void onDescriptorReadRequest(@NonNull final BluetoothDevice device, final int requestId, final int offset,
											@NonNull final BluetoothGattDescriptor descriptor) {
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onDescriptorReadRequest(server, device, requestId, offset, descriptor);
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

		@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
		@Override
		public void onNotificationSent(@NonNull final BluetoothDevice device, final int status) {
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onNotificationSent(server, device, status);
			}
		}

		@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
		@Override
		public void onMtuChanged(@NonNull final BluetoothDevice device, final int mtu) {
			final BleManagerHandler handler = getRequestHandler(device);
			if (handler != null) {
				handler.onMtuChanged(server, device, mtu);
			}
		}
	};
}
