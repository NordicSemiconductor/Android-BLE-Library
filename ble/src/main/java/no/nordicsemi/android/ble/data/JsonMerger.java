package no.nordicsemi.android.ble.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A data merger object that returns true when complete JSON has been received.
 * <p>
 * It will consume packets until the JSON object can be created.
 */
public class JsonMerger implements DataMerger {
	private String buffer = "";

	@Override
	public boolean merge(@NonNull final DataStream output, @Nullable final byte[] lastPacket, final int index) {
		output.write(lastPacket);

		buffer += new String(lastPacket);
		try {
			new JSONObject(buffer);
		} catch (final JSONException e) {
			try {
				new JSONArray(buffer);
			} catch (final JSONException jsonException) {
				return false;
			}
		}
		reset();
		return true;
	}

	/**
	 * Resets the merger so it can start merging from scratch.
	 */
	public void reset() {
		buffer = "";
	}
}
