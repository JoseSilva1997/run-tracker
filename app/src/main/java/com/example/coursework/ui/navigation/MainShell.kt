package com.example.coursework.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val NAV_BAR_HEIGHT_DP = 64
private const val DIAMOND_SIZE_DP = 62
private const val DIAMOND_CORNER_DP = 12
private const val DIAMOND_LIFT_DP = 16
private const val PILL_HEIGHT_DP = 36
private const val PILL_BORDER_DP = 1
private const val PILL_GAP_ABOVE_DIAMOND_DP = 16

enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    Dashboard("dashboard", "Dashboard", Icons.Default.Home),
    History("history", "History", Icons.Default.History)
}

@Composable
fun MainShell(
    showChrome: Boolean,
    currentRoute: String?,
    selectedRunTypeName: String,
    canStart: Boolean,
    onTabSelected: (MainTab) -> Unit,
    onSelectRunType: () -> Unit,
    onStartRun: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val sysBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Reserve enough space for content so bottom bar + diamond don't cover anything.
    val contentBottomReserve = NAV_BAR_HEIGHT_DP.dp + (DIAMOND_SIZE_DP / 2).dp + DIAMOND_LIFT_DP.dp + PILL_HEIGHT_DP.dp + PILL_GAP_ABOVE_DIAMOND_DP.dp + sysBottom

    Box(modifier = Modifier.fillMaxSize()) {
        content(if (showChrome) PaddingValues(bottom = contentBottomReserve) else PaddingValues(0.dp))

        if (!showChrome) return@Box

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            BottomNavBar(
                currentRoute = currentRoute,
                onTabSelected = onTabSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = sysBottom)
            )

            // Pill above diamond — chevron-suffixed dropdown chip with green outline.
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = sysBottom + NAV_BAR_HEIGHT_DP.dp / 2 + DIAMOND_SIZE_DP.dp / 2 + DIAMOND_LIFT_DP.dp + PILL_GAP_ABOVE_DIAMOND_DP.dp)
                    .height(PILL_HEIGHT_DP.dp)
                    .wrapContentSize()
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = PILL_BORDER_DP.dp,
                        color = com.example.coursework.ui.theme.BtnPrimary,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(enabled = true, onClick = onSelectRunType),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(50),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(start = 14.dp, end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = com.example.coursework.ui.theme.BtnPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = selectedRunTypeName.ifBlank { "Choose run type" },
                        color = com.example.coursework.ui.theme.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Open run type picker",
                        modifier = Modifier.size(20.dp),
                        tint = com.example.coursework.ui.theme.BtnPrimary
                    )
                }
            }

            DiamondStartButton(
                enabled = canStart,
                onClick = onStartRun,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = sysBottom + NAV_BAR_HEIGHT_DP.dp / 2 - DIAMOND_SIZE_DP.dp / 2 + DIAMOND_LIFT_DP.dp)
            )
        }
    }
}

@Composable
private fun BottomNavBar(
    currentRoute: String?,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(NAV_BAR_HEIGHT_DP.dp)
            .shadow(elevation = 8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab(
                tab = MainTab.Dashboard,
                selected = currentRoute == MainTab.Dashboard.route,
                onClick = { onTabSelected(MainTab.Dashboard) },
                modifier = Modifier.weight(1f)
            )
            // Center reserved area for the diamond button.
            Spacer(Modifier.width(DIAMOND_SIZE_DP.dp))
            NavTab(
                tab = MainTab.History,
                selected = currentRoute == MainTab.History.route,
                onClick = { onTabSelected(MainTab.History) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavTab(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = tab.label,
                color = tint,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DiamondStartButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (enabled) {
        com.example.coursework.ui.theme.BtnPrimary
    } else {
        com.example.coursework.ui.theme.BtnPrimary.copy(alpha = 0.4f)
    }
    Box(
        modifier = modifier
            .size(DIAMOND_SIZE_DP.dp)
            .rotate(45f)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(DIAMOND_CORNER_DP.dp),
                clip = false
            )
            .clip(RoundedCornerShape(DIAMOND_CORNER_DP.dp))
            .background(container)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Counter-rotate icon so figure stays upright.
        Icon(
            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
            contentDescription = "Start run",
            tint = com.example.coursework.ui.theme.TextPrimary,
            modifier = Modifier
                .rotate(-45f)
                .size(28.dp)
        )
    }
}
