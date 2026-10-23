package com.example.ui.home

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
import coil.compose.AsyncImage
import com.example.data.repository.FoodEatsRepository
import com.example.data.repository.Restaurant
import com.example.ui.theme.*
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.repository.DishItem
import java.util.Locale

// pair a menu dish with its parent restaurant so tapping it can navigate to the merchant or add to bag
data class FeaturedDish(
    val dish: DishItem,
    val restaurant: Restaurant
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBag: () -> Unit,
    onNavigateToRestaurantDetail: (Restaurant) -> Unit
) {
    val restaurants by FoodEatsRepository.restaurants.collectAsState()
    val cart by FoodEatsRepository.cart.collectAsState()
    val serverStatus by FoodEatsRepository.serverStatus.collectAsState()
    val isLoading by FoodEatsRepository.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "Burgers", "Japanese", "Coffee & Bakery", "Khmer", "Desserts")

    LaunchedEffect(searchQuery, selectedCategory) {
        kotlinx.coroutines.delay(400L) // wait 400ms while user is typing
        val cuisineParam = if (selectedCategory == "ALL") null else selectedCategory
        val searchParam = if (searchQuery.isBlank()) null else searchQuery.trim()
        FoodEatsRepository.loadFromBackend(search = searchParam, cuisine = cuisineParam)
    }

    val filteredRestaurants = remember(restaurants, selectedCategory, searchQuery) {
        restaurants.filter { r ->
            val matchCategory = selectedCategory == "ALL" ||
                    r.category.contains(selectedCategory, ignoreCase = true) ||
                    r.cuisine.contains(selectedCategory, ignoreCase = true)

            val matchSearch = searchQuery.isBlank() ||
                    r.name.contains(searchQuery, ignoreCase = true) ||
                    r.cuisine.contains(searchQuery, ignoreCase = true) ||
                    r.menu.any { m -> m.name.contains(searchQuery, ignoreCase = true) }

            matchCategory && matchSearch

        }
    }
    // Trending food & drinks carousel (Round-robin selection for variety across restaurants)
    val trendingDishes = remember(restaurants) {
        val items = mutableListOf<FeaturedDish>()
        val maxMenuSize = restaurants.maxOfOrNull { it.menu.size } ?: 0
        for (i in 0 until maxMenuSize) {
            for (restaurant in restaurants) {
                if (i < restaurant.menu.size) {
                    items.add(FeaturedDish(restaurant.menu[i], restaurant))
                }
            }
        }
        items.take(10)// show top 10 diverse craving

    }

    // curated group for merchant
    val fastDeliverySpots = remember(restaurants) {
        restaurants.filter {
            (it.deliveryTime.filter { c -> c.isDigit() }.toIntOrNull() ?: 30) <= 20
        }
    }

    val cartCount = cart.sumOf { it.quantity }
    val cartSubtotal = cart.sumOf { it.totalPrice }

    Scaffold(
        containerColor = Cream50,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                // Top Brand Bar with Logo, Delivery Location, and Cart Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brand Logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Forest500, Forest700)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "FoodEats",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Ink950
                                )
                                Surface(
                                    color = Forest100,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Customer",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Forest700,
                                        modifier = Modifier.padding(
                                            horizontal = 4.dp,
                                            vertical = 1.dp
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Gourmet Discovery & Delivery",
                                fontSize = 10.sp,
                                color = Ink500
                            )
                        }
                    }

                    // Cart Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onNavigateToBag),
                        color = if (cartCount > 0) Forest500 else Ink50,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = if (cartCount > 0) Color.White else Ink700,
                                modifier = Modifier.size(16.dp)
                            )
                            if (cartCount > 0) {
                                Text(
                                    text = "$cartCount • $${
                                        String.format(
                                            java.util.Locale.US,
                                            "%.2f",
                                            cartSubtotal
                                        )
                                    }",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Cart",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink700
                                )
                            }
                        }
                    }
                }

                // Delivery Address Line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Pin",
                        tint = Forest500,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Deliver to: Building 42, St. 302, BKK1, Phnom Penh",
                        fontSize = 11.sp,
                        color = Ink600,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Search restaurants, dishes, cuisines...",
                                fontSize = 13.sp,
                                color = Ink400
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Forest600,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Ink400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Forest500,
                            unfocusedBorderColor = Sage200,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Sage50
                        )
                    )
                }

                HorizontalDivider(color = Sage100)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Hero Promo Banner matching customer.html
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Forest700, Forest500, Ink800)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = Mint400,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "20% OFF FIRST ORDER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Ink950,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Craving artisan food, delivered straight to your door?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 24.sp
                            )
                            Text(
                                text = "Use code FOODEATS20 at checkout for instant savings.",
                                fontSize = 12.sp,
                                color = Mint100
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat },
                            color = if (isSelected) Forest500 else Color.White,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Sage200
                            ),
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

            val isDiscoverMode = searchQuery.isBlank() && selectedCategory == "ALL"

            if (isDiscoverMode) {
                // 1. Trending Cravings (Dishes & Drinks Carousel)
                if (trendingDishes.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = "🔥 Today's Trending Cravings",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink950
                                )
                                Text(
                                    text = "Popular dishes & artisan drinks nearby",
                                    fontSize = 12.sp,
                                    color = Ink500
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(trendingDishes) { item ->
                                    TrendingDishCard(
                                        item = item,
                                        onClick = { onNavigateToRestaurantDetail(item.restaurant) },
                                        onAddToCart = { dish, restaurant ->
                                            FoodEatsRepository.addToCart(dish, restaurant)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Curated Group: ⚡ Fastest Delivery
                if (fastDeliverySpots.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 20.dp)) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = "⚡ Fastest Delivery",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink950
                                )
                                Text(
                                    text = "At your doorstep in 20 minutes or less",
                                    fontSize = 12.sp,
                                    color = Ink500
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(fastDeliverySpots) { spot ->
                                    CompactRestaurantCard(
                                        restaurant = spot,
                                        onClick = { onNavigateToRestaurantDetail(spot) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. All Restaurants Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🍽️ All Restaurants (${restaurants.size})",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink950
                            )
                            Text(
                                text = "Explore full menus and curated flavors",
                                fontSize = 12.sp,
                                color = Ink500
                            )
                        }
                        IconButton(onClick = { FoodEatsRepository.loadFromBackend(search = null, cuisine = null) }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Forest600,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // All Restaurants Vertical List
                items(restaurants) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        onClick = { onNavigateToRestaurantDetail(restaurant) }
                    )
                }

            } else {
                // Search or Category Filter Active
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "Search Results for \"$searchQuery\" (${filteredRestaurants.size})"
                            else "$selectedCategory Spots (${filteredRestaurants.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink950
                        )
                    }
                }

                if (filteredRestaurants.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔍", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No restaurants or dishes found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Ink950
                            )
                            Text(
                                "Try searching for a different keyword or cuisine",
                                fontSize = 13.sp,
                                color = Ink500
                            )
                        }
                    }
                } else {
                    items(filteredRestaurants) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            onClick = { onNavigateToRestaurantDetail(restaurant) }
                        )
                    }
                }
            }

            // Database Sync / Status Footer (Always visible at the bottom)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (serverStatus.contains("Live")) Mint100 else Cream200,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (serverStatus.contains("Live")) Forest500 else RatingGold)
                            )
                            Text(
                                text = serverStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (serverStatus.contains("Live")) Forest800 else Ink700
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    val favoriteIds by FoodEatsRepository.favoriteRestaurantIds.collectAsState()
    val isFav = favoriteIds.contains(restaurant.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Restaurant Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = restaurant.imageUrl,
                    contentDescription = restaurant.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Rating & Delivery Time Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(text = "⭐", fontSize = 11.sp)
                                Text(
                                    text = restaurant.rating.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Ink950
                                )
                            }
                        }

                        // Heart Favorite Toggle
                        Surface(
                            color = Color.White.copy(alpha = 0.95f),
                            shape = CircleShape,
                            modifier = Modifier.clickable {
                                FoodEatsRepository.toggleFavorite(restaurant.id)
                            }
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) Rust500 else Ink600,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }

            // Restaurant Info
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = restaurant.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink950
                )

                Text(
                    text = restaurant.description,
                    fontSize = 12.sp,
                    color = Ink500,
                    maxLines = 2,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = Forest600,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = restaurant.deliveryTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink800
                        )
                        Text(text = "•", color = Ink300)
                        Text(
                            text = "$${String.format(java.util.Locale.US, "%.2f", restaurant.deliveryFee)} Delivery",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Forest700
                        )
                    }

                    Text(
                        text = restaurant.distance,
                        fontSize = 12.sp,
                        color = Ink400,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun TrendingDishCard(
    item: FeaturedDish,
    onClick: () -> Unit,
    onAddToCart: (DishItem, Restaurant) -> Unit
){
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ){
        Column {
            // food image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)

            ) {
                AsyncImage(
                    model = item.dish.imageUrl,
                    contentDescription = item.dish.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Category Tag
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = item.dish.category,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                    )
                }
            }

            // Food & Merchant Details
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.dish.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.restaurant.name,
                    fontSize = 11.sp,
                    color = Ink500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", item.dish.price)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Forest700
                    )

                    // Quick add to bag button
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Forest500)
                            .clickable { onAddToCart(item.dish, item.restaurant) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactRestaurantCard(
    restaurant: Restaurant,
    onClick:() -> Unit
){
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ){
        Column{
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
            ){
                AsyncImage(
                    model = restaurant.imageUrl,
                    contentDescription = restaurant.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                ){
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically ,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ){
                        Text(text = "⭐", fontSize = 10.sp)
                        Text(
                            text = restaurant.rating.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink950
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ){
                Text(
                    text = restaurant.name ,
                    fontSize = 14.sp ,
                    fontWeight = FontWeight.Bold,
                    color = Ink950,
                    maxLines = 1 ,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = restaurant.deliveryTime ,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Forest700
                    )
                    Text(text = "•", fontSize = 11.sp, color = Ink300)
                    Text(
                        text = restaurant.cuisine,
                        fontSize = 11.sp,
                        color = Ink500 ,
                        maxLines = 1 ,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
