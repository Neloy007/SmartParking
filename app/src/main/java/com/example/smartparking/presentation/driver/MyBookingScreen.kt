package com.example.smartparking.presentation.driver


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyBookingsScreen(
    bookings: List<DemoParkingBooking>
) {
    var selectedFilter by remember { mutableStateOf("Upcoming") }

    val filteredBookings = remember(bookings, selectedFilter) {
        bookings.filter { booking ->
            when (selectedFilter) {
                "Upcoming" -> booking.endTime >= System.currentTimeMillis()
                "Completed" -> booking.endTime < System.currentTimeMillis()
                "Cancelled" -> booking.bookingStatus == "CANCELLED"
                else -> true
            }
        }
    }

    val dateFormat = remember {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    }

    val timeFormat = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
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
            selectedTabIndex = listOf("Upcoming", "Completed", "Cancelled")
                .indexOf(selectedFilter)
        ) {
            listOf("Upcoming", "Completed", "Cancelled").forEachIndexed { index, label ->
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
                    items = filteredBookings.sortedByDescending { it.createdAt },
                    key = { it.id }
                ) { booking ->

                    Card(
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
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        "Demo",
                                        color = Color(0xFF155EEF),
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 5.dp
                                        ),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Text(
                                booking.parkingAddress,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )

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
                                "Duration",
                                "${booking.durationHours} hours"
                            )

                            BookingDetailRow(
                                "Payment method",
                                booking.paymentMethod
                            )

                            BookingDetailRow(
                                "Payment status",
                                booking.paymentStatus
                            )

                            HorizontalDivider()

                            BookingDetailRow(
                                "Total amount",
                                "৳${String.format(Locale.US, "%.2f", booking.totalAmount)}",
                                emphasize = true
                            )

                            Text(
                                "Demo booking only — no real parking slot has been reserved.",
                                color = Color(0xFF64748B),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        Text(
            value,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasize) Color(0xFF155EEF) else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}
