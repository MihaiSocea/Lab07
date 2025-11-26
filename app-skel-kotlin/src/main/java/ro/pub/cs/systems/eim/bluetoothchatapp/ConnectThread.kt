package ro.pub.cs.systems.eim.bluetoothchatapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class ConnectThread(
        private val mainActivity: MainActivity,
        private val bluetoothAdapter: BluetoothAdapter,
        device: BluetoothDevice?,
        uuid: UUID
) : Thread() {

    private var socket: BluetoothSocket? = null

    init {
        try {
            if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                socket = device?.createRfcommSocketToServiceRecord(uuid)
            }
        } catch (e: IOException) {
            Log.d("Connect->Constructor", e.toString())
        }
    }

    override fun run() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (socket == null) {
            Log.d("ConnectThread", "Socket is null, cannot connect.")
            return
        }
        bluetoothAdapter.cancelDiscovery()
        try {
            socket?.connect()
            mainActivity.manageConnectedSocket(socket!!)
        } catch (connectException: IOException) {
            mainActivity.runOnUiThread {
                Toast.makeText(mainActivity, "Connection failed.", Toast.LENGTH_SHORT).show()
            }
            try {
                socket?.close()
            } catch (closeException: IOException) {
                Log.d("Connect->Run", closeException.toString())
            }
        }
    }

    fun cancel() {
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.d("Connect->Cancel", e.toString())
        }
    }
}
