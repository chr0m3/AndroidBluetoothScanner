package me.chr0m3.android_bluetooth_scanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Set;


public class MainActivity extends AppCompatActivity {
    final int PERMISSION_ACCESS_COARSE_LOCATION = 0;
    final int INTENT_REQUEST_BLUETOOTH_ENABLE = 1;

    ArrayAdapter<String> mBluetoothDeviceArrayAdapter = null;
    BluetoothAdapter mBluetoothAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        this.mBluetoothDeviceArrayAdapter = new ArrayAdapter<>(this, R.layout.bluetooth_device);

        ListView listView = findViewById(R.id.bluetoothDeviceList);
        listView.setAdapter(mBluetoothDeviceArrayAdapter);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, this.PERMISSION_ACCESS_COARSE_LOCATION);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.INTENT_REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                this.scanBluetoothDevices();
            }
        }
    }

    public void onStartScanButtonClicked(View view) {
        this.mBluetoothDeviceArrayAdapter.clear();

        Log.i("Android Bluetooth Scanner", "Getting bluetooth adapter...");
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.e("Android Bluetooth Scanner", "Device does not supports bluetooth.");
            Log.e("Android Bluetooth Scanner", "Terminating...");
            System.exit(1);
        }
        Log.i("Android Bluetooth Scanner", "Success!");

        if (!mBluetoothAdapter.isEnabled()) {
            Log.i("Android Bluetooth Scanner", "Bluetooth is not enabled.");
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Log.i("Android Bluetooth Scanner", "Bluetooth enable requested.");
            startActivityForResult(enableBluetoothIntent, this.INTENT_REQUEST_BLUETOOTH_ENABLE);
        } else {
            Log.i("Android Bluetooth Scanner", "Bluetooth already enabled.");
            this.scanBluetoothDevices();
        }
    }

    public void scanBluetoothDevices() {
        // Get paired devices.
        this.mBluetoothAdapter.cancelDiscovery();
        Log.i("Android Bluetooth Scanner", "Getting paired bluetooth devices...");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice d : pairedDevices) {
                this.mBluetoothDeviceArrayAdapter.add(
                        "[Paired]" + d.getName() + "\n"
                        + "MAC: " + d.getAddress());
            }
            this.mBluetoothDeviceArrayAdapter.notifyDataSetChanged();
        }
        Log.i("Android Bluetooth Scanner", "Done!");

        // Discovery devices.
        Log.i("Android Bluetooth Scanner", "Start discovering bluetooth devices...");
        this.mBluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i("Android Bluetooth Scanner", "Found bluetooth device.");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("Android Bluetooth Scanner", device.getName() + "\n" + device.getAddress());
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mBluetoothDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mBluetoothDeviceArrayAdapter.notifyDataSetChanged();
                }
            } else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i("Android Bluetooth Scanner", "Bluetooth device discovering started.");
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("Android Bluetooth Scanner", "Bluetooth device discovering finished.");
            }
        }
    };
}
