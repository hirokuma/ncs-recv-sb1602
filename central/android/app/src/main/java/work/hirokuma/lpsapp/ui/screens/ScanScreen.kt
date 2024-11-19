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

import android.content.res.Configuration
import work.hirokuma.lpsapp.R
import work.hirokuma.lpsapp.data.ble.BleDevice
import work.hirokuma.lpsapp.ui.theme.AppTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: BleViewModel,
    onDeviceConnected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    titleContentColor = colorScheme.primary,
                ),
                title = {
                    Text(stringResource(R.string.app_name))
                }
            )
        },
        bottomBar = {
            val containerColor: Color
            val contentColor: Color
            val buttonId: Int
            if (uiState.scanning) {
                containerColor = colorScheme.tertiary
                contentColor = colorScheme.onTertiary
                buttonId = R.string.scanning_button
            } else {
                containerColor = colorScheme.primary
                contentColor = colorScheme.onPrimary
                buttonId = R.string.scan_button
            }
            BottomAppBar(
                containerColor = containerColor,
                contentColor = contentColor,
                modifier = Modifier.clickable(onClick = {
                    if (!uiState.scanning) {
                        viewModel.startDeviceScan()
                    } else {
                        viewModel.stopDeviceScan()
                    }
                }),
            ) {
                Text(
                    text = stringResource(buttonId),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize()
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = modifier.fillMaxSize(),
            color = colorScheme.background,
            contentColor = colorScheme.onBackground,
        ) {
            DeviceList(
                deviceList = uiState.deviceList,
                modifier = Modifier.padding(innerPadding),
                scanning = uiState.scanning,
                onItemClicked = {dev ->
                    viewModel.connectDevice(dev)
                },
            )
        }
    }
    // Transfer to ConnectedScreen for BLE connection
    if (uiState.selectedDevice != null) {
        onDeviceConnected()
    }
}


@Composable
fun DeviceList(
    deviceList: List<BleDevice>,
    modifier: Modifier = Modifier,
    scanning: Boolean,
    onItemClicked: (BleDevice) -> Unit,
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(deviceList) { item ->
            OutlinedCard(
                onClick = { onItemClicked(item) },
                enabled = !scanning,
                border = BorderStroke(0.dp, color = Color.Transparent),
                shape = RectangleShape,
                modifier = Modifier.height(64.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        text = "${item.address} - ${item.name}",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(alignment = Alignment.CenterStart),
                        fontSize = 24.sp,
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "light mode"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "dark mode"
)
@Composable
fun DeviceListPreview() {
    val list = listOf(
        BleDevice("dummy1", "00:11:22:33:44:55", -10),
        BleDevice("dummy2", "66:77:88:99:aa:bb", -20)
    )
    AppTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background,
            contentColor = colorScheme.onBackground,
        ) {
            DeviceList(
                deviceList = list,
                scanning = false,
                onItemClicked = {}
            )
        }
    }
}
