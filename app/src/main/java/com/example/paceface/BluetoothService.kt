//BluetoothService.kt
package com.example.paceface

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.OutputStream

class BluetoothService : Service() {

    private var piSocket: BluetoothSocket? = null
    private var piOutputStream: OutputStream? = null
    private val binder = BluetoothBinder()

    inner class BluetoothBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    @SuppressLint("MissingPermission")
    fun connectToRaspberryPi() {
        if (piSocket?.isConnected == true) {
            Log.d("BluetoothService", "Already connected")
            return
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device = bluetoothAdapter.bondedDevices.firstOrNull {
            it.name == "raspberrypi"
        } ?: run {
            Log.e("BluetoothService", "Raspberry Pi not found")
            return
        }

        try {
            val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
            piSocket = method.invoke(device, 1) as BluetoothSocket
            bluetoothAdapter.cancelDiscovery()
            piSocket!!.connect()
            piOutputStream = piSocket!!.outputStream
            Log.d("BluetoothService", "Connected successfully")
        } catch (e: Exception) {
            Log.e("BluetoothService", "Connection failed", e)
            piSocket = null
            piOutputStream = null
        }
    }

    fun sendEmotion(emotionId: Int) {
        if (piOutputStream == null) {
            Log.e("BluetoothService", "Cannot send: piOutputStream is null. Attempting to reconnect...")
            connectToRaspberryPi()
        }
        try {
            piOutputStream?.write("$emotionId\n".toByteArray())
            piOutputStream?.flush()
            Log.d("BluetoothService", "Sent emotion: $emotionId")
        } catch (e: Exception) {
            Log.e("BluetoothService", "Send failed", e)
            piSocket = null
            piOutputStream = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            piSocket?.close()
        } catch (e: Exception) {
            Log.e("BluetoothService", "Close failed", e)
        }
    }
}