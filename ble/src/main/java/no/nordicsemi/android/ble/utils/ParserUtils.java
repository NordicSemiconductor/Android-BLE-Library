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
package no.nordicsemi.android.ble.utils;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.PhyRequest;
import no.nordicsemi.android.ble.annotation.BondState;
import no.nordicsemi.android.ble.annotation.ConnectionState;
import no.nordicsemi.android.ble.annotation.PairingVariant;
import no.nordicsemi.android.ble.annotation.PhyMask;
import no.nordicsemi.android.ble.annotation.PhyOption;
import no.nordicsemi.android.ble.annotation.PhyValue;
import no.nordicsemi.android.ble.annotation.WriteType;
import no.nordicsemi.android.ble.callback.PhyCallback;

import static no.nordicsemi.android.ble.BleManager.PAIRING_VARIANT_CONSENT;
import static no.nordicsemi.android.ble.BleManager.PAIRING_VARIANT_DISPLAY_PASSKEY;
import static no.nordicsemi.android.ble.BleManager.PAIRING_VARIANT_DISPLAY_PIN;
import static no.nordicsemi.android.ble.BleManager.PAIRING_VARIANT_OOB_CONSENT;
import static no.nordicsemi.android.ble.BleManager.PAIRING_VARIANT_PASSKEY;
import static no.nordicsemi.android.ble.BleManager.PAIRING_VARIANT_PASSKEY_CONFIRMATION;
import static no.nordicsemi.android.ble.BleManager.PAIRING_VARIANT_PIN;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ParserUtils {
	protected final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	public static String parse(@NonNull final BluetoothGattCharacteristic characteristic) {
		return parse(characteristic.getValue());
	}

	public static String parse(@NonNull final BluetoothGattDescriptor descriptor) {
		return parse(descriptor.getValue());
	}

	public static String parse(@Nullable final byte[] data) {
		if (data == null || data.length == 0)
			return "";

		final char[] out = new char[data.length * 3 - 1];
		for (int j = 0; j < data.length; j++) {
			int v = data[j] & 0xFF;
			out[j * 3] = HEX_ARRAY[v >>> 4];
			out[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
			if (j != data.length - 1)
				out[j * 3 + 2] = '-';
		}
		return "(0x) " + new String(out);
	}

	public static String parseDebug(@Nullable final byte[] data) {
		if (data == null || data.length == 0)
			return "null";

		final char[] out = new char[data.length * 2];
		for (int j = 0; j < data.length; j++) {
			int v = data[j] & 0xFF;
			out[j * 2] = HEX_ARRAY[v >>> 4];
			out[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return "0x" + new String(out);
	}

	@NonNull
	public static String pairingVariantToString(@PairingVariant final int variant) {
		switch (variant) {
			case PAIRING_VARIANT_PIN:
				return "PAIRING_VARIANT_PIN";
			case PAIRING_VARIANT_PASSKEY:
				return "PAIRING_VARIANT_PASSKEY";
			case PAIRING_VARIANT_PASSKEY_CONFIRMATION:
				return "PAIRING_VARIANT_PASSKEY_CONFIRMATION";
			case PAIRING_VARIANT_CONSENT:
				return "PAIRING_VARIANT_CONSENT";
			case PAIRING_VARIANT_DISPLAY_PASSKEY:
				return "PAIRING_VARIANT_DISPLAY_PASSKEY";
			case PAIRING_VARIANT_DISPLAY_PIN:
				return "PAIRING_VARIANT_DISPLAY_PIN";
			case PAIRING_VARIANT_OOB_CONSENT:
				return "PAIRING_VARIANT_OOB_CONSENT";
			default:
				return "UNKNOWN (" + variant + ")";
		}
	}

	@NonNull
	public static String bondStateToString(@BondState final int state) {
		switch (state) {
			case BluetoothDevice.BOND_NONE:
				return "BOND_NONE";
			case BluetoothDevice.BOND_BONDING:
				return "BOND_BONDING";
			case BluetoothDevice.BOND_BONDED:
				return "BOND_BONDED";
			default:
				return "UNKNOWN (" + state + ")";
		}
	}

	@NonNull
	public static String writeTypeToString(@WriteType final int type) {
		switch (type) {
			case BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT:
				return "WRITE REQUEST";
			case BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE:
				return "WRITE COMMAND";
			case BluetoothGattCharacteristic.WRITE_TYPE_SIGNED:
				return "WRITE SIGNED";
			default:
				return "UNKNOWN (" + type + ")";
		}
	}

	/**
	 * Converts the connection state to String value.
	 *
	 * @param state the connection state
	 * @return state as String
	 */
	@NonNull
	public static String stateToString(@ConnectionState final int state) {
		switch (state) {
			case BluetoothProfile.STATE_CONNECTED:
				return "CONNECTED";
			case BluetoothProfile.STATE_CONNECTING:
				return "CONNECTING";
			case BluetoothProfile.STATE_DISCONNECTING:
				return "DISCONNECTING";
			case BluetoothProfile.STATE_DISCONNECTED:
				return "DISCONNECTED";
			default:
				return "UNKNOWN (" + state + ")";
		}
	}

	/**
	 * Converts the PHY number to String value.
	 *
	 * @param phy phy value
	 * @return phy as String
	 */
	@NonNull
	public static String phyToString(@PhyValue final int phy) {
		switch (phy) {
			case PhyCallback.PHY_LE_1M:
				return "LE 1M";
			case PhyCallback.PHY_LE_2M:
				return "LE 2M";
			case PhyCallback.PHY_LE_CODED:
				return "LE Coded";
			default:
				return "UNKNOWN (" + phy + ")";
		}
	}

	@NonNull
	public static String phyMaskToString(@PhyMask final int mask) {
		switch (mask) {
			case PhyRequest.PHY_LE_1M_MASK:
				return "LE 1M";
			case PhyRequest.PHY_LE_2M_MASK:
				return "LE 2M";
			case PhyRequest.PHY_LE_CODED_MASK:
				return "LE Coded";
			case PhyRequest.PHY_LE_1M_MASK | PhyRequest.PHY_LE_2M_MASK:
				return "LE 1M or LE 2M";
			case PhyRequest.PHY_LE_1M_MASK | PhyRequest.PHY_LE_CODED_MASK:
				return "LE 1M or LE Coded";
			case PhyRequest.PHY_LE_2M_MASK | PhyRequest.PHY_LE_CODED_MASK:
				return "LE 2M or LE Coded";
			case PhyRequest.PHY_LE_1M_MASK | PhyRequest.PHY_LE_2M_MASK
					| PhyRequest.PHY_LE_CODED_MASK:
				return "LE 1M, LE 2M or LE Coded";
			default:
				return "UNKNOWN (" + mask + ")";
		}
	}

	@NonNull
	public static String phyCodedOptionToString(@PhyOption final int option) {
		switch (option) {
			case PhyRequest.PHY_OPTION_NO_PREFERRED:
				return "No preferred";
			case PhyRequest.PHY_OPTION_S2:
				return "S2";
			case PhyRequest.PHY_OPTION_S8:
				return "S8";
			default:
				return "UNKNOWN (" + option + ")";
		}
	}
}
