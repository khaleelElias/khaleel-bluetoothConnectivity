package com.kellsworks.bluetoothconnectivity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/*** Created 20/04/2022 00:50 CAT */
/*** Updated 20/04/2022 13:28 CAT */

/*** You'll find few comments in the code explaining some features, Happy Coding! */

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchForDevices = findViewById<Button>(R.id.searchForBluetoothDevices)

        searchForDevices.setOnClickListener {
            searchForBluetoothDevices()
        }

    }

    /*** Checking permissions */

    private fun onClickRequestPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                /** Show an explanation to the user *asynchronously* -- don't block
                    this thread waiting for the user's response! After the user
                    sees the explanation, try again to request the permission. */
            } else {
                /** No explanation needed, we can request the permission */
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1)

                /*** REQUEST_CODE is an
                app-defined int constant. The callback method gets the
                result of the request. - You can also declare it as a private variable outside the MainActivity class scope or a constant object */
            }
        } else {
            /*** Permission has already been granted */
            Toast.makeText(this, "Permission has already been granted", Toast.LENGTH_SHORT)
                .show()
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                /** Show an explanation to the user *asynchronously* -- don't block
                this thread waiting for the user's response! After the user
                sees the explanation, try again to request the permission. */
            } else {
                /** No explanation needed, we can request the permission */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        1)
                }

                /*** REQUEST_CODE is an
                app-defined int constant. The callback method gets the
                result of the request. - You can also declare it as a private variable outside the MainActivity class scope or a constant object */
            }
        } else {
            /*** Permission has already been granted */
            Toast.makeText(this, "Permission has already been granted", Toast.LENGTH_SHORT)
                .show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    /*** Checking permission */
    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.e("Permission", result.toString())

            /** You can also log result to see list of granted permissions */

        }else{
            Log.e("Permission", "Bluetooth Denied")
        }
    }

    /*** Requesting multiple instances of permissions */
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    /*** Searches Bluetooth devices within close range on button click - Android Bluetooth API */
    @SuppressLint("MissingPermission")
    private fun searchForBluetoothDevices(){
        val bAdapter = BluetoothAdapter.getDefaultAdapter()

        /*** Getting views using classic id method*/
        val name = findViewById<TextView>(R.id.name)
        val mac = findViewById<TextView>(R.id.mac)

        /*** Checking availability of Bluetooth hardware */
        if (bAdapter == null) {
            Toast.makeText(applicationContext, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show()
        } else {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                            Toast.makeText(this@MainActivity, "Device discovery started", Toast.LENGTH_SHORT).show()
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            Toast.makeText(this@MainActivity, "Device discovery finished", Toast.LENGTH_SHORT).show()
                        }
                        BluetoothDevice.ACTION_FOUND -> {

                            val device: BluetoothDevice =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!

                            /** Checking Logcat for devices found */



                            /*** Appending textview in activity_main.xml with returned data */

                            name.append(device.name + "\n") /*** Note the escape sequences */
                            mac.append(device.address + "\n") /*** Note the escape sequences */
                        }

                        /*** When nothing is happening at all  - try */

                        else -> {
                            Toast.makeText(this@MainActivity, "No Bluetooth Device Found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
            /*** Register for broadcasts when a device is discovered and actions on lifecycles */
            val intentFilter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            }
            if (bAdapter.isDiscovering) {
                bAdapter.cancelDiscovery()
            }
            registerReceiver(receiver, intentFilter)
            bAdapter.startDiscovery()
        }
    }

    /*** Inflating a menu in the toolbar */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.appbar, menu) /*** Menu layout in menu/appbar.xml */

        return true
    }

    /*** Click listener for toolbar always action bluetooth icon - Awesome  */

    /**** Click on the Bluetooth icon on the toolbar to activate runtime-permissions **/

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.bluetoothIcon -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    /** Checking permissions and granting */
                    onClickRequestPermission()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}