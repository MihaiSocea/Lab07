package ro.pub.cs.systems.eim.bluetoothchatapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var chatArrayAdapter: ArrayAdapter<String>
    private var chatMessages = mutableListOf<String>()

    private var selectedDevice: BluetoothDevice? = null

    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var listDevicesButton: Button

    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    companion object {
        val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_PERMISSIONS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO 1: Initialize views
        initViews()

        // TODO 2: Initialize Bluetooth
        initBluetooth()

        // TODO 3: Request permissions
        checkPermissions()

        // TODO 6: Start server socket for incoming connections
        startServer()

        // TODO 7: Send message using ConnectedThread
        sendMessage()
    }

    // TODO 1: Implement the method that initializes the views
    private fun initViews() {
    }

    // TODO 2: Implement the method that initializes Bluetooth
    private fun initBluetooth() {

    }

    // TODO 3: Implement the method that checks permissions
    private fun checkPermissions() {

    }

    // TODO 5: Implement the method that displays a dialog for selecting a paired device
    private fun listPairedDevices() {

    }

    // TODO 6: Implement server socket to listen for incoming connections
    private fun startServer() {
    }

    // TODO 7: Implement the method that sends a message to the connected device
    private fun sendMessage() {

    }

    // Update the UI with a new message
    fun addChatMessage(message: String) {
        chatMessages.add(message)
        chatArrayAdapter.notifyDataSetChanged()
    }

    // TODO 4: Handle permission results and activate Bluetooth programmatically
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    fun manageConnectedSocket(socket: BluetoothSocket) {
        connectedThread?.cancel()
        connectedThread = ConnectedThread(this, socket).apply { start() }
    }

    // TODO 4: Handle Bluetooth enable result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    // TODO 8: Clean up threads on activity destroy
    override fun onDestroy() {
        super.onDestroy()

    }
}
