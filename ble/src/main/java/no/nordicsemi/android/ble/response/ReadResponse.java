package no.nordicsemi.android.ble.response;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Generic read response class that returns the data received and the device from which data
 * were read.
 * Overriding class must call super on {@link #onDataReceived(BluetoothDevice, Data)} in
 * order to make getters work properly.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ReadResponse implements DataReceivedCallback, Parcelable {
	private BluetoothDevice device;
	private Data data;

	public ReadResponse() {
		// empty
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		this.device = device;
		this.data = data;
	}

	@NonNull
	public BluetoothDevice getBluetoothDevice() {
		return device;
	}

	@NonNull
	public Data getRawData() {
		return data;
	}

	// Parcelable
	protected ReadResponse(final Parcel in) {
		device = in.readParcelable(BluetoothDevice.class.getClassLoader());
		data = in.readParcelable(Data.class.getClassLoader());
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeParcelable(device, flags);
		dest.writeParcelable(data, flags);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<ReadResponse> CREATOR = new Creator<ReadResponse>() {
		@Override
		public ReadResponse createFromParcel(final Parcel in) {
			return new ReadResponse(in);
		}

		@Override
		public ReadResponse[] newArray(final int size) {
			return new ReadResponse[size];
		}
	};
}
