package com.parth.aquasense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.parth.aquasense.presentation.components.FloatingPillNavigation
import com.parth.aquasense.presentation.navigation.NavGraph
import com.parth.aquasense.presentation.navigation.Screen
import com.parth.aquasense.ui.theme.AquaSenseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - The single activity for the entire app
 *
 * Single Activity Architecture:
 * - One Activity hosts all Compose screens
 * - Navigation happens via NavController (not Activity transitions)
 * - Simpler lifecycle management
 * - Better performance (no Activity overhead)
 *
 * @AndroidEntryPoint: Enables Hilt dependency injection in this Activity
 * This allows ViewModels and other dependencies to be injected automatically
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AquaSenseTheme {
                MainScreen()
            }
        }
    }
}

/**
 * Main Screen Composable - Sets up navigation and app structure
 *
 * Structure:
 * - Scaffold: Provides Material 3 layout structure
 * - BottomNavBar: Navigation bar at the bottom
 * - NavGraph: Handles screen navigation
 */
@Composable
fun MainScreen() {
    /**
     * rememberNavController() creates and remembers a NavController
     * - Survives configuration changes (rotation)
     * - Controls navigation throughout the app
     * - Manages back stack
     */
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Hide bottom bar and disable padding for Disease Detection (Camera) screen
    val isCameraScreen = currentRoute == Screen.DiseaseDetection.route

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            /**
             * NavGraph - The main navigation host
             * - Switches between screens based on current route
             * - Applies only top padding to handle status bar/top app bar spacing from Scaffold
             * - Content can draw behind the bottom navigation pill
             */
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
            )

            // Show floating navigation for all screens (can add conditional logic here if needed)
            // It overlays the content at the bottom
            FloatingPillNavigation(
                navController = navController,
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
            )
        }
    }
}
