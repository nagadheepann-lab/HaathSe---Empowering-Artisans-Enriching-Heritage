package com.example.ui.navigation

/**
 * Type-safe destination keys and routing definitions for HaathSe
 */
sealed class HaathSeRoute(val route: String) {
    // Platform Entry & Onboarding
    object Splash : HaathSeRoute("splash")
    object RoleSelection : HaathSeRoute("role_selection")

    // Artisan Auth & Portal
    object ArtisanLogin : HaathSeRoute("artisan_login")
    object ArtisanRegister : HaathSeRoute("artisan_register")
    object ArtisanDashboard : HaathSeRoute("artisan_dashboard")

    // Buyer Auth & Portal
    object BuyerLogin : HaathSeRoute("buyer_login")
    object BuyerRegister : HaathSeRoute("buyer_register")
    object BuyerHome : HaathSeRoute("buyer_home")

    // AI Product Studio Pipeline (Voice-to-Listing, Photography, Pricing, Story)
    object ArtisanProducts : HaathSeRoute("artisan_products")
    object AddProduct : HaathSeRoute("add_product")
    object CameraStudio : HaathSeRoute("camera_studio")
    object PhotoProcessing : HaathSeRoute("photo_processing")
    object VoiceDescription : HaathSeRoute("voice_description")
    object CatalogReview : HaathSeRoute("catalog_review")
    object CraftStory : HaathSeRoute("craft_story")
    object CraftAnalyzer : HaathSeRoute("craft_analyzer")
    object SmartPrice : HaathSeRoute("smart_price")
    object PublishListing : HaathSeRoute("publish_listing")

    // Commerce & Orders
    object Orders : HaathSeRoute("orders")
    object Cart : HaathSeRoute("cart")
    object Checkout : HaathSeRoute("checkout")
    object Profile : HaathSeRoute("profile")
    object Notifications : HaathSeRoute("notifications")

    // Community & Growth Features
    object CraftCircles : HaathSeRoute("craft_circles")
    object TradeEvents : HaathSeRoute("trade_events")
    object SaathiCoach : HaathSeRoute("saathi_coach")
    object MarketPulse : HaathSeRoute("market_pulse")
    object MaterialLedger : HaathSeRoute("material_ledger")

    // Administrative & Impact Portal
    object AdminDashboard : HaathSeRoute("admin_dashboard")
    object IndiaCraftMap : HaathSeRoute("india_craft_map")
    object SchemeFinder : HaathSeRoute("scheme_finder")
}
