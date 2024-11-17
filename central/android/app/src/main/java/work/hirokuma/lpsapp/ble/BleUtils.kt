package work.hirokuma.lpsapp.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import java.util.UUID

private const val TAG = "BleUtils"

object BleUtils {
    fun isBluetoothEnabled(context: Context): Boolean {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter
        return bluetoothAdapter.isEnabled
    }

    fun getService(gatt: BluetoothGatt, uuid: UUID): BluetoothGattService {
        val service = gatt.getService(uuid) ?: throw Exception("service $uuid not found")
        return service
    }
    fun getCharacteristic(service: BluetoothGattService, uuid: UUID): BluetoothGattCharacteristic {
        val chars = service.getCharacteristic(uuid) ?: throw Exception("characteristic $uuid not found")
        return chars
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun writeCharacteristic(gatt: BluetoothGatt, chars: BluetoothGattCharacteristic, data: ByteArray, writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "writeCharacteristic: new")
            val ret = gatt.writeCharacteristic(
                chars,
                data,
                writeType
            )
            Log.d(TAG, "result=$ret")
        } else {
            Log.d(TAG, "writeCharacteristic: old")
            chars.setValue(data)
            gatt.writeCharacteristic(chars)
        }
    }

    enum class SubscribeType {
        Notification,
        Indication
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun writeDescriptor(
        gatt: BluetoothGatt,
        chars: BluetoothGattCharacteristic,
        uuid: UUID,
        type: SubscribeType
    ) {
        gatt.setCharacteristicNotification(chars, true)

        val descriptor = chars.getDescriptor(uuid)
        val value: ByteArray = if (type == SubscribeType.Notification) {
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        } else {
            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "writeDescriptor(API33)")
            gatt.writeDescriptor(descriptor, value)
        } else {
            Log.d(TAG, "writeDescriptor(deprecated)")
            descriptor.value = value
            gatt.writeDescriptor(descriptor)
        }
    }

    fun convertErrorStatus(code: Int): String {
        return when (code) {
            0x00 -> "success"
            0x01 -> "GATT_INVALID_HANDLE"
            0x02 -> "GATT_READ_NOT_PERMIT"
            0x03 -> "GATT_WRITE_NOT_PERMIT"
            0x04 -> "GATT_INVALID_PDU"
            0x05 -> "GATT_INSUF_AUTHENTICATION"
            0x06 -> "GATT_REQ_NOT_SUPPORTED"
            0x07 -> "GATT_INVALID_OFFSET"
            0x08 -> "GATT_INSUF_AUTHORIZATION"
            0x09 -> "GATT_PREPARE_Q_FULL"
            0x0a -> "GATT_NOT_FOUND"
            0x0b -> "GATT_NOT_LONG"
            0x0c -> "GATT_INSUF_KEY_SIZE"
            0x0d -> "GATT_INVALID_ATTR_LEN"
            0x0e -> "GATT_ERR_UNLIKELY"
            0x0f -> "GATT_INSUF_ENCRYPTION"
            0x10 -> "GATT_UNSUPPORT_GRP_TYPE"
            0x11 -> "GATT_INSUF_RESOURCE"
            0x87 -> "GATT_ILLEGAL_PARAMETER"
            0x80 -> "GATT_NO_RESOURCES"
            0x81 -> "GATT_INTERNAL_ERROR"
            0x82 -> "GATT_WRONG_STATE"
            0x83 -> "GATT_DB_FULL"
            0x84 -> "GATT_BUSY"
            0x85 -> "GATT_ERROR"
            0x86 -> "GATT_CMD_STARTED"
            0x88 -> "GATT_PENDING"
            0x89 -> "GATT_AUTH_FAIL"
            0x8a -> "GATT_MORE"
            0x8b -> "GATT_INVALID_CFG"
            0x8c -> "GATT_SERVICE_STARTED"
            else -> "unknown error code"
        }
    }
}