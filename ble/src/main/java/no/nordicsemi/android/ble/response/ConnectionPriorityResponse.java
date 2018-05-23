package no.nordicsemi.android.ble.response;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.ConnectionPriorityCallback;

/**
 * The synchronous response type for connection priority requests.
 *
 * @see ConnectionPriorityCallback
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ConnectionPriorityResponse implements ConnectionPriorityCallback, Parcelable {
	private BluetoothDevice device;
	private int interval;
	private int latency;
	private int supervisionTimeout;

	@Override
	public void onConnectionUpdated(@NonNull final BluetoothDevice device,
									final int interval, final int latency, final int timeout) {
		this.device = device;
		this.interval = interval;
		this.latency = latency;
		this.supervisionTimeout = timeout;
	}

	@NonNull
	public BluetoothDevice getBluetoothDevice() {
		return device;
	}

	/**
	 * The connection interval determines how often the Central will ask for data from the Peripheral.
	 * When the Peripheral requests an update, it supplies a maximum and a minimum wanted interval.
	 * The connection interval must be between 7.5 ms and 4 s.
	 *
	 * @return The current connection interval, in milliseconds.
	 */
	public int getConnectionInterval() {
		return interval;
	}

	/**
	 * By setting a non-zero slave latency, the Peripheral can choose to not answer when
	 * the Central asks for data up to the slave latency number of times.
	 * However, if the Peripheral has data to send, it can choose to send data at any time.
	 * This enables a peripheral to stay sleeping for a longer time, if it doesn't have data to send,
	 * but still send data fast if needed. The text book example of such device is for example
	 * keyboard and mice, which want to be sleeping for as long as possible when there is
	 * no data to send, but still have low latency (and for the mouse: low connection interval)
	 * when needed.
	 *
	 * @return The current slave latency.
	 */
	public int getSlaveLatency() {
		return latency;
	}

	/**
	 * This timeout determines the timeout from the last data exchange till a link is considered lost.
	 * A Central will not start trying to reconnect before the timeout has passed,
	 * so if you have a device which goes in and out of range often, and you need to notice when
	 * that happens, it might make sense to have a short timeout.
	 *
	 * @return The current supervision timeout.
	 */
	public int getSupervisionTimeout() {
		return supervisionTimeout;
	}

	// Parcelable
	protected ConnectionPriorityResponse(final Parcel in) {
		device = in.readParcelable(BluetoothDevice.class.getClassLoader());
		interval = in.readInt();
		latency = in.readInt();
		supervisionTimeout = in.readInt();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeParcelable(device, flags);
		dest.writeInt(interval);
		dest.writeInt(latency);
		dest.writeInt(supervisionTimeout);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<ConnectionPriorityResponse> CREATOR = new Creator<ConnectionPriorityResponse>() {
		@Override
		public ConnectionPriorityResponse createFromParcel(final Parcel in) {
			return new ConnectionPriorityResponse(in);
		}

		@Override
		public ConnectionPriorityResponse[] newArray(final int size) {
			return new ConnectionPriorityResponse[size];
		}
	};
}
