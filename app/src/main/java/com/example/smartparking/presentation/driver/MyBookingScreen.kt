package com.example.smartparking.presentation.driver


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.smartparking.data.model.Booking
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    bookings: List<Booking>,
    onCancelBooking: (Booking) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Upcoming") }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    val dateFormat = remember {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    }

    val timeFormat = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    val filteredBookings = remember(bookings, selectedFilter) {
        bookings.filter { booking ->
            when (selectedFilter) {
                "Upcoming" ->
                    booking.endTime >= System.currentTimeMillis() &&
                            booking.bookingStatus in listOf("PENDING", "CONFIRMED")

                "Completed" ->
                    booking.endTime < System.currentTimeMillis() &&
                            booking.bookingStatus != "CANCELLED"

                "Cancelled" ->
                    booking.bookingStatus == "CANCELLED"

                else -> true
            }
        }.sortedByDescending { it.createdAt }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp)
    ) {
        Text(
            "My Bookings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Manage your parking reservations",
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        TabRow(
            selectedTabIndex = listOf(
                "Upcoming", "Completed", "Cancelled"
            ).indexOf(selectedFilter)
        ) {
            listOf("Upcoming", "Completed", "Cancelled")
                .forEach { label ->
                    Tab(
                        selected = selectedFilter == label,
                        onClick = { selectedFilter = label },
                        text = { Text(label) }
                    )
                }
        }

        Spacer(Modifier.height(16.dp))

        if (filteredBookings.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 70.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    when (selectedFilter) {
                        "Upcoming" -> "No upcoming bookings"
                        "Completed" -> "No completed bookings"
                        else -> "No cancelled bookings"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    "Your bookings will appear here.",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                items(
                    items = filteredBookings,
                    key = { it.id }
                ) { booking ->

                    Card(
                        onClick = {
                            selectedBooking = booking
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    booking.parkingName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                Surface(
                                    color = when (booking.bookingStatus) {
                                        "CANCELLED" -> Color(0xFFFFEBEE)
                                        "CONFIRMED" -> Color(0xFFE8F5E9)
                                        else -> Color(0xFFFFF8E1)
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        booking.bookingStatus,
                                        color = when (booking.bookingStatus) {
                                            "CANCELLED" -> Color(0xFFC62828)
                                            "CONFIRMED" -> Color(0xFF2E7D32)
                                            else -> Color(0xFF9A6700)
                                        },
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 5.dp
                                        ),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            HorizontalDivider()

                            BookingDetailRow(
                                "Booking ID",
                                booking.id
                            )

                            BookingDetailRow(
                                "Date",
                                dateFormat.format(Date(booking.startTime))
                            )

                            BookingDetailRow(
                                "Time",
                                "${timeFormat.format(Date(booking.startTime))} - " +
                                        timeFormat.format(Date(booking.endTime))
                            )

                            BookingDetailRow(
                                "Total amount",
                                "৳${String.format(Locale.US, "%.2f", booking.totalAmount)}",
                                emphasize = true
                            )

                            Text(
                                "Tap to view booking details",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )

                            if (
                                booking.bookingStatus == "PENDING" ||
                                booking.bookingStatus == "CONFIRMED"
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        bookingToCancel = booking
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFD32F2F)
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Cancel Booking")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Booking details popup
    selectedBooking?.let { booking ->
        AlertDialog(
            onDismissRequest = {
                selectedBooking = null
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Booking Details",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            selectedBooking = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close booking details"
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BookingDetailRow("Parking", booking.parkingName)
                    BookingDetailRow("Booking ID", booking.id)

                    BookingDetailRow(
                        "Date",
                        dateFormat.format(Date(booking.startTime))
                    )

                    BookingDetailRow(
                        "Start time",
                        timeFormat.format(Date(booking.startTime))
                    )

                    BookingDetailRow(
                        "End time",
                        timeFormat.format(Date(booking.endTime))
                    )

                    BookingDetailRow(
                        "Duration",
                        "${booking.durationHours} hours"
                    )

                    BookingDetailRow(
                        "Hourly rate",
                        "৳${String.format(Locale.US, "%.2f", booking.pricePerHour)}"
                    )

                    BookingDetailRow(
                        "Payment method",
                        booking.paymentMethod
                    )

                    BookingDetailRow(
                        "Payment status",
                        booking.paymentStatus
                    )

                    BookingDetailRow(
                        "Booking status",
                        booking.bookingStatus
                    )

                    HorizontalDivider()

                    BookingDetailRow(
                        "Total amount",
                        "৳${String.format(Locale.US, "%.2f", booking.totalAmount)}",
                        emphasize = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedBooking = null
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Cancellation confirmation popup
    bookingToCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = {
                bookingToCancel = null
            },
            title = {
                Text("Cancel Booking?")
            },
            text = {
                Text(
                    "Are you sure you want to cancel your booking at " +
                            "${booking.parkingName}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelBooking(booking)
                        bookingToCancel = null
                        selectedBooking = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        bookingToCancel = null
                    }
                ) {
                    Text("Keep Booking")
                }
            }
        )
    }
}

@Composable
private fun BookingDetailRow(
    label: String,
    value: String,
    emphasize: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            value,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasize) Color(0xFF155EEF)
            else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}


