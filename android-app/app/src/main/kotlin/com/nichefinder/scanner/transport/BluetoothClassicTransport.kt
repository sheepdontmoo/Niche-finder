package com.nichefinder.scanner.transport

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.nichefinder.obd2.transport.ObdTransportException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Bluetooth Classic (SPP/RFCOMM) — what $10-25 ELM327 clones and most
 * Vgate/OBDLink adapters speak. Device must already be paired in system
 * settings (standard flow for these adapters; PIN is usually 1234 or 0000).
 */
@SuppressLint("MissingPermission") // BLUETOOTH_CONNECT is checked before any device is offered in UI
class BluetoothClassicTransport(private val device: BluetoothDevice) : StreamTransport() {

    private var socket: BluetoothSocket? = null
    override lateinit var input: InputStream
    override lateinit var output: OutputStream

    override val isConnected: Boolean get() = socket?.isConnected == true

    override suspend fun connect() = withContext(Dispatchers.IO) {
        try {
            val s = device.createRfcommSocketToServiceRecord(SPP_UUID)
            s.connect()
            socket = s
            input = s.inputStream
            output = s.outputStream
        } catch (e: Exception) {
            throw ObdTransportException(
                "Can't connect to ${device.name ?: device.address}. " +
                    "Is the adapter plugged in and the ignition on?", e
            )
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching { socket?.close() }
        socket = null
    }

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
