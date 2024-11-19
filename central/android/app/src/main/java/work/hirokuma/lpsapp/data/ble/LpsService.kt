package work.hirokuma.lpsapp.data.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.util.Log
import java.util.UUID

private const val TAG = "LpsService"

class LpsService: BleServiceBase {
    override val serviceUuid = SERVICE_UUID

    private lateinit var bleGatt: BluetoothGatt

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

    fun sendText(text: String) {
        Log.d(TAG, "setText: $text")
        val service = Utils.getService(bleGatt, SERVICE_UUID)
        val chars = Utils.getCharacteristic(service, PRINT_CHARACTERISTIC_UUID)
        val data = text.toByteArray()
        Utils.writeCharacteristic(bleGatt, chars, data)
    }

    fun clearText() {
        Log.d(TAG, "clearText")
        val service = Utils.getService(bleGatt, SERVICE_UUID)
        val chars = Utils.getCharacteristic(service, CLEAR_CHARACTERISTIC_UUID)
        val data = byteArrayOf(1)
        Utils.writeCharacteristic(bleGatt, chars, data)
    }

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("a00c1710-74ff-4bd5-9e86-cf601d80c054")
        private val PRINT_CHARACTERISTIC_UUID = UUID.fromString("a00c1711-74ff-4bd5-9e86-cf601d80c054")
        private val CLEAR_CHARACTERISTIC_UUID = UUID.fromString("a00c1712-74ff-4bd5-9e86-cf601d80c054")

        private val lbsCharacteristicUuids = listOf(
            PRINT_CHARACTERISTIC_UUID,
            CLEAR_CHARACTERISTIC_UUID,
        )
    }
}