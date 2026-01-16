package com.parth.aquasense.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.res.vectorResource
import com.parth.aquasense.R
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.parth.aquasense.presentation.navigation.Screen
import com.parth.aquasense.ui.theme.AquaBlue
import com.parth.aquasense.ui.theme.NavBackgroundSubtle
import com.parth.aquasense.util.HapticFeedback

import com.parth.aquasense.util.getHapticFeedback


@Composable
fun FloatingPillNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = getHapticFeedback()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    // List of screens to display in the bar
    val screens = listOf(
        Screen.Dashboard,
        Screen.TankList,
        Screen.DiseaseDetection,
        Screen.Marketplace
    )

    // Helper to get icons for screens
    @Composable
    fun getIconsForScreen(screen: Screen): Pair<ImageVector, ImageVector> {
        return when (screen) {
            Screen.Dashboard -> Pair(ImageVector.vectorResource(id = R.drawable.fish), ImageVector.vectorResource(id = R.drawable.fish))
            Screen.TankList -> Pair(Icons.AutoMirrored.Outlined.List, Icons.AutoMirrored.Filled.List)
            Screen.DiseaseDetection -> Pair(Icons.Outlined.Healing, Icons.Default.Healing)
            Screen.Marketplace -> Pair(Icons.Outlined.ShoppingBag, Icons.Default.ShoppingBag)
            else -> Pair(Icons.Outlined.Home, Icons.Default.Home)
        }
    }
    
    fun getTitleForScreen(screen: Screen): String {
        return when (screen) {
            Screen.Dashboard -> "Home"
            Screen.TankList -> "Tanks"
            Screen.DiseaseDetection -> "Disease"
            Screen.Marketplace -> "Shop"
            else -> ""
        }
    }

    val selectedIndex = screens.indexOfFirst { it.route == currentDestination }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val itemWidth = this.maxWidth / screens.size

        // Main Container
        Box(
            modifier = Modifier
                .height(68.dp)
                .width(this.maxWidth)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.1f),
                    ambientColor = Color.Black.copy(alpha = 0.05f)
                )
                .clip(CircleShape)
                .background(color = NavBackgroundSubtle)
                .zIndex(15f)
        ) {

            // Animated Indicator (The "Blob")
            val indicatorOffset by animateDpAsState(
                targetValue = if (selectedIndex >= 0) itemWidth * selectedIndex else (-itemWidth),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "IndicatorOffset"
            )

            if (selectedIndex >= 0) {
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(itemWidth)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .background(
                            color = AquaBlue,
                            shape = CircleShape
                        )
                        .zIndex(1f)
                )
            }


            // Icons Layer
            Row(
                modifier = Modifier.fillMaxSize().zIndex(20f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                screens.forEach { screen ->
                    val isSelected = currentDestination == screen.route
                    val (outlinedIcon, filledIcon) = getIconsForScreen(screen)
                    
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "IconScale"
                    )
                    
                    Box(
                        modifier = Modifier
                            .width(itemWidth)
                            .fillMaxHeight()
                            .clickable(
                                onClick = {
                                    if (!isSelected) {
                                        hapticFeedback.performHapticFeedback(HapticFeedback.FeedbackType.LIGHT)
                                        navController.navigate(screen.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // reselecting the same item
                                            launchSingleTop = true
                                            // Restore state when reselecting a previously selected item
                                            restoreState = true
                                        }
                                    }
                                },
                                indication = null, // Disable default ripple
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Icon Color Logic
                        // Selected: White (on Blue blob)
                        // Unselected: Black (on White background)
                        val iconColor = if (isSelected) Color.White else Color.Black
                        
                        Icon(
                            imageVector = if (isSelected) filledIcon else outlinedIcon,
                            contentDescription = getTitleForScreen(screen),
                            tint = iconColor,
                            modifier = Modifier
                                .size(26.dp)
                                .scale(scale)
                        )
                    }

                }
            }
        }
    }
}
