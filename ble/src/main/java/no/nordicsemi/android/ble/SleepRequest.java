package no.nordicsemi.android.ble;

import android.support.annotation.NonNull;

@SuppressWarnings("unused")
public final class SleepRequest extends Request {
	private long delay;

	SleepRequest(@NonNull final Type type, final long delay) {
		super(type);
		this.delay = delay;
	}

	long getDelay() {
		return delay;
	}
}
