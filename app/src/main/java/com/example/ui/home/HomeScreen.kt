package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.repository.DishItem
import com.example.data.repository.DishOption
import com.example.data.repository.FoodEatsRepository
import com.example.data.repository.Restaurant
import com.example.ui.restaurant.DishCustomizationDialog
import com.example.ui.theme.*
import java.util.Locale
import kotlinx.coroutines.launch

data class FeaturedDish(val dish: DishItem, val restaurant: Restaurant)

@Composable
fun HomeScreen(
    onNavigateToBag: () -> Unit,
    onNavigateToRestaurantDetail: (Restaurant) -> Unit
) {
    val restaurants by FoodEatsRepository.restaurants.collectAsState()
    val cart by FoodEatsRepository.cart.collectAsState()
    val serverStatus by FoodEatsRepository.serverStatus.collectAsState()
    val isLoading by FoodEatsRepository.isLoading.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var itemForCustomization by remember { mutableStateOf<FeaturedDish?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val categories = remember(restaurants) {
        listOf("All") + restaurants.map { it.category.ifBlank { it.cuisine }.trim() }
            .filter { it.isNotBlank() && it != "All" }.distinct()
    }
    val query = searchQuery.trim()
    val isDiscoverMode = query.isEmpty() && selectedCategory == "All"
    val isOffline = serverStatus.contains("Offline", ignoreCase = true)

    // Startup loads the catalog in MainActivity. Returning to this screen keeps it intact.
    LaunchedEffect(categories, isLoading) {
        if (!isLoading && selectedCategory !in categories) selectedCategory = "All"
    }

    val filteredRestaurants = remember(restaurants, selectedCategory, query) {
        restaurants.filter { restaurant ->
            val matchesCategory = selectedCategory == "All" ||
                restaurant.category.contains(selectedCategory, ignoreCase = true) ||
                restaurant.cuisine.contains(selectedCategory, ignoreCase = true)
            val matchesSearch = query.isEmpty() ||
                restaurant.name.contains(query, ignoreCase = true) ||
                restaurant.cuisine.contains(query, ignoreCase = true) ||
                restaurant.category.contains(query, ignoreCase = true) ||
                restaurant.menu.any { it.name.contains(query, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }
    val featuredDishes = remember(restaurants) {
        buildList {
            val largestMenu = restaurants.maxOfOrNull { it.menu.size } ?: 0
            for (index in 0 until largestMenu) {
                restaurants.forEach { restaurant ->
                    restaurant.menu.getOrNull(index)?.let { add(FeaturedDish(it, restaurant)) }
                }
            }
        }.take(10)
    }
    val quickDeliverySpots = remember(restaurants) {
        restaurants.filter { restaurant ->
            val minutes = Regex("\\d+").findAll(restaurant.deliveryTime)
                .mapNotNull { it.value.toIntOrNull() }.toList()
            minutes.isNotEmpty() && minutes.max() <= 20
        }
    }
    val cartCount = cart.sumOf { it.quantity }
    val refreshCatalog = { FoodEatsRepository.loadFromBackend(search = null, cuisine = null) }
    val resetFilters = {
        searchQuery = ""
        selectedCategory = "All"
        focusManager.clearFocus()
    }

    fun addFeaturedDish(dish: DishItem, restaurant: Restaurant, options: List<DishOption> = emptyList()) {
        val added = FoodEatsRepository.addToCart(dish, restaurant, options)
        val message = if (added) "${dish.name} added to your bag"
        else "Your bag has items from another restaurant. Review your bag to start a new order."
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            if (snackbarHostState.showSnackbar(message, actionLabel = "View bag", withDismissAction = true) == SnackbarResult.ActionPerformed) {
                onNavigateToBag()
            }
        }
    }

    Scaffold(
        containerColor = Cream50,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = Cream50) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(color = Forest700, shape = RoundedCornerShape(16.dp)) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(12.dp).size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, color = Ink950, fontWeight = FontWeight.Bold)
                            Text("Find your next favorite", style = MaterialTheme.typography.bodySmall, color = Ink500)
                        }
                        FilledTonalButton(
                            onClick = onNavigateToBag,
                            modifier = Modifier.heightIn(min = 48.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Forest100, contentColor = Forest800),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (cartCount > 0) "Bag ($cartCount)" else "Bag")
                        }
                    }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 12.dp),
                        placeholder = { Text("Search food or restaurants", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Forest700) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Ink500)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Forest600,
                            unfocusedBorderColor = Sage200,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Refreshing restaurants" },
                            color = Forest600,
                            trackColor = Forest100
                        )
                    } else {
                        HorizontalDivider(color = Sage100)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isOffline && !isLoading) {
                item(key = "offline") {
                    Surface(
                        modifier = Modifier.padding(horizontal = 20.dp).padding(top = 16.dp),
                        color = Cream100,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = Ink600, modifier = Modifier.size(22.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Couldn't refresh menus", style = MaterialTheme.typography.labelLarge, color = Ink950)
                                Text("Showing available menus. Details may have changed.", style = MaterialTheme.typography.bodySmall, color = Ink600)
                            }
                            TextButton(onClick = refreshCatalog, modifier = Modifier.heightIn(min = 48.dp)) { Text("Retry") }
                        }
                    }
                }
            }

            if (isDiscoverMode) {
                item(key = "welcome") {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = if (isOffline) 0.dp else 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Forest800
                    ) {
                        Column(
                            modifier = Modifier.background(Brush.linearGradient(listOf(Forest800, Forest600))).padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("A LITTLE INSPIRATION", style = MaterialTheme.typography.labelMedium, color = Mint200)
                            Text("What sounds good today?", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Explore menus, discover a new favorite, and make it your next meal.", style = MaterialTheme.typography.bodyMedium, color = Mint100)
                        }
                    }
                }
            }

            item(key = "categories") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it }) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category; focusManager.clearFocus() },
                            label = { Text(category) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            leadingIcon = if (selectedCategory == category) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = Ink700,
                                selectedContainerColor = Forest700,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }
            }

            if (isDiscoverMode && featuredDishes.isNotEmpty()) {
                item(key = "featured") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DiscoverSectionHeading("On the menu", "A taste of what's available")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(featuredDishes, key = { "${it.restaurant.id}-${it.dish.id}" }) { item ->
                                TrendingDishCard(
                                    item = item,
                                    onClick = { onNavigateToRestaurantDetail(item.restaurant) },
                                    onAddToCart = { dish, restaurant ->
                                        if (dish.options.isNotEmpty()) {
                                            itemForCustomization = FeaturedDish(dish, restaurant)
                                        } else {
                                            addFeaturedDish(dish, restaurant)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (isDiscoverMode && quickDeliverySpots.isNotEmpty()) {
                item(key = "quick-delivery") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DiscoverSectionHeading("Short on time?", "Estimated delivery of 20 minutes or less")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(quickDeliverySpots, key = { it.id }) { restaurant ->
                                CompactRestaurantCard(restaurant) { onNavigateToRestaurantDetail(restaurant) }
                            }
                        }
                    }
                }
            }

            item(key = "results-heading") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isDiscoverMode) "Explore restaurants" else if (query.isNotEmpty()) "Search results" else selectedCategory,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Ink950
                        )
                        Text(
                            text = if (isLoading) "Refreshing menus…"
                            else "${filteredRestaurants.size} ${if (filteredRestaurants.size == 1) "restaurant" else "restaurants"}${if (query.isNotEmpty()) " for “$query”" else " to explore"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink500,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isDiscoverMode) {
                        IconButton(onClick = refreshCatalog, enabled = !isLoading) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh restaurants", tint = if (isLoading) Ink300 else Forest700)
                        }
                    } else {
                        TextButton(onClick = resetFilters, modifier = Modifier.heightIn(min = 48.dp)) { Text("Reset") }
                    }
                }
            }

            if (filteredRestaurants.isEmpty()) {
                item(key = "empty") {
                    DiscoverEmptyState(
                        isLoading = isLoading,
                        isFiltered = !isDiscoverMode,
                        onAction = if (isDiscoverMode) refreshCatalog else resetFilters
                    )
                }
            } else {
                items(filteredRestaurants, key = { "restaurant-${it.id}" }) { restaurant ->
                    RestaurantCard(restaurant) { onNavigateToRestaurantDetail(restaurant) }
                }
            }
        }
    }

    itemForCustomization?.let { item ->
        DishCustomizationDialog(
            dish = item.dish,
            onDismiss = { itemForCustomization = null },
            onConfirm = { options ->
                itemForCustomization = null
                addFeaturedDish(item.dish, item.restaurant, options)
            }
        )
    }
}

@Composable
private fun DiscoverSectionHeading(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Ink950)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Ink500)
    }
}

@Composable
private fun DiscoverEmptyState(isLoading: Boolean, isFiltered: Boolean, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Forest600, modifier = Modifier.size(40.dp))
        } else {
            Surface(color = Forest100, shape = CircleShape) {
                Icon(if (isFiltered) Icons.Default.Search else Icons.Default.Restaurant, contentDescription = null, tint = Forest700, modifier = Modifier.padding(20.dp).size(32.dp))
            }
        }
        Text(
            if (isLoading) "Finding your next meal" else if (isFiltered) "No matches yet" else "Menus are on their way",
            style = MaterialTheme.typography.titleLarge,
            color = Ink950,
            textAlign = TextAlign.Center
        )
        Text(
            if (isLoading) "We're loading the available restaurants."
            else if (isFiltered) "Try a different dish or restaurant, or reset your filters to explore all menus."
            else "We couldn't load any restaurants. Try refreshing in a moment.",
            style = MaterialTheme.typography.bodyMedium,
            color = Ink500,
            textAlign = TextAlign.Center
        )
        if (!isLoading) {
            Button(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(if (isFiltered) "Reset filters" else "Try again")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    val favoriteIds by FoodEatsRepository.favoriteRestaurantIds.collectAsState()
    val isFavorite = restaurant.id in favoriteIds

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Sage200)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            DiscoverFoodImage(restaurant.imageUrl, Modifier.fillMaxSize())
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(color = Color.White.copy(alpha = 0.96f), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f, fill = false)) {
                    Text(restaurant.cuisine, style = MaterialTheme.typography.labelMedium, color = Forest800, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(10.dp))
                Surface(color = Color.White, shape = CircleShape) {
                    IconToggleButton(
                        checked = isFavorite,
                        onCheckedChange = { FoodEatsRepository.toggleFavorite(restaurant.id) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove ${restaurant.name} from favorites" else "Save ${restaurant.name} to favorites",
                            tint = if (isFavorite) Rust500 else Ink600
                        )
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(restaurant.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ink950)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = RatingGold, modifier = Modifier.size(18.dp))
                    Text(String.format(Locale.US, "%.1f", restaurant.rating), style = MaterialTheme.typography.labelLarge, color = Ink950)
                }
                Text("(${restaurant.reviewsCount} reviews)", style = MaterialTheme.typography.bodySmall, color = Ink500)
                if (restaurant.distance.isNotBlank()) {
                    Text("· ${restaurant.distance}", style = MaterialTheme.typography.bodySmall, color = Ink500)
                }
            }
            if (restaurant.description.isNotBlank()) {
                Text(restaurant.description, style = MaterialTheme.typography.bodyMedium, color = Ink500, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            HorizontalDivider(color = Sage100)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Forest700, modifier = Modifier.size(18.dp))
                    Text(restaurant.deliveryTime, style = MaterialTheme.typography.labelLarge, color = Ink700)
                }
                Text(
                    if (restaurant.deliveryFee == 0.0) "Free delivery" else "${money(restaurant.deliveryFee)} delivery",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = Forest700,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun TrendingDishCard(item: FeaturedDish, onClick: () -> Unit, onAddToCart: (DishItem, Restaurant) -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Sage200)
    ) {
        DiscoverFoodImage(item.dish.imageUrl, Modifier.fillMaxWidth().height(132.dp))
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.dish.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink950, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(item.restaurant.name, style = MaterialTheme.typography.bodySmall, color = Ink500, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(money(item.dish.price), style = MaterialTheme.typography.titleSmall, color = Forest700, fontWeight = FontWeight.Bold)
                FilledTonalButton(
                    onClick = { onAddToCart(item.dish, item.restaurant) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics {
                        contentDescription = if (item.dish.options.isNotEmpty()) "Choose options for ${item.dish.name}" else "Add ${item.dish.name} to bag"
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Forest100, contentColor = Forest800)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(if (item.dish.options.isNotEmpty()) "Options" else "Add", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun CompactRestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(240.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Sage200)
    ) {
        DiscoverFoodImage(restaurant.imageUrl, Modifier.fillMaxWidth().height(120.dp))
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(restaurant.name, style = MaterialTheme.typography.titleSmall, color = Ink950, fontWeight = FontWeight.Bold, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Forest700, modifier = Modifier.size(17.dp))
                Text(restaurant.deliveryTime, style = MaterialTheme.typography.labelLarge, color = Forest700)
                Text("·", color = Ink400)
                Icon(Icons.Default.Star, contentDescription = "Rating", tint = RatingGold, modifier = Modifier.size(17.dp))
                Text(String.format(Locale.US, "%.1f", restaurant.rating), style = MaterialTheme.typography.labelLarge, color = Ink700)
            }
        }
    }
}

@Composable
private fun DiscoverFoodImage(url: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Sage100), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Restaurant, contentDescription = null, tint = Sage400, modifier = Modifier.size(40.dp))
        AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}

private fun money(amount: Double): String = "$${String.format(Locale.US, "%.2f", amount)}"
