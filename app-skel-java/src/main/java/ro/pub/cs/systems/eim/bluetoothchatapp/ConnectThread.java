package ro.pub.cs.systems.eim.bluetoothchatapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private BluetoothSocket socket;
    private final BluetoothAdapter bluetoothAdapter;
    private final MainActivity mainActivity;

    public ConnectThread(MainActivity activity, BluetoothAdapter adapter, BluetoothDevice device, UUID uuid) {
        this.mainActivity = activity;
        this.bluetoothAdapter = adapter;

        BluetoothSocket tmp = null;
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Log.d("Connect->Constructor", e.toString());
        }
        socket = tmp;
    }

    public void run() {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (socket == null) {
            Log.d("ConnectThread", "Socket is null, cannot connect.");
            return;
        }
        bluetoothAdapter.cancelDiscovery();
        try {
            socket.connect();
            mainActivity.manageConnectedSocket(socket);
        } catch (IOException connectException) {
            mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "Connection failed.", Toast.LENGTH_SHORT).show());
            try {
                socket.close();
            } catch (IOException closeException) {
                Log.d("Connect->Run", closeException.toString());
            }
        }
    }

    public void cancel() {
        try {
            if (socket == null) {
                return;
            }
            socket.close();
        } catch (IOException e) {
            Log.d("Connect->Cancel", e.toString());
        }
    }
}
