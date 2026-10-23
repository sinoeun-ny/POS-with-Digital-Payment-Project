package com.example.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FoodEatsRepository
import com.example.data.repository.PlacedOrder
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    authViewModel: AuthViewModel,
    onNavigateToDiscover: () -> Unit
) {
    val orders by FoodEatsRepository.orders.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "PAID", "COD_PENDING"
    var selectedOrderForReceipt by remember { mutableStateOf<PlacedOrder?>(null) }

    val filteredOrders = remember(orders, selectedFilter) {
        when (selectedFilter) {
            "PAID" -> orders.filter { it.paymentStatus == "CONFIRMED_PAID" || it.paymentStatus == "PAID" || it.status == "DELIVERED" }
            "COD_PENDING" -> orders.filter { it.paymentStatus != "CONFIRMED_PAID" && it.paymentStatus != "PAID" && it.status != "DELIVERED" }
            else -> orders
        }
    }

    val totalSpent = remember(orders) {
        orders.sumOf { it.total }
    }
    val paidCount = remember(orders) {
        orders.count { it.paymentStatus == "CONFIRMED_PAID" || it.paymentStatus == "PAID" || it.status == "DELIVERED" }
    }
    val pendingCodCount = remember(orders) {
        orders.count { it.paymentStatus != "CONFIRMED_PAID" && it.paymentStatus != "PAID" && it.status != "DELIVERED" }
    }

    Scaffold(
        containerColor = Cream50,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Payments & Transactions",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = Ink950
                        )
                        Text(
                            text = "Financial settlement & transaction invoices",
                            fontSize = 11.sp,
                            color = Ink500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Financial Summary Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Transaction Volume",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink500
                            )
                            Surface(
                                color = Mint100,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "COD Settlement Active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest800,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalSpent)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Forest700
                        )

                        HorizontalDivider(color = Sage100)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Paid Invoices", fontSize = 11.sp, color = Ink500)
                                Text("$paidCount Orders", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Forest700)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Pending COD Due", fontSize = 11.sp, color = Ink500)
                                Text("$pendingCodCount Orders", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Rust500)
                            }
                        }
                    }
                }
            }

            // 2. Filter Pills: All / Paid / Pending COD
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "All (${orders.size})",
                        "PAID" to "Paid ($paidCount)",
                        "COD_PENDING" to "Pending COD ($pendingCodCount)"
                    )
                    filters.forEach { (code, label) ->
                        val isSelected = selectedFilter == code
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedFilter = code },
                            color = if (isSelected) Forest500 else Color.White,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Sage200),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Ink700,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // 3. Transactions List
            if (filteredOrders.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Ink300,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No transactions found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink950
                        )
                        Text(
                            text = "Place a food order to generate your first transaction receipt.",
                            fontSize = 12.sp,
                            color = Ink500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateToDiscover,
                            colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Explore Restaurants", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(filteredOrders) { order ->
                    val isConfirmedPaid = order.paymentStatus == "CONFIRMED_PAID" || order.paymentStatus == "PAID" || order.status == "DELIVERED"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Row: Transaction ID and Status Badge
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
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = Forest600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "TXN-${order.id.toString().padStart(6, '0')}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Ink950
                                    )
                                }

                                Surface(
                                    color = if (isConfirmedPaid) Mint100 else Cream200,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isConfirmedPaid) "✓ Paid" else "⏳ Pending COD",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isConfirmedPaid) Forest800 else Rust900,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Sage100)

                            // Middle: Merchant & Amount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = order.merchantName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink950
                                    )
                                    Text(
                                        text = "${order.itemsCount} item(s) • ${order.orderTime}",
                                        fontSize = 11.sp,
                                        color = Ink500
                                    )
                                    if (order.itemsSummary.isNotBlank()) {
                                        Text(
                                            text = order.itemsSummary,
                                            fontSize = 11.sp,
                                            color = Ink600,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", order.total)}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Forest700
                                )
                            }

                            // Payment Method & Action Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Cream100,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (order.paymentMethod.contains("CASH", ignoreCase = true) || order.paymentMethod.contains("COD", ignoreCase = true)) "💵 Cash on Delivery" else "🏦 KHQR / Bank",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink800,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                TextButton(
                                    onClick = { selectedOrderForReceipt = order },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text("View Invoice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Forest600)
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Forest600, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Digital Invoice & Receipt Dialog
    selectedOrderForReceipt?.let { order ->
        val isConfirmedPaid = order.paymentStatus == "CONFIRMED_PAID" || order.paymentStatus == "PAID" || order.status == "DELIVERED"

        AlertDialog(
            onDismissRequest = { selectedOrderForReceipt = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Official Receipt", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink950)
                    Surface(
                        color = if (isConfirmedPaid) Mint100 else Cream200,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isConfirmedPaid) "PAID" else "COD DUE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConfirmedPaid) Forest800 else Rust900,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Invoice #: TXN-${order.id.toString().padStart(6, '0')}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Ink600)
                    Text("Date: ${order.orderTime}", fontSize = 12.sp, color = Ink600)
                    Text("Merchant: ${order.merchantName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink950)
                    Text("Customer: ${order.customerName}", fontSize = 12.sp, color = Ink600)
                    Text("Delivery To: ${order.deliveryAddress}", fontSize = 12.sp, color = Ink600)

                    HorizontalDivider(color = Sage200)

                    Text("Items Ordered (${order.itemsCount}):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ink950)
                    Text(
                        text = order.itemsSummary,
                        fontSize = 12.sp,
                        color = Ink800,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = Sage200)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = 12.sp, color = Ink600)
                        Text("$${String.format(Locale.US, "%.2f", order.subtotal)}", fontSize = 12.sp, color = Ink800)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivery Fee", fontSize = 12.sp, color = Ink600)
                        Text("$${String.format(Locale.US, "%.2f", order.deliveryFee)}", fontSize = 12.sp, color = Ink800)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount Due", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink950)
                        Text(
                            "$${String.format(Locale.US, "%.2f", order.total)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Forest700
                        )
                    }

                    Surface(
                        color = Cream100,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Payment Method: ${if (order.paymentMethod.contains("CASH", ignoreCase = true) || order.paymentMethod.contains("COD", ignoreCase = true)) "Cash on Delivery" else "KHQR / Digital Payment"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink700,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedOrderForReceipt = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close Invoice", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
