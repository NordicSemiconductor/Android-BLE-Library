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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.annotation.PhyMask;
import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

/**
 * The connect request is used to connect to a Bluetooth LE device. The request will end when
 * the device gets connected, the connection timeouts, or an error occurs.
 * <p>
 * The {@link #done(SuccessCallback)} callback will be called after the device is ready, that is
 * when it is connected, the services were discovered, the required services were found and the
 * initialization queue set in {@link BleManager.BleManagerGattCallback#initialize()} is complete
 * (without or with errors).
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ConnectRequest extends TimeoutableRequest {
	@NonNull
	private BluetoothDevice device;
	@PhyMask
	private int preferredPhy;
	@IntRange(from = 0)
	private int attempt = 0, retries = 0;
	@IntRange(from = 0)
	private int delay = 0;
	private boolean autoConnect = false;

	ConnectRequest(@NonNull final Type type, @NonNull final BluetoothDevice device) {
		super(type);
		this.device = device;
		this.preferredPhy = PhyRequest.PHY_LE_1M_MASK;
	}

	@NonNull
	@Override
	ConnectRequest setRequestHandler(@NonNull final RequestHandler requestHandler) {
		super.setRequestHandler(requestHandler);
		return this;
	}

	@NonNull
	@Override
	public ConnectRequest setHandler(@NonNull final Handler handler) {
		super.setHandler(handler);
		return this;
	}

	@NonNull
	@Override
	public ConnectRequest timeout(@IntRange(from = 0) final long timeout) {
		super.timeout(timeout);
		return this;
	}

	/**
	 * Use to set a completion callback. The callback will be invoked when the operation has
	 * finished successfully unless {@link #await()} or its variant was used, in which case this
	 * callback will be ignored.
	 * <p>
	 * The done callback will also be called when one or more of initialization requests has
	 * failed due to a reason other than disconnect event. This is because
	 * {@link BleManagerCallbacks#onDeviceReady(BluetoothDevice)} is called no matter
	 * if the requests succeeded, or not. Set failure callbacks to initialization requests
	 * to get information about failures.
	 *
	 * @param callback the callback.
	 * @return The request.
	 */
	@NonNull
	@Override
	public ConnectRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@NonNull
	@Override
	public ConnectRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public ConnectRequest invalid(@NonNull final InvalidRequestCallback callback) {
		super.invalid(callback);
		return this;
	}

	@Override
	@NonNull
	public ConnectRequest before(@NonNull final BeforeCallback callback) {
		super.before(callback);
		return this;
	}

	/**
	 * Sets an optional retry count. The BleManager will do that many attempts to connect to the
	 * device in case of an error. The library will NOT retry if the device is not reachable,
	 * that is when the 30 sec. timeout occurs. In that case the app should scan before
	 * connecting, to make sure the target is in range.
	 *
	 * @param count how many times should the BleManager retry to connect.
	 * @return The request.
	 * @see #retry(int, int)
	 */
	public ConnectRequest retry(@IntRange(from = 0) final int count) {
		this.retries = count;
		this.delay = 0;
		return this;
	}

	/**
	 * Sets an optional retry count and a delay that the process will wait before each connection
	 * attempt. The library will NOT retry if the device is not reachable, that is when the 30 sec.
	 * timeout occurs. In that case the app should scan before connecting, to make sure the
	 * target is in range.
	 *
	 * @param count how many times should the BleManager retry to connect.
	 * @param delay the delay between each connection attempt, in milliseconds.
	 *              The real delay will be 200 ms longer than specified, as
	 *              {@link BluetoothGatt#clone()} is estimated to last
	 *              {@link BleManagerHandler#internalConnect(BluetoothDevice, ConnectRequest) 200 ms}.
	 * @return The request.
	 * @see #retry(int)
	 */
	public ConnectRequest retry(@IntRange(from = 0) final int count,
								@IntRange(from = 0) final int delay) {
		this.retries = count;
		this.delay = delay;
		return this;
	}

	/**
	 * This method replaces the {@link BleManager#shouldAutoConnect()} method.
	 * <p>
	 * Sets whether to connect to the remote device just once (false) or to add the address to
	 * white list of devices that will be automatically connect as soon as they become available
	 * (true). In the latter case, if Bluetooth adapter is enabled, Android scans periodically
	 * for devices from the white list and, if an advertising packet is received from such, it tries
	 * to connect to it. When the connection is lost, the system will keep trying to reconnect to
	 * it. If method is called with parameter set to true, and the connection to the device is
	 * lost, the {@link BleManagerCallbacks#onLinkLossOccurred(BluetoothDevice)} callback is
	 * called instead of {@link BleManagerCallbacks#onDeviceDisconnected(BluetoothDevice)}.
	 * <p>
	 * This feature works much better on newer Android phone models and may have issues on older
	 * phones.
	 * <p>
	 * This method should only be used with bonded devices, as otherwise the device may change
	 * it's address. It will however work also with non-bonded devices with private static address.
	 * A connection attempt to a non-bonded device with private resolvable address will fail.
	 * <p>
	 * The first connection to a device will always be created with autoConnect flag to false
	 * (see {@link BluetoothDevice#connectGatt(Context, boolean, BluetoothGattCallback)}). This is
	 * to make it quick as the user most probably waits for a quick response. If autoConnect is
	 * used (true), the following connections will be done using {@link BluetoothGatt#connect()},
	 * which forces the autoConnect parameter to true.
	 *
	 * @param autoConnect true to use autoConnect feature on the second and following connections.
	 *                    The first connection is always done with autoConnect parameter equal to
	 *                    false, to make it faster and allow to timeout it the device is unreachable.
	 *                    Default value is false.
	 * @return The request.
	 */
	public ConnectRequest useAutoConnect(final boolean autoConnect) {
		this.autoConnect = autoConnect;
		return this;
	}

	/**
	 * Sets the preferred PHY used for connection. Th value should be a bitmask composed of
	 * {@link PhyRequest#PHY_LE_1M_MASK}, {@link PhyRequest#PHY_LE_2M_MASK} or
	 * {@link PhyRequest#PHY_LE_CODED_MASK}.
	 * <p>
	 * Different PHYs are available only on more recent devices with Android 8+.
	 * Check {@link BluetoothAdapter#isLe2MPhySupported()} and
	 * {@link BluetoothAdapter#isLeCodedPhySupported()} if required PHYs are supported by this
	 * Android device. The default PHY is {@link PhyRequest#PHY_LE_1M_MASK}.
	 *
	 * @param phy preferred PHY for connections to remote LE device. Bitwise OR of any of
	 *            {@link PhyRequest#PHY_LE_1M_MASK}, {@link PhyRequest#PHY_LE_2M_MASK},
	 *            and {@link PhyRequest#PHY_LE_CODED_MASK}. This option does not take effect
	 *            if {@code autoConnect} is set to true.
	 * @return The request.
	 */
	public ConnectRequest usePreferredPhy(@PhyMask final int phy) {
		this.preferredPhy = phy;
		return this;
	}

	@NonNull
	public BluetoothDevice getDevice() {
		return device;
	}

	@PhyMask
	int getPreferredPhy() {
		return preferredPhy;
	}

	boolean canRetry() {
		if (retries > 0) {
			retries -= 1;
			return true;
		}
		return false;
	}

	boolean isFirstAttempt() {
		return attempt++ == 0;
	}

	@IntRange(from = 0)
	int getRetryDelay() {
		return delay;
	}

	boolean shouldAutoConnect() {
		return autoConnect;
	}
}
