package ro.pub.cs.systems.eim.bluetoothchatapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

class AcceptThread(
        private val mainActivity: MainActivity,
        adapter: BluetoothAdapter,
        uuid: UUID
) : Thread() {

    private var serverSocket: BluetoothServerSocket? = null

    init {
        var tmp: BluetoothServerSocket? = null
        try {
            if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                tmp = adapter.listenUsingRfcommWithServiceRecord("BluetoothChatApp", uuid)
            }
        } catch (e: IOException) {
            Log.d("Accept->Constructor", e.toString())
        }
        serverSocket = tmp
    }

    override fun run() {
        if (serverSocket == null) {
            Log.d("AcceptThread", "ServerSocket is null, cannot accept connections.")
            return
        }

        while (true) {
            val socket: BluetoothSocket? = try {
                serverSocket?.accept()
            } catch (e: IOException) {
                Log.d("Accept->Run", "Socket accept failed: ${e.message}")
                break
            }

            socket?.let {
                mainActivity.manageConnectedSocket(it)
                try {
                    serverSocket?.close()
                } catch (e: IOException) {
                    Log.d("Accept->Run", "Failed to close server socket: ${e.message}")
                }
            }
        }
    }

    fun cancel() {
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            Log.d("Accept->Cancel", "Failed to close server socket: ${e.message}")
        }
    }
}
