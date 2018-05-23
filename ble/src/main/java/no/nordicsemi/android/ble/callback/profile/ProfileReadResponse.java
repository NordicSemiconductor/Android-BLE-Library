package no.nordicsemi.android.ble.callback.profile;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.response.ReadResponse;

/**
 * A response type for read requests with basic validation check.
 * When read was requested as a synchronous call the {@link #isValid()} can be used to
 * check if data were parsed successfully. Parsing method must call super methods on
 * both {@link #onDataReceived(BluetoothDevice, Data)} and
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} in order to make getters working properly.
 * <p>
 * Check out profile data callbacks in the Android BLE Common Library for example of usage.
 * </p>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ProfileReadResponse extends ReadResponse implements ProfileDataCallback, Parcelable {
	private boolean valid = true;

	public ProfileReadResponse() {
		// empty
	}

	@Override
	public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		this.valid = false;
	}

	/**
	 * Returns true if {@link #onInvalidDataReceived(BluetoothDevice, Data)} wasn't called.
	 *
	 * @return true if profile data were valid, false if parsing error occurred.
	 */
	public boolean isValid() {
		return valid;
	}

	// Parcelable
	protected ProfileReadResponse(final Parcel in) {
		super(in);
		valid = in.readByte() != 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeByte((byte) (valid ? 1 : 0));
	}

	public static final Creator<ProfileReadResponse> CREATOR = new Creator<ProfileReadResponse>() {
		@Override
		public ProfileReadResponse createFromParcel(final Parcel in) {
			return new ProfileReadResponse(in);
		}

		@Override
		public ProfileReadResponse[] newArray(final int size) {
			return new ProfileReadResponse[size];
		}
	};
}
