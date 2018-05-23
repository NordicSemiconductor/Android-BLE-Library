package no.nordicsemi.android.ble.response;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Generic write response class that returns the target device and the data that were sent.
 * Overriding class must call super on {@link #onDataSent(BluetoothDevice, Data)} in
 * order to make getters work properly.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class WriteResponse implements DataSentCallback, Parcelable {
	private BluetoothDevice device;
	private Data data;

	@Override
	public void onDataSent(@NonNull final BluetoothDevice device, @NonNull final Data data) {
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
	protected WriteResponse(final Parcel in) {
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

	public static final Creator<WriteResponse> CREATOR = new Creator<WriteResponse>() {
		@Override
		public WriteResponse createFromParcel(final Parcel in) {
			return new WriteResponse(in);
		}

		@Override
		public WriteResponse[] newArray(final int size) {
			return new WriteResponse[size];
		}
	};
}
