package no.nordicsemi.android.ble.common.callback.alert;

import android.bluetooth.BluetoothDevice;

import org.junit.Test;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.common.data.alert.AlertLevelData;
import no.nordicsemi.android.ble.common.profile.alert.AlertLevelCallback;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.*;

public class AlertLevelDataCallbackTest {

	@Test
	public void onAlertLevelChanged_high() {
		final DataReceivedCallback callback = new AlertLevelDataCallback() {
			@Override
			public void onAlertLevelChanged(@NonNull final BluetoothDevice device, final int level) {
				assertEquals("High alert received", AlertLevelCallback.ALERT_HIGH, level);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				super.onInvalidDataReceived(device, data);
				fail("Correct data reported as invalid");
			}
		};
		final Data data = AlertLevelData.highAlert();
		callback.onDataReceived(null, data);

		assertEquals("Correct value", 0x02, AlertLevelCallback.ALERT_HIGH);
	}

	@Test
	public void onAlertLevelChanged_mild() {
		final DataReceivedCallback callback = new AlertLevelDataCallback() {
			@Override
			public void onAlertLevelChanged(@NonNull final BluetoothDevice device, final int level) {
				assertEquals("Mild alert received", AlertLevelCallback.ALERT_MILD, level);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				super.onInvalidDataReceived(device, data);
				fail("Correct data reported as invalid");
			}
		};
		final Data data = AlertLevelData.mildAlert();
		callback.onDataReceived(null, data);

		assertEquals("Correct value", 0x01, AlertLevelCallback.ALERT_MILD);
	}

	@Test
	public void onAlertLevelChanged_none() {
		final DataReceivedCallback callback = new AlertLevelDataCallback() {
			@Override
			public void onAlertLevelChanged(@NonNull final BluetoothDevice device, final int level) {
				assertEquals("No alert received", AlertLevelCallback.ALERT_NONE, level);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				super.onInvalidDataReceived(device, data);
				fail("Correct data reported as invalid");
			}
		};
		final Data data = AlertLevelData.noAlert();
		callback.onDataReceived(null, data);

		assertEquals("Correct value", 0x00, AlertLevelCallback.ALERT_NONE);
	}

	@Test
	public void onAlertLevelChanged_invalid() {
		final DataReceivedCallback callback = new AlertLevelDataCallback() {
			@Override
			public void onAlertLevelChanged(@NonNull final BluetoothDevice device, final int level) {
				fail("Invalid data reported as valid");
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				super.onInvalidDataReceived(device, data);
				assertEquals("Invalid data", 2, data.size());
			}
		};
		final Data data = Data.opCode((byte) 0x01, (byte) 0x02);
		callback.onDataReceived(null, data);

		assertEquals("Correct value", 0x00, AlertLevelCallback.ALERT_NONE);
	}
}