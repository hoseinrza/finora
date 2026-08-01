package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.permissions.PermissionPreferences
import com.example.permissions.PermissionRationaleDialog
import com.example.permissions.PermissionSettingsRedirectDialog
import com.example.permissions.PermissionUiState
import com.example.permissions.explanationFor
import com.example.services.NotificationHelper
import com.example.ui.FinoraApp
import com.example.ui.FinoraViewModel

/**
 * MainActivity - نقطه ورود اصلی برنامه مدیریت مالی Finora
 */
class MainActivity : ComponentActivity() {

    private val viewModel: FinoraViewModel by viewModels()

    /** Permissions requested at launch. Order matters only for which rationale shows first. */
    private val requiredPermissions: List<String>
        get() = buildList {
            add(Manifest.permission.RECEIVE_SMS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

    private var permissionUiState by mutableStateOf<PermissionUiState>(PermissionUiState.None)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results -> onPermissionResults(results) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        setContent {
            FinoraApp(viewModel = viewModel)

            when (val state = permissionUiState) {
                is PermissionUiState.Rationale -> PermissionRationaleDialog(
                    explanation = state.explanation,
                    onConfirm = {
                        permissionUiState = PermissionUiState.None
                        launchPermissionRequest(listOf(state.permission))
                    },
                    onDismiss = { permissionUiState = PermissionUiState.None }
                )

                is PermissionUiState.PermanentlyDenied -> PermissionSettingsRedirectDialog(
                    explanation = state.explanation,
                    onOpenSettings = {
                        permissionUiState = PermissionUiState.None
                        openAppSettings()
                    },
                    onDismiss = { permissionUiState = PermissionUiState.None }
                )

                PermissionUiState.None -> Unit
            }
        }

        checkAndRequestPermissions()
    }

    override fun onResume() {
        super.onResume()
        // Re-check after returning from Settings (or from backgrounding the app to grant a
        // permission some other way) so the dialogs above reflect the current real state.
        if (permissionUiState is PermissionUiState.PermanentlyDenied) {
            val stillDenied = requiredPermissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (!stillDenied) permissionUiState = PermissionUiState.None
        }
    }

    private fun checkAndRequestPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) return

        val needsRationale = missing.firstOrNull { shouldShowRequestPermissionRationale(it) }
        if (needsRationale != null) {
            permissionUiState = PermissionUiState.Rationale(needsRationale, explanationFor(needsRationale))
            return
        }

        val permanentlyDenied = missing.firstOrNull {
            PermissionPreferences.hasRequestedBefore(this, it)
        }
        if (permanentlyDenied != null) {
            permissionUiState = PermissionUiState.PermanentlyDenied(permanentlyDenied, explanationFor(permanentlyDenied))
            return
        }

        // Never asked before for any of the missing permissions - just show the system dialog.
        launchPermissionRequest(missing)
    }

    private fun launchPermissionRequest(permissions: List<String>) {
        permissions.forEach { PermissionPreferences.markRequested(this, it) }
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun onPermissionResults(results: Map<String, Boolean>) {
        val stillDenied = results.filterValues { granted -> !granted }.keys
        if (stillDenied.isEmpty()) {
            permissionUiState = PermissionUiState.None
            return
        }

        val permanentlyDenied = stillDenied.firstOrNull { !shouldShowRequestPermissionRationale(it) }
        permissionUiState = if (permanentlyDenied != null) {
            PermissionUiState.PermanentlyDenied(permanentlyDenied, explanationFor(permanentlyDenied))
        } else {
            PermissionUiState.None
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
