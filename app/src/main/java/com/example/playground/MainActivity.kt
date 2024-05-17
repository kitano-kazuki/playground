package com.example.playground

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.playground.ui.theme.PlayGroundTheme

class MainActivity : ComponentActivity() {

    public var isWifiP2pEnabled = false
    lateinit var channel: WifiP2pManager.Channel
    lateinit var manager: WifiP2pManager
    lateinit var receiver: Receiver
    val peers = mutableListOf<WifiP2pDevice>()

    private val intentFilter = IntentFilter()


    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
    val refreshedPeers = peerList.deviceList
    if (refreshedPeers != peers) {
        peers.clear()
        peers.addAll(refreshedPeers)

        // Perform any other updates needed based on the new list of
        // peers connected to the Wi-Fi P2P network.
    }

    if (peers.isEmpty()) {
        Log.d("MainActiity", "No devices found")
        return@PeerListListener
    }
}

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
            } else {
                Log.i("Permission: ", "Denied")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayGroundTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(this)
                }
            }
        }

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        checkPermission(Manifest.permission.NEARBY_WIFI_DEVICES)


        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
        connectChannel(manager, channel)

        discoverPeersTest(this,manager, channel)
    }

    /** register the BroadcastReceiver with the intent values to be matched  */
    public override fun onResume() {
        super.onResume()
        receiver = Receiver(manager, channel, this, peerListListener)
        registerReceiver(receiver, intentFilter)
    }

    public override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }


    private fun checkPermission(permission: String) {
        val context = this
        val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager;
        try {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) -> {
                }

                else -> {
                    requestPermissionLauncher.launch(permission)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun connectChannel(manager: WifiP2pManager, channel: WifiP2pManager.Channel) {

        val listener =
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    }

                override fun onFailure(reason: Int) {
                }
            }
        manager.createGroup(channel,listener)
}



fun discoverPeersTest(context: Context,manager: WifiP2pManager, channel : WifiP2pManager.Channel) {

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.NEARBY_WIFI_DEVICES
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.d("MainActivity", "Permission not granted")
        return
    }
    manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            Log.d("MainActivity", "Discover peers success")
        }

        override fun onFailure(reasonCode: Int) {
            Log.d("MainActivity", "Discover peers failed")
        }
    }
    )
}


@SuppressLint("MissingPermission")
fun connectTest(context: Context, manager: WifiP2pManager, channel: WifiP2pManager.Channel, device: WifiP2pDevice, peers: List<WifiP2pDevice>) {
    // Picking the first device found on the network.
    val device = peers[0]

    val config = WifiP2pConfig().apply {
        deviceAddress = device.deviceAddress
        wps.setup = WpsInfo.PBC
    }


    manager.connect(channel, config, object : WifiP2pManager.ActionListener {

        override fun onSuccess() {
            // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
        }

        override fun onFailure(reason: Int) {
        }
    })
}








@Composable
fun Greeting(activity : MainActivity) {
    Button(onClick = { discoverPeersTest(activity, activity.manager, activity.channel) }) {
        Text("discoverPeersTest")
    }
    Button(onClick = { activity.connectTest}) {
        Text("")
    }
}