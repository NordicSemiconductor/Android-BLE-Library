package no.nordicsemi.android.ble.livedata.state;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/** @noinspection unused*/
public class BondState {

	public enum State {
		NOT_BONDED,
		BONDING,
		BONDED
	}

	public static final class NotBonded extends BondState {
		@RestrictTo(RestrictTo.Scope.LIBRARY)
		public static final NotBonded INSTANCE = new NotBonded();
		private NotBonded() {
			super(State.NOT_BONDED);
		}
	}

	public static final class Bonding extends BondState {
		@RestrictTo(RestrictTo.Scope.LIBRARY)
		public static final Bonding INSTANCE = new Bonding();
		private Bonding() {
			super(State.BONDING);
		}
	}

	public static final class Bonded extends BondState {
		@RestrictTo(RestrictTo.Scope.LIBRARY)
		public static final Bonded INSTANCE = new Bonded();
		private Bonded() {
			super(State.BONDED);
		}
	}

	protected final BondState.State state;

	private BondState(@NonNull final BondState.State state) {
		this.state = state;
	}

	/**
	 * The bonding state. This can be used in <i>switch</i> in Java.
	 */
	public final BondState.State getState() {
		return state;
	}

	/**
	 * Whether bonding was established.
	 */
	public final boolean isBonded() {
		return state == State.BONDED;
	}
}
