package com.example.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.UserAddressEntity
import com.example.data.repository.CartItem
import com.example.data.repository.FoodEatsRepository
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingBagScreen(
    onNavigateBack: () -> Unit,
    onStartShopping: () -> Unit,
    onOrderPlaced: () -> Unit = {},
    authViewModel: AuthViewModel? = null
) {
    val cart by FoodEatsRepository.cart.collectAsState()
    val authState by authViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(AuthUiState()) }
    val authToken = authState.activeSession?.jwtToken
    val customerName = authState.currentUser?.fullName ?: "Customer"
    val defaultAddress = authState.userAddresses.firstOrNull { it.isDefault } ?: authState.userAddresses.firstOrNull()
    var selectedPaymentMethod by rememberSaveable { mutableStateOf("MOCK_KHQR") }
    var selectedAddress by rememberSaveable { mutableStateOf(defaultAddress?.let(::checkoutAddress) ?: "") }
    var addressEdited by rememberSaveable { mutableStateOf(false) }
    var isPlacingOrder by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var orderError by remember { mutableStateOf<String?>(null) }
    var promoCodeInput by rememberSaveable { mutableStateOf("") }
    var promoFeedback by remember { mutableStateOf<String?>(null) }
    var promoIsError by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val appliedVoucher by FoodEatsRepository.appliedVoucher.collectAsState()

    LaunchedEffect(defaultAddress) {
        if (!addressEdited && defaultAddress != null) selectedAddress = checkoutAddress(defaultAddress)
    }

    val merchant = cart.firstOrNull()?.merchant
    val itemCount = cart.sumOf { it.quantity }
    val subtotal = cart.sumOf { it.totalPrice }
    val baseDeliveryFee = merchant?.deliveryFee ?: 1.50
    val deliveryFee = if (appliedVoucher?.isFreeDelivery == true) 0.0 else baseDeliveryFee
    val discount = when {
        appliedVoucher == null -> 0.0
        appliedVoucher!!.isFreeDelivery -> 0.0
        appliedVoucher!!.discountPercent > 0.0 -> subtotal * appliedVoucher!!.discountPercent
        appliedVoucher!!.discountAmount > 0.0 -> appliedVoucher!!.discountAmount.coerceAtMost(subtotal)
        else -> 0.0
    }
    val grandTotal = if (cart.isNotEmpty()) (subtotal - discount + deliveryFee).coerceAtLeast(0.0) else 0.0

    Scaffold(
        containerColor = Cream50,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Your bag", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cart.isNotEmpty()) TextButton(onClick = { showClearConfirmation = true }, enabled = !isPlacingOrder) {
                        Text("Clear bag", color = Rust500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream50),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Surface(color = Color.White, shadowElevation = 8.dp) {
                    Column(Modifier.fillMaxWidth().imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Total", fontSize = 14.sp, color = Ink600)
                                Text("$itemCount ${if (itemCount == 1) "item" else "items"} · Delivery included", fontSize = 12.sp, color = Ink500)
                            }
                            Text(checkoutMoney(grandTotal), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink950)
                        }
                        orderError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
                        if (selectedAddress.isBlank()) Text("Enter a delivery address to continue.", color = Ink600, fontSize = 12.sp)
                        Button(
                            onClick = {
                                keyboard?.hide()
                                isPlacingOrder = true
                                orderError = null
                                try {
                                    FoodEatsRepository.placeOrder(
                                        customerName = customerName,
                                        deliveryAddress = selectedAddress.trim(),
                                        paymentMethod = if (selectedPaymentMethod == "MOCK_KHQR") "KHQR / Bakong" else if (selectedPaymentMethod == "CARD") "Credit Card" else "Cash on Delivery",
                                        authToken = authToken
                                    )
                                    onOrderPlaced()
                                } catch (_: IllegalStateException) {
                                    orderError = "We couldn't place this order. Review your bag and try again."
                                } finally {
                                    isPlacingOrder = false
                                }
                            },
                            enabled = !isPlacingOrder && selectedAddress.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Forest600)
                        ) {
                            if (isPlacingOrder) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("Placing your order…", fontWeight = FontWeight.Bold)
                            } else {
                                Text("Place order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(Modifier.width(10.dp))
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cart.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(shape = CircleShape, color = Forest50) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Forest600, modifier = Modifier.padding(26.dp).size(48.dp))
                }
                Spacer(Modifier.height(24.dp))
                Text("Good food starts here", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink950, textAlign = TextAlign.Center)
                Text("Your bag is empty. Explore the restaurants and add something you'll love.", color = Ink600, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp, bottom = 24.dp))
                Button(onClick = onStartShopping, modifier = Modifier.heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                    Text("Explore restaurants", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                merchant?.let { restaurant ->
                    item {
                        Surface(color = Forest800, shape = RoundedCornerShape(20.dp)) {
                            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Mint100, modifier = Modifier.size(28.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Your order from", fontSize = 12.sp, color = Mint100)
                                    Text(restaurant.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Estimated delivery · ${restaurant.deliveryTime}", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                                }
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Review your items", Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink950)
                        TextButton(onClick = onStartShopping) { Text("Add more") }
                    }
                }
                itemsIndexed(cart) { index, item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { FoodEatsRepository.updateQuantity(index, 1) },
                        onDecrease = { FoodEatsRepository.updateQuantity(index, -1) },
                        onRemove = { FoodEatsRepository.removeFromCart(index) }
                    )
                }
                item {
                    CheckoutSection("Delivery address", "Where should we bring your order?") {
                        if (authState.userAddresses.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(authState.userAddresses) { address ->
                                    val fullAddress = checkoutAddress(address)
                                    FilterChip(
                                        selected = selectedAddress == fullAddress,
                                        onClick = { selectedAddress = fullAddress; addressEdited = true },
                                        label = { Text(address.label) },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = selectedAddress,
                            onValueChange = { selectedAddress = it; addressEdited = true; orderError = null },
                            label = { Text("Full delivery address") },
                            placeholder = { Text("Building, street, area and city") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            supportingText = { Text(if (addressEdited && selectedAddress.isBlank()) "Enter an address to place your order." else "Include a building or apartment number, if needed.") },
                            isError = addressEdited && selectedAddress.isBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                    }
                }
                item {
                    CheckoutSection("Payment method", "Choose a method for this order") {
                        PaymentMethodTile("KHQR / Bakong", "Cambodian banking apps", "KHQR", selectedPaymentMethod == "MOCK_KHQR", Rust500) { selectedPaymentMethod = "MOCK_KHQR" }
                        PaymentMethodTile("Credit or debit card", "Visa, Mastercard, UnionPay", "CARD", selectedPaymentMethod == "CARD", Forest600) { selectedPaymentMethod = "CARD" }
                        PaymentMethodTile("Cash on delivery", "Pay when your order arrives", "CASH", selectedPaymentMethod == "CASH", Ink600) { selectedPaymentMethod = "CASH" }
                        Surface(color = Cream100, shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Ink500, modifier = Modifier.size(18.dp))
                                Text("Your payment choice is recorded with the order. No payment is collected on this screen.", fontSize = 12.sp, color = Ink600)
                            }
                        }
                    }
                }
                item {
                    CheckoutSection("Have a promo code?") {
                        val voucher = appliedVoucher
                        if (voucher != null) {
                            Surface(color = Forest50, shape = RoundedCornerShape(14.dp)) {
                                Row(Modifier.fillMaxWidth().padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Forest600, modifier = Modifier.size(20.dp))
                                    Column(Modifier.weight(1f).padding(10.dp)) {
                                        Text("${voucher.code} applied", fontWeight = FontWeight.Bold, color = Forest700)
                                        Text(voucher.title, fontSize = 12.sp, color = Ink600)
                                    }
                                    IconButton(onClick = { FoodEatsRepository.removeAppliedVoucher(); promoFeedback = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove promo code", tint = Forest700)
                                    }
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = promoCodeInput,
                                    onValueChange = { promoCodeInput = it.uppercase(Locale.ROOT); promoFeedback = null },
                                    label = { Text("Promo code") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    isError = promoIsError && promoFeedback != null,
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                                )
                                FilledTonalButton(
                                    onClick = {
                                        keyboard?.hide()
                                        val result = FoodEatsRepository.applyVoucherCode(promoCodeInput.trim(), subtotal)
                                        promoFeedback = result.second
                                        promoIsError = !result.first
                                        if (result.first) promoCodeInput = ""
                                    },
                                    enabled = promoCodeInput.isNotBlank(),
                                    modifier = Modifier.heightIn(min = 52.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) { Text("Apply", fontWeight = FontWeight.Bold) }
                            }
                        }
                        promoFeedback?.let { Text(it, fontSize = 12.sp, color = if (promoIsError) MaterialTheme.colorScheme.error else Forest700) }
                    }
                }
                item {
                    CheckoutSection("Order summary") {
                        SummaryLine("Subtotal ($itemCount ${if (itemCount == 1) "item" else "items"})", checkoutMoney(subtotal))
                        if (discount > 0) SummaryLine("Discount · ${appliedVoucher?.code}", "−${checkoutMoney(discount)}", highlight = true)
                        SummaryLine("Delivery", if (appliedVoucher?.isFreeDelivery == true) "Free" else checkoutMoney(deliveryFee), highlight = appliedVoucher?.isFreeDelivery == true)
                        HorizontalDivider(color = Sage100, modifier = Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink950)
                            Text(checkoutMoney(grandTotal), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Forest700)
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
            title = { Text("Clear your bag?") },
            text = { Text("This removes all $itemCount ${if (itemCount == 1) "item" else "items"} from your order.") },
            confirmButton = { TextButton(onClick = { FoodEatsRepository.clearCart(); showClearConfirmation = false }) { Text("Clear bag", color = Rust500) } },
            dismissButton = { TextButton(onClick = { showClearConfirmation = false }) { Text("Keep items") } }
        )
    }
}

@Composable
private fun CheckoutSection(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Sage100)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink950)
                subtitle?.let { Text(it, fontSize = 13.sp, color = Ink500) }
            }
            content()
        }
    }
}

@Composable
private fun SummaryLine(label: String, amount: String, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp, color = if (highlight) Forest700 else Ink600)
        Text(amount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (highlight) Forest700 else Ink950)
    }
}

@Composable
fun CartItemRow(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Sage100)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                AsyncImage(
                    model = item.dish.imageUrl, contentDescription = null,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)).background(Sage100),
                    contentScale = ContentScale.Crop
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.dish.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink950)
                    if (item.selectedOptions.isNotEmpty()) Text(item.selectedOptions.joinToString(" · ") { it.optionName }, fontSize = 12.sp, color = Ink600)
                    Text("${checkoutMoney(item.unitPrice)} each", fontSize = 13.sp, color = Ink500)
                }
                Text(checkoutMoney(item.totalPrice), fontWeight = FontWeight.Bold, color = Forest700, fontSize = 16.sp)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onRemove) { Text("Remove", color = Ink600, fontSize = 13.sp) }
                Surface(shape = RoundedCornerShape(14.dp), color = Forest50) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDecrease, modifier = Modifier.size(48.dp)) {
                            Icon(if (item.quantity == 1) Icons.Default.DeleteOutline else Icons.Default.Remove, contentDescription = if (item.quantity == 1) "Remove ${item.dish.name}" else "Decrease ${item.dish.name} quantity", tint = Forest700)
                        }
                        Text(item.quantity.toString(), modifier = Modifier.widthIn(min = 28.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Ink950)
                        IconButton(onClick = onIncrease, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Increase ${item.dish.name} quantity", tint = Forest700)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodTile(title: String, subtitle: String, badgeText: String, isSelected: Boolean, badgeColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        color = if (isSelected) Forest50 else Color.White,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Forest600 else Sage200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(color = badgeColor, shape = RoundedCornerShape(8.dp)) {
                Text(badgeText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink950)
                Text(subtitle, fontSize = 12.sp, color = Ink600)
            }
            RadioButton(selected = isSelected, onClick = null)
        }
    }
}

private fun checkoutMoney(amount: Double) = "$${String.format(Locale.US, "%.2f", amount)}"

private fun checkoutAddress(address: UserAddressEntity) =
    listOf(address.streetAddress, address.apartment, address.city).filter { it.isNotBlank() }.joinToString(", ")
