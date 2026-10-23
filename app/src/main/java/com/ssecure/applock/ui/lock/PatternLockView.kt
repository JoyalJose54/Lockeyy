package com.ssecure.applock.ui.lock

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

@Composable
fun PatternLockView(
    isError: Boolean,
    onPatternCompleted: (String) -> Unit,
    onCancelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedNodes = remember { mutableStateListOf<Int>() }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }

    // Clean OneUI pattern dimensions without bubbles
    val idleDotRadius = 18f        // Thick, clear dots
    val selectedDotRadius = 20f    // Slightly bolder active dot
    val hitRadius = 80f            // Generous touch hitbox
    val lineStrokeWidth = 18f      // Clean, solid drawn lines
    val liveLineStrokeWidth = 16f  // Tracking line to touch point

    // Light grey line styling as requested (with red on error)
    val strokeColor = if (isError) Color(0xFFEF4444) else Color(0xFFE2E8F0)
    val selectedDotColor = if (isError) Color(0xFFEF4444) else Color.White
    val idleDotColor = Color(0xFF94A3B8) // Visible slate-silver dots

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(310.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedNodes.clear()
                            currentTouchPoint = offset
                            val node = findNodeAt(offset, size.width.toFloat(), size.height.toFloat(), hitRadius)
                            if (node != -1) {
                                selectedNodes.add(node)
                                triggerHapticTick(context)
                            }
                        },
                        onDrag = { change, _ ->
                            currentTouchPoint = change.position
                            val node = findNodeAt(change.position, size.width.toFloat(), size.height.toFloat(), hitRadius)
                            if (node != -1 && !selectedNodes.contains(node)) {
                                selectedNodes.add(node)
                                triggerHapticTick(context)
                            }
                        },
                        onDragEnd = {
                            currentTouchPoint = null
                            if (selectedNodes.isNotEmpty()) {
                                val patternString = selectedNodes.joinToString("")
                                onPatternCompleted(patternString)
                            }
                        },
                        onDragCancel = {
                            currentTouchPoint = null
                            selectedNodes.clear()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height

                // Calculate 3x3 node positions evenly
                val nodeOffsets = (0..8).map { index ->
                    val col = index % 3
                    val row = index / 3
                    val x = (col + 0.5f) * (width / 3f)
                    val y = (row + 0.5f) * (height / 3f)
                    Offset(x, y)
                }

                // Draw clean light grey connecting lines between selected nodes
                for (i in 0 until selectedNodes.size - 1) {
                    val start = nodeOffsets[selectedNodes[i]]
                    val end = nodeOffsets[selectedNodes[i + 1]]
                    drawLine(
                        color = strokeColor,
                        start = start,
                        end = end,
                        strokeWidth = lineStrokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Draw clean light grey line to current touch point
                if (selectedNodes.isNotEmpty() && currentTouchPoint != null) {
                    val lastNode = nodeOffsets[selectedNodes.last()]
                    drawLine(
                        color = strokeColor.copy(alpha = 0.75f),
                        start = lastNode,
                        end = currentTouchPoint!!,
                        strokeWidth = liveLineStrokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Draw the 9 pattern dots - clean dots ONLY, no bubbles around them
                nodeOffsets.forEachIndexed { index, center ->
                    val isSelected = selectedNodes.contains(index)
                    if (isSelected) {
                        drawCircle(
                            color = selectedDotColor,
                            radius = selectedDotRadius,
                            center = center
                        )
                    } else {
                        drawCircle(
                            color = idleDotColor,
                            radius = idleDotRadius,
                            center = center
                        )
                    }
                }
            }
        }

        if (onCancelClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onCancelClick)
                    .padding(horizontal = 28.dp, vertical = 10.dp)
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

private fun triggerHapticTick(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            v?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    } catch (ignored: Exception) {}
}

private fun findNodeAt(touch: Offset, width: Float, height: Float, hitRadius: Float): Int {
    for (i in 0..8) {
        val col = i % 3
        val row = i / 3
        val cx = (col + 0.5f) * (width / 3f)
        val cy = (row + 0.5f) * (height / 3f)
        val dx = touch.x - cx
        val dy = touch.y - cy
        val dist = sqrt((dx * dx + dy * dy).toDouble())
        if (dist <= hitRadius) {
            return i
        }
    }
    return -1
}
