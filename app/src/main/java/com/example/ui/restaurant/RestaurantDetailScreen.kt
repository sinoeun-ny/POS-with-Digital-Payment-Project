package com.example.ui.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.repository.DishItem
import com.example.data.repository.DishOption
import com.example.data.repository.FoodEatsRepository
import com.example.data.repository.Restaurant
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val cart by FoodEatsRepository.cart.collectAsState()
    var selectedCategoryTab by remember { mutableStateOf("ALL") }
    var itemForCustomization by remember { mutableStateOf<DishItem?>(null) }
    var showMerchantConflictDialog by remember { mutableStateOf(false) }
    var pendingDishToAdd by remember { mutableStateOf<Pair<DishItem, List<DishOption>>?>(null) }

    val categories = remember(restaurant.menu) {
        listOf("ALL") + restaurant.menu.map { it.category }.distinct()
    }

    val filteredMenu = remember(selectedCategoryTab, restaurant.menu) {
        if (selectedCategoryTab == "ALL") restaurant.menu
        else restaurant.menu.filter { it.category == selectedCategoryTab }
    }

    val cartCount = cart.sumOf { it.quantity }
    val cartSubtotal = cart.sumOf { it.totalPrice }

    Scaffold(
        containerColor = Cream50,
        bottomBar = {
            if (cartCount > 0) {
                Surface(
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$cartCount items in cart",
                                fontSize = 12.sp,
                                color = Ink500,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$${String.format(java.util.Locale.US, "%.2f", cartSubtotal)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Ink950
                            )
                        }
                        Button(
                            onClick = onNavigateToCart,
                            colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "View Cart",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Cart",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Image Banner with Overlay Actions
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    AsyncImage(
                        model = restaurant.bannerUrl.ifEmpty { restaurant.imageUrl },
                        contentDescription = restaurant.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    // Navigation Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Ink950
                            )
                        }

                        if (cartCount > 0) {
                            IconButton(
                                onClick = onNavigateToCart,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Forest500)
                            ) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = Rust500, contentColor = Color.White) {
                                            Text(cartCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Cart",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Restaurant Title & Cuisine over Banner
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = restaurant.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Surface(
                                color = Mint500,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = restaurant.cuisine,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink950,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "⭐ ${restaurant.rating} (${restaurant.reviewsCount} reviews)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = restaurant.description,
                            fontSize = 13.sp,
                            color = Ink700,
                            lineHeight = 18.sp
                        )

                        HorizontalDivider(color = Sage100)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "DELIVERY TIME", fontSize = 10.sp, color = Ink500, fontWeight = FontWeight.Bold)
                                Text(text = restaurant.deliveryTime, fontSize = 13.sp, color = Ink950, fontWeight = FontWeight.ExtraBold)
                            }
                            Column {
                                Text(text = "DELIVERY FEE", fontSize = 10.sp, color = Ink500, fontWeight = FontWeight.Bold)
                                Text(text = "$${String.format(java.util.Locale.US, "%.2f", restaurant.deliveryFee)}", fontSize = 13.sp, color = Forest600, fontWeight = FontWeight.ExtraBold)
                            }
                            Column {
                                Text(text = "HOURS", fontSize = 10.sp, color = Ink500, fontWeight = FontWeight.Bold)
                                Text(text = restaurant.openingHours, fontSize = 13.sp, color = Ink950, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Forest500,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = restaurant.address,
                                fontSize = 12.sp,
                                color = Ink600,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryTab == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategoryTab = cat },
                            color = if (isSelected) Forest500 else Color.White,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Sage200),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Ink700,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Section Title
            item {
                Text(
                    text = "Featured Gourmet Menu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink950,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            // Menu Items List
            items(filteredMenu) { dish ->
                DishItemCard(
                    dish = dish,
                    onAddClick = {
                        if (dish.options.isNotEmpty()) {
                            itemForCustomization = dish
                        } else {
                            val success = FoodEatsRepository.addToCart(dish, restaurant)
                            if (!success) {
                                pendingDishToAdd = Pair(dish, emptyList())
                                showMerchantConflictDialog = true
                            }
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Customization Dialog for Menu Options (patty size, spice level, add-ons, etc.)
    itemForCustomization?.let { dish ->
        DishCustomizationDialog(
            dish = dish,
            onDismiss = { itemForCustomization = null },
            onConfirm = { selectedOptions ->
                val success = FoodEatsRepository.addToCart(dish, restaurant, selectedOptions)
                itemForCustomization = null
                if (!success) {
                    pendingDishToAdd = Pair(dish, selectedOptions)
                    showMerchantConflictDialog = true
                }
            }
        )
    }

    // Multi-merchant Warning Dialog
    if (showMerchantConflictDialog) {
        AlertDialog(
            onDismissRequest = { showMerchantConflictDialog = false },
            title = {
                Text(text = "Start new order?", fontWeight = FontWeight.Bold, color = Ink950)
            },
            text = {
                Text(
                    text = "Your cart already contains items from another restaurant. Would you like to clear your cart and start a new order with ${restaurant.name}?",
                    color = Ink700,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDishToAdd?.let { (dish, options) ->
                            FoodEatsRepository.replaceCartAndAdd(dish, restaurant, options)
                        }
                        showMerchantConflictDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest500)
                ) {
                    Text("Clear Cart & Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMerchantConflictDialog = false }) {
                    Text("Cancel", color = Ink500)
                }
            }
        )
    }
}

@Composable
fun DishItemCard(
    dish: DishItem,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dish Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                dish.dietaryTag?.let { tag ->
                    Surface(
                        color = Forest50,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest700,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = dish.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink950
                )

                Text(
                    text = dish.description,
                    fontSize = 12.sp,
                    color = Ink500,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", dish.price)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Forest600
                    )
                    Text(
                        text = "• ${dish.prepTimeMinutes} mins",
                        fontSize = 11.sp,
                        color = Ink400
                    )
                }
            }

            // Image & Add Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AsyncImage(
                    model = dish.imageUrl,
                    contentDescription = dish.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (dish.options.isNotEmpty()) "+ Customize" else "+ Add",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DishCustomizationDialog(
    dish: DishItem,
    onDismiss: () -> Unit,
    onConfirm: (List<DishOption>) -> Unit
) {
    val selectedOptions = remember {
        mutableStateListOf<DishOption>().apply {
            // Auto-select defaults
            dish.options.filter { it.isDefault }.forEach { add(it) }
        }
    }

    val groupedOptions = remember(dish.options) {
        dish.options.groupBy { it.optionGroup }
    }

    val currentTotal = dish.price + selectedOptions.sumOf { it.priceAdjustment }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Customize ${dish.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink950
                )
                Text(
                    text = "Select your preferred size, spice, or extra toppings",
                    fontSize = 12.sp,
                    color = Ink500,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                HorizontalDivider(color = Sage100)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 340.dp)
                        .padding(vertical = 8.dp)
                ) {
                    groupedOptions.forEach { (group, options) ->
                        item {
                            Text(
                                text = group.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Forest700,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }

                        items(options) { opt ->
                            val isSelected = selectedOptions.any { it.id == opt.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        // If single-choice style (e.g. Patty Size, Spice, Sweetness), remove others in same group
                                        if (group.contains("Size", ignoreCase = true) ||
                                            group.contains("Level", ignoreCase = true) ||
                                            group.contains("Heat", ignoreCase = true) ||
                                            group.contains("Firmness", ignoreCase = true)
                                        ) {
                                            selectedOptions.removeAll { it.optionGroup == group }
                                            selectedOptions.add(opt)
                                        } else {
                                            if (isSelected) {
                                                selectedOptions.removeAll { it.id == opt.id }
                                            } else {
                                                selectedOptions.add(opt)
                                            }
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = Forest500)
                                    )
                                    Text(
                                        text = opt.optionName,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Ink950 else Ink700,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                                if (opt.priceAdjustment > 0) {
                                    Text(
                                        text = "+$${String.format(java.util.Locale.US, "%.2f", opt.priceAdjustment)}",
                                        fontSize = 12.sp,
                                        color = Forest600,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Sage100, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Ink500)
                    }

                    Button(
                        onClick = { onConfirm(selectedOptions.toList()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Add to Cart • $${String.format(java.util.Locale.US, "%.2f", currentTotal)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
