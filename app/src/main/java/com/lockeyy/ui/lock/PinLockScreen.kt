package com.lockeyy.ui.lock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PinLockScreen(
    onPinEntered: (String) -> Unit,
    onBiometricClick: () -> Unit,
    title: String = "Enter PIN",
    appName: String = "App Locked",
    appIcon: ImageBitmap? = null,
    isError: Boolean = false,
    onPinChange: () -> Unit = {} // Callback to reset error when user types
) {
    var pin by remember { mutableStateOf("") }

    // Shake Animation State
    val shakeOffset = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(isError) {
        if (isError) {
            for (i in 0..4) {
                shakeOffset.animateTo(30f, androidx.compose.animation.core.tween(50))
                shakeOffset.animateTo(-30f, androidx.compose.animation.core.tween(50))
            }
            shakeOffset.animateTo(0f, androidx.compose.animation.core.tween(50))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // OneUI Dark Mode background
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.8f))

        // App Icon & Name
        if (appIcon != null) {
            Image(
                bitmap = appIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = appName,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle/Instruction
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // PIN Dots
        Row(
            modifier = Modifier
                .height(20.dp)
                .offset(x = shakeOffset.value.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val filled = index < pin.length
                val color = if (isError) MaterialTheme.colorScheme.error else if (filled) Color.White else Color.Gray.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error Message Placeholder
        Text(
            text = if (isError) "Incorrect PIN" else "",
            color = MaterialTheme.colorScheme.error,
            fontSize = 14.sp,
            modifier = Modifier.height(20.dp) // Reserve space to prevent jump
        )

        Spacer(modifier = Modifier.weight(1f))

        // Number Pad
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Biometric", "0", "Delete")
            )

            for (row in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (key in row) {
                        PinKey(
                            key = key,
                            onClick = {
                                onPinChange() // Reset error on any interaction
                                when (key) {
                                    "Delete" -> {
                                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                    }
                                    "Biometric" -> {
                                        onBiometricClick()
                                    }
                                    else -> {
                                        if (pin.length < 4) {
                                            pin += key
                                            if (pin.length == 4) {
                                                onPinEntered(pin)
                                                pin = "" // Clear immediately
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PinKey(key: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            "Delete" -> Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.White)
            "Biometric" -> Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            else -> Text(key, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Normal)
        }
    }
}
