package com.ssecure.applock.ui.lock

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class KeypadItem(val digit: String, val letters: String)

@Composable
fun PinLockView(
    pinLength: Int,
    maxDigits: Int = 4,
    isError: Boolean,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keypadRows = listOf(
        listOf(KeypadItem("1", ""), KeypadItem("2", "A B C"), KeypadItem("3", "D E F")),
        listOf(KeypadItem("4", "G H I"), KeypadItem("5", "J K L"), KeypadItem("6", "M N O")),
        listOf(KeypadItem("7", "P Q R S"), KeypadItem("8", "T U V"), KeypadItem("9", "W X Y Z")),
        listOf(KeypadItem("bio", ""), KeypadItem("0", "+"), KeypadItem("del", ""))
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN dot indicators (Samsung OneUI style)
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(vertical = 20.dp)
        ) {
            repeat(maxDigits) { index ->
                val isFilled = index < pinLength
                val dotColor by animateColorAsState(
                    targetValue = when {
                        isError -> Color(0xFFEF4444)
                        isFilled -> Color(0xFF2563EB)
                        else -> Color(0xFF475569)
                    },
                    label = "dotColor"
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keypad grid
        keypadRows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { item ->
                    when (item.digit) {
                        "bio" -> {
                            if (onBiometricClick != null) {
                                KeypadButton(
                                    onClick = onBiometricClick,
                                    content = {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Biometrics",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                )
                            } else {
                                Spacer(modifier = Modifier.size(76.dp))
                            }
                        }
                        "del" -> {
                            KeypadButton(
                                onClick = onDeleteClick,
                                content = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFE2E8F0),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            )
                        }
                        else -> {
                            KeypadButton(
                                onClick = { onDigitClick(item.digit) },
                                content = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = item.digit,
                                            color = Color.White,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                        if (item.letters.isNotEmpty()) {
                                            Text(
                                                text = item.letters,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp,
                                                letterSpacing = 1.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (onCancelClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onCancelClick)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF94A3B8),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color(0xFF3B82F6), bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
