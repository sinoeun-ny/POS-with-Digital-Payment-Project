package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FoodEatsRepository
import com.example.data.repository.Restaurant
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
    var currentScreen by remember { mutableStateOf(AppScreen.DISCOVER) }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }

    val cart by FoodEatsRepository.cart.collectAsState()
    val orders by FoodEatsRepository.orders.collectAsState()

    val cartCount = cart.sumOf { it.quantity }
    val activeOrdersCount = orders.count { it.status != "DELIVERED" }

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
                                                contentDescription = info.first,
                                                tint = if (isSelected) Forest600 else Ink400,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = info.second,
                                            contentDescription = info.first,
                                            tint = if (isSelected) Forest600 else Ink400,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = info.first,
                                        fontSize = 11.sp,
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
                .background(Cream50)
        ) {
            when (currentScreen) {
                AppScreen.DISCOVER -> HomeScreen(
                    onNavigateToBag = { currentScreen = AppScreen.CART },
                    onNavigateToRestaurantDetail = { restaurant ->
                        selectedRestaurant = restaurant
                        currentScreen = AppScreen.RESTAURANT_DETAIL
                    }
                )
                AppScreen.RESTAURANT_DETAIL -> {
                    selectedRestaurant?.let { restaurant ->
                        RestaurantDetailScreen(
                            restaurant = restaurant,
                            onNavigateBack = { currentScreen = AppScreen.DISCOVER },
                            onNavigateToCart = { currentScreen = AppScreen.CART }
                        )
                    } ?: run {
                        currentScreen = AppScreen.DISCOVER
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
                    onNavigateBack = { currentScreen = AppScreen.DISCOVER },
                    onStartShopping = { currentScreen = AppScreen.DISCOVER },
                    onOrderPlaced = { currentScreen = AppScreen.ORDERS },
                    authViewModel = authViewModel
                )
                AppScreen.PROFILE -> ProfileScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                    onNavigateToRegister = { currentScreen = AppScreen.REGISTER },
                    onNavigateToAddresses = { currentScreen = AppScreen.ADDRESS_BOOK },
                    onNavigateToCountry = { currentScreen = AppScreen.COUNTRY_SELECTION }
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

            // Small Floating Cart Icon with Badge (appears as soon as any item is in the cart)
            if (showBottomBar && currentScreen != AppScreen.CART && cartCount > 0) {
                FloatingActionButton(
                    onClick = { currentScreen = AppScreen.CART },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp),
                    containerColor = Forest500,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = Rust500,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
