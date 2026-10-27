package com.example.ui.restaurant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.repository.DishItem
import com.example.data.repository.DishOption
import com.example.data.repository.FoodEatsRepository
import com.example.data.repository.Restaurant
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val cart by FoodEatsRepository.cart.collectAsState()
    var selectedCategory by rememberSaveable(restaurant.id) { mutableStateOf("All") }
    var searchQuery by rememberSaveable(restaurant.id) { mutableStateOf("") }
    var itemForCustomization by remember { mutableStateOf<DishItem?>(null) }
    var pendingDish by remember { mutableStateOf<Pair<DishItem, List<DishOption>>?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val categories = remember(restaurant.menu) { listOf("All") + restaurant.menu.map { it.category }.distinct() }
    val filteredMenu = remember(selectedCategory, searchQuery, restaurant.menu) {
        restaurant.menu.filter { dish ->
            (selectedCategory == "All" || dish.category == selectedCategory) &&
                (searchQuery.isBlank() || dish.name.contains(searchQuery.trim(), ignoreCase = true) ||
                    dish.description.contains(searchQuery.trim(), ignoreCase = true))
        }
    }
    val cartCount = cart.sumOf { it.quantity }
    val cartSubtotal = cart.sumOf { it.totalPrice }

    fun addDish(dish: DishItem, options: List<DishOption>) {
        if (FoodEatsRepository.addToCart(dish, restaurant, options)) {
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar("${dish.name} added to your bag")
            }
        } else {
            pendingDish = dish to options
        }
    }

    Scaffold(
        containerColor = Cream50,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Restaurant menu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to restaurants")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCart) {
                        BadgedBox(badge = { if (cartCount > 0) Badge { Text(cartCount.toString()) } }) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = "Shopping bag, $cartCount items")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream50),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        bottomBar = {
            if (cartCount > 0) {
                Surface(color = Color.White, shadowElevation = 8.dp) {
                    Button(
                        onClick = onNavigateToCart,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Forest600),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("View bag", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("$cartCount ${if (cartCount == 1) "item" else "items"}", fontSize = 12.sp)
                        }
                        Text(menuMoney(cartSubtotal), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(24.dp))) {
                    AsyncImage(
                        model = restaurant.bannerUrl.ifEmpty { restaurant.imageUrl },
                        contentDescription = null,
                        modifier = Modifier.matchParentSize().background(Forest800),
                        contentScale = ContentScale.Crop
                    )
                    Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Forest900.copy(alpha = 0.95f)))))
                    Column(Modifier.padding(20.dp).padding(top = 64.dp)) {
                        Text(restaurant.cuisine, color = Mint100, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(restaurant.name, color = Color.White, fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
                        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD479), modifier = Modifier.size(18.dp))
                            Text(" ${restaurant.rating} · ${restaurant.reviewsCount} reviews", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
            item {
                Surface(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MenuInfo("Delivery", restaurant.deliveryTime, Modifier.weight(1f))
                            MenuInfo("Delivery fee", menuMoney(restaurant.deliveryFee), Modifier.weight(1f))
                            MenuInfo("Hours", restaurant.openingHours, Modifier.weight(1f))
                        }
                        if (restaurant.description.isNotBlank()) {
                            HorizontalDivider(color = Sage100)
                            Text(restaurant.description, style = MaterialTheme.typography.bodyMedium, color = Ink600)
                        }
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Forest600, modifier = Modifier.size(18.dp))
                            Text(restaurant.address, style = MaterialTheme.typography.bodySmall, color = Ink600)
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Find something on the menu") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear menu search")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Sage200, focusedBorderColor = Forest600, unfocusedContainerColor = Color.White)
                )
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Forest600, selectedLabelColor = Color.White, containerColor = Color.White)
                        )
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selectedCategory == "All") "Explore the menu" else selectedCategory, Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink950)
                    Text("${filteredMenu.size} ${if (filteredMenu.size == 1) "dish" else "dishes"}", fontSize = 13.sp, color = Ink500)
                }
            }
            if (filteredMenu.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Forest600, modifier = Modifier.size(36.dp))
                        Text("No dishes found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Try another dish or category.", color = Ink600)
                        TextButton(onClick = { searchQuery = ""; selectedCategory = "All" }) { Text("Show all dishes") }
                    }
                }
            }
            items(filteredMenu, key = { it.id }) { dish ->
                DishItemCard(dish = dish, onAddClick = {
                    if (dish.options.isNotEmpty()) itemForCustomization = dish else addDish(dish, emptyList())
                })
            }
        }
    }

    itemForCustomization?.let { dish ->
        DishCustomizationDialog(dish, onDismiss = { itemForCustomization = null }, onConfirm = { options ->
            itemForCustomization = null
            addDish(dish, options)
        })
    }
    pendingDish?.let { (dish, options) ->
        AlertDialog(
            onDismissRequest = { pendingDish = null },
            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
            title = { Text("Start a new bag?") },
            text = { Text("Your bag has items from another restaurant. Adding ${dish.name} will replace those items with a new order from ${restaurant.name}.") },
            confirmButton = {
                TextButton(onClick = {
                    FoodEatsRepository.replaceCartAndAdd(dish, restaurant, options)
                    pendingDish = null
                }) { Text("Replace bag", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { pendingDish = null }) { Text("Keep current bag") } }
        )
    }
}

@Composable
private fun MenuInfo(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = Ink500)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink950)
    }
}

@Composable
fun DishItemCard(dish: DishItem, onAddClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Sage100)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    dish.dietaryTag?.takeIf { it.isNotBlank() }?.let { tag ->
                        Text(tag, color = Forest700, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(dish.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink950)
                    Text(dish.description, style = MaterialTheme.typography.bodyMedium, color = Ink600)
                    Text("${dish.prepTimeMinutes} min preparation", fontSize = 12.sp, color = Ink500)
                }
                AsyncImage(
                    model = dish.imageUrl, contentDescription = null,
                    modifier = Modifier.size(88.dp).clip(RoundedCornerShape(16.dp)).background(Sage100),
                    contentScale = ContentScale.Crop
                )
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(menuMoney(dish.price), fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Forest700)
                FilledTonalButton(
                    onClick = onAddClick,
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Forest50, contentColor = Forest700),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (dish.options.isNotEmpty()) "Choose options" else "Add to bag", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DishCustomizationDialog(dish: DishItem, onDismiss: () -> Unit, onConfirm: (List<DishOption>) -> Unit) {
    val selectedOptions = remember(dish.id) { mutableStateListOf<DishOption>().apply { addAll(dish.options.filter { it.isDefault }) } }
    val groupedOptions = remember(dish.options) { dish.options.groupBy { it.optionGroup } }
    val currentTotal = dish.price + selectedOptions.sumOf { it.priceAdjustment }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.padding(16.dp).widthIn(max = 560.dp).fillMaxWidth().heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp), color = Cream50
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Make it yours", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Forest700)
                        Text(dish.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink950)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close options") }
                }
                Text("Choose your options below.", color = Ink600, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp, bottom = 16.dp))
                HorizontalDivider(color = Sage200)
                LazyColumn(Modifier.weight(1f, fill = false).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    groupedOptions.forEach { (group, options) ->
                        val isSingleChoice = group.contains("Size", true) || group.contains("Level", true) || group.contains("Heat", true) || group.contains("Firmness", true)
                        item {
                            Column(Modifier.padding(top = 12.dp, bottom = 6.dp)) {
                                Text(group, fontWeight = FontWeight.Bold, color = Ink950, fontSize = 16.sp)
                                Text(if (isSingleChoice) "Choose one" else "Choose any you like", color = Ink500, fontSize = 12.sp)
                            }
                        }
                        items(options, key = { it.id }) { option ->
                            val isSelected = selectedOptions.any { it.id == option.id }
                            val onSelect: () -> Unit = {
                                if (isSingleChoice) {
                                    selectedOptions.removeAll { it.optionGroup == group }
                                    selectedOptions.add(option)
                                } else if (isSelected) selectedOptions.removeAll { it.id == option.id }
                                else selectedOptions.add(option)
                            }
                            val selectionModifier = if (isSingleChoice) Modifier.selectable(isSelected, role = Role.RadioButton, onClick = onSelect)
                            else Modifier.toggleable(isSelected, role = Role.Checkbox, onValueChange = { onSelect() })
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Forest50 else Color.White)
                                    .then(selectionModifier).heightIn(min = 52.dp).padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (isSingleChoice) RadioButton(selected = isSelected, onClick = null)
                                else Checkbox(checked = isSelected, onCheckedChange = null)
                                Text(option.optionName, Modifier.weight(1f), fontSize = 14.sp, color = Ink950)
                                Text(
                                    if (option.priceAdjustment > 0) "+${menuMoney(option.priceAdjustment)}" else if (option.priceAdjustment < 0) "−${menuMoney(-option.priceAdjustment)}" else "Included",
                                    fontSize = 12.sp, color = Forest700, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = Sage200, modifier = Modifier.padding(vertical = 12.dp))
                Button(
                    onClick = { onConfirm(selectedOptions.toList()) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Forest600),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Add to bag", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(menuMoney(currentTotal), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun menuMoney(amount: Double) = "$${String.format(Locale.US, "%.2f", amount)}"
