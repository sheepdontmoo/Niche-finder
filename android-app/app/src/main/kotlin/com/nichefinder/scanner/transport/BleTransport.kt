package com.nichefinder.scanner.transport

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.nichefinder.obd2.transport.ObdTransport
import com.nichefinder.obd2.transport.ObdTransportException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import java.util.UUID

/**
 * Bluetooth Low Energy adapters (Veepeak OBDCheck BLE, vLinker, OBDLink CX).
 * These expose a UART-style service: one characteristic to write, one that
 * notifies response bytes. Known service layouts are tried in order.
 *
 * This is the feature Torque Pro never shipped in 8 years of requests.
 */
@SuppressLint("MissingPermission") // BLUETOOTH_CONNECT/SCAN checked before scanning in UI
class BleTransport(
    private val context: Context,
    private val device: BluetoothDevice,
) : ObdTransport {

    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private val incoming = Channel<ByteArray>(Channel.UNLIMITED)
    private var ready = CompletableDeferred<Unit>()

    override val isConnected: Boolean get() = writeChar != null

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> g.discoverServices()
                BluetoothProfile.STATE_DISCONNECTED -> {
                    writeChar = null
                    if (!ready.isCompleted) ready.completeExceptionally(
                        ObdTransportException("BLE adapter disconnected (status $status)")
                    )
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            for ((serviceUuid, writeUuid, notifyUuid) in KNOWN_LAYOUTS) {
                val service = g.getService(serviceUuid) ?: continue
                val w = service.getCharacteristic(writeUuid) ?: continue
                val n = service.getCharacteristic(notifyUuid) ?: continue
                g.setCharacteristicNotification(n, true)
                n.getDescriptor(CCC_DESCRIPTOR)?.let { d ->
                    @Suppress("DEPRECATION")
                    d.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    g.writeDescriptor(d)
                }
                writeChar = w
                ready.complete(Unit)
                return
            }
            ready.completeExceptionally(
                ObdTransportException("No known ELM327 BLE service on ${device.name ?: device.address}")
            )
        }

        @Deprecated("Pre-33 callback still delivered on all current devices")
        override fun onCharacteristicChanged(g: BluetoothGatt, c: BluetoothGattCharacteristic) {
            @Suppress("DEPRECATION")
            c.value?.let { incoming.trySend(it) }
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt, c: BluetoothGattCharacteristic, value: ByteArray,
        ) {
            incoming.trySend(value)
        }
    }

    override suspend fun connect() {
        ready = CompletableDeferred()
        gatt = device.connectGatt(context, false, callback)
        withTimeout(CONNECT_TIMEOUT_MS) { ready.await() }
    }

    override suspend fun disconnect() {
        writeChar = null
        runCatching { gatt?.disconnect(); gatt?.close() }
        gatt = null
    }

    override suspend fun exchange(command: String): String {
        val g = gatt ?: throw ObdTransportException("Not connected")
        val w = writeChar ?: throw ObdTransportException("Not connected")
        // Drain stale bytes from a previous timed-out command
        while (incoming.tryReceive().isSuccess) { /* discard */ }

        @Suppress("DEPRECATION")
        w.value = (command + "\r").toByteArray(Charsets.US_ASCII)
        w.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        @Suppress("DEPRECATION")
        if (!g.writeCharacteristic(w)) throw ObdTransportException("BLE write failed for '$command'")

        val sb = StringBuilder()
        return withTimeout(StreamTransport.RESPONSE_TIMEOUT_MS) {
            while (true) {
                val chunk = incoming.receive()
                for (b in chunk) {
                    val c = b.toInt().toChar()
                    if (c == '>') return@withTimeout sb.toString()
                    sb.append(c)
                }
            }
            @Suppress("UNREACHABLE_CODE")
            sb.toString()
        }
    }

    companion object {
        private const val CONNECT_TIMEOUT_MS = 15_000L
        private val CCC_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private fun uuid16(short: String): UUID =
            UUID.fromString("0000$short-0000-1000-8000-00805f9b34fb")

        /** (service, write, notify) layouts seen on common BLE adapters. */
        private val KNOWN_LAYOUTS = listOf(
            Triple(uuid16("fff0"), uuid16("fff2"), uuid16("fff1")),   // most Chinese BLE modules
            Triple(uuid16("ffe0"), uuid16("ffe1"), uuid16("ffe1")),   // HM-10 style, single char
            Triple(                                                    // Nordic UART (OBDLink CX)
                UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"),
                UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"),
                UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"),
            ),
        )
    }
}
