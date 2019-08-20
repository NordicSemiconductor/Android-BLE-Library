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

import no.nordicsemi.android.ble.callback.BeforeCallback
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.callback.InvalidRequestCallback
import no.nordicsemi.android.ble.callback.SuccessCallback

class ReliableWriteRequest : RequestQueue() {
    private var initialized: Boolean = false
    private var closed: Boolean = false
    private var cancelled: Boolean = false

    override val next: Request?
        get() {
            if (!initialized) {
                initialized = true
                return Request.newBeginReliableWriteRequest()
            }
            if (super.isEmpty) {
                closed = true

                return if (cancelled) Request.newAbortReliableWriteRequest() else Request.newExecuteReliableWriteRequest()
            }
            return super.next
        }

    override fun setManager(manager: BleManager<*>): ReliableWriteRequest {
        super.setManager(manager)
        return this
    }

    override fun done(callback: SuccessCallback): ReliableWriteRequest {
        super.done(callback)
        return this
    }

    override fun fail(callback: FailCallback): ReliableWriteRequest {
        super.fail(callback)
        return this
    }

    override fun invalid(callback: InvalidRequestCallback): ReliableWriteRequest {
        super.invalid(callback)
        return this
    }

    override fun before(callback: BeforeCallback): ReliableWriteRequest {
        super.before(callback)
        return this
    }

    override fun add(operation: Operation): ReliableWriteRequest {
        super.add(operation)
        // Make sure the write request uses splitting, as Long Write is not supported
        // in Reliable Write sub-procedure.
        if (operation is WriteRequest) {
            operation.forceSplit()
        }
        return this
    }

    override fun cancelQueue() {
        cancelled = true
        super.cancelQueue()
    }

    /**
     * Alias for [.cancelQueue].
     */
    fun abort() {
        cancelQueue()
    }

    override fun size(): Int {
        var size = super.size()

        // Add Begin Reliable Write
        if (!initialized)
            size += 1

        // Add Execute or Abort Reliable Write
        if (!closed)
            size += 1
        return size
    }

    override fun hasMore(): Boolean {
        // If no operations were added, consider the RW request empty, no requests will be executed.
        return if (!initialized) super.hasMore() else !closed
    }
}
