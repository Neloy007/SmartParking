
package com.example.smartparking.presentation.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
private val ParkwayBlack = Color(0xFF080808)
private val ParkwayLime = Color(0xFFB8F34A)
private val ParkwayGray = Color(0xFF777777)
private val ParkwayLightGray = Color(0xFFF4F4F4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: OwnerProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        color = ParkwayBlack,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ParkwayBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ParkwayBlack)
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.error ?: "Unable to load profile.",
                        color = Color.Red
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.loadProfile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParkwayBlack
                        )
                    ) {
                        Text("Try Again", color = Color.White)
                    }
                }
            }

            uiState.user != null -> {
                val user = uiState.user!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))

                    // Profile avatar
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape)
                            .background(ParkwayLightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name
                                .trim()
                                .firstOrNull()
                                ?.uppercase() ?: "O",
                            fontSize = 46.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ParkwayBlack
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = user.name.ifBlank { "Owner" },
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ParkwayBlack
                    )

                    Spacer(Modifier.height(7.dp))

                    Surface(
                        color = ParkwayLime.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "PARKWAY OWNER",
                            modifier = Modifier.padding(
                                horizontal = 15.dp,
                                vertical = 7.dp
                            ),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ParkwayBlack,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(Modifier.height(30.dp))

                    // Email
                    ProfileInfoCard(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = ParkwayBlack
                            )
                        },
                        title = "Email address",
                        value = user.email.ifBlank { "Not provided" }
                    )

                    Spacer(Modifier.height(13.dp))

                    // Phone
                    ProfileInfoCard(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = ParkwayBlack
                            )
                        },
                        title = "Phone number",
                        value = user.phone.ifBlank { "Not provided" }
                    )

                    Spacer(Modifier.height(13.dp))

                    // Role
                    ProfileInfoCard(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = ParkwayBlack
                            )
                        },
                        title = "Account role",
                        value = user.role.ifBlank { "OWNER" }
                    )

                    Spacer(Modifier.height(13.dp))

                    // Verification status
                    ProfileInfoCard(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = ParkwayBlack
                            )
                        },
                        title = "Verification status",
                        value = if (user.isVerified) {
                            "Verified"
                        } else {
                            "Not verified"
                        }
                    )

                    Spacer(Modifier.height(32.dp))

                    // Logout button
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParkwayBlack
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = ParkwayLime
                        )

                        Spacer(Modifier.width(9.dp))

                        Text(
                            text = "Logout",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(
    icon: @Composable () -> Unit,
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ParkwayLightGray
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = ParkwayLime.copy(alpha = 0.35f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    icon()
                }
            }

            Spacer(Modifier.width(13.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = ParkwayGray
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ParkwayBlack
                )
            }
        }
    }
}
