package work.hirokuma.lpsapp.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "BleScan"

class BleScan(
    @ApplicationContext private val context: Context)
{
    private val bluetoothLeScanner: BluetoothLeScanner
    init {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    private var scanCallback: ScanCallback? = null
    @SuppressLint("MissingPermission")
    fun startScan(): Flow<BleDevice> = callbackFlow {
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val record = result.scanRecord ?: return
                Log.d(TAG, "ScanRecord: ${result.scanRecord}")
                if (record.deviceName == null) {
                    return
                }
                val dev = BleDevice(
                    address = result.device.address,
                    name = record.deviceName!!,
                    ssid = result.rssi,
                    device = result.device,
                    scanRecord = record
                )
                try {
                    trySend(dev)
                } catch (e: Exception) {
                    Log.e(TAG, "fail trySend: $e")
                }
            }
        }
        bluetoothLeScanner.startScan(scanCallback)

        awaitClose {
            Log.w(TAG, "Job cancelled")
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (scanCallback == null) {
            return
        }
        bluetoothLeScanner.stopScan(scanCallback)
        scanCallback = null
    }
}