package no.nordicsemi.andorid.ble.test.server.data

enum class SplitterFlag(val value: Byte) {
    FULL(0b11.toByte()),
    BEGIN(0b00.toByte()),
    CONTINUATION(0b01.toByte()),
    END(0b10.toByte())
}