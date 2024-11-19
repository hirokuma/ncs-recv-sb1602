package work.hirokuma.lpsapp.data.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord

data class BleDevice(
    val name: String = "",
    val address: String = "",
    val ssid: Int = 0,
    val device: BluetoothDevice? = null,
    val scanRecord: ScanRecord? = null,
)