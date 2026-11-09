
package com.example.smartparking.presentation.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartparking.presentation.owner.components.OwnerLogoutButton
import com.example.smartparking.data.model.Parking
// ----------------------------------------------------
// PARKWAY BRAND COLORS
// ----------------------------------------------------

private val ParkwayBlack = Color(0xFF080808)
private val ParkwayWhite = Color(0xFFFFFFFF)
private val ParkwayLime = Color(0xFFB8F34A)

private val ParkwayGray = Color(0xFF777777)
private val ParkwayLightGray = Color(0xFFF4F4F4)
private val ParkwayBorder = Color(0xFFEEEEEE)

private val ParkwayRed = Color(0xFFD32F2F)


// ----------------------------------------------------
// OWNER HOME SCREEN
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    onRegisterParking: () -> Unit = {},
    onEditParking: (Parking) -> Unit = {},
    onLogout: () -> Unit,
    viewModel: OwnerParkingListViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadParkings()
    }

    Scaffold(
        containerColor = ParkwayWhite,

        topBar = {

            TopAppBar(
                title = {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = ParkwayBlack
                        ) {

                            Box(
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    "P",
                                    color = ParkwayLime,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(Modifier.width(11.dp))

                        Column {

                            Text(
                                "parkway",
                                color = ParkwayBlack,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Text(
                                "OWNER DASHBOARD",
                                color = ParkwayGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },

                actions = {

                    IconButton(
                        onClick = {
                            viewModel.loadParkings()
                        }
                    ) {

                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = ParkwayBlack
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ParkwayWhite
                )
            )
        },


        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ParkwayWhite)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // REGISTER PARKING
                Button(
                    onClick = onRegisterParking,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParkwayBlack
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = ParkwayLime
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        "Register",
                        color = ParkwayWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                // LOGOUT WITH CONFIRMATION
                OwnerLogoutButton(
                    onLogout = onLogout,
                    modifier = Modifier.weight(1f)
                )
            }
        }


    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ParkwayWhite)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(Modifier.height(20.dp))

            // ----------------------------------------
            // DASHBOARD INTRODUCTION
            // ----------------------------------------

            Text(
                text = "Manage your spaces.",
                color = ParkwayBlack,
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = "Manage your parking facilities in one place.",
                color = ParkwayGray,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(22.dp))


            // ----------------------------------------
            // STATISTICS
            // ----------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OwnerStatCard(
                    title = "Facilities",
                    value = "${uiState.parkings.size}",
                    modifier = Modifier.weight(1f)
                )

                OwnerStatCard(
                    title = "Active",
                    value = "${uiState.parkings.count { it.active }}",
                    modifier = Modifier.weight(1f)
                )

                OwnerStatCard(
                    title = "Inactive",
                    value = "${uiState.parkings.count { !it.active }}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(27.dp))


            // ----------------------------------------
            // FACILITY SECTION HEADER
            // ----------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        "My facilities",
                        color = ParkwayBlack,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Your registered parking locations",
                        color = ParkwayGray,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    color = ParkwayLightGray,
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Text(
                        "${uiState.parkings.size} total",
                        color = ParkwayBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))


            // ----------------------------------------
            // FACILITY CONTENT
            // ----------------------------------------

            when {

                // LOADING

                uiState.isLoading -> {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            CircularProgressIndicator(
                                color = ParkwayBlack,
                                strokeWidth = 3.dp
                            )

                            Spacer(Modifier.height(14.dp))

                            Text(
                                "Loading your facilities...",
                                color = ParkwayGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }


                // ERROR

                uiState.error != null -> {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = ParkwayRed,
                                modifier = Modifier.size(45.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = uiState.error ?: "Something went wrong",
                                color = ParkwayRed,
                                fontSize = 13.sp
                            )

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.loadParkings()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ParkwayBlack
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {

                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null
                                )

                                Spacer(Modifier.width(7.dp))

                                Text("Try Again")
                            }
                        }
                    }
                }


                // EMPTY STATE

                uiState.parkings.isEmpty() -> {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Surface(
                                color = ParkwayLightGray,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.size(85.dp)
                            ) {

                                Box(
                                    contentAlignment = Alignment.Center
                                ) {

                                    Icon(
                                        Icons.Default.LocalParking,
                                        contentDescription = null,
                                        tint = ParkwayBlack,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            Text(
                                "No facilities yet",
                                color = ParkwayBlack,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(7.dp))

                            Text(
                                "Register your first parking facility.",
                                color = ParkwayGray,
                                fontSize = 12.sp
                            )

                            Spacer(Modifier.height(18.dp))

                            Button(
                                onClick = onRegisterParking,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ParkwayBlack
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {

                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = ParkwayLime
                                )

                                Spacer(Modifier.width(7.dp))

                                Text("Register your first parking")
                            }
                        }
                    }
                }


                // PARKING FACILITIES LIST

                else -> {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),

                        contentPadding = PaddingValues(
                            bottom = 20.dp
                        ),

                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        items(
                            items = uiState.parkings,
                            key = { it.id }
                        ) { parking ->

                            OwnerParkingCard(
                                name = parking.name,
                                address = parking.address,
                                availableSlots = parking.availableSlots,
                                totalSlots = parking.totalSlots,
                                pricePerHour = parking.pricePerHour,
                                active = parking.active,
                                onClick = {
                                    onEditParking(parking)
                                }
                            )
                        }
                    }
                }
            }


        }
    }
}


// ----------------------------------------------------
// OWNER STATISTICS CARD
// ----------------------------------------------------

@Composable
private fun OwnerStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ParkwayLightGray
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp)
        ) {

            Text(
                text = value,
                color = ParkwayBlack,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = title,
                color = ParkwayGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// ----------------------------------------------------
// OWNER PARKING CARD
// ----------------------------------------------------

@Composable
private fun OwnerParkingCard(
    name: String,
    address: String,
    availableSlots: Int,
    totalSlots: Int,
    pricePerHour: Double,
    active: Boolean,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ParkwayWhite
        ),
        border = BorderStroke(
            1.dp,
            ParkwayBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // TOP SECTION

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    color = ParkwayLightGray,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(45.dp)
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = ParkwayBlack,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }


                // STATUS

                Surface(
                    color = if (active) {
                        ParkwayLime.copy(alpha = 0.35f)
                    } else {
                        ParkwayLightGray
                    },
                    shape = RoundedCornerShape(30.dp)
                ) {

                    Row(
                        modifier = Modifier.padding(
                            horizontal = 11.dp,
                            vertical = 7.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(7.dp),
                            shape = CircleShape,
                            color = if (active) {
                                Color(0xFF4D8B21)
                            } else {
                                ParkwayGray
                            }
                        ) {}

                        Spacer(Modifier.width(6.dp))

                        Text(
                            if (active) "Active" else "Inactive",
                            color = ParkwayBlack,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))


            // FACILITY NAME

            Text(
                text = name,
                color = ParkwayBlack,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))


            // ADDRESS

            Row(
                verticalAlignment = Alignment.Top
            ) {

                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = ParkwayGray,
                    modifier = Modifier.size(15.dp)
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = address,
                    color = ParkwayGray,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(17.dp))

            HorizontalDivider(
                color = ParkwayBorder,
                thickness = 1.dp
            )

            Spacer(Modifier.height(14.dp))


            // FACILITY INFORMATION

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // AVAILABLE SLOTS

                Column {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = ParkwayGray,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(Modifier.width(5.dp))

                        Text(
                            "Availability",
                            color = ParkwayGray,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "$availableSlots / $totalSlots",
                        color = ParkwayBlack,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Available slots",
                        color = ParkwayGray,
                        fontSize = 10.sp
                    )
                }


                // PRICE

                Column(
                    horizontalAlignment = Alignment.End
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null,
                            tint = ParkwayGray,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(Modifier.width(5.dp))

                        Text(
                            "Parking fee",
                            color = ParkwayGray,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "৳$pricePerHour",
                        color = ParkwayBlack,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Per hour",
                        color = ParkwayGray,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))


            // SLOT PROGRESS

            val progress = if (totalSlots > 0) {
                (availableSlots.toFloat() / totalSlots.toFloat())
                    .coerceIn(0f, 1f)
            } else {
                0f
            }

            Column {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        "Slot availability",
                        color = ParkwayGray,
                        fontSize = 10.sp
                    )

                    Text(
                        "${(progress * 100).toInt()}%",
                        color = ParkwayBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(7.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = ParkwayLime,
                    trackColor = ParkwayLightGray,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}
