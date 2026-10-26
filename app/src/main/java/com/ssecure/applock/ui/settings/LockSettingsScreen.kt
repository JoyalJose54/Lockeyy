package com.ssecure.applock.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssecure.applock.data.AppLockPreferences
import com.ssecure.applock.model.LockType
import com.ssecure.applock.receiver.SamsungSystemBridge
import com.ssecure.applock.service.AppLockForegroundService

@Composable
fun LockSettingsScreen(
    prefs: AppLockPreferences,
    onBack: () -> Unit,
    onNavigateToLockType: () -> Unit,
    onNavigateToLockedApps: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(prefs.isAppLockEnabled) }
    var isBiometricsEnabled by remember { mutableStateOf(prefs.isBiometricsEnabled) }

    val lockedCount = remember(isEnabled) { prefs.getLockedPackages().size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // OneUI Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Lock apps",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // OneUI Master SwitchBar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        val next = !isEnabled
                        isEnabled = next
                        prefs.isAppLockEnabled = next
                        if (next) {
                            AppLockForegroundService.startService(context)
                            SamsungSystemBridge.syncWithSamsungSystem(context, true, prefs.lockType.id)
                        } else {
                            AppLockForegroundService.stopService(context)
                            SamsungSystemBridge.syncWithSamsungSystem(context, false, prefs.lockType.id)
                        }
                    },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEnabled) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFF18181B)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEnabled) "On" else "Off",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color(0xFF38BDF8) else Color(0xFF71717A)
                    )

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            isEnabled = checked
                            prefs.isAppLockEnabled = checked
                            if (checked) {
                                AppLockForegroundService.startService(context)
                                SamsungSystemBridge.syncWithSamsungSystem(context, true, prefs.lockType.id)
                            } else {
                                AppLockForegroundService.stopService(context)
                                SamsungSystemBridge.syncWithSamsungSystem(context, false, prefs.lockType.id)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2563EB),
                            uncheckedThumbColor = Color(0xFF71717A),
                            uncheckedTrackColor = Color(0xFF27272A)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Group: Security & Lock Type
            Text(
                text = "SECURITY",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF71717A),
                modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B))
            ) {
                Column {
                    OneUiSettingRow(
                        icon = Icons.Default.Pin,
                        iconBg = Color(0xFF1E293B),
                        iconTint = Color(0xFF38BDF8),
                        title = "App lock type",
                        subtitle = when (prefs.lockType) {
                            LockType.PIN -> "PIN"
                            LockType.PATTERN -> "Pattern"
                            LockType.PASSWORD -> "Password"
                        },
                        enabled = isEnabled,
                        onClick = onNavigateToLockType
                    )

                    OneUiDivider()

                    // Biometrics Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = if (isEnabled && isBiometricsEnabled) Color(0xFF38BDF8) else Color(0xFF71717A),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fingerprints",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isEnabled) Color.White else Color(0xFF71717A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Unlock apps using biometric sensor",
                                fontSize = 13.sp,
                                color = Color(0xFF71717A)
                            )
                        }

                        Switch(
                            checked = isBiometricsEnabled,
                            enabled = isEnabled,
                            onCheckedChange = { checked ->
                                isBiometricsEnabled = checked
                                prefs.isBiometricsEnabled = checked
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2563EB),
                                uncheckedThumbColor = Color(0xFF71717A),
                                uncheckedTrackColor = Color(0xFF27272A)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Settings Group: Apps Management
            Text(
                text = "APPS",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF71717A),
                modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B))
            ) {
                OneUiSettingRow(
                    icon = Icons.Default.Lock,
                    iconBg = Color(0xFF1E293B),
                    iconTint = Color(0xFF38BDF8),
                    title = "Locked apps",
                    subtitle = "$lockedCount apps",
                    enabled = isEnabled,
                    onClick = onNavigateToLockedApps
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // OneUI Informational Footnote
            Text(
                text = "Locked apps require verification before opening to keep your privacy secure.",
                fontSize = 13.sp,
                color = Color(0xFF71717A),
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
private fun OneUiSettingRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (enabled) iconBg else Color(0xFF27272A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) iconTint else Color(0xFF52525B),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = if (enabled) Color.White else Color(0xFF71717A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = if (enabled) Color(0xFF38BDF8) else Color(0xFF52525B)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = if (enabled) Color(0xFF71717A) else Color(0xFF3F3F46),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun OneUiDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 68.dp, end = 16.dp)
            .height(0.6.dp)
            .background(Color(0xFF27272A))
    )
}
