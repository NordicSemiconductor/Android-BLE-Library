package no.nordicsemi.android.ble;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.Queue;

import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

@SuppressWarnings("WeakerAccess")
public class RequestQueue extends SimpleRequest {
	/** A list of operations that will be executed together. */
	@NonNull
	private final Queue<ConnectionRequest> requests;

	RequestQueue() {
		super(Type.SET);
		requests = new LinkedList<>();
	}

	@NonNull
	@Override
	RequestQueue setManager(@NonNull final BleManager manager) {
		super.setManager(manager);
		return this;
	}

	@Override
	@NonNull
	public RequestQueue done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public RequestQueue fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public RequestQueue invalid(@NonNull final InvalidRequestCallback callback) {
		super.invalid(callback);
		return this;
	}

	@Override
	@NonNull
	public RequestQueue before(@NonNull final BeforeCallback callback) {
		super.before(callback);
		return this;
	}

	/**
	 * Enqueues a new operation. All operations will be executed sequentially in order they were
	 * added.
	 *
	 * @param request the new operation to be enqueued.
	 * @throws IllegalStateException if the operation was enqueued before.
	 */
	@NonNull
	public RequestQueue add(@NonNull final ConnectionRequest request) {
		// Validate
		if (request.enqueued)
			throw new IllegalStateException("Request already enqueued");
		// Add
		requests.add(request);
		// Mark
		request.enqueued = true;
		return this;
	}

	/**
	 * Returns number of enqueued operations.
	 *
	 * @return the size of the internal operations list.
	 */
	@IntRange(from = 0)
	public int size() {
		return requests.size();
	}

	/**
	 * Returns whether the set is empty, or not.
	 *
	 * @return true if the set is empty. Set gets emptied while it all enqueued operations
	 * are being executed.
	 */
	public boolean isEmpty() {
		return requests.isEmpty();
	}

	/**
	 * Cancels all the enqueued operations that were not executed yet.
	 * The one currently executed will be finished.
	 * <p>
	 * It is safe to call this method in {@link Request#done(SuccessCallback)} or
	 * {@link Request#fail(FailCallback)} callback;
	 */
	public void cancelQueue() {
		requests.clear();
	}

	/**
	 * Returns the next {@link Request} to be enqueued.
	 * @return the next request.
	 */
	@Nullable
	Request getNext() {
		try {
			return requests.remove();
			// poll() may also throw an exception
			// See: https://github.com/NordicSemiconductor/Android-BLE-Library/issues/37
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Returns whether there are more operations to be executed.
	 * @return true, if not all operations were completed.
	 */
	boolean hasMore() {
		return !requests.isEmpty();
	}
}
