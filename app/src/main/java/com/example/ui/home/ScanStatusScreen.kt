package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.primitives.LGButton
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors

// ── Scanning / Analyzing progress ─────────────────────────────────────────────
@Composable
fun ScanStatusScreen(label: String) {
    val colors = LocalLGColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "ScanPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "PulseAlpha"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Progress dot trio
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) { i ->
                val dotAlpha by rememberInfiniteTransition(label = "dot$i").animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        tween(600, delayMillis = i * 200, easing = LinearEasing),
                        RepeatMode.Reverse
                    ),
                    label = "DotAlpha$i"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(colors.Accent.copy(alpha = dotAlpha), RoundedCornerShape(4.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = label,
            style = LGType.Heading,
            color = colors.TextPrimary.copy(alpha = alpha),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Running on-device only. Nothing leaves your phone.",
            style = LGType.Caption,
            color = colors.TextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

// ── Error screen ───────────────────────────────────────────────────────────────
@Composable
fun ScanErrorScreen(message: String, onReset: () -> Unit) {
    val colors = LocalLGColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.RiskHigh.copy(alpha = 0.08f), RoundedCornerShape(12.dp))  // BannerDangerBackground → derived
                    .border(BorderStroke(1.dp, colors.RiskHigh.copy(alpha = 0.20f)), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Error",
                    tint = colors.RiskHigh,  // AccentDanger → RiskHigh
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = message,
                    style = LGType.Body.copy(fontWeight = FontWeight.Normal),
                    color = colors.TextPrimary,  // BannerDangerContent → TextPrimary
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                LGButton(text = "Try Again", onClick = onReset, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
