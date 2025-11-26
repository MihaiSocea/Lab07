package ro.pub.cs.systems.eim.bluetoothchatapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
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
    private val chatMessages = mutableListOf<String>()

    private var selectedDevice: BluetoothDevice? = null
    private lateinit var messageEditText: EditText

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

        val chatListView: ListView = findViewById(R.id.chatListView)
        messageEditText = findViewById(R.id.messageEditText)
        val sendButton: Button = findViewById(R.id.sendButton)
        val listDevicesButton: Button = findViewById(R.id.listDevicesButton)
        val startServerButton: Button = findViewById(R.id.startServerButton) // Initialize the new button

        chatArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, chatMessages)
        chatListView.adapter = chatArrayAdapter

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Check if Bluetooth is supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        requestPermissions()

        listDevicesButton.setOnClickListener { listPairedDevices() }
        // Set up button listeners
        startServerButton.setOnClickListener {
            if (acceptThread == null) {
                acceptThread = AcceptThread(this, bluetoothAdapter, MY_UUID)
                acceptThread?.start()
                Toast.makeText(this, "Server started. Listening for connections.", Toast.LENGTH_SHORT).show()
                startServerButton.setBackgroundColor(Color.parseColor("#FF0000"))
                startServerButton.text = "Stop Server"
            } else {
                acceptThread?.cancel()
                acceptThread = null
                Toast.makeText(this, "Server stopped.", Toast.LENGTH_SHORT).show()
                Log.d("MainActivity", "Server stopped.")
                startServerButton.setBackgroundColor(Color.parseColor("#00FF00"))
                startServerButton.text = "Start Server"
            }
        }

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString()
            if (message.isNotEmpty()) {
                if (connectedThread != null) {
                    connectedThread?.write(message.toByteArray())
                    messageEditText.text.clear()
                    addChatMessage("Me: $message")
                } else {
                    Toast.makeText(this, "No connected device.", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "No connected device.")
                }
            }
        }
    }

    fun addChatMessage(message: String) {
        // Post updates to the UI thread
        runOnUiThread {
            chatMessages.add(message)
            chatArrayAdapter.notifyDataSetChanged()
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSIONS)
    }

    private fun listPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Request permission or show a toast
            Toast.makeText(this, "Bluetooth Connect permission not granted.", Toast.LENGTH_SHORT).show()
            return
        }
        val pairedDevices = bluetoothAdapter.bondedDevices
        val deviceList = mutableListOf<String>()
        val devices = mutableListOf<BluetoothDevice>()

        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                deviceList.add("${device.name}\n${device.address}")
                devices.add(device)
            }
        }

        AlertDialog.Builder(this).apply {
            setTitle("Select Device")
            val deviceArrayAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, deviceList)
            setAdapter(deviceArrayAdapter) { _, which ->
                selectedDevice = devices[which]
                connectThread = ConnectThread(this@MainActivity, bluetoothAdapter, selectedDevice!!, MY_UUID).apply { start() }
            }
            show()
        }
    }

    fun manageConnectedSocket(socket: BluetoothSocket) {
        connectedThread?.cancel()
        connectedThread = ConnectedThread(this, socket).apply { start() }

        // Stop the AcceptThread once a connection is established
        if (acceptThread != null) {
            acceptThread?.cancel()
            acceptThread = null
            // Update button on UI thread
            runOnUiThread {
                val startServerButton: Button = findViewById(R.id.startServerButton)
                startServerButton.text = "Start Server"
                startServerButton.setBackgroundColor(Color.parseColor("#00FF00"))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }

            if (!allPermissionsGranted) {
                Toast.makeText(this, "Permissions are required for Bluetooth functionality.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                // Ensure Bluetooth is enabled after permissions are granted
                if (!bluetoothAdapter.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK) {
            Toast.makeText(this, "Bluetooth must be enabled to continue.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        acceptThread?.cancel()
        connectThread?.cancel()
        connectedThread?.cancel()
    }
}
