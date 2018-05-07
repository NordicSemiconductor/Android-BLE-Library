package no.nordicsemi.android.ble.exception;

import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;

public final class InvalidDataException extends Exception {
	private final ProfileReadResponse response;

	public InvalidDataException(final @NonNull ProfileReadResponse response) {
		this.response = response;
	}

	public ProfileReadResponse getResponse() {
		return response;
	}
}
