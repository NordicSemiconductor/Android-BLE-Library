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

package no.nordicsemi.android.ble

import androidx.annotation.IntRange
import no.nordicsemi.android.ble.callback.BeforeCallback
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.callback.InvalidRequestCallback
import no.nordicsemi.android.ble.callback.SuccessCallback
import java.util.*

open class RequestQueue internal constructor() : SimpleRequest(Request.Type.SET) {
    /**
     * A list of operations that will be executed together.
     */
    private val requests: Queue<Request>

    /**
     * Returns whether the set is empty, or not.
     *
     * @return true if the set is empty. Set gets emptied while it all enqueued operations
     * are being executed.
     */
    val isEmpty: Boolean
        get() = requests.isEmpty()

    /**
     * Returns the next [Request] to be enqueued.
     *
     * @return the next request.
     */
    internal open// poll() may also throw an exception
    // See: https://github.com/NordicSemiconductor/Android-BLE-Library/issues/37
    val next: Request?
        get() {
            try {
                return requests.remove()
            } catch (e: Exception) {
                return null
            }

        }

    init {
        requests = LinkedList()
    }

    override fun setManager(manager: BleManager<*>): RequestQueue {
        super.setManager(manager)
        return this
    }

    override fun done(callback: SuccessCallback): RequestQueue {
        super.done(callback)
        return this
    }

    override fun fail(callback: FailCallback): RequestQueue {
        super.fail(callback)
        return this
    }

    override fun invalid(callback: InvalidRequestCallback): RequestQueue {
        super.invalid(callback)
        return this
    }

    override fun before(callback: BeforeCallback): RequestQueue {
        super.before(callback)
        return this
    }

    /**
     * Enqueues a new operation. All operations will be executed sequentially in order they were
     * added.
     *
     * @param operation the new operation to be enqueued.
     * @throws IllegalStateException    if the operation was enqueued before.
     * @throws IllegalArgumentException if the operation is not a [Request].
     */
    open fun add(operation: Operation): RequestQueue {
        if (operation is Request) {
            val request = operation as Request
            // Validate
            if (request.enqueued)
                throw IllegalStateException("Request already enqueued")
            // Add
            requests.add(request)
            // Mark
            request.enqueued = true
            return this
        } else {
            throw IllegalArgumentException("Operation does not extend Request")
        }
    }

    /**
     * Returns number of enqueued operations.
     *
     * @return the size of the internal operations list.
     */
    @IntRange(from = 0)
    open fun size(): Int {
        return requests.size
    }

    /**
     * Cancels all the enqueued operations that were not executed yet.
     * The one currently executed will be finished.
     *
     *
     * It is safe to call this method in [Request.done] or
     * [Request.fail] callback;
     */
    open fun cancelQueue() {
        requests.clear()
    }

    /**
     * Returns whether there are more operations to be executed.
     *
     * @return true, if not all operations were completed.
     */
    internal open fun hasMore(): Boolean {
        return !requests.isEmpty()
    }
}
