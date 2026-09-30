package com.example.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FoodEatsRepository
import com.example.data.repository.PlacedOrder
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onNavigateToDiscover: () -> Unit
) {
    val orders by FoodEatsRepository.orders.collectAsState()

    Scaffold(
        containerColor = Cream50,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Order History & Tracking",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = Ink950
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (orders.isEmpty()) {
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
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "No Orders",
                            tint = Forest600,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "No orders yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink950
                    )
                    Text(
                        text = "Craving something delicious? Order from top artisan merchants and track your delivery live!",
                        fontSize = 13.sp,
                        color = Ink500,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = onNavigateToDiscover,
                        colors = ButtonDefaults.buttonColors(containerColor = Forest500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Discover Food")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderTrackingCard(
                        order = order,
                        onSimulateStep = {
                            FoodEatsRepository.advanceOrderStatus(order.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderTrackingCard(
    order: PlacedOrder,
    onSimulateStep: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: ID, Merchant, Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "#ORD-${order.id}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Forest700
                        )
                        Text(
                            text = "• ${order.orderTime}",
                            fontSize = 11.sp,
                            color = Ink400
                        )
                    }
                    Text(
                        text = order.merchantName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink950
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", order.total)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink950
                    )
                    Surface(
                        color = if (order.paymentStatus.contains("PAID")) Mint100 else Cream200,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = order.paymentStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.paymentStatus.contains("PAID")) Forest700 else Rust500,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = order.itemsSummary,
                fontSize = 13.sp,
                color = Ink600,
                lineHeight = 18.sp
            )

            HorizontalDivider(color = Sage100)

            // 4-Step Visual State Machine Progress Bar
            Text(
                text = "DELIVERY PROGRESS",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink400,
                letterSpacing = 1.sp
            )

            OrderStatusStepper(currentStatus = order.status)

            HorizontalDivider(color = Sage100)

            // Delivery Details & Driver
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = "Courier",
                        tint = Forest500,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = order.driver,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink800
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Address",
                        tint = Ink400,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = order.deliveryAddress,
                        fontSize = 12.sp,
                        color = Ink600
                    )
                }
            }

            // Interactive simulation button (to test live order tracker)
            if (order.status != "DELIVERED") {
                Button(
                    onClick = onSimulateStep,
                    colors = ButtonDefaults.buttonColors(containerColor = Ink50),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Advance",
                        tint = Forest600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Advance Delivery Stage (Demo)",
                        color = Forest600,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatusStepper(currentStatus: String) {
    val steps = listOf(
        "Placed" to "PLACED",
        "Preparing" to "ACCEPTED",
        "On the Way" to "OUT_FOR_DELIVERY",
        "Delivered" to "DELIVERED"
    )

    val currentStepIndex = when (currentStatus) {
        "PLACED" -> 0
        "ACCEPTED" -> 1
        "OUT_FOR_DELIVERY" -> 2
        "DELIVERED" -> 3
        else -> 1
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, _) ->
            val isDone = index < currentStepIndex
            val isActive = index == currentStepIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone -> Forest500
                                isActive -> Forest600
                                else -> Sage100
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color.White else Ink400
                        )
                    }
                }

                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) Forest700 else if (isDone) Ink800 else Ink400
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(0.6f)
                        .background(if (index < currentStepIndex) Forest500 else Sage200)
                )
            }
        }
    }
}
