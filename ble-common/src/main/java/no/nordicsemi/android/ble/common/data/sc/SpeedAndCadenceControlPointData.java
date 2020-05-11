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

package no.nordicsemi.android.ble.common.data.sc;

import no.nordicsemi.android.ble.common.profile.sc.SensorLocation;
import no.nordicsemi.android.ble.common.profile.sc.SensorLocationTypes;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.MutableData;

@SuppressWarnings("unused")
public final class SpeedAndCadenceControlPointData implements SensorLocationTypes {
	private final static byte SC_OP_CODE_SET_CUMULATIVE_VALUE = 1;
	private final static byte SC_OP_CODE_START_SENSOR_CALIBRATION = 2;
	private final static byte SC_OP_CODE_UPDATE_SENSOR_LOCATION = 3;
	private final static byte SC_OP_CODE_REQUEST_SUPPORTED_SENSOR_LOCATIONS = 4;

	/**
	 * Sets cumulative value. The value parameter is of type long to allow setting value
	 * from range 2<sup>31</sup>-1 to 2<sup>32</sup>.
	 *
	 * @param value new cumulative value.
	 * @return Data object.
	 */
	public static Data setCumulativeValue(final long value) {
		final MutableData data = new MutableData(new byte[5]);
		data.setByte(SC_OP_CODE_SET_CUMULATIVE_VALUE, 0);
		data.setValue(value, Data.FORMAT_UINT32, 1);
		return data;
	}

	/**
	 * Starts the calibration of the RSC Sensor.
	 * @return Data object.
	 */
	public static Data startSensorCalibration() {
		return new MutableData(new byte[] { SC_OP_CODE_START_SENSOR_CALIBRATION });
	}

	/**
	 * Update to the location of the sensor with the value sent as parameter to this op code.
	 *
	 * @param location new location id (UINT8). See SENSOR_LOCATION_* constants.
	 * @return Data object.
	 */
	public static Data updateSensorLocation(@SensorLocation final int location) {
		final MutableData data =  new MutableData(new byte[2]);
		data.setByte(SC_OP_CODE_UPDATE_SENSOR_LOCATION, 0);
		data.setValue(location, Data.FORMAT_UINT8, 1);
		return data;
	}

	/**
	 * Request a list of supported locations where the sensor can be attached.
	 *
	 * @return Data object.
	 */
	public static Data requestSupportedSensorLocations() {
		return new MutableData(new byte[] { SC_OP_CODE_REQUEST_SUPPORTED_SENSOR_LOCATIONS });
	}
}
