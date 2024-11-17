package work.hirokuma.lpsapp.ui.screens

import work.hirokuma.lpsapp.ble.BleServiceBase
import work.hirokuma.lpsapp.ble.LpsService
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import java.util.UUID

@Composable
fun LpsView(
    @Suppress("UNUSED_PARAMETER") uiState: UiState,
    services: Map<UUID, BleServiceBase>
) {
    val service = services[LpsService.SERVICE_UUID]!! as LpsService
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier) {
        Text(
            text = "LPS Service",
            fontSize = 32.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    colorScheme.tertiaryContainer
                )
        )
        Text(
            text = "Print",
            fontSize = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.primaryContainer)
        )
        Row {
            TextField(
                value = text,
                onValueChange = {
                    if (it.length < 12) {
                        text = it
                    }
                }
            )
            Button(
                onClick = { service.sendText(text) }
            ) {
                Text("Send")
            }
        }
        Text(
            text = "Clear",
            fontSize = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.primaryContainer)
        )
        Button(
            onClick = { service.clearText() }
        ) {
            Text("Clear")
        }
    }
}
