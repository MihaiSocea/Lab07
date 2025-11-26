package ro.pub.cs.systems.eim.bluetoothchatapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class AcceptThread extends Thread {
    private BluetoothServerSocket serverSocket;
    private final MainActivity mainActivity;

    public AcceptThread(MainActivity activity, BluetoothAdapter adapter, UUID uuid) {
        this.mainActivity = activity;

        BluetoothServerSocket tmp = null;
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            tmp = adapter.listenUsingRfcommWithServiceRecord("BluetoothChatApp", uuid);
        } catch (IOException e) {
            Log.d("Accept->Constructor", e.toString());
        }
        serverSocket = tmp;
    }

    public void run() {
        if (serverSocket == null) {
            Log.d("AcceptThread", "ServerSocket is null, cannot accept connections.");
            return;
        }
        BluetoothSocket socket;
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                break;
            }

            if (socket != null) {
                mainActivity.manageConnectedSocket(socket);
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    Log.d("Accept->Run", e.toString());
                }
                break;
            }
        }
    }

    public void cancel() {
        try {
            if (serverSocket == null) {
                Log.d("AcceptThread", "ServerSocket is null, cannot close.");
                return;
            }
            serverSocket.close();
        } catch (IOException e) {
            Log.d("Accept->Cancel", e.toString());
        }
    }
}
