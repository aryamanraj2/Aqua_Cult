package com.parth.aquasense.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.parth.aquasense.presentation.screens.cart.CartScreen
import com.parth.aquasense.presentation.screens.cart.CartViewModel
import com.parth.aquasense.presentation.screens.dashboard.DashboardScreen
import com.parth.aquasense.presentation.screens.disease.DiseaseDetectionScreen
import com.parth.aquasense.presentation.screens.disease.DiseaseResultsScreen
import com.parth.aquasense.presentation.screens.marketplace.MarketplaceScreen

import com.parth.aquasense.presentation.screens.productdetail.ProductDetailScreen
import com.parth.aquasense.presentation.screens.tankdetail.TankDetailScreen
import com.parth.aquasense.presentation.screens.tankform.TankFormScreen
import com.parth.aquasense.presentation.screens.tanklist.TankListScreen
import com.parth.aquasense.presentation.screens.waterqualityform.WaterQualityFormScreen
import com.parth.aquasense.presentation.screens.voiceagent.VoiceAgentScreen

/**
 * Navigation Graph - Defines all navigation routes and their destinations
 *
 * This is the "map" of your app. It connects routes to screens.
 *
 * @param navController Controls navigation (like a GPS)
 * @param modifier Styling modifiers
 * @param startDestination The first screen to show
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Dashboard.route
) {
    /**
     * NavHost is the container that switches between screens
     *
     * How it works:
     * 1. Shows screen based on current route
     * 2. Handles animations between screens
     * 3. Manages back stack (back button)
     * 4. Passes arguments to screens
     */
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        /**
         * Dashboard Screen
         * Route: "dashboard"
         */
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTanks = {
                    navController.navigate(Screen.TankList.route)
                },
                onNavigateToAddTank = {
                    navController.navigate(Screen.TankForm.createRoute())
                },
                onNavigateToTankDetail = { tankId ->
                    navController.navigate(Screen.TankDetail.createRoute(tankId))
                },
                onNavigateToVoiceAgent = {
                    navController.navigate(Screen.VoiceAgent.route)
                }
            )
        }

        /**
         * Tank List Screen
         * Route: "tanks"
         */
        composable(route = Screen.TankList.route) {
            TankListScreen(
                onNavigateToTankDetail = { tankId ->
                    navController.navigate(Screen.TankDetail.createRoute(tankId))
                },
                onNavigateToTankForm = {
                    navController.navigate(Screen.TankForm.createRoute())
                }
            )
        }

        /**
         * Tank Detail Screen
         * Route: "tank/{tankId}"
         *
         * arguments: Defines what data this screen expects
         * - tankId: Required String argument from the route
         */
        composable(
            route = Screen.TankDetail.route,
            arguments = listOf(
                navArgument(Screen.TankDetail.ARG_TANK_ID) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            TankDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { tankId ->
                    navController.navigate(Screen.TankForm.createRoute(tankId))
                },
                onNavigateToAddReading = { tankId ->
                    navController.navigate(Screen.WaterQuality.createRoute(tankId))
                }
            )
        }

        /**
         * Tank Form Screen (Add/Edit)
         * Route: "tank/form?tankId={tankId}"
         *
         * tankId is optional (null = new tank, value = edit existing)
         */
        composable(
            route = Screen.TankForm.route,
            arguments = listOf(
                navArgument(Screen.TankForm.ARG_TANK_ID) {
                    type = NavType.StringType
                    nullable = true // Optional argument
                    defaultValue = null
                }
            )
        ) {
            TankFormScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        /**
         * Water Quality Screen
         * Route: "tank/{tankId}/water-quality"
         */
        composable(
            route = Screen.WaterQuality.route,
            arguments = listOf(
                navArgument(Screen.WaterQuality.ARG_TANK_ID) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            WaterQualityFormScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        /**
         * Disease Detection Screen
         * Route: "disease-detection"
         *
         * Standalone screen for detecting fish diseases from photos
         */
        composable(route = Screen.DiseaseDetection.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.DiseaseDetection.route)
            }
            DiseaseDetectionScreen(
                onNavigateToResults = {
                    navController.navigate(Screen.DiseaseResults.route)
                },
                viewModel = hiltViewModel(parentEntry)
            )
        }

        /**
         * Disease Results Screen
         * Route: "disease/results"
         *
         * Shows disease detection results after analysis
         */
        composable(route = Screen.DiseaseResults.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.DiseaseDetection.route)
            }
            DiseaseResultsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = hiltViewModel(parentEntry)
            )
        }

        /**
         * Marketplace Screen
         * Route: "marketplace"
         *
         * Browse products by category and navigate to product details
         */
        composable(route = Screen.Marketplace.route) { backStackEntry ->
            // Share CartViewModel across marketplace flow
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Marketplace.route)
            }
            val cartViewModel: CartViewModel = hiltViewModel(parentEntry)

            MarketplaceScreen(
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                }
            )
        }

        /**
         * Product Detail Screen
         * Route: "product/{productId}"
         *
         * View detailed product information and add to cart
         */
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument(Screen.ProductDetail.ARG_PRODUCT_ID) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            // Get shared CartViewModel
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Marketplace.route)
            }
            val cartViewModel: CartViewModel = hiltViewModel(parentEntry)

            ProductDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddToCart = { product, quantity ->
                    cartViewModel.addToCart(product, quantity)
                    navController.navigate(Screen.Cart.route)
                }
            )
        }

        /**
         * Cart Screen
         * Route: "cart"
         *
         * View and manage shopping cart items
         */
        composable(route = Screen.Cart.route) { backStackEntry ->
            // Get shared CartViewModel
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Marketplace.route)
            }
            val cartViewModel: CartViewModel = hiltViewModel(parentEntry)

            CartScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderPlaced = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Marketplace.route) { inclusive = true }
                    }
                },
                viewModel = cartViewModel
            )
        }


        /**
         * Order History Screen
         * Route: "orders"
         *
         * View all past orders with enriched product names
         */

        /**
         * Voice Agent Screen
         * Route: "voice_agent?tankId={tankId}"
         *
         * AI voice assistant for hands-free interaction
         * Optional tankId for tank-specific context
         */
        composable(
            route = Screen.VoiceAgent.route,
            arguments = listOf(
                navArgument(Screen.VoiceAgent.ARG_TANK_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            VoiceAgentScreen()
        }
    }
}

/**
 * Placeholder screen for testing navigation
 * This will be replaced with actual screens later
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
