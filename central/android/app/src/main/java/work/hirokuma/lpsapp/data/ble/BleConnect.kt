package work.hirokuma.lpsapp.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "BleConnect"

class BleConnect(
    @ApplicationContext private val context: Context
) {
    private var bleGatt: BluetoothGatt? = null

    // TODO Add BLE service classes
    private val lbsService = LbsService()
    private val lpsService = LpsService()
    val services = mapOf(
        lbsService.serviceUuid to lbsService,
        lpsService.serviceUuid to lpsService,
    )

    @OptIn(ExperimentalStdlibApi::class)
    @SuppressLint("MissingPermission")
    fun connectDevice(device: BleDevice): Flow<Boolean> = callbackFlow {
        val callback = object: BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.d(TAG, "onConnectionStateChange: status=$status, newState=$newState")
                if (gatt == null) {
                    Log.e(TAG, "onConnectionStateChange: gatt is null")
                    return
                }
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "onConnectionStateChange: not success(status=${Utils.convertErrorStatus(status)})")
                    disconnectDevice(gatt)
                    return
                }
                val conn = when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(TAG, "onConnectionStateChange: connected!")
                        gatt.discoverServices()
                        true
                    }
                    BluetoothGatt.STATE_DISCONNECTED -> {
                        Log.d(TAG, "onConnectionStateChange: disconnected!")
                        disconnectDevice(gatt)
                        false
                    }
                    else -> {
                        Log.e(TAG, "onConnectionStateChange: unknown state($newState)")
                        disconnectDevice(gatt)
                        false
                    }
                }
                try {
                    trySend(conn)
                } catch (e: Exception) {
                    Log.e(TAG, "fail trySend: $e")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                Log.d(TAG, "onServicesDiscovered: status=$status")
                if (gatt == null) {
                    Log.e(TAG, "onServicesDiscovered: gatt is null")
                    return
                }
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "onServicesDiscovered: failed(status=${Utils.convertErrorStatus(status)})")
                    gatt.disconnect()
                    return
                }
                for (service in gatt.services) {
                    if (!services.containsKey(service.uuid)) {
                        continue
                    }
                    val callback = services[service.uuid]?.callback
                    for (uuid in callback?.uuids!!) {
                        val chars = service.getCharacteristic(uuid)
                        if (chars == null) {
                            Log.e(TAG, "onServicesDiscovered: characteristic not found: $uuid")
                            gatt.disconnect()
                            return
                        }
                    }
                    val result = callback.onServicesDiscovered(gatt, service)
                    if (!result) {
                        gatt.disconnect()
                        return
                    }
                }

                Log.d(TAG, "onServicesDiscovered: done")
            }

            @Deprecated("Deprecated in Java")
            @Suppress("DEPRECATION")
            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, status)
                if (gatt == null) {
                    Log.e(TAG, "onCharacteristicRead: gatt is null")
                    return
                }
                characteristic?.let {
                    Log.d(TAG, "onCharacteristicRead: deprecated: status=$status, uuid=${it.uuid}, value=${it.value.toHexString()}")
                    val service = characteristic.service
                    if (services.containsKey(service.uuid)) {
                        services[service.uuid]?.let { svc ->
                            val result = svc.callback.onCharacteristicRead(
                                gatt, characteristic, it.value
                            )
                            if (!result) {
                                disconnectDevice()
                            }
                        }
                    }
                }
            }

//            override fun onCharacteristicRead(
//                gatt: BluetoothGatt,
//                characteristic: BluetoothGattCharacteristic,
//                value: ByteArray,
//                status: Int
//            ) {
//                super.onCharacteristicRead(gatt, characteristic, value, status)
//                Log.d(TAG, "onCharacteristicRead: API33: status=$status, uuid=${characteristic.uuid}, value=${value.toHexString()}")
//            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                if (gatt == null) {
                    Log.e(TAG, "onCharacteristicWrite: gatt is null")
                    return
                }
                characteristic?.let {
                    Log.d(TAG, "onCharacteristicWrite: status=$status, uuid=${it.uuid}")
                    val service = characteristic.service
                    if (services.containsKey(service.uuid)) {
                        services[service.uuid]?.let { svc ->
                            val result = svc.callback.onCharacteristicWrite(
                                gatt, characteristic
                            )
                            if (!result) {
                                disconnectDevice()
                            }
                        }
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            @Suppress("DEPRECATION")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                if (gatt == null) {
                    Log.e(TAG, "onCharacteristicChanged: gatt is null")
                    return
                }
                characteristic?.let {
                    Log.d(TAG, "onCharacteristicChanged: uuid=${it.uuid}, value=${it.value.toHexString()}")
                    val service = characteristic.service
                    if (services.containsKey(service.uuid)) {
                        services[service.uuid]?.let { svc ->
                            val result = svc.callback.onCharacteristicChanged(
                                gatt, characteristic, it.value
                            )
                            if (!result) {
                                disconnectDevice()
                            }
                        }
                    }
                }
            }

//            override fun onCharacteristicChanged(
//                gatt: BluetoothGatt,
//                characteristic: BluetoothGattCharacteristic,
//                value: ByteArray
//            ) {
//                super.onCharacteristicChanged(gatt, characteristic, value)
//            }

            @Deprecated("Deprecated in Java")
            @Suppress("DEPRECATION")
            override fun onDescriptorRead(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                super.onDescriptorRead(gatt, descriptor, status)
                Log.d(TAG, "onDescriptorRead: deprecated: status=$status")
            }

//            override fun onDescriptorRead(
//                gatt: BluetoothGatt,
//                descriptor: BluetoothGattDescriptor,
//                status: Int,
//                value: ByteArray
//            ) {
//                super.onDescriptorRead(gatt, descriptor, status, value)
//                Log.d(TAG, "onDescriptorRead: API33: status=$status, value=${value.toHexString()}")
//            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                super.onDescriptorWrite(gatt, descriptor, status)
                Log.d(TAG, "onDescriptorWrite: status=$status")
            }

            override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
                super.onReliableWriteCompleted(gatt, status)
                Log.d(TAG, "onReliableWriteCompleted: status=$status")
            }

            override fun onServiceChanged(gatt: BluetoothGatt) {
                super.onServiceChanged(gatt)
                Log.d(TAG, "onServiceChanged")
            }
        }
        bleGatt = device.device?.connectGatt(context, false, callback)

        awaitClose {
            Log.d(TAG, "awaitClose")
            disconnectDevice(bleGatt)
        }
    }


    @SuppressLint("MissingPermission")
    fun disconnectDevice(gatt: BluetoothGatt? = null) {
        if (bleGatt == null) {
            if (gatt != null) {
                gatt.disconnect()
                return
            }
            Log.w(TAG, "already disconnected")
            return
        }
        bleGatt!!.disconnect()
        bleGatt = null
        Log.d(TAG, "BLE disconnect")
    }
}