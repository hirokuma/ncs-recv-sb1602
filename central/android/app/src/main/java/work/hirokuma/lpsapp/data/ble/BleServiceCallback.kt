package work.hirokuma.lpsapp.data.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.UUID

abstract class BleServiceCallback(val uuids: List<UUID>) {
    open fun onServicesDiscovered(
        gatt: BluetoothGatt,
        service: BluetoothGattService
    ): Boolean {
        return true
    }

    open fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): Boolean {
        return true
    }

    open fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ): Boolean {
        return true
    }

    open fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): Boolean {
        return true
    }

//    open fun onDescriptorReadDeprecated(
//        gatt: BluetoothGatt,
//        descriptor: BluetoothGattDescriptor,
//        status: Int
//    ): Boolean {
//        return true
//    }
//
//    open fun onDescriptorRead(
//        gatt: BluetoothGatt,
//        descriptor: BluetoothGattDescriptor,
//        status: Int,
//        value: ByteArray
//    ): Boolean {
//        return true
//    }
//
//    open fun onDescriptorWrite(
//        gatt: BluetoothGatt,
//        descriptor: BluetoothGattDescriptor,
//        status: Int
//    ): Boolean {
//        return true
//    }
}