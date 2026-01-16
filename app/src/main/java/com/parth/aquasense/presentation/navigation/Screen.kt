package com.parth.aquasense.presentation.navigation

/**
 * Navigation Routes - Defines all screens in the app
 *
 * Why sealed class?
 * - Only these screens can exist (no random strings as routes)
 * - Compile-time safety (typos in routes caught immediately)
 * - Easy to see all screens in one place
 * - Can add helper functions for each screen
 *
 * Each screen has:
 * - route: The base path (e.g., "dashboard")
 * - Optional arguments: Data passed between screens (e.g., tankId)
 */
sealed class Screen(val route: String) {

    /**
     * Dashboard screen - Shows overview of all tanks
     * Route: "dashboard"
     */
    data object Dashboard : Screen("dashboard")

    /**
     * Tank List screen - Shows list of all tanks
     * Route: "tanks"
     */
    data object TankList : Screen("tanks")

    /**
     * Tank Detail screen - Shows details of a specific tank
     * Route: "tank/{tankId}"
     *
     * tankId is a path parameter that gets passed when navigating
     * Example: "tank/123e4567-e89b-12d3-a456-426614174000"
     */
    data object TankDetail : Screen("tank/{tankId}") {
        /**
         * Create route with actual tankId value
         * Usage: navigate(TankDetail.createRoute(tankId))
         */
        fun createRoute(tankId: String): String {
            return "tank/$tankId"
        }

        /**
         * Argument key for extracting tankId from NavBackStackEntry
         */
        const val ARG_TANK_ID = "tankId"
    }

    /**
     * Add/Edit Tank screen - Create new tank or edit existing
     * Route: "tank/form?tankId={tankId}"
     *
     * tankId is optional (null for new tank, UUID for editing)
     */
    data object TankForm : Screen("tank/form?tankId={tankId}") {
        /**
         * Create route for adding new tank
         * Usage: navigate(TankForm.createRoute())
         */
        fun createRoute(tankId: String? = null): String {
            return if (tankId != null) {
                "tank/form?tankId=$tankId"
            } else {
                "tank/form"
            }
        }

        const val ARG_TANK_ID = "tankId"
    }

    /**
     * Water Quality screen - View/add water quality readings
     * Route: "tank/{tankId}/water-quality"
     */
    data object WaterQuality : Screen("tank/{tankId}/water-quality") {
        fun createRoute(tankId: String): String {
            return "tank/$tankId/water-quality"
        }

        const val ARG_TANK_ID = "tankId"
    }

    /**
     * Disease Detection screen - Detect fish diseases from photos
     * Route: "disease-detection"
     *
     * New standalone screen accessible from bottom navigation
     */
    data object DiseaseDetection : Screen("disease-detection")

    /**
     * Disease Results screen - Show disease detection results
     * Route: "disease/results"
     *
     * Navigated to after successful disease analysis
     */
    data object DiseaseResults : Screen("disease/results")

    /**
     * Marketplace screen - Browse and purchase products
     * Route: "marketplace"
     */
    data object Marketplace : Screen("marketplace")

    /**
     * Product Detail screen - View detailed product information
     * Route: "product/{productId}"
     */
    data object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: String): String = "product/$productId"
        const val ARG_PRODUCT_ID = "productId"
    }

    /**
     * Cart screen - View and manage shopping cart
     * Route: "cart"
     */
    data object Cart : Screen("cart")


    /**
     * Order History screen - View past orders
     * Route: "orders"
     */
    data object OrderHistory : Screen("orders")

    /**
     * Voice Agent screen - AI voice assistant
     * Route: "voice_agent?tankId={tankId}"
     *
     * tankId is optional (null for general questions, UUID for tank-specific context)
     */
    data object VoiceAgent : Screen("voice_agent?tankId={tankId}") {
        /**
         * Create route with optional tankId for context
         * Usage: navigate(VoiceAgent.createRoute(tankId))
         */
        fun createRoute(tankId: String? = null): String {
            return if (tankId != null) {
                "voice_agent?tankId=$tankId"
            } else {
                "voice_agent"
            }
        }

        const val ARG_TANK_ID = "tankId"
    }
}

/**
 * Bottom Navigation Screens - Screens shown in bottom nav bar
 *
 * These are the main sections of the app that users can always access
 */
val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.TankList,
    Screen.DiseaseDetection,
    Screen.Marketplace
)

