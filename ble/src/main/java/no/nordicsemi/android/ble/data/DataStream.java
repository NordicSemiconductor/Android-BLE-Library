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

package no.nordicsemi.android.ble.data;

import java.io.ByteArrayOutputStream;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public class DataStream {
	private final ByteArrayOutputStream buffer;

	public DataStream() {
		buffer = new ByteArrayOutputStream();
	}

	@SuppressWarnings("SimplifiableIfStatement")
	public boolean write(@Nullable final byte[] data) {
		if (data == null)
			return false;

		return write(data, 0, data.length);
	}

	public boolean write(@Nullable final byte[] data,
						 @IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		if (data == null || data.length < offset)
			return false;

		final int len = Math.min(data.length - offset, length);
		buffer.write(data, offset, len);
		return true;
	}

	public boolean write(@Nullable final Data data) {
		return data != null && write(data.getValue());
	}

	@IntRange(from = 0)
	public int size() {
		return buffer.size();
	}

	@NonNull
	public byte[] toByteArray() {
		return buffer.toByteArray();
	}

	@NonNull
	public Data toData() {
		return new Data(buffer.toByteArray());
	}
}
