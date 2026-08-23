package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.admin.AdminRoleDivisionScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.cart.ShoppingBagScreen
import com.example.ui.category.CategoryMenuScreen
import com.example.ui.country.CountrySelectionScreen
import com.example.ui.home.HomeScreen
import com.example.ui.profile.AddressBookScreen
import com.example.ui.profile.ProfileScreen

enum class AppScreen {
    HOME,
    CATEGORY_MENU,
    BRANDS,
    WISHLIST,
    PROFILE,
    LOGIN,
    REGISTER,
    SHOPPING_BAG,
    ADDRESS_BOOK,
    COUNTRY_SELECTION,
    ADMIN_ROLE_DIVISION
}

@Composable
fun MainContainer(authViewModel: AuthViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    val bottomNavScreens = listOf(
        AppScreen.HOME to ("Home" to Icons.Default.Home),
        AppScreen.CATEGORY_MENU to ("Menu" to Icons.Default.Menu),
        AppScreen.BRANDS to ("Stores" to Icons.Default.LocalOffer),
        AppScreen.WISHLIST to ("Wish List" to Icons.Default.FavoriteBorder),
        AppScreen.PROFILE to ("Me" to Icons.Default.PersonOutline)
    )

    val showBottomBar = currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.CATEGORY_MENU,
        AppScreen.BRANDS,
        AppScreen.WISHLIST,
        AppScreen.PROFILE
    )

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ) {
                        bottomNavScreens.forEach { (screen, info) ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    Icon(
                                        imageVector = info.second,
                                        contentDescription = info.first,
                                        tint = if (isSelected) Color.Black else Color.Gray
                                    )
                                },
                                label = {
                                    Text(
                                        text = info.first,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else Color.Gray
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color(0xFFF2F2F2)
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
                .background(Color.White)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    onNavigateToBag = { currentScreen = AppScreen.SHOPPING_BAG },
                    onNavigateToMerchant = { currentScreen = AppScreen.CATEGORY_MENU }
                )
                AppScreen.CATEGORY_MENU -> CategoryMenuScreen(
                    onNavigateToBag = { currentScreen = AppScreen.SHOPPING_BAG },
                    onCategoryClick = { currentScreen = AppScreen.SHOPPING_BAG }
                )
                AppScreen.BRANDS -> CategoryMenuScreen(
                    onNavigateToBag = { currentScreen = AppScreen.SHOPPING_BAG },
                    onCategoryClick = { currentScreen = AppScreen.SHOPPING_BAG }
                )
                AppScreen.WISHLIST -> ShoppingBagScreen(
                    onNavigateBack = { currentScreen = AppScreen.HOME },
                    onStartShopping = { currentScreen = AppScreen.HOME }
                )
                AppScreen.PROFILE -> ProfileScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                    onNavigateToRegister = { currentScreen = AppScreen.REGISTER },
                    onNavigateToAddresses = { currentScreen = AppScreen.ADDRESS_BOOK },
                    onNavigateToCountry = { currentScreen = AppScreen.COUNTRY_SELECTION },
                    onNavigateToAdminRoleDivision = { currentScreen = AppScreen.ADMIN_ROLE_DIVISION }
                )
                AppScreen.LOGIN -> LoginScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { currentScreen = AppScreen.PROFILE },
                    onNavigateToRegister = { currentScreen = AppScreen.REGISTER },
                    onLoginSuccess = { currentScreen = AppScreen.PROFILE }
                )
                AppScreen.REGISTER -> RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { currentScreen = AppScreen.PROFILE },
                    onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                    onRegisterSuccess = { currentScreen = AppScreen.PROFILE }
                )
                AppScreen.SHOPPING_BAG -> ShoppingBagScreen(
                    onNavigateBack = { currentScreen = AppScreen.HOME },
                    onStartShopping = { currentScreen = AppScreen.HOME }
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
            }
        }
    }
}
