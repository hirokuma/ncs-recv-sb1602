package work.hirokuma.lpsapp.ble

import java.util.UUID

interface BleServiceBase {
    val serviceUuid: UUID
    val callback: BleServiceCallback
}

/*
package work.hirokuma.lpsapp.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.util.Log
import java.util.UUID

private const val TAG = "XxxService"

class XxxService: BleServiceBase {
    override val serviceUuid = LbsService.SERVICE_UUID

    private lateinit var bleGatt: BluetoothGatt

    // @OptIn(ExperimentalStdlibApi::class)
    override val callback = object : BleServiceCallback(
        lbsCharacteristicUuids
    ) {
        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            service: BluetoothGattService
        ): Boolean {
            bleGatt = gatt
            Log.d(TAG, "onServicesDiscovered: done")
            return true
        }
    }

    companion object {
        val SERVICE_UUID = UUID.fromString("")
        private val XXX_CHARACTERISTIC_UUID = UUID.fromString("")

        private val lbsCharacteristicUuids = listOf(
            XXX_CHARACTERISTIC_UUID,
        )
    }
}
 */