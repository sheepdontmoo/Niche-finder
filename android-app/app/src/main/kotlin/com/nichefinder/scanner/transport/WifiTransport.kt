package com.nichefinder.scanner.transport

import com.nichefinder.obd2.transport.ObdTransportException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * WiFi ELM327 adapters: phone joins the adapter's access point, adapter
 * listens on a TCP socket — 192.168.0.10:35000 on nearly all of them.
 */
class WifiTransport(
    private val host: String = "192.168.0.10",
    private val port: Int = 35000,
) : StreamTransport() {

    private var socket: Socket? = null
    override lateinit var input: InputStream
    override lateinit var output: OutputStream

    override val isConnected: Boolean get() = socket?.isConnected == true

    override suspend fun connect() = withContext(Dispatchers.IO) {
        try {
            val s = Socket()
            s.soTimeout = RESPONSE_TIMEOUT_MS.toInt()
            s.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
            socket = s
            input = s.getInputStream()
            output = s.getOutputStream()
        } catch (e: Exception) {
            throw ObdTransportException(
                "Can't reach $host:$port. Is your phone on the adapter's WiFi network?", e
            )
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching { socket?.close() }
        socket = null
    }

    companion object {
        private const val CONNECT_TIMEOUT_MS = 8_000
    }
}
