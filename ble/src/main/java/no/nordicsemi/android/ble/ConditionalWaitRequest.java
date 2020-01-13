package no.nordicsemi.android.ble;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class ConditionalWaitRequest<T> extends AwaitingRequest implements Operation {

	/**
	 * The condition object.
	 */
	public interface Condition<T> {
		boolean predicate(@Nullable final T parameter);
	}

	@NonNull
	private final Condition<T> condition;
	@Nullable
	private final T parameter;
	/** Expected value of the condition to stop waiting. */
	private boolean expected = false;

	ConditionalWaitRequest(@NonNull final Type type, @NonNull final Condition<T> condition,
						   @Nullable final T parameter) {
		super(type);
		this.condition = condition;
		this.parameter = parameter;
	}

	@NonNull
	@Override
	ConditionalWaitRequest<T> setRequestHandler(@NonNull final RequestHandler requestHandler) {
		super.setRequestHandler(requestHandler);
		return this;
	}

	@NonNull
	@Override
	public ConditionalWaitRequest<T> setHandler(@NonNull final Handler handler) {
		super.setHandler(handler);
		return this;
	}

	@Override
	@NonNull
	public ConditionalWaitRequest<T> done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public ConditionalWaitRequest<T> fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public ConditionalWaitRequest<T> invalid(@NonNull final InvalidRequestCallback callback) {
		super.invalid(callback);
		return this;
	}

	@Override
	@NonNull
	public ConditionalWaitRequest<T> before(@NonNull final BeforeCallback callback) {
		super.before(callback);
		return this;
	}

	/**
	 * Negates the expected value of the predicate.
	 *
	 * @return The request.
	 */
	@NonNull
	public ConditionalWaitRequest<T> negate() {
		expected = true;
		return this;
	}

	boolean isFulfilled() {
		try {
			return condition.predicate(parameter) == expected;
		} catch (final Exception e) {
			Log.e("ConditionalWaitRequest", "Error while checking predicate", e);
			return true;
		}
	}
}
