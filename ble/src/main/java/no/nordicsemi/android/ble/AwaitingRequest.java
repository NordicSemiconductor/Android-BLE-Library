package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.exception.BluetoothDisabledException;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidRequestException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

public abstract class AwaitingRequest<T> extends TimeoutableValueRequest<T> {
	private static final int NOT_STARTED = -123456;
	private static final int STARTED = NOT_STARTED + 1;

	private Request trigger;
	private int triggerStatus = BluetoothGatt.GATT_SUCCESS;

	AwaitingRequest(@NonNull final Type type) {
		super(type);
	}

	AwaitingRequest(@NonNull final Type type, @Nullable final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	AwaitingRequest(@NonNull final Type type, @Nullable final BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	/**
	 * Sets an optional request that is suppose to trigger the notification or indication.
	 * This is to ensure that the characteristic value won't change before the callback was set.
	 *
	 * @param trigger the operation that triggers the notification, usually a write characteristic
	 *                request that write some OP CODE.
	 * @return The request.
	 */
	@NonNull
	public AwaitingRequest trigger(@NonNull final Operation trigger) {
		if (trigger instanceof Request) {
			this.trigger = (Request) trigger;
			this.triggerStatus = NOT_STARTED;
			// The trigger will never receive invalid request event.
			// If the BluetoothDevice wasn't set, the whole WaitForValueChangedRequest would be invalid.
			/*this.trigger.invalid(() -> {
				// never called
			});*/
			this.trigger.internalBefore(device -> triggerStatus = STARTED);
			this.trigger.internalSuccess(device -> triggerStatus = BluetoothGatt.GATT_SUCCESS);
			this.trigger.internalFail((device, status) -> {
				triggerStatus = status;
				syncLock.open();
				notifyFail(device, status);
			});
		}
		return this;
	}

	@NonNull
	@Override
	public <E extends T> E await(@NonNull final E response)
			throws RequestFailedException, DeviceDisconnectedException, BluetoothDisabledException,
			InvalidRequestException, InterruptedException {
		assertNotMainThread();

		try {
			// Ensure the trigger request it enqueued after the callback has been set.
			if (trigger != null && trigger.enqueued) {
				throw new IllegalStateException("Trigger request already enqueued");
			}
			super.await(response);
			return response;
		} catch (final RequestFailedException e) {
			if (triggerStatus != BluetoothGatt.GATT_SUCCESS) {
				// Trigger will never have invalid request status. The outer request will.
				/*if (triggerStatus == RequestCallback.REASON_REQUEST_INVALID) {
					throw new InvalidRequestException(trigger);
				}*/
				throw new RequestFailedException(trigger, triggerStatus);
			}
			throw e;
		}
	}

	@Nullable
	Request getTrigger() {
		return trigger;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	boolean isTriggerPending() {
		return triggerStatus == NOT_STARTED;
	}

	boolean isTriggerCompleteOrNull() {
		return triggerStatus != STARTED;
	}
}
