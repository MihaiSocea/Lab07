package ro.pub.cs.systems.eim.bluetoothchatapp

import android.Manifest
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ConnectedThread(
        private val mainActivity: MainActivity,
        private val socket: BluetoothSocket
) : Thread() {

    private val inputStream: InputStream?
    private val outputStream: OutputStream?

            init {
        var tmpIn: InputStream? = null
        var tmpOut: OutputStream? = null

        try {
            tmpIn = socket.inputStream
        } catch (e: IOException) {
            Log.d("Connected->Constructor", e.toString())
        }

        try {
            tmpOut = socket.outputStream
        } catch (e: IOException) {
            Log.d("Connected->Constructor", e.toString())
        }

        inputStream = tmpIn
        outputStream = tmpOut
    }

    override fun run() {
        val buffer = ByteArray(1024)
        var bytes: Int

        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val remoteDeviceName = socket.remoteDevice.name

        mainActivity.runOnUiThread {
            Toast.makeText(mainActivity, "Connected to $remoteDeviceName", Toast.LENGTH_SHORT).show()
        }

        while (true) {
            try {
                bytes = inputStream?.read(buffer) ?: break
                        val incomingMessage = String(buffer, 0, bytes)
                mainActivity.runOnUiThread {
                    mainActivity.addChatMessage("$remoteDeviceName: $incomingMessage")
                }
            } catch (e: IOException) {
                mainActivity.runOnUiThread {
                    Toast.makeText(mainActivity, "Connection lost.", Toast.LENGTH_SHORT).show()
                }
                break
            }
        }
    }

    fun write(bytes: ByteArray) {
        try {
            outputStream?.write(bytes)
        } catch (e: IOException) {
            mainActivity.runOnUiThread {
                Toast.makeText(mainActivity, "Failed to send message.", Toast.LENGTH_SHORT).show()
            }
            Log.d("Connected->Write", e.toString())
        }
    }

    fun cancel() {
        try {
            socket.close()
        } catch (e: IOException) {
            Log.d("Connected->Cancel", e.toString())
        }
    }
}
