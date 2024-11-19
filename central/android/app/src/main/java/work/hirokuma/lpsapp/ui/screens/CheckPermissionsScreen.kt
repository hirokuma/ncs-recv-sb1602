package work.hirokuma.lpsapp.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import work.hirokuma.lpsapp.R
import work.hirokuma.lpsapp.data.ble.Utils.isBluetoothEnabled
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private const val TAG = "CheckPermissionsScreen"

// https://github.com/google/accompanist/blob/52a660d1988f838dc8b8ff84ee5f267b060b6e04/sample/src/main/java/com/google/accompanist/sample/permissions/RequestMultiplePermissionsSample.kt
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckPermissionsScreen(
    onCheckPassed: () -> Unit
) {
    val context = LocalContext.current

    CheckBluetooth()
    var firstChecked by remember { mutableStateOf(false) }
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = permissions,
    )
    if (multiplePermissionsState.allPermissionsGranted) {
        // If all permissions are granted, then show screen with the feature enabled
        Log.d(TAG, "all permissions are granted")
        onCheckPassed()
    } else {
        if (!firstChecked) {
            firstChecked = true
            LaunchedEffect(null) {
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.wrapContentSize(Alignment.Center)
            ) {
                Button(
                    onClick = {
                        Log.d(TAG, "push Request button")
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        (context as Activity).startActivity(intent)
                    }
                ) {
                    Text(stringResource(R.string.permission_setting_button))
                }
            }
        }
        Log.d(TAG, "check permissions")
    }
}

@Composable
private fun CheckBluetooth() {
    val context = LocalContext.current
    if (!isBluetoothEnabled(context)) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(stringResource(R.string.bluetooth_alert_title))
            },
            text = {
                Text(stringResource(R.string.bluetooth_alert_text))
            },
            confirmButton = {
                Button(
                    onClick = {
                        (context as Activity).finish()
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }
}
