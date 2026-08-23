package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.Permission
import com.example.security.RbacPolicy
import com.example.security.UserRole
import com.example.ui.auth.AuthViewModel

@Composable
fun AdminRoleDivisionScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val activeRole = uiState.activeSession?.activeRole?.let { UserRole.fromCode(it) } ?: UserRole.CUSTOMER
    val currentPermissions = uiState.userPermissions

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Role Architecture & Permissions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111), RoundedCornerShape(10.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Spec",
                            tint = Color(0xFFFFD54F)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Role Division & Permission Architecture",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "How to divide Admin, Merchant, Driver, and Customer roles across Spring Boot backend, Web UI, and Mobile UI.",
                        fontSize = 12.sp,
                        color = Color(0xFFDDDDDD),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: Interactive Live Role Switcher Test Bench
            Text(
                text = "1. Live Role Switcher (Sprint 1 Testing)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Tap a role below to simulate changing JWT claims in real-time:",
                fontSize = 13.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UserRole.values().forEach { role ->
                    val isCurrent = activeRole == role
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) Color.Black else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(if (isCurrent) Color(0xFFFAF8F5) else Color.White)
                            .clickable { authViewModel.switchRole(role) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (role) {
                                        UserRole.CUSTOMER -> Icons.Default.Person
                                        UserRole.MERCHANT -> Icons.Default.Storefront
                                        UserRole.DRIVER -> Icons.Default.DirectionsCar
                                        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                    },
                                    contentDescription = role.displayName,
                                    tint = if (isCurrent) Color.Black else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = role.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = role.description,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 2: Permission Matrix for Active Role
            Text(
                text = "2. Active Permission Matrix (${activeRole.displayName})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Permission.values().forEach { permission ->
                        val isGranted = currentPermissions.contains(permission)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = permission.code,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isGranted) Color(0xFF1B5E20) else Color(0xFF757575)
                                )
                                Text(
                                    text = permission.description,
                                    fontSize = 11.sp,
                                    color = Color(0xFF666666)
                                )
                            }

                            Icon(
                                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = if (isGranted) "Granted" else "Denied",
                                tint = if (isGranted) Color(0xFF2E7D32) else Color(0xFFB71C1C)
                            )
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: How & Where to Divide Admin vs User Page Roles
            Text(
                text = "3. Architectural Guide: Where to Divide Roles",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Web Strategy Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F6F9))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Computer, contentDescription = "Web", tint = Color(0xFF1565C0))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Web Portal Strategy (zandoshops.com)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Admin Page (/admin/*): Host on a dedicated web route protected by Spring Security `@PreAuthorize(\"hasRole('ADMIN')\")`. Include user management, merchant approvals, platform fee analytics, and system audit logs.\n" +
                                "• Merchant Portal (/merchant/*): Dedicated dashboard for store managers to edit menus, inventory toggles, and view store revenue.\n" +
                                "• Customer Web (/store/*): Clean e-commerce shopping experience matching zandoshops.com layout.",
                        fontSize = 12.sp,
                        color = Color(0xFF333333),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mobile Strategy Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Mobile", tint = Color(0xFFF57F17))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Mobile Android Strategy (Kotlin / Compose)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Single APK with Role-Aware Navigation: Decode JWT claims from local Room database session.\n" +
                                "• Dynamic Tab Visibility: Admin / Merchant / Driver tabs are rendered ONLY if `currentPermissions.contains(Permission.ADMIN_PANEL_ACCESS)`.\n" +
                                "• Instant Context Switching: Users with dual roles (e.g. Customer + Merchant) can toggle their active role in Profile Screen without re-logging.",
                        fontSize = 12.sp,
                        color = Color(0xFF333333),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
