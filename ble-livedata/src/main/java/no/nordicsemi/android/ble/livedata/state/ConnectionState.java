package no.nordicsemi.android.ble.livedata.state;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import no.nordicsemi.android.ble.annotation.DisconnectionReason;

public class ConnectionState {

	public enum State {
		CONNECTING,
		INITIALIZING,
		READY,
		DISCONNECTING,
		DISCONNECTED
	}

	public static final class Connecting extends ConnectionState {
		@RestrictTo(RestrictTo.Scope.LIBRARY)
		public static Connecting INSTANCE = new Connecting();
		private Connecting() {
			super(State.CONNECTING);
		}
	}

	public static final class Initializing extends ConnectionState {
		@RestrictTo(RestrictTo.Scope.LIBRARY)
		public static Initializing INSTANCE = new Initializing();
		private Initializing() {
			super(State.INITIALIZING);
		}
	}

	public static final class Ready extends ConnectionState {
		@RestrictTo(RestrictTo.Scope.LIBRARY)
		public static Ready INSTANCE = new Ready();
		private Ready() {
			super(State.READY);
		}
	}

	public static final class Disconnecting extends ConnectionState {
		@RestrictTo(RestrictTo.Scope.LIBRARY)
		public static Disconnecting INSTANCE = new Disconnecting();
		private Disconnecting() {
			super(State.DISCONNECTING);
		}
	}

	public static final class Disconnected extends ConnectionState {
		@DisconnectionReason
		private final int reason;

		@RestrictTo(RestrictTo.Scope.LIBRARY)
		public Disconnected(@DisconnectionReason final int reason) {
			super(State.DISCONNECTED);
			this.reason = reason;
		}

		@DisconnectionReason
		public int getReason() {
			return reason;
		}
	}

	protected final State state;

	private ConnectionState(@NonNull final State state) {
		this.state = state;
	}

	/**
	 * The connection state. This can be used in <i>switch</i> in Java.
	 */
	public final State getState() {
		return state;
	}

	/**
	 * Whether the target device is connected, or not.
	 */
	public final boolean isConnected() {
		return state == State.INITIALIZING || state == State.READY;
	}

	/**
	 * Whether the target device is ready to use.
	 */
	public final boolean isReady() {
		return state == State.READY;
	}
}
