package no.nordicsemi.android.ble.response;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.MtuCallback;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MtuResult implements MtuCallback, Parcelable {
	private BluetoothDevice device;
	private int mtu;

	@Override
	public void onMtuChanged(final @NonNull BluetoothDevice device, final int mtu) {
		this.device = device;
		this.mtu = mtu;
	}

	@NonNull
	public BluetoothDevice getBluetoothDevice() {
		return device;
	}

	/**
	 * Returns the agreed MTU. The maximum packet size is 3 bytes less then MTU.
	 * @return the MTU.
	 */
	public int getMtu() {
		return mtu;
	}

	// Parcelable
	protected MtuResult(final Parcel in) {
		device = in.readParcelable(BluetoothDevice.class.getClassLoader());
		mtu = in.readInt();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeParcelable(device, flags);
		dest.writeInt(mtu);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<MtuResult> CREATOR = new Creator<MtuResult>() {
		@Override
		public MtuResult createFromParcel(final Parcel in) {
			return new MtuResult(in);
		}

		@Override
		public MtuResult[] newArray(final int size) {
			return new MtuResult[size];
		}
	};
}
