package com.nichefinder.obd2.transport

/**
 * Byte-stream link to an ELM327-class adapter. Implementations on Android wrap
 * Bluetooth Classic RFCOMM, BLE (Nordic UART-style), or a WiFi TCP socket
 * (typically 192.168.0.10:35000); tests use a simulator.
 */
interface ObdTransport {
    suspend fun connect()
    suspend fun disconnect()
    val isConnected: Boolean

    /**
     * Send one raw command line (without trailing CR) and return the adapter's
     * response up to (excluding) the '>' prompt. Implementations must handle
     * partial reads: ELM clones fragment responses arbitrarily.
     */
    suspend fun exchange(command: String): String
}

class ObdTransportException(message: String, cause: Throwable? = null) : Exception(message, cause)
