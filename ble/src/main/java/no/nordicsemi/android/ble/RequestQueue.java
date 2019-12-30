/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.ble;

import android.os.Handler;

import java.util.LinkedList;
import java.util.Queue;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

@SuppressWarnings("WeakerAccess")
public class RequestQueue extends SimpleRequest {
	/**
	 * A list of operations that will be executed together.
	 */
	@NonNull
	private final Queue<Request> requests;

	RequestQueue() {
		super(Type.SET);
		requests = new LinkedList<>();
	}

	@NonNull
	@Override
	RequestQueue setRequestHandler(@NonNull final RequestHandler requestHandler) {
		super.setRequestHandler(requestHandler);
		return this;
	}

	@NonNull
	@Override
	public RequestQueue setHandler(@NonNull final Handler handler) {
		super.setHandler(handler);
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
	 * @param operation the new operation to be enqueued.
	 * @throws IllegalStateException    if the operation was enqueued before.
	 * @throws IllegalArgumentException if the operation is not a {@link Request}.
	 */
	@NonNull
	public RequestQueue add(@NonNull final Operation operation) {
		if (operation instanceof Request) {
			final Request request = (Request) operation;
			// Validate
			if (request.enqueued)
				throw new IllegalStateException("Request already enqueued");
			// Add
			requests.add(request);
			// Mark
			request.enqueued = true;
			return this;
		} else {
			throw new IllegalArgumentException("Operation does not extend Request");
		}
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
	 *
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
	 *
	 * @return true, if not all operations were completed.
	 */
	boolean hasMore() {
		return !requests.isEmpty();
	}
}
