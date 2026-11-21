package com.example.smartparking.presentation.owner


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.smartparking.data.model.ParkingSession
import com.example.smartparking.data.repository.BookingRepository
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ParkwayBlack = Color(0xFF080808)
private val ParkwayLime = Color(0xFFB8F34A)
private val ParkwayGray = Color(0xFF777777)
private val ParkwayLightGray = Color(0xFFF4F4F4)
private val ParkwayBorder = Color(0xFFEEEEEE)
private val ParkwayRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerParkingDriversScreen(
    onBack: () -> Unit,
    bookingRepository: BookingRepository = remember { BookingRepository() }
) {
    var sessions by remember { mutableStateOf<List<ParkingSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Update the active-session list as sessions expire.
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(30_000L)
        }
    }

    // Listen to this owner's confirmed parking sessions.
    DisposableEffect(bookingRepository) {
        val registration = bookingRepository.listenToOwnerParkingSessions(
            onSessionsChanged = { updatedSessions ->
                sessions = updatedSessions
                isLoading = false
                errorMessage = null
            },
            onError = { error ->
                errorMessage = error.localizedMessage
                    ?: "Unable to load parking drivers."
                isLoading = false
            }
        )

        onDispose {
            registration.remove()
        }
    }

    val activeSessions = sessions
        .filter { it.expiresAt > currentTime }
        .sortedBy { it.expiresAt }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Parking Drivers",
                            color = ParkwayBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "${activeSessions.size} currently parking",
                            color = ParkwayGray,
                            fontSize = 12.sp
                        )
                    }
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
    ) { paddingValues ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ParkwayLime)
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Something went wrong.",
                        color = ParkwayRed
                    )
                }
            }

            activeSessions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    ParkwayLightGray,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("P", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "No Active Drivers",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ParkwayBlack
                        )

                        Text(
                            text = "Drivers currently using your parking facilities will appear here.",
                            color = ParkwayGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "CURRENTLY PARKING",
                            color = ParkwayGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    items(
                        items = activeSessions,
                        key = { it.id }
                    ) { session ->
                        ParkingDriverCard(session = session)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParkingDriverCard(
    session: ParkingSession
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            ParkwayBorder
        )
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
                    text = session.parkingName.ifBlank { "Parking Facility" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ParkwayBlack,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .background(
                            ParkwayLime.copy(alpha = 0.25f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFF369B45), CircleShape)
                    )
                    Text(
                        text = " Active",
                        color = Color(0xFF267A32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = ParkwayBorder)

            Text(
                text = session.driverName.ifBlank { "Driver" },
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = ParkwayBlack
            )

            InfoRow(
                label = "Phone",
                value = session.driverPhone.ifBlank { "Not provided" }
            )

            InfoRow(
                label = "Car Number",
                value = "Not added yet"
            )

            HorizontalDivider(color = ParkwayBorder)

            InfoRow(
                label = "Parking Started",
                value = formatDateTime(session.startedAt)
            )

            InfoRow(
                label = "Parking Expires",
                value = formatDateTime(session.expiresAt)
            )

            InfoRow(
                label = "Duration",
                value = "${session.durationMinutes} minutes"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        ParkwayLightGray,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Paid",
                    color = ParkwayGray,
                    fontSize = 13.sp
                )

                Text(
                    text = "৳${session.totalPaid}",
                    color = ParkwayBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = ParkwayGray,
            fontSize = 13.sp,
            modifier = Modifier.weight(0.8f)
        )

        Text(
            text = value,
            color = ParkwayBlack,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.2f)
        )
    }
}

private fun formatDateTime(timestamp: Long): String {
    if (timestamp <= 0L) return "Not available"

    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date(timestamp))
}
