package com.ssecure.applock.ui.settings

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.ssecure.applock.ui.lock.PatternLockView
import com.ssecure.applock.ui.lock.PinLockView

@Composable
fun LockTypeSetupScreen(
    prefs: AppLockPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(prefs.lockType) }
    var isSettingNewCreds by remember { mutableStateOf(false) }

    // PIN Setup states
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isPinConfirmStep by remember { mutableStateOf(false) }

    // Pattern Setup states
    var firstPattern by remember { mutableStateOf("") }
    var isPatternConfirmStep by remember { mutableStateOf(false) }
    var isPatternError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isSettingNewCreds) {
                    isSettingNewCreds = false
                    firstPin = ""
                    confirmPin = ""
                    firstPattern = ""
                    isPinConfirmStep = false
                    isPatternConfirmStep = false
                } else {
                    onBack()
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = if (isSettingNewCreds) "Set new ${selectedType.title}" else "App lock type",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (!isSettingNewCreds) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "SELECT SECURITY TYPE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B))
                ) {
                    Column {
                        LockTypeOptionRow(
                            icon = Icons.Default.Pin,
                            title = "PIN",
                            description = "Numeric code",
                            isSelected = selectedType == LockType.PIN,
                            onClick = {
                                selectedType = LockType.PIN
                                prefs.lockType = LockType.PIN
                                SamsungSystemBridge.syncWithSamsungSystem(context, prefs.isAppLockEnabled, LockType.PIN.id)
                                isSettingNewCreds = true
                            }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(0.5.dp)
                                .background(Color(0xFF27272A))
                        )

                        LockTypeOptionRow(
                            icon = Icons.Default.Gesture,
                            title = "Pattern",
                            description = "Connect-the-dots grid pattern",
                            isSelected = selectedType == LockType.PATTERN,
                            onClick = {
                                selectedType = LockType.PATTERN
                                prefs.lockType = LockType.PATTERN
                                SamsungSystemBridge.syncWithSamsungSystem(context, prefs.isAppLockEnabled, LockType.PATTERN.id)
                                isSettingNewCreds = true
                            }
                        )
                    }
                }
            }
        } else {
            // New credentials setup wizard
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header prompt brought down
                Column(
                    modifier = Modifier.padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedType == LockType.PIN) {
                        Text(
                            text = if (!isPinConfirmStep) "Enter your new 4-digit PIN" else "Confirm your new 4-digit PIN",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    } else if (selectedType == LockType.PATTERN) {
                        Text(
                            text = if (!isPatternConfirmStep) "Draw a pattern (at least 4 dots)" else "Draw pattern again to confirm",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Spacer pushes input to the bottom
                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (selectedType == LockType.PIN) {
                        val currentPin = if (!isPinConfirmStep) firstPin else confirmPin

                        PinLockView(
                            pinLength = currentPin.length,
                            isError = false,
                            onDigitClick = { digit ->
                                if (!isPinConfirmStep) {
                                    if (firstPin.length < 4) {
                                        firstPin += digit
                                        if (firstPin.length == 4) {
                                            isPinConfirmStep = true
                                        }
                                    }
                                } else {
                                    if (confirmPin.length < 4) {
                                        confirmPin += digit
                                        if (confirmPin.length == 4) {
                                            if (confirmPin == firstPin) {
                                                prefs.setPin(confirmPin)
                                                Toast.makeText(context, "New PIN saved!", Toast.LENGTH_SHORT).show()
                                                onBack()
                                            } else {
                                                Toast.makeText(context, "PINs did not match, try again", Toast.LENGTH_SHORT).show()
                                                firstPin = ""
                                                confirmPin = ""
                                                isPinConfirmStep = false
                                            }
                                        }
                                    }
                                }
                            },
                            onDeleteClick = {
                                if (!isPinConfirmStep && firstPin.isNotEmpty()) {
                                    firstPin = firstPin.dropLast(1)
                                } else if (isPinConfirmStep && confirmPin.isNotEmpty()) {
                                    confirmPin = confirmPin.dropLast(1)
                                }
                            },
                            onCancelClick = {
                                isSettingNewCreds = false
                                firstPin = ""
                                confirmPin = ""
                                isPinConfirmStep = false
                            }
                        )
                    } else if (selectedType == LockType.PATTERN) {
                        PatternLockView(
                            isError = isPatternError,
                            onPatternCompleted = { pattern ->
                                if (!isPatternConfirmStep) {
                                    if (pattern.length < 4) {
                                        Toast.makeText(context, "Connect at least 4 dots", Toast.LENGTH_SHORT).show()
                                    } else {
                                        firstPattern = pattern
                                        isPatternConfirmStep = true
                                    }
                                } else {
                                    if (pattern == firstPattern) {
                                        prefs.setPattern(pattern)
                                        Toast.makeText(context, "New Pattern saved!", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    } else {
                                        isPatternError = true
                                        Toast.makeText(context, "Patterns did not match, start over", Toast.LENGTH_SHORT).show()
                                        firstPattern = ""
                                        isPatternConfirmStep = false
                                    }
                                }
                            },
                            onCancelClick = {
                                isSettingNewCreds = false
                                firstPattern = ""
                                isPatternConfirmStep = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockTypeOptionRow(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFF27272A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF71717A),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF71717A)
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
