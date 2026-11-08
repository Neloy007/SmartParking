
package com.example.smartparking.presentation.driver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartparking.data.repository.ParkingSessionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    sessionId: String,
    parkingName: String,
    hourlyRate: Double,
    onDone: () -> Unit
) {
    val repository = remember { ParkingSessionRepository() }
    val scope = rememberCoroutineScope()

    var processing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var expiresAt by remember { mutableStateOf<Long?>(null) }
    var remainingMillis by remember { mutableStateOf(0L) }
    var totalPaid by remember { mutableStateOf(0.0) }
    var sessionStatus by remember { mutableStateOf("PENDING_PAYMENT") }

    var selectedMinutes by remember { mutableIntStateOf(30) }

    // Listen for changes to this parking session.
    DisposableEffect(sessionId) {
        val registration = FirebaseFirestore.getInstance()
            .collection("parking_sessions")
            .document(sessionId)
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {
                    error = exception.message
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    sessionStatus =
                        snapshot.getString("status") ?: "PENDING_PAYMENT"

                    expiresAt = snapshot.getLong("expiresAt")

                    totalPaid = snapshot.getDouble("totalPaid")
                        ?: snapshot.getLong("totalPaid")?.toDouble()
                                ?: 0.0

                    expiresAt?.let {
                        remainingMillis =
                            (it - System.currentTimeMillis()).coerceAtLeast(0L)
                    }
                }
            }

        onDispose {
            registration.remove()
        }
    }

    // Update the countdown every second.
    LaunchedEffect(expiresAt) {
        val expiry = expiresAt ?: return@LaunchedEffect

        while (true) {
            remainingMillis =
                (expiry - System.currentTimeMillis()).coerceAtLeast(0L)

            if (remainingMillis <= 0L) break

            delay(1000L)
        }
    }

    val hours = remainingMillis / 3_600_000L
    val minutes = (remainingMillis % 3_600_000L) / 60_000L
    val seconds = (remainingMillis % 60_000L) / 1_000L

    val countdown = String.format(
        Locale.US,
        "%02d:%02d:%02d",
        hours,
        minutes,
        seconds
    )

    val extensionCost = hourlyRate * selectedMinutes / 60.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (expiresAt == null) {
                            "Parking Payment"
                        } else {
                            "Active Parking Session"
                        }
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // PARKING DETAILS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = Color(0xFF16A34A)
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            parkingName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hourly rate")
                        Text(
                            String.format(
                                Locale.US,
                                "৳ %.2f",
                                hourlyRate
                            ),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (expiresAt != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total paid")
                            Text(
                                String.format(
                                    Locale.US,
                                    "৳ %.2f",
                                    totalPaid
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (expiresAt == null) {

                // INITIAL PAYMENT
                Text(
                    "Initial parking payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text("Initial parking duration: 1 hour")

                Text(
                    String.format(
                        Locale.US,
                        "Amount: ৳ %.2f",
                        hourlyRate
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEFF6FF)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Demo payment",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Development testing only. No real money will be charged.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            processing = true
                            error = null

                            try {
                                repository.markDemoPaymentSuccessful(
                                    sessionId = sessionId,
                                    durationMinutes = 60
                                )
                            } catch (e: Exception) {
                                error = e.message
                                    ?: "Unable to confirm payment."
                            } finally {
                                processing = false
                            }
                        }
                    },
                    enabled = !processing &&
                            sessionStatus == "PENDING_PAYMENT",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    if (processing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Confirm demo payment")
                    }
                }

            } else {

                // ACTIVE SESSION COUNTDOWN
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (remainingMillis > 0L) {
                            Color(0xFFECFDF5)
                        } else {
                            Color(0xFFFFF1F2)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (remainingMillis > 0L) {
                                Icons.Default.AccessTime
                            } else {
                                Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = if (remainingMillis > 0L) {
                                Color(0xFF16A34A)
                            } else {
                                Color(0xFFDC2626)
                            },
                            modifier = Modifier.size(36.dp)
                        )

                        Text(
                            if (remainingMillis > 0L) {
                                "Time Remaining"
                            } else {
                                "Parking Time Expired"
                            },
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            countdown,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingMillis > 0L) {
                                Color(0xFF15803D)
                            } else {
                                Color(0xFFDC2626)
                            }
                        )

                        Text(
                            if (remainingMillis > 0L) {
                                "Your parking session is active."
                            } else {
                                "Your allocated parking time has ended."
                            },
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text(
                    "Extend parking time",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Choose additional time before your session expires.",
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30, 60, 120).forEach { duration ->
                        FilterChip(
                            selected = selectedMinutes == duration,
                            onClick = {
                                selectedMinutes = duration
                            },
                            label = {
                                Text(
                                    when (duration) {
                                        30 -> "30 min"
                                        60 -> "1 hour"
                                        else -> "2 hours"
                                    }
                                )
                            }
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Additional duration")
                            Text("$selectedMinutes minutes")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Additional payment")
                            Text(
                                String.format(
                                    Locale.US,
                                    "৳ %.2f",
                                    extensionCost
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEFF6FF)
                    )
                ) {
                    Text(
                        "Demo extension payment only. No real money will be charged.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            processing = true
                            error = null

                            try {
                                repository.extendDemoSession(
                                    sessionId = sessionId,
                                    additionalMinutes = selectedMinutes
                                )
                            } catch (e: Exception) {
                                error = e.message
                                    ?: "Unable to extend parking time."
                            } finally {
                                processing = false
                            }
                        }
                    },
                    enabled = !processing && remainingMillis > 0L,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    if (processing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Confirm extension payment")
                    }
                }

                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
