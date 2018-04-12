package no.nordicsemi.android.ble.callback;

import android.os.Build;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public interface ConnectionPriorityCallback {

	/**
	 * Callback indicating the connection parameters were updated. Works on Android 8+.
	 *
	 * @param interval Connection interval used on this connection, 1.25ms unit. Valid range is from
	 *                 6 (7.5ms) to 3200 (4000ms).
	 * @param latency  Slave latency for the connection in number of connection events. Valid range
	 *                 is from 0 to 499
	 * @param timeout  Supervision timeout for this connection, in 10ms unit. Valid range is from 10
	 *                 (0.1s) to 3200 (32s)
	 */
	@RequiresApi(api = Build.VERSION_CODES.O)
	void onConnectionUpdated(final int interval, final int latency, final int timeout);
}
