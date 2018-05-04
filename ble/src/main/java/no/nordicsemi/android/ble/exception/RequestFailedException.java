package no.nordicsemi.android.ble.exception;

import no.nordicsemi.android.ble.Request;

@SuppressWarnings("WeakerAccess")
public class RequestFailedException extends Exception {
	private final Request<?> request;
	private final int status;

	public RequestFailedException(final Request<?> request, final int status) {
		super("Request failed with status " + status);
		this.request = request;
		this.status = status;
	}

	/**
	 * Returns the request status. One of {{@link android.bluetooth.BluetoothGatt}} GATT_*
	 * of {@link no.nordicsemi.android.ble.callback.FailCallback} REASON_* codes.
	 *
	 * @return error code
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Returns the request that failed.
	 * @return the request that failed.
	 */
	public Request<?> getRequest() {
		return request;
	}
}
