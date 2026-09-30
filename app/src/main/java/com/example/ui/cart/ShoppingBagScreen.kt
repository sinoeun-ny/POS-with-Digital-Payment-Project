package com.example.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.repository.CartItem
import com.example.data.repository.FoodEatsRepository
import com.example.ui.theme.*
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingBagScreen(
    onNavigateBack: () -> Unit,
    onStartShopping: () -> Unit,
    onOrderPlaced: () -> Unit = {},
    authViewModel: AuthViewModel? = null
) {
    val cart by FoodEatsRepository.cart.collectAsState()
    val authState by authViewModel?.uiState?.collectAsState() ?: remember {mutableStateOf(AuthUiState())}
    val authToken = authState.activeSession?.jwtToken
    val customerName = authState.currentUser?.fullName ?: "Sinoeun Customer"
    var selectedPaymentMethod by remember { mutableStateOf("MOCK_KHQR") }
    var selectedAddress by remember { mutableStateOf("Building 42, St. 302, BKK1, Phnom Penh") }
    var isPlacingOrder by remember { mutableStateOf(false) }

    val merchant = cart.firstOrNull()?.merchant
    val subtotal = cart.sumOf { it.totalPrice }
    val deliveryFee = merchant?.deliveryFee ?: 1.50
    val grandTotal = if (cart.isNotEmpty()) subtotal + deliveryFee else 0.0

    Scaffold(
        containerColor = Cream50,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Cart & Checkout",
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = Ink950
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink950
                        )
                    }
                },
                actions = {
                    if (cart.isNotEmpty()) {
                        TextButton(onClick = { FoodEatsRepository.clearCart() }) {
                            Text("Clear", color = Rust500, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Surface(
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grand Total",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink700
                            )
                            Text(
                                text = "$${String.format(java.util.Locale.US, "%.2f", grandTotal)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Ink950
                            )
                        }

                        Button(
                            onClick = {
                                isPlacingOrder = true
                                FoodEatsRepository.placeOrder(
                                    customerName = customerName ,
                                    deliveryAddress = selectedAddress,
                                    paymentMethod = if (selectedPaymentMethod == "Mock_KHQR")
                                    "KHQR / Bakong" else if (selectedPaymentMethod == "CARD") "Credit Card"
                                    else "Cash on Delivery" ,
                                    authToken = authToken //the place the jwt token pass

                                )
                                isPlacingOrder = false
                                onOrderPlaced()
                            },
                            enabled = !isPlacingOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isPlacingOrder) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Pay",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedPaymentMethod == "MOCK_KHQR") "Confirm KHQR & Place Order" else "Place Order",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cart.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Mint100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Empty Bag",
                            tint = Forest600,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "Your bag is empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink950
                    )
                    Text(
                        text = "Explore restaurants and pick your favorite burgers, ramen, or specialty coffee!",
                        fontSize = 13.sp,
                        color = Ink500,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onStartShopping,
                        colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start Exploring")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Merchant Banner
                merchant?.let { m ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Forest50),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Forest500),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = "Merchant",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = m.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Forest900
                                    )
                                    Text(
                                        text = "Estimated delivery: ${m.deliveryTime} • $${String.format(java.util.Locale.US, "%.2f", m.deliveryFee)} Fee",
                                        fontSize = 11.sp,
                                        color = Forest700
                                    )
                                }
                            }
                        }
                    }
                }

                // Ordered Items
                itemsIndexed(cart) { index, item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { FoodEatsRepository.updateQuantity(index, 1) },
                        onDecrease = { FoodEatsRepository.updateQuantity(index, -1) },
                        onRemove = { FoodEatsRepository.removeFromCart(index) }
                    )
                }

                // Delivery Address Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DELIVERY ADDRESS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Ink400,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Home (BKK1)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest600
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Pin",
                                    tint = Forest500,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = selectedAddress,
                                    fontSize = 13.sp,
                                    color = Ink800,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Payment Method Selector (KHQR Bakong, Card, COD)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "PAYMENT METHOD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Ink400,
                                letterSpacing = 1.sp
                            )

                            // KHQR / Bakong Option
                            PaymentMethodTile(
                                title = "KHQR / Bakong Payment",
                                subtitle = "Scan with Bakong or any Cambodian Banking App",
                                badgeText = "KHQR",
                                isSelected = selectedPaymentMethod == "MOCK_KHQR",
                                badgeColor = Rust500,
                                onClick = { selectedPaymentMethod = "MOCK_KHQR" }
                            )

                            // Simulated KHQR QR Code Preview when selected
                            if (selectedPaymentMethod == "MOCK_KHQR") {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    color = Color(0xFFFFF7F5),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Rust500.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = Rust500,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "KHQR",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        // Simulated QR Code Graphic
                                        Box(
                                            modifier = Modifier
                                                .size(130.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .border(2.dp, Ink900, RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCode2,
                                                contentDescription = "KHQR Pattern",
                                                tint = Ink950,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Text(
                                            text = "Amount: $${String.format(java.util.Locale.US, "%.2f", grandTotal)} USD",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Ink950
                                        )
                                        Text(
                                            text = "Merchant: ${merchant?.name ?: "FoodEats Partner"}",
                                            fontSize = 11.sp,
                                            color = Ink500
                                        )
                                    }
                                }
                            }

                            // Credit / Debit Card Option
                            PaymentMethodTile(
                                title = "Credit / Debit Card",
                                subtitle = "Visa, Mastercard, UnionPay",
                                badgeText = "CARD",
                                isSelected = selectedPaymentMethod == "CARD",
                                badgeColor = Forest500,
                                onClick = { selectedPaymentMethod = "CARD" }
                            )

                            // Cash on Delivery Option
                            PaymentMethodTile(
                                title = "Cash on Delivery",
                                subtitle = "Pay courier cash upon receiving meal",
                                badgeText = "CASH",
                                isSelected = selectedPaymentMethod == "CASH",
                                badgeColor = Ink600,
                                onClick = { selectedPaymentMethod = "CASH" }
                            )
                        }
                    }
                }

                // Cost Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ORDER SUMMARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Ink400,
                                letterSpacing = 1.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Subtotal", fontSize = 13.sp, color = Ink700)
                                Text(text = "$${String.format(java.util.Locale.US, "%.2f", subtotal)}", fontSize = 13.sp, color = Ink950, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Delivery Fee", fontSize = 13.sp, color = Ink700)
                                Text(text = "$${String.format(java.util.Locale.US, "%.2f", deliveryFee)}", fontSize = 13.sp, color = Forest600, fontWeight = FontWeight.SemiBold)
                            }
                            HorizontalDivider(color = Sage100, modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Total to Pay", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink950)
                                Text(text = "$${String.format(java.util.Locale.US, "%.2f", grandTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Forest700)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            AsyncImage(
                model = item.dish.imageUrl,
                contentDescription = item.dish.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.dish.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink950
                )

                if (item.selectedOptions.isNotEmpty()) {
                    Text(
                        text = item.selectedOptions.joinToString(", ") { it.optionName },
                        fontSize = 11.sp,
                        color = Ink500,
                        maxLines = 1
                    )
                }

                Text(
                    text = "$${String.format(java.util.Locale.US, "%.2f", item.unitPrice)} each",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Forest600,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Ink50)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = Ink800,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = item.quantity.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink950,
                    modifier = Modifier.widthIn(min = 18.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Forest500)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodTile(
    title: String,
    subtitle: String,
    badgeText: String,
    isSelected: Boolean,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Forest50 else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Forest500 else Sage200
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink950
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Ink500
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = Forest500)
            )
        }
    }
}
