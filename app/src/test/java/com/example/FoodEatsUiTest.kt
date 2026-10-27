package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.data.repository.DishItem
import com.example.data.repository.DishOption
import com.example.data.repository.FoodEatsRepository
import com.example.data.repository.Restaurant
import com.example.ui.cart.ShoppingBagScreen
import com.example.ui.components.ZandoOutlinedTextField
import com.example.ui.navigation.CartSummaryBar
import com.example.ui.home.HomeScreen
import com.example.ui.restaurant.DishCustomizationDialog
import com.example.ui.restaurant.RestaurantDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w360dp-h800dp-xhdpi")
class FoodEatsUiTest {
    @get:Rule val compose = createComposeRule()

    private val dish = DishItem(9001L, "Garden rice bowl", "Roasted vegetables, jasmine rice and a fresh lime dressing.", 8.0, "Bowls", "")
    private val restaurant = Restaurant(
        id = 9001L, name = "The Green Kitchen", cuisine = "Fresh & local", category = "Healthy",
        distance = "1.2 km", rating = 4.8, reviewsCount = 120, deliveryFee = 1.5,
        deliveryTime = "20–30 min", address = "12 Riverside Street", imageUrl = "", bannerUrl = "",
        description = "Seasonal bowls and comforting favorites, made to order.", phone = "", openingHours = "10am–9pm", menu = listOf(dish)
    )

    @Before fun clearCart() { FoodEatsRepository.clearCart(); FoodEatsRepository.removeAppliedVoucher() }
    @After fun cleanUp() { FoodEatsRepository.clearCart(); FoodEatsRepository.removeAppliedVoucher() }

    @Test fun menuSearchCanRecoverFromNoResults() {
        compose.setContent { MyApplicationTheme { RestaurantDetailScreen(restaurant, {}, {}) } }
        compose.onNodeWithText("Find something on the menu").performScrollTo().performTextInput("not on this menu")
        compose.onNodeWithText("No dishes found").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Show all dishes").performClick()
        compose.onNodeWithText(dish.name).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Add to bag").performScrollTo().performClick()
        compose.onNodeWithText("1 item").assertIsDisplayed()
        assertEquals(1, FoodEatsRepository.cart.value.single().quantity)
    }

    @Test fun checkoutRequiresAddressAndClearBagCanBeCancelled() {
        FoodEatsRepository.addToCart(dish, restaurant)
        var shoppingOpened = false
        compose.setContent { MyApplicationTheme { ShoppingBagScreen({}, { shoppingOpened = true }) } }
        compose.onNodeWithText("Add more").performClick()
        assertTrue(shoppingOpened)
        compose.onNodeWithText("Place order").assertIsNotEnabled()
        compose.onNodeWithText("Full delivery address").performScrollTo().performTextInput("12 Riverside Street, Phnom Penh")
        compose.onNodeWithText("Place order").assertIsEnabled()
        compose.onNodeWithText("Clear bag").performClick()
        compose.onNodeWithText("Keep items").performClick()
        assertEquals(1, FoodEatsRepository.cart.value.size)
        compose.onNodeWithText("Clear bag").performClick()
        compose.onAllNodesWithText("Clear bag").onLast().performClick()
        compose.onNodeWithText("Good food starts here").assertIsDisplayed()
        assertTrue(FoodEatsRepository.cart.value.isEmpty())
    }

    @Test fun optionsUpdateTotalAndReachCartCallback() {
        val extra = DishOption(9002L, "Extras", "Avocado", 1.5)
        var confirmed = emptyList<DishOption>()
        compose.setContent { MyApplicationTheme { DishCustomizationDialog(dish.copy(options = listOf(extra)), {}, { confirmed = it }) } }
        compose.onNodeWithText("Avocado").performClick()
        compose.onNodeWithText("$9.50").assertIsDisplayed()
        compose.onNodeWithText("Add to bag").performClick()
        assertEquals(listOf(extra), confirmed)
    }

    @Test fun passwordVisibilityToggleKeepsEnteredValue() {
        var value by mutableStateOf("meal12345")
        compose.setContent {
            MyApplicationTheme {
                Surface { Column(Modifier.padding(24.dp)) { ZandoOutlinedTextField(value, { value = it }, "Password", "Enter password", isPassword = true) } }
            }
        }
        compose.onNodeWithContentDescription("Show password").performClick()
        compose.onNodeWithText("meal12345").assertIsDisplayed()
        compose.onNodeWithContentDescription("Hide password").performClick()
        compose.onNodeWithContentDescription("Show password").assertExists()
        assertEquals("meal12345", value)
    }

    @Test fun bagSummaryShowsSubtotalAndOpensBag() {
        var opened = false
        compose.setContent { MyApplicationTheme { CartSummaryBar(2, 16.0) { opened = true } } }
        compose.onNodeWithText("2 items").assertIsDisplayed()
        compose.onNodeWithText("$16.00").assertIsDisplayed()
        compose.onNodeWithText("Subtotal").assertIsDisplayed()
        compose.onNodeWithText("View bag").performClick()
        assertTrue(opened)
    }

    @Test fun restaurantPreview() {
        compose.setContent { MyApplicationTheme { RestaurantDetailScreen(restaurant, {}, {}) } }
        capture("restaurant")
    }

    @Test fun discoverSearchResetAndPreview() {
        compose.setContent { MyApplicationTheme { HomeScreen({}, {}) } }
        capture("discover")
        compose.onNodeWithText("Search food or restaurants").performTextInput("no-such-dish-9384")
        compose.onNodeWithText("No matches yet").assertIsDisplayed()
        compose.onNodeWithText("Reset filters").performClick()
        compose.onNodeWithText("No matches yet").assertDoesNotExist()
        compose.onNodeWithText("Search food or restaurants").assertIsDisplayed()
    }

    @Test fun checkoutPreview() {
        FoodEatsRepository.addToCart(dish, restaurant)
        compose.setContent { MyApplicationTheme { ShoppingBagScreen({}, {}) } }
        capture("checkout")
    }

    private fun capture(name: String) {
        File("build/ui-previews").mkdirs()
        compose.onRoot().captureRoboImage(filePath = "build/ui-previews/$name.png")
    }
}
