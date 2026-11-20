
package com.example.smartparking.presentation.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartparking.data.model.Booking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ParkwayBlack = Color(0xFF080808)
private val ParkwayLime = Color(0xFFB8F34A)
private val ParkwayGray = Color(0xFF777777)
private val ParkwayLightGray = Color(0xFFF4F4F4)
private val ParkwayBorder = Color(0xFFEEEEEE)
private val ParkwayError = Color(0xFFD32F2F)

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
                "Upcoming" -> booking.bookingStatus in listOf(
                    "PENDING",
                    "WAITING_FOR_APPROVAL",
                    "CONFIRMED"
                )

                "Completed" -> booking.bookingStatus == "COMPLETED"

                "Cancelled" -> booking.bookingStatus in listOf(
                    "CANCELLED",
                    "REJECTED"
                )

                else -> true
            }
        }.sortedByDescending { it.createdAt }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // HEADER
        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 18.dp
            )
        ) {
            Text(
                text = "My Bookings",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = ParkwayBlack
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Manage your parking reservations",
                fontSize = 13.sp,
                color = ParkwayGray
            )
        }

        // FILTER TABS
        val filters = listOf("Upcoming", "Completed", "Cancelled")

        TabRow(
            selectedTabIndex = filters.indexOf(selectedFilter),
            containerColor = Color.White,
            contentColor = ParkwayBlack
        ) {
            filters.forEach { filter ->
                Tab(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    text = {
                        Text(
                            text = filter,
                            fontWeight = if (selectedFilter == filter) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredBookings.isEmpty()) {
            EmptyBookingsState(selectedFilter)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = filteredBookings,
                    key = { it.id }
                ) { booking ->

                    BookingCard(
                        booking = booking,
                        dateFormat = dateFormat,
                        timeFormat = timeFormat,
                        onClick = { selectedBooking = booking },
                        onCancel = { bookingToCancel = booking }
                    )
                }
            }
        }
    }

    // BOOKING DETAILS DIALOG
    selectedBooking?.let { booking ->
        AlertDialog(
            onDismissRequest = { selectedBooking = null },
            title = {
                Text(
                    text = "Booking Details",
                    fontWeight = FontWeight.Bold,
                    color = ParkwayBlack
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    BookingDetailRow("Parking", booking.parkingName)
                    BookingDetailRow("Booking ID", booking.id)
                    BookingDetailRow(
                        "Date",
                        dateFormat.format(Date(booking.startTime))
                    )
                    BookingDetailRow(
                        "Start",
                        timeFormat.format(Date(booking.startTime))
                    )
                    BookingDetailRow(
                        "End",
                        timeFormat.format(Date(booking.endTime))
                    )
                    BookingDetailRow(
                        "Duration",
                        "${booking.durationHours} hours"
                    )
                    BookingDetailRow(
                        "Hourly Rate",
                        "৳${booking.pricePerHour}"
                    )
                    BookingDetailRow(
                        "Payment Method",
                        booking.paymentMethod
                    )
                    BookingDetailRow(
                        "Transaction ID",
                        booking.transactionId.ifBlank { "Not available" }
                    )
                    BookingDetailRow(
                        "Payment Status",
                        booking.paymentStatus
                    )
                    BookingDetailRow(
                        "Booking Status",
                        booking.bookingStatus
                    )
                    BookingDetailRow(
                        "Total Amount",
                        "৳${booking.totalAmount}"
                    )

                    if (booking.rejectionReason.isNotBlank()) {
                        Divider(color = ParkwayBorder)

                        Text(
                            text = "Rejection Reason",
                            fontWeight = FontWeight.Bold,
                            color = ParkwayError
                        )

                        Text(
                            text = booking.rejectionReason,
                            color = ParkwayGray,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { selectedBooking = null }
                ) {
                    Text(
                        "Close",
                        color = ParkwayBlack
                    )
                }
            },
            containerColor = Color.White
        )
    }

    // CANCEL CONFIRMATION DIALOG
    bookingToCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = ParkwayError
                )
            },
            title = {
                Text(
                    "Cancel Booking?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to cancel your booking at ${booking.parkingName}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelBooking(booking)
                        bookingToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParkwayError
                    )
                ) {
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { bookingToCancel = null }
                ) {
                    Text(
                        "Keep Booking",
                        color = ParkwayBlack
                    )
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    dateFormat: SimpleDateFormat,
    timeFormat: SimpleDateFormat,
    onClick: () -> Unit,
    onCancel: () -> Unit
) {
    val statusColor = when (booking.bookingStatus) {
        "CONFIRMED" -> Color(0xFF218838)
        "WAITING_FOR_APPROVAL" -> Color(0xFFE59B19)
        "REJECTED", "CANCELLED" -> ParkwayError
        "COMPLETED" -> ParkwayGray
        else -> ParkwayGray
    }

    val statusLabel = when (booking.bookingStatus) {
        "WAITING_FOR_APPROVAL" -> "Waiting for Approval"
        "CONFIRMED" -> "Confirmed"
        "REJECTED" -> "Rejected"
        "CANCELLED" -> "Cancelled"
        "COMPLETED" -> "Completed"
        else -> booking.bookingStatus
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = booking.parkingName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ParkwayBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "ID: ${booking.id}",
                        fontSize = 10.sp,
                        color = ParkwayGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = ParkwayGray,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = dateFormat.format(Date(booking.startTime)),
                    fontSize = 12.sp,
                    color = ParkwayGray
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = ParkwayGray,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${timeFormat.format(Date(booking.startTime))} - ${
                        timeFormat.format(Date(booking.endTime))
                    }",
                    fontSize = 12.sp,
                    color = ParkwayGray
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = ParkwayBorder)

            Spacer(modifier = Modifier.height(13.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Amount",
                        fontSize = 11.sp,
                        color = ParkwayGray
                    )

                    Text(
                        text = "৳${booking.totalAmount}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = ParkwayBlack
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (
                        booking.bookingStatus == "PENDING" ||
                        booking.bookingStatus == "WAITING_FOR_APPROVAL"
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = 8.dp
                            )
                        ) {
                            Text(
                                "Cancel",
                                color = ParkwayError,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParkwayLime
                        )
                    ) {
                        Text(
                            "Details",
                            color = ParkwayBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (booking.bookingStatus == "WAITING_FOR_APPROVAL") {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your TrxID has been submitted. Waiting for the owner to verify your payment.",
                    color = Color(0xFF9A6900),
                    fontSize = 11.sp
                )
            }

            if (booking.bookingStatus == "CONFIRMED") {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF218838),
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Payment approved. Your parking session is active.",
                        color = Color(0xFF218838),
                        fontSize = 11.sp
                    )
                }
            }

            if (booking.bookingStatus == "REJECTED") {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Payment verification was rejected.",
                    color = ParkwayError,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun BookingDetailRow(
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
            fontSize = 12.sp,
            color = ParkwayGray,
            modifier = Modifier.weight(0.9f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ParkwayBlack,
            modifier = Modifier.weight(1.1f)
        )
    }
}

@Composable
private fun EmptyBookingsState(filter: String) {
    val message = when (filter) {
        "Upcoming" -> "You don't have any upcoming bookings."
        "Completed" -> "You haven't completed any parking sessions yet."
        "Cancelled" -> "You don't have any cancelled or rejected bookings."
        else -> "No bookings found."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ParkwayLightGray,
            modifier = Modifier.size(75.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = ParkwayGray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "No Bookings Yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ParkwayBlack
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = message,
            fontSize = 13.sp,
            color = ParkwayGray
        )
    }
}
