package com.example.ui.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FoodEatsRepository
import com.example.data.repository.PlacedOrder
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel? = null
) {
    val context = LocalContext.current
    val authState by authViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    val authToken = authState?.activeSession?.jwtToken

    val allOrders by FoodEatsRepository.orders.collectAsState()
    val activeJob by FoodEatsRepository.activeDriverJob.collectAsState()

    // Available jobs are orders placed/accepted that don't have a finished status
    val availableJobs = allOrders.filter { it.status == "ACCEPTED" && activeJob?.id != it.id }

    var selectedTab by remember { mutableStateOf(if (activeJob != null) 1 else 0) } // 0 = Board, 1 = Active

    Scaffold(
        containerColor = Cream50,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Driver Dispatch Console", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Ink950)
                        Text("Sprint 5 • Courier Fleet Engine", fontSize = 11.sp, color = Forest600, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink950)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Driver Profile Status Bar
            DriverHeaderCard()

            // Tab Bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Forest700
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Available Jobs (${availableJobs.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (activeJob != null) "Active Delivery (1)" else "Active Delivery (0)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            // Tab Content
            if (selectedTab == 0) {
                // ==========================================
                // 1. RECEIVE TAB: Available Jobs Board
                // ==========================================
                if (availableJobs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No orders waiting for pickup right now.\nCheck back in a few seconds!", color = Ink500, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableJobs) { job ->
                            AvailableJobCard(
                                order = job,
                                onAccept = {
                                    FoodEatsRepository.acceptJobAsDriver(job, authToken)
                                    selectedTab = 1 // Switch automatically to Active Tab!
                                }
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // 2. PICKUP & DELIVER TAB
                // ==========================================
                if (activeJob == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Moped, contentDescription = null, tint = Forest500, modifier = Modifier.size(48.dp))
                            Text("No Active Delivery", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink950)
                            Text("Accept a job from the Available Jobs tab to start!", fontSize = 12.sp, color = Ink500)
                        }
                    }
                } else {
                    val currentJob = activeJob!!
                    val isPickupStage = currentJob.status == "ACCEPTED"

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ActiveDeliveryCard(
                                order = currentJob,
                                isPickupStage = isPickupStage,
                                onOpenMaps = { address ->
                                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                },
                                onCallPhone = { phone ->
                                    val uri = Uri.parse("tel:$phone")
                                    context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                                },
                                onConfirmPickup = {
                                    FoodEatsRepository.confirmFoodPickedUp(currentJob.id, authToken)
                                },
                                onCompleteDelivery = {
                                    FoodEatsRepository.completeDelivery(currentJob.id, authToken)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverHeaderCard() {
    Surface(color = Color.White, shadowElevation = -1.dp){
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Forest500), contentAlignment = Alignment.Center){
                    Icon(Icons.Default.DirectionsBike,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("K'noeun Express" ,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Ink950)
                    Text("Hondat Dream 125 * 4.9 ★ ",
                        fontSize = 11.sp,
                        color = Ink500)
                }
            }
            Surface(color = Mint100,
                shape = RoundedCornerShape(20.dp)){
                    Text("🟢 ONLINE",
                        color = Forest800,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp,
                            vertical = 4.dp))
            }
        }
    }
}

// 1. RECEIVE CARD: Used in Available Jobs tab
@Composable
fun AvailableJobCard(order: PlacedOrder, onAccept: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order #${order.id}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Forest700)
                Text("+$${String.format(java.util.Locale.US, "%.2f", order.deliveryFee)} Earning", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Ink950)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🏪 Pick up: ${order.merchantName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink900)
                Text("📍 Drop off: ${order.deliveryAddress}", fontSize = 12.sp, color = Ink600)
                Text("📦 ${order.itemsSummary}", fontSize = 12.sp, color = Ink500)
            }
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Forest500)
            ) {
                Text("🛵 Accept Delivery Job", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}


//2&3 pickup & deliver card : flip based on ispickupstahe
@Composable
fun ActiveDeliveryCard(
    order: PlacedOrder,
    isPickupStage: Boolean,
    onOpenMaps: (String) -> Unit,
    onCallPhone: (String) -> Unit,
    onConfirmPickup: () -> Unit,
    onCompleteDelivery: () -> Unit
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ){
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)){
            //stage indicator
            Surface(
                color = if (isPickupStage) Color(0xFFFFF3CD) else Mint100,
                shape = RoundedCornerShape(8.dp)
            ){
                Text(
                    text = if (isPickupStage) "STAGE 1: Head to Restaurant For Pickup"
                    else "STAGE 2: On The Way To Customer",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isPickupStage) Color(0xFF856404) else Forest800,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            if (isPickupStage){
                //step 2 : pickup card

                Text(order.merchantName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Ink600)

                OutlinedButton(
                    onClick = { onOpenMaps(order.merchantName + " , Phnom Penh") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ){
                    Icon(Icons.Default.Navigation, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                    Text("Directions in Google Maps (App)" ,
                        fontWeight = FontWeight.Bold)

                }

                HorizontalDivider(color = Sage100)
                Text("Order Checklist:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink700)
                Text(order.itemsSummary, fontSize = 13.sp, color = Ink900)
                Button(
                    onClick = onConfirmPickup,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22))
                ) {
                    Text("🍳 Confirm Food Picked Up", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                // ====================
                // STEP 3: DELIVER CARD
                // ====================
                Text("Customer: ${order.customerName}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink950)
                Text("Delivery Address: ${order.deliveryAddress}", fontSize = 13.sp, color = Ink700)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onCallPhone("+85512345678") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Ink50),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Forest700, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Call Customer", color = Forest700, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onOpenMaps(order.deliveryAddress) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Ink50),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = Forest700, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Open Map", color = Forest700, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(color = Cream100, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Payment: ${order.paymentMethod} • Collect: $${String.format(java.util.Locale.US, "%.2f", order.total)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Rust600, modifier = Modifier.padding(10.dp))
                }
                Button(
                    onClick = onCompleteDelivery,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Forest500)
                ) {
                    Text("✅ Complete Delivery & Settle", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

            }
        }
    }
}

