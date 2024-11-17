/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package work.hirokuma.lpsapp.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.content.Context
import work.hirokuma.lpsapp.ble.BleServiceBase
import work.hirokuma.lpsapp.ble.BleServiceCallback
import work.hirokuma.lpsapp.ble.BleUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

private const val TAG = "BleViewModel"

class BleViewModel(
    @ApplicationContext private val context: Context,
    private val services: Map<UUID, BleServiceBase>
) : ViewModel() {
    private val bluetoothLeScanner: BluetoothLeScanner
    init {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var bleGatt: BluetoothGatt? = null

    private fun addDevice(device: Device) {
        if (_uiState.value.deviceList.find { it.address == device.address } != null) {
            return
        }
        _uiState.update { state ->
            val newList = state.deviceList.toMutableList()
            newList.add(device)
            state.copy(
                deviceList = newList,
            )
        }
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "onScanResult: ${result.device}")
            val record = result.scanRecord ?: return
            Log.d(TAG, "ScanRecord: $result.scanRecord}")
            if (record.deviceName == null) {
                return
            }
            addDevice(
                Device(
                    address = result.device.address,
                    name = record.deviceName!!,
                    ssid = result.rssi,
                    device = result.device,
                    scanRecord = record
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startDeviceScan() {
        if (!_uiState.value.scanning) {
            _uiState.update {
                it.copy(
                    deviceList = emptyList(),
                    scanning = true
                )
            }
            Log.d(TAG, "onClickScan: start searching")
            bluetoothLeScanner.startScan(scanCallback)
        } else {
            _uiState.update {
                it.copy(scanning = false)
            }
            Log.d(TAG, "onClickScan: stop searching")
            stopDeviceScan()
        }
    }

    fun stopDeviceScan(): Boolean {
        if (!_uiState.value.scanning) {
            Log.d(TAG, "not scanning")
            return false
        }
        try {
            bluetoothLeScanner.stopScan(scanCallback)
            _uiState.update {
                it.copy(scanning = false)
            }
        }
        catch (e: SecurityException) {
            Log.e(TAG, "stopScan: $e")
            return false
        }
        return true
    }

    @OptIn(ExperimentalStdlibApi::class)
    @SuppressLint("MissingPermission")
    fun connectDevice(device: Device) {
        val callback = object: BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.d(TAG, "onConnectionStateChange: status=$status, newState=$newState")
                if (gatt == null) {
                    Log.e(TAG, "onConnectionStateChange: gatt is null")
                    return
                }
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "onConnectionStateChange: not success(status=${BleUtils.convertErrorStatus(status)})")
                    disconnectDevice(gatt)
                    return
                }
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(TAG, "onConnectionStateChange: connected!")
                        gatt.discoverServices()
                    }
                    BluetoothGatt.STATE_DISCONNECTED -> {
                        Log.d(TAG, "onConnectionStateChange: disconnected!")
                        disconnectDevice(gatt)
                    }
                    else -> {
                        Log.e(TAG, "onConnectionStateChange: unknown state($newState)")
                        disconnectDevice(gatt)
                    }
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
                    Log.e(TAG, "onServicesDiscovered: failed(status=${BleUtils.convertErrorStatus(status)})")
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

        _uiState.update { state ->
            state.copy(
                selectedDevice = device,
            )
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
        _uiState.update { state ->
            state.copy(
                selectedDevice = null,
            )
        }
        bleGatt!!.disconnect()
        bleGatt = null
    }
}

data class UiState(
    val deviceList: List<Device> = emptyList(),
    val scanning: Boolean = false,
    val selectedDevice: Device? = null,
)

data class Device(
    val name: String = "",
    val address: String = "",
    val ssid: Int = 0,
    val device: BluetoothDevice? = null,
    val scanRecord: ScanRecord? = null,
)
