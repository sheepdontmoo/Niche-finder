package com.nichefinder.scanner.transport

import com.nichefinder.obd2.transport.ObdTransport
import com.nichefinder.obd2.transport.ObdTransportException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.InputStream
import java.io.OutputStream

/**
 * Shared logic for stream-based adapters (Bluetooth Classic RFCOMM, WiFi TCP):
 * write "CMD\r", accumulate bytes until the ELM327 '>' prompt.
 */
abstract class StreamTransport : ObdTransport {
    protected abstract val input: InputStream
    protected abstract val output: OutputStream

    override suspend fun exchange(command: String): String = withContext(Dispatchers.IO) {
        try {
            withTimeout(RESPONSE_TIMEOUT_MS) {
                output.write((command + "\r").toByteArray(Charsets.US_ASCII))
                output.flush()
                val sb = StringBuilder()
                val buf = ByteArray(256)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) throw ObdTransportException("Adapter closed the connection")
                    for (i in 0 until n) {
                        val c = buf[i].toInt().toChar()
                        if (c == '>') return@withTimeout sb.toString()
                        sb.append(c)
                    }
                }
                @Suppress("UNREACHABLE_CODE")
                sb.toString()
            }
        } catch (e: ObdTransportException) {
            throw e
        } catch (e: Exception) {
            throw ObdTransportException("Command '$command' failed: ${e.message}", e)
        }
    }

    companion object {
        // Generous: cheap clones answer slow, and ATZ + SEARCHING can take seconds.
        const val RESPONSE_TIMEOUT_MS = 10_000L
    }
}
