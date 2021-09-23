package no.nordicsemi.android.ble.livedata;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.livedata.state.BondState;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.ble.observer.BondingObserver;
import no.nordicsemi.android.ble.observer.ConnectionObserver;

public abstract class ObservableBleManager extends BleManager {
	public final LiveData<ConnectionState> state;
	public final LiveData<BondState> bondingState;

	public ObservableBleManager(@NonNull final Context context) {
		this(context, new Handler(Looper.getMainLooper()));
	}

	public ObservableBleManager(@NonNull final Context context, @NonNull final Handler handler) {
		super(context, handler);

		state = new ConnectionStateLiveData();
		bondingState = new BondingStateLiveData();

		setConnectionObserver((ConnectionObserver) state);
		setBondingObserver((BondingObserver) bondingState);
	}
}
