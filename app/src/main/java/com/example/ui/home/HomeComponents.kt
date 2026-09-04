package com.example.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.primitives.LGSpacing
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors

@Composable
fun AnimatedPrimaryButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLGColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ButtonScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = colors.PrimaryAccent,
            contentColor = Color.White
        ),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp,
            hoveredElevation = 5.dp
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(LGSpacing.sm))
        }
        Text(
            text = text,
            style = LGType.Title.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        )
    }
}

@Composable
fun PrivacyBanner() {
    val colors = LocalLGColors.current

    Surface(
        color = colors.BannerSafeBackground,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = LGSpacing.lg, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Secure",
                tint = colors.BannerSafeContent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(LGSpacing.sm))
            Text(
                text = "100% Offline & Secure. Your data never leaves your device.",
                style = LGType.BodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = colors.BannerSafeContent
                )
            )
        }
    }
}

@Composable
fun StudioFooter() {
    Text(
        text = "Powered by Anixium Studios",
        style = LGType.Caption.copy(
            color = LocalLGColors.current.TextSecondary,
            fontWeight = FontWeight.Medium
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LGSpacing.lg),
        textAlign = TextAlign.Center
    )
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = LGType.Title.copy(fontWeight = FontWeight.Bold),
        color = color
    )
}

@Composable
fun CostRow(label: String, value: String) {
    val colors = LocalLGColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = LGType.Body.copy(fontWeight = FontWeight.Medium),
            color = colors.TextSecondary
        )
        Text(
            text = value,
            style = LGType.Body.copy(fontWeight = FontWeight.SemiBold),
            color = colors.TextPrimary
        )
    }
}

fun formatAmount(amount: Double): String {
    return String.format("%,.2f", amount)
}
