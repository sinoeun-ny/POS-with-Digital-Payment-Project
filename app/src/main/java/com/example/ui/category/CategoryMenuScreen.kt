package com.example.ui.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryMenuScreen(
    onNavigateToBag: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf("KHMER") }
    val mainTabs = listOf("KHMER", "BEVERAGES", "ASIAN", "FAST FOOD", "DESSERTS")

    var expandedCategory by remember { mutableStateOf<String?>("Traditional Mains") }

    val categories = remember {
        listOf(
            "New In Specials" to listOf("Chef Special Lok Lak", "Amok Souffle", "Crispy Spring Rolls"),
            "Traditional Mains" to listOf("Fish Amok", "Pork Rice", "Nom Banh Chok", "Beef Rice Noodles"),
            "Side Dishes & Snacks" to listOf("Fresh Salad Roll", "Fried Chive Cake", "Papaya Salad"),
            "Beverages & Drinks" to listOf("Khmer Iced Coffee", "Iced Green Tea", "Fresh Coconut Juice"),
            "Desserts & Sweets" to listOf("Mango Sticky Rice", "Pandan Layer Cake", "Coconut Ice Cream")
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // Top Header matching Image 6
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Black)
                    }

                    Text(
                        text = "CATEGORIES",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        letterSpacing = 1.sp
                    )

                    IconButton(onClick = onNavigateToBag) {
                        BadgedBox(
                            badge = { Badge(containerColor = Color(0xFFD32F2F)) { Text("2") } }
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = "Cart", tint = Color.Black)
                        }
                    }
                }

                // Horizontal Category Tabs matching Image 6
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(mainTabs) { tab ->
                        val isSelected = selectedTab == tab
                        Column(
                            modifier = Modifier
                                .clickable { selectedTab = tab }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = tab,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(2.dp)
                                    .background(if (isSelected) Color.Black else Color.Transparent)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Accordions matching Image 6
            categories.forEach { (categoryName, itemsList) ->
                val isExpanded = expandedCategory == categoryName

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedCategory = if (isExpanded) null else categoryName
                            }
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoryName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111111)
                        )

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                            contentDescription = "Expand",
                            tint = Color.Gray
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFAFAFA))
                                .padding(horizontal = 32.dp, vertical = 8.dp)
                        ) {
                            itemsList.forEach { subItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onCategoryClick(subItem) }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = subItem,
                                        fontSize = 14.sp,
                                        color = Color(0xFF444444)
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFEEEEEE))
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}
