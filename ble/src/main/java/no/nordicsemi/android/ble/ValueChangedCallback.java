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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.ConditionVariable;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.ReadProgressCallback;
import no.nordicsemi.android.ble.callback.RequiredDataReceivedCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataMerger;
import no.nordicsemi.android.ble.data.DataStream;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class ValueChangedCallback {
	private final BleManager<?> bleManager;
	private final ConditionVariable syncLock;
	private ReadProgressCallback progressCallback;
	private DataReceivedCallback valueCallback;
	private DataMerger dataMerger;
	private DataStream buffer;
	private int count = 0;
	private boolean deviceDisconnected;
	private int triggerStatus;
	private Runnable timeoutHandler;

	ValueChangedCallback(final BleManager<?> manager) {
		bleManager = manager;
		syncLock = new ConditionVariable(true);
	}

	/**
	 * Sets the asynchronous data callback that will be called whenever a notification or
	 * an indication is received on given characteristic.
	 * <p>
	 * This callback is ignored when synchronous call is made using {@link #await(Class, int)}
	 * or any of variants.
	 *
	 * @param callback the data callback.
	 * @return The request.
	 */
	@NonNull
	public ValueChangedCallback with(@NonNull final DataReceivedCallback callback) {
		this.valueCallback = callback;
		cancelTimeout();
		return this;
	}

	/**
	 * Sets the asynchronous data callback that will be called whenever a notification or
	 * an indication is received on given characteristic.
	 * <p>
	 * The first notification or indication must be received before the timeout occurs,
	 * otherwise {@link RequiredDataReceivedCallback#onTimeoutOccurred(BluetoothDevice)}
	 * will be called. If a merger is set, the whole message must be completed before the timeout.
	 * Set timeout to 0 to only be notified when the device disconnected before the
	 * characteristic value has changed (infinite timeout).
	 * <p>
	 * This callback is ignored when synchronous call is made using {@link #await(Class, int)}
	 * or any of variants.
	 *
	 * @param callback the data callback.
	 * @param timeout the time in which a notification or an indication is expected.
	 *                If {@link #merge(DataMerger)} was used, the whole message needs to be
	 *                received before the timeout. In milliseconds. Use 0 for infinite timeout.
	 * @return The request.
	 */
	@NonNull
	public ValueChangedCallback with(@NonNull final RequiredDataReceivedCallback callback,
									 final long timeout) {
		this.valueCallback = callback;
		this.timeoutHandler = () -> {
			timeoutHandler = null;
			if (deviceDisconnected) {
				callback.onDeviceDisconnected(bleManager.getBluetoothDevice());
			} else {
				callback.onTimeoutOccurred(bleManager.getBluetoothDevice());
			}
		};
		if (timeout > 0) {
			bleManager.mHandler.postDelayed(timeoutHandler, timeout);
		}
		return this;
	}

	/**
	 * Adds a merger that will be used to merge multiple packets into a single Data.
	 * The merger may modify each packet if necessary.
	 *
	 * @return The request.
	 */
	@NonNull
	public ValueChangedCallback merge(@NonNull final DataMerger merger) {
		this.dataMerger = merger;
		this.progressCallback = null;
		return this;
	}

	/**
	 * Adds a merger that will be used to merge multiple packets into a single Data.
	 * The merger may modify each packet if necessary.
	 *
	 * @return The request.
	 */
	@NonNull
	public ValueChangedCallback merge(@NonNull final DataMerger merger,
									  @NonNull final ReadProgressCallback callback) {
		this.dataMerger = merger;
		this.progressCallback = callback;
		return this;
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * <p>
	 * The value of returned notification or indication is ignored.
	 *
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 */
	@SuppressWarnings("ConstantConditions")
	public void await() throws DeviceDisconnectedException {
		await((DataReceivedCallback) null);
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic,
	 * for at most given number of milliseconds.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * <p>
	 * The value of returned notification or indication is ignored.
	 *
	 * @param timeout optional timeout in milliseconds.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 */
	@SuppressWarnings("ConstantConditions")
	public void await(final int timeout) throws InterruptedException, DeviceDisconnectedException {
		await((DataReceivedCallback) null, timeout);
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 *
	 * @param responseClass the response class. This class will be instantiate, therefore it has
	 *                      to have a default constructor.
	 * @return The object received with a notification or indication.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 */
	@SuppressWarnings({"NullableProblems", "ConstantConditions"})
	@NonNull
	public <E extends DataReceivedCallback> E await(@NonNull final Class<E> responseClass)
			throws DeviceDisconnectedException {
		try {
			return awaitAfter(null, responseClass);
		} catch (final RequestFailedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 *
	 * @param response the response object that will be returned.
	 * @return The object received with a notification or indication.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 */
	@SuppressWarnings({"NullableProblems", "ConstantConditions"})
	@NonNull
	public <E extends DataReceivedCallback> E await(@NonNull final E response)
			throws DeviceDisconnectedException {
		try {
			return awaitAfter(null, response);
		} catch (final RequestFailedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 *
	 * @param timeout       optional timeout in milliseconds
	 * @param responseClass the response class. This class will be instantiate, therefore it has
	 *                      to have a default constructor.
	 * @return The object received with a notification or indication.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 */
	@SuppressWarnings({"NullableProblems", "ConstantConditions"})
	@NonNull
	public <E extends DataReceivedCallback> E await(@NonNull final Class<E> responseClass,
													final int timeout)
			throws InterruptedException, DeviceDisconnectedException {
		try {
			return awaitAfter(null, responseClass, timeout);
		} catch (final RequestFailedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 *
	 * @param timeout  optional timeout in milliseconds
	 * @param response the response object that will be returned.
	 * @return The object received with a notification or indication.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 */
	@SuppressWarnings({"NullableProblems", "ConstantConditions"})
	@NonNull
	public <E extends DataReceivedCallback> E await(@NonNull final E response,
													final int timeout)
			throws InterruptedException, DeviceDisconnectedException {
		try {
			return awaitAfter(null, response, timeout);
		} catch (final RequestFailedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * This method works just like {@link #awaitAfter(Request, Class, int)},
	 * but will wait without a timeout until a notification/indication is received, or the
	 * device disconnects.
	 *
	 * @param trigger       an optional request that will be executed after the notification
	 *                      callback has been initiated. Usually it's a write request that triggers
	 *                      the notification or indication. This request may no have been enqueued
	 *                      before, so you have to use {@link Request}.new...Request() instead of
	 *                      {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, byte[])}
	 *                      which returns already enqueued request.
	 * @param responseClass the response class. This class will be instantiate, therefore it has to
	 *                      have a default constructor.
	 * @return The object received with a notification or indication.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 * @see #awaitAfter(Request, E)
	 */
	@SuppressWarnings("NullableProblems")
	@NonNull
	public <E extends DataReceivedCallback> E awaitAfter(@NonNull final Request trigger,
														 @NonNull final Class<E> responseClass)
			throws DeviceDisconnectedException, RequestFailedException {
		try {
			return awaitAfter(trigger, responseClass, 0);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * This method works just like {@link #awaitAfter(Request, E, int)},
	 * but will wait without a timeout until a notification/indication is received, or the
	 * device disconnects.
	 *
	 * @param trigger  an optional request that will be executed after the notification
	 *                 callback has been initiated. Usually it's a write request that triggers
	 *                 the notification or indication. This request may no have been enqueued
	 *                 before, so you have to use {@link Request}.new...Request() instead of i.e.
	 *                 {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, byte[])}
	 *                 which returns already enqueued request.
	 * @param response the response object that will be returned.
	 * @return The object received with a notification or indication.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 * @see #awaitAfter(Request, Class)
	 */
	@SuppressWarnings("NullableProblems")
	@NonNull
	public <E extends DataReceivedCallback> E awaitAfter(@NonNull final Request trigger,
														 @NonNull final E response)
			throws DeviceDisconnectedException, RequestFailedException {
		try {
			return awaitAfter(trigger, response, 0);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic,
	 * for at most given number of milliseconds. Before binding the value change callback
	 * with given characteristic this method will execute the trigger's {@link Runnable#run()}.
	 * This is to ensure that the value change handler is bound to the characteristic before the
	 * trigger request is performed. Otherwise, a race condition may occur when either the triggered
	 * notification is received before setting the callback, or the callback awaits a notification
	 * but the trigger command has never been sent.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 * <p>
	 * Example of synchronous usage:
	 * <pre>
	 *     // First, enable notifications
	 *     enableNotifications(characteristic).await();
	 *
	 *     // Then, bind notification callback and write op code.
	 *     SomeResponse response = setNotificationCallback(characteristic)
	 *          .awaitAfter(Request.newWriteRequest(characteristic, OP_CODE),
	 *                      SomeResponse.class, 1000);
	 * </pre>
	 * </p>
	 *
	 * @param trigger       an optional request that will be executed after the notification
	 *                      callback has been initiated. Usually it's a write request that triggers
	 *                      the notification or indication. This request may no have been enqueued
	 *                      before, so you have to use {@link Request}.new...Request() instead of
	 *                      {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, byte[])}
	 *                      which returns already enqueued request.
	 * @param responseClass the response class. This class will be instantiate, therefore it has to
	 *                      have a default constructor.
	 * @param timeout       optional timeout in milliseconds.
	 * @return The object received with a notification or indication.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 */
	@SuppressWarnings({"NullableProblems", "ConstantConditions"})
	@NonNull
	public <E extends DataReceivedCallback> E awaitAfter(@NonNull final Request trigger,
														 @NonNull final Class<E> responseClass,
														 final int timeout)
			throws InterruptedException, DeviceDisconnectedException, RequestFailedException {
		try {
			Request.assertNotMainThread();

			E response = null;
			if (responseClass != null)
				response = responseClass.newInstance();
			return awaitAfter(trigger, response, timeout);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Couldn't instantiate " + responseClass.getCanonicalName()
					+ " class. Is the default constructor accessible?");
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Couldn't instantiate " + responseClass.getCanonicalName()
					+ " class. Does it have a default constructor with no arguments?");
		}
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic,
	 * for at most given number of milliseconds. Before binding the value change callback
	 * with given characteristic this method will execute the trigger.
	 * This is to ensure that the value change handler is bound to the characteristic before the
	 * trigger request is performed and that, if the request will fail, the app will not wait
	 * forever for the notification. Otherwise, a race condition may occur when either the triggered
	 * notification is received before setting the callback, or the callback awaits a notification
	 * but the trigger command has never been sent.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 * <p>
	 * Example of synchronous usage:
	 * <pre>
	 *     // First, enable notifications
	 *     enableNotifications(characteristic).await();
	 *
	 *     // Then, bind notification callback and write op code.
	 *     SomeResponse response = setNotificationCallback(characteristic)
	 *          .awaitAfter(Request.newWriteRequest(characteristic, OP_CODE),
	 *                      new SomeResponse(parameter), 1000);
	 * </pre>
	 * </p>
	 *
	 * @param trigger  an optional request that will be executed after the notification callback has
	 *                 been initiated. Usually it's a write request that triggers the
	 *                 notification or indication. This request may no have been enqueued before,
	 *                 so you have to use {@link Request}.new...Request() instead of i.e.
	 *                 {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, byte[])}
	 *                 which returns already enqueued request.
	 * @param response the response object that will be returned.
	 * @param timeout  optional timeout in milliseconds.
	 * @return The object received with a notification or indication.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 */
	@SuppressWarnings({"NullableProblems", "ConstantConditions"})
	@NonNull
	public <E extends DataReceivedCallback> E awaitAfter(@NonNull final Request trigger,
														 @NonNull final E response,
														 final int timeout)
			throws InterruptedException, DeviceDisconnectedException, RequestFailedException {
		Request.assertNotMainThread();
		cancelTimeout();

		final DataReceivedCallback vc = valueCallback;
		try {
			syncLock.close();
			with(response);

			deviceDisconnected = !bleManager.isConnected();

			// Ensure the trigger request it done after the callback has been set
			triggerStatus = BluetoothGatt.GATT_SUCCESS;
			if (trigger != null && trigger.enqueued) {
				throw new IllegalStateException("Request already enqueued; " +
						"use Request.new...Request() instead.");
			}
			if (!deviceDisconnected && trigger != null) {
				trigger.internalFail((device, status) -> {
					triggerStatus = status;
					syncLock.open();
				});
				bleManager.enqueue(trigger);
			}
			// Wait for value change event
			if (!deviceDisconnected && !syncLock.block(timeout)) {
				throw new InterruptedException();
			}
			if (deviceDisconnected) {
				syncLock.open();
				throw new DeviceDisconnectedException();
			}
			if (triggerStatus != BluetoothGatt.GATT_SUCCESS) {
				throw new RequestFailedException(trigger, triggerStatus);
			}
			return response;
		} finally {
			valueCallback = vc;
		}
	}

	/**
	 * Similar to {@link #await(Class)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is invalid, an exception is thrown.
	 * This allows to keep all error handling in one place.
	 *
	 * @param responseClass the result class. This class will be instantiate, therefore it
	 *                      has to have a default constructor.
	 * @param <E>           a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InvalidDataException        exception thrown when the data received were invalid and
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}
	 *                                     was called during parsing them.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 */
	@SuppressWarnings("ConstantConditions")
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(@NonNull final Class<E> responseClass)
			throws InvalidDataException, DeviceDisconnectedException {
		try {
			return awaitValidAfter(null, responseClass, 0);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		} catch (final RequestFailedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Similar to {@link #await(Class)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is invalid, an exception is thrown.
	 * This allows to keep all error handling in one place.
	 *
	 * @param response the result object.
	 * @param <E>      a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InvalidDataException        exception thrown when the data received were invalid and
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}
	 *                                     was called during parsing them.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 */
	@SuppressWarnings("ConstantConditions")
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(@NonNull final E response)
			throws InvalidDataException, DeviceDisconnectedException {
		try {
			return awaitValidAfter(null, response, 0);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		} catch (final RequestFailedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Similar to {@link #await(Class)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is invalid, an exception is thrown.
	 * This allows to keep all error handling in one place.
	 *
	 * @param responseClass the result class. This class will be instantiate, therefore it
	 *                      has to have a default constructor.
	 * @param <E>           a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws InvalidDataException        exception thrown when the data received were invalid and
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}
	 *                                     was called during parsing them.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 */
	@SuppressWarnings("ConstantConditions")
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(@NonNull final Class<E> responseClass,
														final int timeout)
			throws InterruptedException, InvalidDataException, DeviceDisconnectedException {
		try {
			return awaitValidAfter(null, responseClass, timeout);
		} catch (final RequestFailedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Similar to {@link #await(Class)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is invalid, an exception is thrown.
	 * This allows to keep all error handling in one place.
	 *
	 * @param response the result object.
	 * @param <E>      a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws InvalidDataException        exception thrown when the data received were invalid and
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}
	 *                                     was called during parsing them.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 */
	@SuppressWarnings("ConstantConditions")
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(@NonNull final E response,
														final int timeout)
			throws InterruptedException, InvalidDataException, DeviceDisconnectedException {
		try {
			return awaitValidAfter(null, response, timeout);
		} catch (final RequestFailedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Same as {@link #awaitAfter(Request, Class)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is not valid, this method will thrown
	 * an exception instead of just returning a response with {@link ProfileReadResponse#isValid()}
	 * returning false.
	 *
	 * @param trigger       an action that will be executed after the notification callback has
	 *                      been initiated. Usually it's a write request that triggers the
	 *                      notification or indication.
	 * @param responseClass the result class. This class will be instantiate, therefore it
	 *                      has to have a default constructor.
	 * @param <E>           a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InvalidDataException        exception thrown when the data received were invalid and
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}
	 *                                     was called during parsing them.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 * @see #awaitAfter(Request, Class)
	 */
	@NonNull
	public <E extends ProfileReadResponse> E awaitValidAfter(@NonNull final Request trigger,
															 @NonNull final Class<E> responseClass)
			throws InvalidDataException, DeviceDisconnectedException, RequestFailedException {
		try {
			return awaitValidAfter(trigger, responseClass, 0);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Same as {@link #awaitAfter(Request, Class)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is not valid, this method will thrown
	 * an exception instead of just returning a response with {@link ProfileReadResponse#isValid()}
	 * returning false.
	 *
	 * @param trigger  an action that will be executed after the notification callback has
	 *                 been initiated. Usually it's a write request that triggers the
	 *                 notification or indication.
	 * @param response the result object.
	 * @param <E>      a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InvalidDataException        exception thrown when the data received were invalid and
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}
	 *                                     was called during parsing them.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 * @see #awaitAfter(Request, Class)
	 */
	@NonNull
	public <E extends ProfileReadResponse> E awaitValidAfter(@NonNull final Request trigger,
															 @NonNull final E response)
			throws InvalidDataException, DeviceDisconnectedException, RequestFailedException {
		try {
			return awaitValidAfter(trigger, response, 0);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Same as {@link #awaitAfter(Request, Class, int)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is not valid, this method will thrown
	 * an exception instead of just returning a response with {@link ProfileReadResponse#isValid()}
	 * returning false.
	 *
	 * @param trigger       an action that will be executed after the notification callback has
	 *                      been initiated. Usually it's a write request that triggers the
	 *                      notification or indication.
	 * @param responseClass the result class. This class will be instantiate, therefore it
	 *                      has to have a default constructor.
	 * @param timeout       optional timeout in milliseconds.
	 * @param <E>           a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws InvalidDataException        exception thrown when the data received were invalid and
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}
	 *                                     was called during parsing them.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 */
	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	@NonNull
	public <E extends ProfileReadResponse> E awaitValidAfter(@NonNull final Request trigger,
															 @NonNull final Class<E> responseClass,
															 final int timeout)
			throws InterruptedException, InvalidDataException, DeviceDisconnectedException,
			RequestFailedException {
		final E response = awaitAfter(trigger, responseClass, timeout);
		if (response != null && !response.isValid()) {
			throw new InvalidDataException(response);
		}
		return response;
	}

	/**
	 * Same as {@link #awaitAfter(Request, Class, int)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is not valid, this method will thrown
	 * an exception instead of just returning a response with {@link ProfileReadResponse#isValid()}
	 * returning false.
	 *
	 * @param trigger  an action that will be executed after the notification callback has
	 *                 been initiated. Usually it's a write request that triggers the
	 *                 notification or indication.
	 * @param response the result object.
	 * @param timeout  optional timeout in milliseconds.
	 * @param <E>      a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws InvalidDataException        exception thrown when the data received were invalid and
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}
	 *                                     was called during parsing them.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 */
	@SuppressWarnings({"ConstantConditions", "NullableProblems"})
	@NonNull
	public <E extends ProfileReadResponse> E awaitValidAfter(@NonNull final Request trigger,
															 @NonNull final E response,
															 final int timeout)
			throws InterruptedException, InvalidDataException, DeviceDisconnectedException,
			RequestFailedException {
		final E result = awaitAfter(trigger, response, timeout);
		if (result != null && !result.isValid()) {
			throw new InvalidDataException(result);
		}
		return result;
	}

	ValueChangedCallback free() {
		valueCallback = null;
		dataMerger = null;
		progressCallback = null;
		deviceDisconnected = false;
		buffer = null;
		count = 0;
		return this;
	}

	void notifyValueChanged(final BluetoothDevice device, final byte[] value) {
		// Keep a reference to the value callback, as it may change during execution
		final DataReceivedCallback valueCallback = this.valueCallback;

		// With no value callback there is no need for any merging
		if (valueCallback == null) {
			syncLock.open();
			return;
		}

		if (dataMerger == null) {
			cancelTimeout();
			valueCallback.onDataReceived(device, new Data(value));
			syncLock.open();
		} else {
			if (progressCallback != null)
				progressCallback.onPacketReceived(device, value, count);
			if (buffer == null)
				buffer = new DataStream();
			if (dataMerger.merge(buffer, value, count++)) {
				cancelTimeout();
				valueCallback.onDataReceived(device, buffer.toData());
				buffer = null;
				count = 0;
				syncLock.open();
			} // else
			// wait for more packets to be merged
		}
	}

	void notifyDeviceDisconnected(final BluetoothDevice device) {
		deviceDisconnected = true;
		syncLock.open();
		notifyDataNotReceived();
	}

	private void cancelTimeout() {
		final Runnable handler = timeoutHandler;
		if (handler != null) {
			bleManager.mHandler.removeCallbacks(handler);
		}
		timeoutHandler = null;
	}

	private void notifyDataNotReceived() {
		final Runnable handler = timeoutHandler;
		if (handler != null) {
			handler.run();
			cancelTimeout();
		}
	}
}
