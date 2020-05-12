package no.nordicsemi.android.ble.common.callback.alert;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.profile.alert.AlertLevelCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class AlertLevelDataCallback extends ProfileReadResponse implements AlertLevelCallback {

	public AlertLevelDataCallback() {
		// empty
	}

	protected AlertLevelDataCallback(final Parcel in) {
		super(in);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);

		if (data.size() == 1) {
			final Integer level = data.getIntValue(Data.FORMAT_UINT8, 0);
			if (level != null && level <= AlertLevelCallback.ALERT_HIGH) {
				onAlertLevelChanged(device, level);
				return;
			}
		}
		onInvalidDataReceived(device, data);
	}
}
