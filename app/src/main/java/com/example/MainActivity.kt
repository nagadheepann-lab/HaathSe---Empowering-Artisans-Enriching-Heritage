package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppRole
import com.example.data.models.AuthUser
import com.example.data.models.SupportedLanguage
import com.example.data.repository.DemoAuthRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.KarigarRepository
import com.example.ui.components.TopBar
import com.example.ui.screens.*
import com.example.ui.screens.auth.*
import com.example.ui.screens.notifications.NotificationCenterScreen
import com.example.ui.theme.MyApplicationTheme

import com.example.ui.theme.PeacockTealTertiary
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodels.AuthViewModel
import com.example.utils.AudioVoiceHelper
import com.example.utils.MultilingualManager
import com.example.utils.RazorpayManager
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    private var audioHelper: AudioVoiceHelper? = null
    private lateinit var repository: KarigarRepository
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audioHelper = AudioVoiceHelper(this)
        repository = KarigarRepository(this)
        val authRepo = FirebaseAuthRepository()
        authViewModel = AuthViewModel(authRepo)

        setContent {
            MyApplicationTheme {
                KarigarSetuApp(
                    repository = repository,
                    authViewModel = authViewModel,
                    audioHelper = audioHelper
                )
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?, paymentData: PaymentData?) {
        android.util.Log.d("MainActivity", "Razorpay Payment Success: $razorpayPaymentID, Data: ${paymentData?.paymentId}")
        RazorpayManager.notifyPaymentSuccess(razorpayPaymentID ?: paymentData?.paymentId ?: "pay_success", paymentData)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        android.util.Log.e("MainActivity", "Razorpay Payment Failed. Code: $code, Resp: $response")
        RazorpayManager.notifyPaymentError(code, response, paymentData)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioHelper?.shutdown()
        audioHelper = null
    }
}

enum class AppDestination {
    // Onboarding & Auth
    SPLASH,
    ROLE_SELECTION,
    ARTISAN_LOGIN,
    ARTISAN_REGISTER,
    BUYER_LOGIN,
    BUYER_REGISTER,

    // Artisan Business Command Center destinations
    ARTISAN_HOME,
    ONE_TAP_STUDIO,
    SAATHI,
    MARKET_PULSE,
    CRAFT_CIRCLES,
    CRAFT_EVENTS,
    ARTISAN_PRODUCTS,
    ARTISAN_PROFILE,
    ORDERS_STOCK,
    MATERIAL_LEDGER,
    BUSINESS_COACH,

    // Buyer destinations
    BUYER_MARKETPLACE,
    BUYER_POST_RFQ,
    UNIVERSAL_CHAT,

    // Admin & Hub destinations
    ADMIN_ANALYTICS,
    INDIA_CRAFT_MAP,
    SCHEME_FINDER,
    NOTIFICATION_CENTER
}

@Composable
fun KarigarSetuApp(
    repository: KarigarRepository,
    authViewModel: AuthViewModel,
    audioHelper: AudioVoiceHelper?
) {
    val authState by authViewModel.uiState.collectAsState()
    var currentRole by remember { mutableStateOf(AppRole.ARTISAN) }
    var currentLanguage by remember { mutableStateOf(SupportedLanguage.ENGLISH) }
    var isSimpleMode by remember { mutableStateOf(false) }
    var currentDestination by remember { mutableStateOf(AppDestination.ROLE_SELECTION) }
    var saathiActiveQuery by remember { mutableStateOf<String?>(null) }
    val unreadNotifCount by (if (currentRole == AppRole.ARTISAN) repository.unreadArtisanCount else repository.unreadBuyerCount).collectAsState(initial = 0)

    LaunchedEffect(authState.currentUser) {
        authState.currentUser?.let { user ->
            repository.setCurrentUser(user)
        }
    }

    fun openSaathiWithQuery(query: String) {
        saathiActiveQuery = query
        currentDestination = AppDestination.SAATHI
    }

    // When role changes, switch default destination
    fun selectRole(role: AppRole) {
        currentRole = role
        currentDestination = when (role) {
            AppRole.ARTISAN -> AppDestination.ARTISAN_HOME
            AppRole.BUYER -> AppDestination.BUYER_MARKETPLACE
            AppRole.ADMIN -> AppDestination.ADMIN_ANALYTICS
        }
    }

    val hideTopAndBottomBar = currentDestination == AppDestination.SPLASH ||
            currentDestination == AppDestination.ROLE_SELECTION ||
            currentDestination == AppDestination.ARTISAN_LOGIN ||
            currentDestination == AppDestination.ARTISAN_REGISTER ||
            currentDestination == AppDestination.BUYER_LOGIN ||
            currentDestination == AppDestination.BUYER_REGISTER ||
            currentDestination == AppDestination.ONE_TAP_STUDIO ||
            currentDestination == AppDestination.SAATHI ||
            currentDestination == AppDestination.MARKET_PULSE ||
            currentDestination == AppDestination.CRAFT_CIRCLES ||
            currentDestination == AppDestination.CRAFT_EVENTS ||
            currentDestination == AppDestination.NOTIFICATION_CENTER ||
            currentDestination == AppDestination.ARTISAN_PRODUCTS ||
            currentDestination == AppDestination.ARTISAN_PROFILE ||
            currentDestination == AppDestination.ORDERS_STOCK ||
            currentDestination == AppDestination.MATERIAL_LEDGER ||
            currentDestination == AppDestination.BUSINESS_COACH ||
            currentDestination == AppDestination.BUYER_POST_RFQ ||
            currentDestination == AppDestination.UNIVERSAL_CHAT ||
            currentDestination == AppDestination.INDIA_CRAFT_MAP ||
            currentDestination == AppDestination.SCHEME_FINDER ||
            (currentRole == AppRole.BUYER && currentDestination == AppDestination.BUYER_MARKETPLACE)

    Scaffold(
        topBar = {
            if (!hideTopAndBottomBar) {
                TopBar(
                    currentRole = currentRole,
                    currentLanguage = currentLanguage,
                    isSimpleMode = isSimpleMode,
                    onRoleSelected = { selectRole(it) },
                    onLanguageSelected = {
                        currentLanguage = it
                        audioHelper?.speak(it.voiceGreeting, it)
                    },
                    onToggleSimpleMode = { isSimpleMode = !isSimpleMode },
                    audioHelper = audioHelper,
                    onNavigateNotifications = { currentDestination = AppDestination.NOTIFICATION_CENTER },
                    unreadNotificationCount = unreadNotifCount
                )
            }
        },

        bottomBar = {
            if (!hideTopAndBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    when (currentRole) {
                        AppRole.ARTISAN -> {
                            // 1. Home
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.ARTISAN_HOME,
                                onClick = { currentDestination = AppDestination.ARTISAN_HOME },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text(MultilingualManager.tr("Home", currentLanguage), fontSize = 11.sp, fontWeight = if (currentDestination == AppDestination.ARTISAN_HOME) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.testTag("nav_artisan_home")
                            )

                            // 2. Products
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.ARTISAN_PRODUCTS,
                                onClick = { currentDestination = AppDestination.ARTISAN_PRODUCTS },
                                icon = { Icon(Icons.Default.Inventory2, contentDescription = "Products") },
                                label = { Text(MultilingualManager.tr("Products", currentLanguage), fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_artisan_products")
                            )

                            // 3. Central Prominent Elevated ADD (One-Tap Studio)
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.ONE_TAP_STUDIO,
                                onClick = { currentDestination = AppDestination.ONE_TAP_STUDIO },
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(TerracottaPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Product",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                },
                                label = { Text(MultilingualManager.tr("Add", currentLanguage), fontWeight = FontWeight.ExtraBold, color = TerracottaPrimary, fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_one_tap_add")
                            )

                            // 4. Circles
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.CRAFT_CIRCLES,
                                onClick = { currentDestination = AppDestination.CRAFT_CIRCLES },
                                icon = { Icon(Icons.Default.Groups, contentDescription = "Circles") },
                                label = { Text(MultilingualManager.tr("Circles", currentLanguage), fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_artisan_circles")
                            )

                            // 5. Profile
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.ARTISAN_PROFILE,
                                onClick = { currentDestination = AppDestination.ARTISAN_PROFILE },
                                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                                label = { Text(MultilingualManager.tr("Profile", currentLanguage), fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_artisan_profile")
                            )
                        }

                        AppRole.BUYER -> {
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.BUYER_MARKETPLACE,
                                onClick = { currentDestination = AppDestination.BUYER_MARKETPLACE },
                                icon = { Icon(Icons.Default.Storefront, contentDescription = "Explore") },
                                label = { Text(MultilingualManager.tr("Explore", currentLanguage), fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.BUYER_POST_RFQ,
                                onClick = { currentDestination = AppDestination.BUYER_POST_RFQ },
                                icon = { Icon(Icons.Default.PostAdd, contentDescription = "Post RFQ") },
                                label = { Text(MultilingualManager.tr("Post RFQ", currentLanguage), fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.UNIVERSAL_CHAT,
                                onClick = { currentDestination = AppDestination.UNIVERSAL_CHAT },
                                icon = { Icon(Icons.Default.Chat, contentDescription = "Messages") },
                                label = { Text(MultilingualManager.tr("Chat", currentLanguage), fontSize = 11.sp) }
                            )
                        }

                        AppRole.ADMIN -> {
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.ADMIN_ANALYTICS,
                                onClick = { currentDestination = AppDestination.ADMIN_ANALYTICS },
                                icon = { Icon(Icons.Default.Analytics, contentDescription = "Impact") },
                                label = { Text(MultilingualManager.tr("Impact", currentLanguage), fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.INDIA_CRAFT_MAP,
                                onClick = { currentDestination = AppDestination.INDIA_CRAFT_MAP },
                                icon = { Icon(Icons.Default.Map, contentDescription = "Craft Map") },
                                label = { Text(MultilingualManager.tr("Craft Map", currentLanguage), fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.SCHEME_FINDER,
                                onClick = { currentDestination = AppDestination.SCHEME_FINDER },
                                icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Schemes") },
                                label = { Text(MultilingualManager.tr("Schemes", currentLanguage), fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.SPLASH -> SplashScreen(
                    currentLanguage = currentLanguage,
                    audioHelper = audioHelper,
                    onNavigateNext = { currentDestination = AppDestination.ROLE_SELECTION }
                )

                AppDestination.ROLE_SELECTION -> RoleSelectionScreen(
                    currentLanguage = currentLanguage,
                    onLanguageChange = { currentLanguage = it },
                    onSelectRole = { role ->
                        selectRole(role)
                    },
                    onNavigateArtisanLogin = { currentDestination = AppDestination.ARTISAN_LOGIN },
                    onNavigateArtisanRegister = { currentDestination = AppDestination.ARTISAN_REGISTER },
                    onNavigateBuyerLogin = { currentDestination = AppDestination.BUYER_LOGIN },
                    onNavigateBuyerRegister = { currentDestination = AppDestination.BUYER_REGISTER },
                    audioHelper = audioHelper
                )

                AppDestination.ARTISAN_LOGIN -> ArtisanLoginScreen(
                    viewModel = authViewModel,
                    audioHelper = audioHelper,
                    repository = repository,
                    currentLanguage = currentLanguage,
                    onNavigateBack = { currentDestination = AppDestination.ROLE_SELECTION },
                    onNavigateToRegister = { currentDestination = AppDestination.ARTISAN_REGISTER },
                    onLoginSuccess = { user ->
                        repository.setCurrentUser(user)
                        currentRole = AppRole.ARTISAN
                        currentLanguage = user.selectedLanguage
                        currentDestination = AppDestination.ARTISAN_HOME
                    }
                )

                AppDestination.ARTISAN_REGISTER -> ArtisanRegisterScreen(
                    viewModel = authViewModel,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.ROLE_SELECTION },
                    onNavigateToLogin = { currentDestination = AppDestination.ARTISAN_LOGIN },
                    onRegistrationSuccess = { user ->
                        repository.setCurrentUser(user)
                        currentRole = AppRole.ARTISAN
                        currentLanguage = user.selectedLanguage
                        currentDestination = AppDestination.ARTISAN_HOME
                    }
                )

                AppDestination.BUYER_LOGIN -> BuyerLoginScreen(
                    viewModel = authViewModel,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.ROLE_SELECTION },
                    onNavigateToRegister = { currentDestination = AppDestination.BUYER_REGISTER },
                    onLoginSuccess = { user ->
                        repository.setCurrentUser(user)
                        currentRole = AppRole.BUYER
                        currentLanguage = user.selectedLanguage
                        currentDestination = AppDestination.BUYER_MARKETPLACE
                    }
                )

                AppDestination.BUYER_REGISTER -> BuyerRegisterScreen(
                    viewModel = authViewModel,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.ROLE_SELECTION },
                    onNavigateToLogin = { currentDestination = AppDestination.BUYER_LOGIN },
                    onRegistrationSuccess = { user ->
                        repository.setCurrentUser(user)
                        currentRole = AppRole.BUYER
                        currentLanguage = user.selectedLanguage
                        currentDestination = AppDestination.BUYER_MARKETPLACE
                    }
                )

                // Complete Artisan Business Command Center
                AppDestination.ARTISAN_HOME -> ArtisanHomeScreen(
                    currentLanguage = currentLanguage,
                    isSimpleMode = isSimpleMode,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateOneTapStudio = { currentDestination = AppDestination.ONE_TAP_STUDIO },
                    onNavigateSaathi = {
                        saathiActiveQuery = null
                        currentDestination = AppDestination.SAATHI
                    },
                    onNavigateMarketPulse = { currentDestination = AppDestination.MARKET_PULSE },
                    onNavigateOrders = { currentDestination = AppDestination.ORDERS_STOCK },
                    onNavigateMaterials = { currentDestination = AppDestination.MATERIAL_LEDGER },
                    onNavigateCircles = { currentDestination = AppDestination.CRAFT_CIRCLES },
                    onNavigateEvents = { currentDestination = AppDestination.CRAFT_EVENTS },
                    onNavigateNotifications = { currentDestination = AppDestination.NOTIFICATION_CENTER },
                    onNavigateProducts = { currentDestination = AppDestination.ARTISAN_PRODUCTS },
                    onNavigateProfile = { currentDestination = AppDestination.ARTISAN_PROFILE },
                    onOpenSaathiWithQuery = { query -> openSaathiWithQuery(query) }
                )

                // Saathi AI Companion Screen
                AppDestination.SAATHI -> SaathiScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    initialQuery = saathiActiveQuery,
                    onNavigateBack = { currentDestination = AppDestination.ARTISAN_HOME },
                    onNavigateMarketPulse = { currentDestination = AppDestination.MARKET_PULSE },
                    onNavigateOneTapStudio = { currentDestination = AppDestination.ONE_TAP_STUDIO },
                    onNavigateOrders = { currentDestination = AppDestination.ORDERS_STOCK }
                )

                // Market Pulse Screen
                AppDestination.MARKET_PULSE -> MarketPulseScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.ARTISAN_HOME },
                    onOpenSaathiWithQuery = { query -> openSaathiWithQuery(query) }
                )

                // Craft Circles Screen
                AppDestination.CRAFT_CIRCLES -> CraftCirclesScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.ARTISAN_HOME },
                    onOpenSaathiWithQuery = { query -> openSaathiWithQuery(query) }
                )

                // Craft Events Screen
                AppDestination.CRAFT_EVENTS -> CraftEventsScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.ARTISAN_HOME }
                )

                // Notification Center Screen
                AppDestination.NOTIFICATION_CENTER -> NotificationCenterScreen(
                    currentRole = currentRole,
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = {
                        currentDestination = when (currentRole) {
                            AppRole.ARTISAN -> AppDestination.ARTISAN_HOME
                            AppRole.BUYER -> AppDestination.BUYER_MARKETPLACE
                            else -> AppDestination.ADMIN_ANALYTICS
                        }
                    },
                    onActionRoute = { route ->
                        when (route) {
                            "ORDERS_STOCK" -> currentDestination = AppDestination.ORDERS_STOCK
                            "CRAFT_CIRCLES" -> currentDestination = AppDestination.CRAFT_CIRCLES
                            "CRAFT_EVENTS" -> currentDestination = AppDestination.CRAFT_EVENTS
                            "ARTISAN_PROFILE" -> currentDestination = AppDestination.ARTISAN_PROFILE
                            "MARKET_PULSE" -> currentDestination = AppDestination.MARKET_PULSE
                            "ONE_TAP_STUDIO" -> currentDestination = AppDestination.ONE_TAP_STUDIO
                            "BUYER_ORDERS" -> {
                                currentRole = AppRole.BUYER
                                currentDestination = AppDestination.BUYER_MARKETPLACE
                            }
                            else -> currentDestination = if (currentRole == AppRole.ARTISAN) AppDestination.ARTISAN_HOME else AppDestination.BUYER_MARKETPLACE
                        }
                    }
                )



                // Artisan Products Catalog Screen
                AppDestination.ARTISAN_PRODUCTS -> ArtisanProductsScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.ARTISAN_HOME },
                    onNavigateAddProduct = { currentDestination = AppDestination.ONE_TAP_STUDIO }
                )

                // Artisan Profile Screen
                AppDestination.ARTISAN_PROFILE -> ArtisanProfileScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onLanguageSelected = { currentLanguage = it },
                    onNavigateBack = { currentDestination = AppDestination.ARTISAN_HOME },
                    onLogout = { currentDestination = AppDestination.ROLE_SELECTION }
                )

                AppDestination.ONE_TAP_STUDIO -> OneTapStudioScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateHome = { currentDestination = AppDestination.ARTISAN_HOME }
                )

                AppDestination.ORDERS_STOCK -> ArtisanOrdersScreen(
                    currentLanguage = currentLanguage,
                    cartViewModel = repository.cartViewModel,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.ARTISAN_HOME }
                )

                AppDestination.MATERIAL_LEDGER -> MaterialLedgerScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.ARTISAN_HOME }
                )

                AppDestination.BUSINESS_COACH -> BusinessCoachScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.ARTISAN_HOME }
                )

                AppDestination.BUYER_MARKETPLACE -> BuyerMarketplaceScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateChat = { currentDestination = AppDestination.UNIVERSAL_CHAT },
                    onNavigatePostRfq = { currentDestination = AppDestination.BUYER_POST_RFQ },
                    onSwitchToArtisanMode = { selectRole(AppRole.ARTISAN) }
                )

                AppDestination.BUYER_POST_RFQ -> BuyerRfqScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.BUYER_MARKETPLACE }
                )

                AppDestination.UNIVERSAL_CHAT -> UniversalChatScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = { currentDestination = AppDestination.BUYER_MARKETPLACE }
                )

                AppDestination.ADMIN_ANALYTICS -> AdminAnalyticsScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateCraftMap = { currentDestination = AppDestination.INDIA_CRAFT_MAP },
                    onNavigateSchemes = { currentDestination = AppDestination.SCHEME_FINDER }
                )

                AppDestination.INDIA_CRAFT_MAP -> IndiaCraftMapScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = {
                        currentDestination = if (currentRole == AppRole.ARTISAN) AppDestination.ARTISAN_HOME else AppDestination.ADMIN_ANALYTICS
                    }
                )

                AppDestination.SCHEME_FINDER -> SchemeFinderScreen(
                    currentLanguage = currentLanguage,
                    repository = repository,
                    audioHelper = audioHelper,
                    onNavigateBack = {
                        currentDestination = if (currentRole == AppRole.ARTISAN) AppDestination.ARTISAN_HOME else AppDestination.ADMIN_ANALYTICS
                    }
                )
            }
        }
    }
}
