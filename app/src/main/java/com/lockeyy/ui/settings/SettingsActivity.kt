package com.lockeyy.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
        setContent {
            com.lockeyy.ui.theme.LockeyyTheme {
                SettingsScreen(onBack = { finish() })
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    
    var showPinDialog by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Change Master PIN") },
            text = {
                OutlinedTextField(
                    value = newPinText,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinText = it },
                    label = { Text("Enter New 4-digit PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinText.length == 4) {
                            viewModel.updatePin(newPinText)
                            showPinDialog = false
                            newPinText = ""
                        }
                    }
                ) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsCategory("Security")
            
            SettingsItem(
                title = "Change PIN",
                subtitle = "Update your master access code",
                onClick = { showPinDialog = true }
            )
            
            val relockTimeout by viewModel.relockTimeout.collectAsState()
            var showTimeoutDialog by remember { mutableStateOf(false) }

            SettingsItem(
                title = "Relock Timeout",
                subtitle = when(relockTimeout) {
                    0L -> "Immediately"
                    -1L -> "After Screen Off"
                    60000L -> "1 Minute"
                    300000L -> "5 Minutes"
                    600000L -> "10 Minutes"
                    1200000L -> "20 Minutes"
                    else -> "Immediately"
                },
                onClick = { showTimeoutDialog = true }
            )

            if (showTimeoutDialog) {
                AlertDialog(
                    onDismissRequest = { showTimeoutDialog = false },
                    title = { Text("Relock Timeout") },
                    text = {
                        Column {
                            val options = listOf(
                                "Immediately" to 0L,
                                "1 Minute" to 60000L,
                                "5 Minutes" to 300000L,
                                "10 Minutes" to 600000L,
                                "20 Minutes" to 1200000L,
                                "After Screen Off" to -1L
                            )
                            options.forEach { (label, value) ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setRelockTimeout(value)
                                            showTimeoutDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = (relockTimeout == value), onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(label)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showTimeoutDialog = false }) { Text("Cancel") }
                    }
                )
            }
            
            SettingsSwitchItem(
                title = "Biometric Unlock",
                subtitle = "Use fingerprint/face to unlock apps",
                checked = isBiometricEnabled,
                onCheckedChange = { viewModel.toggleBiometric(it) }
            )
        }
    }
}


@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    title: String, 
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
