package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.primitives.LGSpacing
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors

val safeguardExplanations = mapOf(
    "Notice to Cure / Default Period" to
        "Gives you a grace period to fix any lease violations or overdue rent before the landlord can impose penalties or initiate eviction proceedings.",
    "Right to Quiet Enjoyment" to
        "The landlord cannot enter your property without proper notice or unreasonably disrupt your peaceful and private use of the space.",
    "Mutual Termination Rights" to
        "Allows both you and the landlord to end the agreement under fair conditions, rather than granting unilateral termination power to the landlord.",
    "Security Deposit Return Timeline" to
        "Requires the landlord to return your deposit within a specified statutory timeframe after move-out with an itemized statement of any deductions.",
    "Landlord Maintenance Obligations" to
        "Obligates the landlord to keep the property habitable — maintaining plumbing, electrical, heating, structural integrity, and essential services at their expense.",
    "Habitability Warranty" to
        "Guarantees that the premises comply with local health, safety, and building codes throughout the tenancy.",
    "Limitation of Liability" to
        "Protects the tenant from unreasonable one-sided indemnification clauses and unlimited financial liability for regular wear and tear.",
    "Payment Terms / Late Payment Fee" to
        "Specifies clear invoice payment deadlines and late interest rates, protecting against arbitrary non-payment or delayed disbursements.",
    "IP Ownership Assigned Upon Payment" to
        "Ensures intellectual property rights are retained until invoices are settled in full, preventing unauthorized exploitation without compensation.",
    "Cap on Aggregate Liability" to
        "Restricts maximum financial exposure (typically capped at total fees paid), preventing business-ending liability claims.",
    "Statutory Termination Notice Period" to
        "Guarantees required minimum statutory notice periods prior to dismissal.",
    "Severance / Dispute Resolution Mechanism" to
        "Provides clear rules for resolving compensation disputes or statutory severance claims."
)

@Composable
fun SafeguardExpandableItem(clause: String) {
    val colors = LocalLGColors.current
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
    ) {
        // Collapsed header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Missing safeguard",
                tint = colors.AccentWarning,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = clause,
                style = LGType.Subtitle.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = if (expanded) "Collapse explanation" else "Show explanation",
                tint = if (expanded) colors.TextPrimary else colors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Expanded explanation
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + expandVertically(
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(tween(150)) + shrinkVertically(
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 4.dp, top = 2.dp, bottom = 8.dp),
                color = colors.ExpandedSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = safeguardExplanations[clause]
                        ?: "$clause: Standard protection clause that must be included to safeguard legal rights.",
                    style = LGType.BodySmall.copy(
                        lineHeight = 19.sp,
                        color = colors.TextPrimary
                    ),
                    modifier = Modifier.padding(LGSpacing.md)
                )
            }
        }
    }
}
