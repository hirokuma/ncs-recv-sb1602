package work.hirokuma.lpsapp.data.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

private const val TAG = "LbsControl"

class LbsService: BleServiceBase {
    override val serviceUuid = SERVICE_UUID

    private lateinit var bleGatt: BluetoothGatt

    private val _buttonState = MutableStateFlow(false)
    val buttonState: StateFlow<Boolean> = _buttonState.asStateFlow()

    @OptIn(ExperimentalStdlibApi::class)
    override val callback = object : BleServiceCallback(
        lbsCharacteristicUuids
    ) {
        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            service: BluetoothGattService
        ): Boolean {
            bleGatt = gatt

            // Notify有効
            val buttonChas = Utils.getCharacteristic(service, BUTTON_CHARACTERISTIC_UUID)
            Utils.writeDescriptor(
                gatt,
                buttonChas,
                CCCD_UUID,
                Utils.SubscribeType.Notification
            )

            Log.d(TAG, "onServicesDiscovered: done")

            return true
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ): Boolean {
            Log.d(TAG, "onCharacteristicRead: API33: uuid=${characteristic.uuid}, value=${value.toHexString()}")
            return true
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ): Boolean {
            Log.d(TAG, "onCharacteristicChanged: API33: value=${value.toHexString()}")
            if (characteristic.uuid == BUTTON_CHARACTERISTIC_UUID) {
                Log.d(TAG, "button")
                _buttonState.update { _ ->
                    value[0].toInt() != 0x00
                }
            }
            return true
        }
    }

    fun setLed(onoff: Boolean) {
        Log.d(TAG, "setLed: $onoff")
        val service = Utils.getService(bleGatt, SERVICE_UUID)
        val chars = Utils.getCharacteristic(service, LED_CHARACTERISTIC_UUID)
        val data = byteArrayOf(if (onoff) 1 else 0)
        Utils.writeCharacteristic(bleGatt, chars, data)
    }

    // https://docs.nordicsemi.com/bundle/ncs-latest/page/nrf/libraries/bluetooth_services/services/lbs.html
    // https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/506cabe8884364cd4302cc490664ec020c42728b/blinky/spec/src/main/java/no/nordicsemi/android/blinky/spec/BlinkySpec.kt#L10
    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("00001523-1212-efde-1523-785feabcd123")
        private val BUTTON_CHARACTERISTIC_UUID: UUID = UUID.fromString("00001524-1212-efde-1523-785feabcd123")
        private val LED_CHARACTERISTIC_UUID: UUID = UUID.fromString("00001525-1212-efde-1523-785feabcd123")
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

        private val lbsCharacteristicUuids = listOf(
            BUTTON_CHARACTERISTIC_UUID,
            LED_CHARACTERISTIC_UUID,
        )
    }
}
