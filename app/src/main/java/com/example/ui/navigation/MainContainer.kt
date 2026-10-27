package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FoodEatsRepository
import com.example.ui.admin.AdminRoleDivisionScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.cart.ShoppingBagScreen
import com.example.ui.country.CountrySelectionScreen
import com.example.ui.delivery.DeliveryLoginScreen
import com.example.ui.delivery.DriverDashboardScreen
import com.example.ui.home.HomeScreen
import com.example.ui.orders.OrderHistoryScreen
import com.example.ui.payment.PaymentHistoryScreen
import com.example.ui.profile.AddressBookScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.restaurant.RestaurantDetailScreen
import com.example.ui.theme.*
import java.util.Locale

enum class AppScreen {
    DISCOVER,
    RESTAURANT_DETAIL,
    ORDERS,
    CART,
    PAYMENTS,
    PROFILE,
    LOGIN,
    REGISTER,
    ADDRESS_BOOK,
    COUNTRY_SELECTION,
    ADMIN_ROLE_DIVISION,
    DRIVER_DASHBOARD,
    DELIVERY_LOGIN
}

@Composable
fun MainContainer(authViewModel: AuthViewModel) {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.DISCOVER) }
    var cartReturnScreen by rememberSaveable { mutableStateOf(AppScreen.DISCOVER) }
    var selectedRestaurantId by rememberSaveable { mutableStateOf<Long?>(null) }
    val screenState = rememberSaveableStateHolder()

    val cart by FoodEatsRepository.cart.collectAsState()
    val orders by FoodEatsRepository.orders.collectAsState()
    val restaurants by FoodEatsRepository.restaurants.collectAsState()
    val isCatalogLoading by FoodEatsRepository.isLoading.collectAsState()
    val selectedRestaurant = restaurants.firstOrNull { it.id == selectedRestaurantId }

    val cartCount = cart.sumOf { it.quantity }
    val activeOrdersCount = orders.count { it.status !in listOf("DELIVERED", "CANCELLED") }
    val openCart: () -> Unit = {
        cartReturnScreen = currentScreen
        currentScreen = AppScreen.CART
    }
    val navigateBack: () -> Unit = {
        currentScreen = when (currentScreen) {
            AppScreen.CART -> cartReturnScreen
            AppScreen.RESTAURANT_DETAIL, AppScreen.ORDERS,
            AppScreen.PAYMENTS, AppScreen.PROFILE -> AppScreen.DISCOVER
            else -> AppScreen.PROFILE
        }
    }
    BackHandler(enabled = currentScreen != AppScreen.DISCOVER, onBack = navigateBack)

    // Bottom Navigation Screens: Discover, Orders, Payments, Profile
    val bottomNavScreens = listOf(
        AppScreen.DISCOVER to Triple("Discover", Icons.Default.Explore, 0),
        AppScreen.ORDERS to Triple("Orders", Icons.AutoMirrored.Filled.ReceiptLong, activeOrdersCount),
        AppScreen.PAYMENTS to Triple("Payments", Icons.Default.Payment, 0),
        AppScreen.PROFILE to Triple("Profile", Icons.Default.Person, 0)
    )

    val showBottomBar = currentScreen in listOf(
        AppScreen.DISCOVER,
        AppScreen.ORDERS,
        AppScreen.PAYMENTS,
        AppScreen.PROFILE
    )

    Scaffold(
        containerColor = Cream50,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    if (cartCount > 0) {
                        CartSummaryBar(
                            itemCount = cartCount,
                            subtotal = cart.sumOf { it.totalPrice },
                            onClick = openCart
                        )
                    }
                    HorizontalDivider(color = Sage200)
                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = Ink950
                    ) {
                        bottomNavScreens.forEach { (screen, info) ->
                            val isSelected = currentScreen == screen
                            val badgeNumber = info.third

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    if (badgeNumber > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(
                                                    containerColor = Rust500,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(badgeNumber.toString())
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = info.second,
                                                contentDescription = null,
                                                tint = if (isSelected) Forest600 else Ink400,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = info.second,
                                            contentDescription = null,
                                            tint = if (isSelected) Forest600 else Ink400,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = info.first,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Forest700 else Ink400
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Forest100,
                                    selectedIconColor = Forest600,
                                    unselectedIconColor = Ink400
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .background(Cream50)
        ) {
          screenState.SaveableStateProvider(if (currentScreen == AppScreen.RESTAURANT_DETAIL) "restaurant-$selectedRestaurantId" else currentScreen.name) {
            when (currentScreen) {
                AppScreen.DISCOVER -> HomeScreen(
                    onNavigateToBag = openCart,
                    onNavigateToRestaurantDetail = { restaurant ->
                        selectedRestaurantId = restaurant.id
                        currentScreen = AppScreen.RESTAURANT_DETAIL
                    }
                )
                AppScreen.RESTAURANT_DETAIL -> {
                    selectedRestaurant?.let { restaurant ->
                        RestaurantDetailScreen(
                            restaurant = restaurant,
                            onNavigateBack = { currentScreen = AppScreen.DISCOVER },
                            onNavigateToCart = openCart
                        )
                    } ?: run {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isCatalogLoading) CircularProgressIndicator()
                            Text(if (isCatalogLoading) "Loading menu…" else "This menu is currently unavailable", style = MaterialTheme.typography.titleMedium)
                            Button(onClick = { currentScreen = AppScreen.DISCOVER }) { Text("Browse restaurants") }
                        }
                    }
                }
                AppScreen.ORDERS -> OrderHistoryScreen(
                    authViewModel = authViewModel,
                    onNavigateToDiscover = { currentScreen = AppScreen.DISCOVER }
                )
                AppScreen.PAYMENTS -> PaymentHistoryScreen(
                    authViewModel = authViewModel,
                    onNavigateToDiscover = { currentScreen = AppScreen.DISCOVER }
                )
                AppScreen.CART -> ShoppingBagScreen(
                    onNavigateBack = navigateBack,
                    onStartShopping = {
                        val merchantId = cart.firstOrNull()?.merchant?.id
                        if (merchantId != null && restaurants.any { it.id == merchantId }) {
                            selectedRestaurantId = merchantId
                            currentScreen = AppScreen.RESTAURANT_DETAIL
                        } else {
                            currentScreen = AppScreen.DISCOVER
                        }
                    },
                    onOrderPlaced = { currentScreen = AppScreen.ORDERS },
                    authViewModel = authViewModel
                )
                AppScreen.PROFILE -> ProfileScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                    onNavigateToRegister = { currentScreen = AppScreen.REGISTER },
                    onNavigateToAddresses = { currentScreen = AppScreen.ADDRESS_BOOK },
                    onNavigateToCountry = { currentScreen = AppScreen.COUNTRY_SELECTION },
                    onNavigateToAdminRoleDivision = { currentScreen = AppScreen.ADMIN_ROLE_DIVISION },
                    onNavigateToDriver = { currentScreen = AppScreen.DRIVER_DASHBOARD },
                    onNavigateToDeliveryLogin = { currentScreen = AppScreen.DELIVERY_LOGIN }
                )
                AppScreen.LOGIN -> LoginScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { currentScreen = AppScreen.PROFILE },
                    onNavigateToRegister = { currentScreen = AppScreen.REGISTER },
                    onLoginSuccess = { currentScreen = AppScreen.PROFILE }
                )
                AppScreen.DELIVERY_LOGIN -> DeliveryLoginScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { currentScreen = AppScreen.PROFILE },
                    onNavigateToRegisterDriver = { currentScreen = AppScreen.REGISTER },
                    onNavigateToStandardLogin = { currentScreen = AppScreen.LOGIN },
                    onLoginSuccess = { currentScreen = AppScreen.DRIVER_DASHBOARD }
                )
                AppScreen.REGISTER -> RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { currentScreen = AppScreen.PROFILE },
                    onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                    onRegisterSuccess = { currentScreen = AppScreen.PROFILE }
                )
                AppScreen.ADDRESS_BOOK -> AddressBookScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { currentScreen = AppScreen.PROFILE }
                )
                AppScreen.COUNTRY_SELECTION -> CountrySelectionScreen(
                    onCountrySelected = { currentScreen = AppScreen.PROFILE }
                )
                AppScreen.ADMIN_ROLE_DIVISION -> AdminRoleDivisionScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { currentScreen = AppScreen.PROFILE }
                )
                AppScreen.DRIVER_DASHBOARD -> DriverDashboardScreen(
                    onNavigateBack = { currentScreen = AppScreen.PROFILE },
                    authViewModel = authViewModel
                )
            }

          }
        }
    }
}

@Composable
internal fun CartSummaryBar(itemCount: Int, subtotal: Double, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Forest700,
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null)
            Column(Modifier.weight(1f)) {
                Text("View bag", style = MaterialTheme.typography.titleSmall)
                Text("$itemCount ${if (itemCount == 1) "item" else "items"}", style = MaterialTheme.typography.bodySmall, color = Forest100)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${String.format(Locale.US, "%.2f", subtotal)}", style = MaterialTheme.typography.titleMedium)
                Text("Subtotal", style = MaterialTheme.typography.labelSmall, color = Forest100)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}
