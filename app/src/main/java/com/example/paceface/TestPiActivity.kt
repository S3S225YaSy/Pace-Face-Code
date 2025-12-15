package com.example.paceface
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paceface.databinding.TestPiBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.reflect.Method
class TestPiActivity : AppCompatActivity() {
    private lateinit var binding: TestPiBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TestPiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.onClick.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                sendTestData()
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun sendTestData() {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device: BluetoothDevice = bluetoothAdapter.bondedDevices.firstOrNull {
            it.name == "raspberrypi"
        } ?: run {
            println("ラズパイが見つかりません")
            return
        }
        val socket: BluetoothSocket = try {
            // ★ hidden API を reflection で呼ぶ
            val method: Method =
                device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
            method.invoke(device, 1) as BluetoothSocket
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        try {
            bluetoothAdapter.cancelDiscovery()
            socket.connect()
            val message = "1\n"
            socket.outputStream.write(message.toByteArray())
            socket.outputStream.flush()
            println("送信成功: $message")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}