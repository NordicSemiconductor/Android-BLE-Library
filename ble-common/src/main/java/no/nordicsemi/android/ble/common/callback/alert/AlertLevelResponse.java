package no.nordicsemi.android.ble.common.callback.alert;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.common.profile.alert.AlertLevel;
import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     AlertLevelResponse response = waitForWrite(characteristic)
 *           .awaitValid(AlertLevelResponse.class);
 *     int alertLevel = response.getAlertLevel();
 *     ...
 * } catch ({@link RequestFailedException} e) {
 *     Log.w(TAG, "Request failed with status " + e.getStatus(), e);
 * } catch ({@link InvalidDataException} e) {
 *     Log.w(TAG, "Invalid data received: " + e.getResponse().getRawData());
 * }
 * </pre>
 * </p>
 */
public final class AlertLevelResponse extends AlertLevelDataCallback implements Parcelable {
	private int level;

	public AlertLevelResponse() {
		// empty
	}

	@Override
	public void onAlertLevelChanged(@NonNull final BluetoothDevice device, final int level) {
		this.level = level;
	}

	/**
	 * Returns the Alert Level value.
	 *
	 * @return the received alert level value.
	 */
	@AlertLevel
	public int getAlertLevel() {
		return level;
	}

	// Parcelable
	private AlertLevelResponse(final Parcel in) {
		super(in);
		level = in.readInt();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(level);
	}

	public static final Creator<AlertLevelResponse> CREATOR = new Creator<AlertLevelResponse>() {
		@Override
		public AlertLevelResponse createFromParcel(final Parcel in) {
			return new AlertLevelResponse(in);
		}

		@Override
		public AlertLevelResponse[] newArray(final int size) {
			return new AlertLevelResponse[size];
		}
	};
}
