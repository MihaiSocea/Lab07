package ro.pub.cs.systems.eim.bluetoothchatapp;

import android.bluetooth.BluetoothSocket;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final MainActivity mainActivity;

    public ConnectedThread(MainActivity activity, BluetoothSocket socket) {
        this.mainActivity = activity;
        this.socket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.d("Connected->Constructor", e.toString());
        }

        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("Connected->Constructor", e.toString());
        }

        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String remoteDeviceName = socket.getRemoteDevice().getName();

        mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "Connected to " + remoteDeviceName, Toast.LENGTH_SHORT).show());

        while (true) {
            try {
                bytes = inputStream.read(buffer);
                String incomingMessage = new String(buffer, 0, bytes);
                mainActivity.runOnUiThread(() -> mainActivity.addChatMessage(remoteDeviceName + ": " + incomingMessage));
            } catch (IOException e) {
                mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "Connection lost.", Toast.LENGTH_SHORT).show());
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "Failed to send message.", Toast.LENGTH_SHORT).show());
            Log.d("Connected->Write", e.toString());
        }
    }

    public void cancel() {
        try {
            if (socket == null) {
                Log.d("ConnectedThread", "Socket is null, cannot close.");
                return;
            }
            socket.close();
        } catch (IOException e) {
            Log.d("Connected->Cancel", e.toString());
        }
    }
}
