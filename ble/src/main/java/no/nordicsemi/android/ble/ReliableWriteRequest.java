package no.nordicsemi.android.ble;

import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

public final class ReliableWriteRequest extends RequestQueue {
	private boolean initialized;
	private boolean closed;
	private boolean cancelled;

	@NonNull
	@Override
	ReliableWriteRequest setManager(@NonNull final BleManager manager) {
		super.setManager(manager);
		return this;
	}

	@Override
	@NonNull
	public ReliableWriteRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public ReliableWriteRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public ReliableWriteRequest invalid(@NonNull final InvalidRequestCallback callback) {
		super.invalid(callback);
		return this;
	}

	@Override
	@NonNull
	public ReliableWriteRequest before(@NonNull final BeforeCallback callback) {
		super.before(callback);
		return this;
	}

	@NonNull
	@Override
	public ReliableWriteRequest add(@NonNull final ConnectionRequest request) {
		super.add(request);
		// Make sure the write request uses splitting, as Long Write is not supported
		// in Reliable Write sub-procedure.
		if (request instanceof WriteRequest) {
			((WriteRequest) request).forceSplit();
		}
		return this;
	}

	@Override
	Request getNext() {
		if (!initialized) {
			initialized = true;
			return newBeginReliableWriteRequest();
		}
		if (super.isEmpty()) {
			closed = true;

			if (cancelled)
				return newAbortReliableWriteRequest();
			return newExecuteReliableWriteRequest();
		}
		return super.getNext();
	}

	@Override
	public int size() {
		int size = super.size();

		// Add Begin Reliable Write
		if (!initialized)
			size += 1;

		// Add Execute or Abort Reliable Write
		if (!closed)
			size += 1;
		return size;
	}

	@Override
	boolean hasMore() {
		// If no operations were added, consider the RW request empty, no requests will be executed.
		if (!initialized)
			return super.hasMore();
		return !closed;
	}

	@Override
	public void cancelQueue() {
		cancelled = true;
		super.cancelQueue();
	}

	/**
	 * Alias for {@link #cancelQueue()}.
	 */
	public void abort() {
		cancelQueue();
	}
}
