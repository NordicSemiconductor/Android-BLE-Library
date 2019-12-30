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

import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.annotation.PhyMask;
import no.nordicsemi.android.ble.annotation.PhyOption;
import no.nordicsemi.android.ble.annotation.PhyValue;
import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.PhyCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

@SuppressWarnings({"unused"})
public final class PhyRequest extends SimpleValueRequest<PhyCallback> implements Operation  {

	/**
	 * Bluetooth LE 1M PHY mask. Used to specify LE 1M Physical Channel as one of many available
	 * options in a bitmask.
	 */
	public static final int PHY_LE_1M_MASK = 1;

	/**
	 * Bluetooth LE 2M PHY mask. Used to specify LE 2M Physical Channel as one of many available
	 * options in a bitmask.
	 */
	public static final int PHY_LE_2M_MASK = 1 << 1;

	/**
	 * Bluetooth LE Coded PHY mask. Used to specify LE Coded Physical Channel as one of many
	 * available options in a bitmask.
	 */
	public static final int PHY_LE_CODED_MASK = 1 << 2;

	/**
	 * No preferred coding when transmitting on the LE Coded PHY.
	 */
	public static final int PHY_OPTION_NO_PREFERRED = 0;

	/**
	 * Prefer the S=2 coding to be used when transmitting on the LE Coded PHY.
	 */
	public static final int PHY_OPTION_S2 = 1;

	/**
	 * Prefer the S=8 coding to be used when transmitting on the LE Coded PHY.
	 */
	public static final int PHY_OPTION_S8 = 2;

	private final int txPhy;
	private final int rxPhy;
	private final int phyOptions;

	PhyRequest(@NonNull final Type type) {
		super(type);
		this.txPhy = 0;
		this.rxPhy = 0;
		this.phyOptions = 0;
	}

	PhyRequest(@NonNull final Type type,
			   @PhyMask int txPhy, @PhyMask int rxPhy, @PhyOption int phyOptions) {
		super(type);
		if ((txPhy & ~(PHY_LE_1M_MASK | PHY_LE_2M_MASK | PHY_LE_CODED_MASK)) > 0)
			txPhy = PHY_LE_1M_MASK;
		if ((rxPhy & ~(PHY_LE_1M_MASK | PHY_LE_2M_MASK | PHY_LE_CODED_MASK)) > 0)
			rxPhy = PHY_LE_1M_MASK;
		if (phyOptions < PHY_OPTION_NO_PREFERRED || phyOptions > PHY_OPTION_S8)
			phyOptions = PHY_OPTION_NO_PREFERRED;
		this.txPhy = txPhy;
		this.rxPhy = rxPhy;
		this.phyOptions = phyOptions;
	}

	@NonNull
	@Override
	PhyRequest setRequestHandler(@NonNull final RequestHandler requestHandler) {
		super.setRequestHandler(requestHandler);
		return this;
	}

	@NonNull
	@Override
	public PhyRequest setHandler(@NonNull final Handler handler) {
		super.setHandler(handler);
		return this;
	}

	@Override
	@NonNull
	public PhyRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public PhyRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public PhyRequest invalid(@NonNull final InvalidRequestCallback callback) {
		super.invalid(callback);
		return this;
	}

	@Override
	@NonNull
	public PhyRequest before(@NonNull final BeforeCallback callback) {
		super.before(callback);
		return this;
	}

	@Override
	@NonNull
	public PhyRequest with(@NonNull final PhyCallback callback) {
		super.with(callback);
		return this;
	}

	void notifyPhyChanged(@NonNull final BluetoothDevice device,
						  @PhyValue final int txPhy, @PhyValue final int rxPhy) {
		handler.post(() -> {
			if (valueCallback != null)
				valueCallback.onPhyChanged(device, txPhy, rxPhy);
		});
	}

	void notifyLegacyPhy(@NonNull final BluetoothDevice device) {
		handler.post(() -> {
			if (valueCallback != null)
				valueCallback.onPhyChanged(device, PhyCallback.PHY_LE_1M, PhyCallback.PHY_LE_1M);
		});
	}

	@PhyMask
	int getPreferredTxPhy() {
		return txPhy;
	}

	@PhyMask
	int getPreferredRxPhy() {
		return rxPhy;
	}

	@PhyOption
	int getPreferredPhyOptions() {
		return phyOptions;
	}
}
