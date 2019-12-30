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

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

@SuppressWarnings("unused")
public final class ReliableWriteRequest extends RequestQueue {
	private boolean initialized;
	private boolean closed;
	private boolean cancelled;

	@NonNull
	@Override
	ReliableWriteRequest setRequestHandler(@NonNull final RequestHandler requestHandler) {
		super.setRequestHandler(requestHandler);
		return this;
	}

	@NonNull
	@Override
	public ReliableWriteRequest setHandler(@NonNull final Handler handler) {
		super.setHandler(handler);
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
	public ReliableWriteRequest add(@NonNull final Operation operation) {
		super.add(operation);
		// Make sure the write request uses splitting, as Long Write is not supported
		// in Reliable Write sub-procedure.
		if (operation instanceof WriteRequest) {
			((WriteRequest) operation).forceSplit();
		}
		return this;
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
	boolean hasMore() {
		// If no operations were added, consider the RW request empty, no requests will be executed.
		if (!initialized)
			return super.hasMore();
		return !closed;
	}
}
