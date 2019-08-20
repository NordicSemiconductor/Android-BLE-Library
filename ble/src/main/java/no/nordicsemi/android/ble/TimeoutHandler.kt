package no.nordicsemi.android.ble

abstract class TimeoutHandler {

    /**
     * Method called when the request timed out.
     *
     * @param request the request that timed out.
     */
    internal abstract fun onRequestTimeout(request: TimeoutableRequest)
}
