package work.hirokuma.lpsapp.ui.screens

import work.hirokuma.lpsapp.R
import work.hirokuma.lpsapp.ble.BleServiceBase
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedScreen(
    viewModel: BleViewModel,
    services: Map<UUID, BleServiceBase>,
    goToBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    titleContentColor = colorScheme.primary,
                ),
                title = {
                    Text(uiState.selectedDevice?.name ?: "")
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                modifier = Modifier.clickable(onClick = {
                    // Automatically transfer to previous screen upon BLE disconnection
                    viewModel.disconnectDevice()
                }),
            ) {
                Text(
                    text = stringResource(R.string.back_button),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize()
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = colorScheme.background,
            contentColor = colorScheme.onBackground,
        ) {
            Column {
                // TODO Add service view
                LbsView(uiState, services)
                LpsView(uiState, services)
            }
        }
    }
    // Transfer to ScanScreen for BLE disconnection
    if (uiState.selectedDevice == null) {
        goToBack()
    }
    BackHandler {
        viewModel.disconnectDevice()
    }
}
