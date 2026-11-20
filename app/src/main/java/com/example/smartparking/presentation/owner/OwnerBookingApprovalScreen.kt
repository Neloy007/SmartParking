
package com.example.smartparking.presentation.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
private val ParkwayRed = Color(0xFFD32F2F)
private val ParkwayGreen = Color(0xFF218838)

@Composable
fun OwnerBookingApprovalScreen(
    bookings: List<Booking>,
    isLoading: Boolean,
    errorMessage: String? = null,
    processingBookingId: String? = null,
    onApprove: (Booking) -> Unit,
    onReject: (Booking) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAction by remember {
        mutableStateOf<Pair<Booking, String>?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {

        // HEADER
        Text(
            text = "Booking Requests",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = ParkwayBlack
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Review driver payments and manage booking requests.",
            fontSize = 13.sp,
            color = ParkwayGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SUMMARY CARD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    ParkwayLightGray,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Pending requests",
                    color = ParkwayGray,
                    fontSize = 13.sp
                )

                Text(
                    text = bookings.size.toString(),
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold,
                    color = ParkwayBlack
                )
            }

            Button(
                onClick = onRefresh,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParkwayBlack,
                    disabledContainerColor = ParkwayGray
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isLoading) "Loading..." else "Refresh",
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ERROR MESSAGE
        if (!errorMessage.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "Something went wrong",
                        color = ParkwayRed,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = errorMessage,
                        color = ParkwayRed,
                        fontSize = 13.sp
                    )

                    TextButton(onClick = onRefresh) {
                        Text(
                            text = "Try again",
                            color = ParkwayRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // LOADING STATE
        if (isLoading && bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = ParkwayLime
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Loading booking requests...",
                        color = ParkwayGray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // EMPTY STATE
        else if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No pending requests",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = ParkwayBlack
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = "New driver payment requests will appear here.",
                        fontSize = 13.sp,
                        color = ParkwayGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onRefresh,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Refresh requests")
                    }
                }
            }
        }

        // BOOKING REQUEST LIST
        else {
            Text(
                text = "Payment requests (${bookings.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ParkwayBlack
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = bookings,
                    key = { it.id }
                ) { booking ->

                    OwnerBookingRequestCard(
                        booking = booking,
                        isProcessing = processingBookingId == booking.id,
                        onApprove = {
                            selectedAction = booking to "APPROVE"
                        },
                        onReject = {
                            selectedAction = booking to "REJECT"
                        }
                    )
                }
            }
        }
    }

    // APPROVAL / REJECTION CONFIRMATION
    selectedAction?.let { (booking, action) ->

        val isApprove = action == "APPROVE"

        AlertDialog(
            onDismissRequest = {
                selectedAction = null
            },
            title = {
                Text(
                    text = if (isApprove) {
                        "Approve payment?"
                    } else {
                        "Reject payment?"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isApprove) {
                        "Confirm that you have received the bKash payment. This will confirm the booking and create a parking session."
                    } else {
                        "Are you sure you want to reject this driver's payment request?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedAction = null

                        if (isApprove) {
                            onApprove(booking)
                        } else {
                            onReject(booking)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApprove) {
                            ParkwayGreen
                        } else {
                            ParkwayRed
                        }
                    )
                ) {
                    Text(
                        if (isApprove) "Confirm approval"
                        else "Confirm rejection"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedAction = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun OwnerBookingRequestCard(
    booking: Booking,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            ParkwayBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // BOOKING HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = booking.parkingName.ifBlank {
                            "Parking facility"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ParkwayBlack
                    )

                    Text(
                        text = "Booking ID: ${booking.id.take(12)}",
                        fontSize = 11.sp,
                        color = ParkwayGray
                    )
                }

                Surface(
                    color = Color(0xFFFFF3CD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "PENDING",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = Color(0xFF856404),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            HorizontalDivider(color = ParkwayBorder)

            Spacer(modifier = Modifier.height(14.dp))

            // DRIVER INFORMATION
            Text(
                text = "DRIVER INFORMATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ParkwayGray
            )

            Spacer(modifier = Modifier.height(9.dp))

            BookingInfoRow(
                label = "Driver",
                value = booking.driverName.ifBlank { "Not provided" }
            )

            BookingInfoRow(
                label = "Phone",
                value = booking.driverPhone.ifBlank { "Not provided" }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // SCHEDULE
            Text(
                text = "BOOKING SCHEDULE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ParkwayGray
            )

            Spacer(modifier = Modifier.height(9.dp))

            BookingInfoRow(
                label = "Start",
                value = formatBookingDate(booking.startTime)
            )

            BookingInfoRow(
                label = "End",
                value = formatBookingDate(booking.endTime)
            )

            BookingInfoRow(
                label = "Duration",
                value = "${booking.durationHours} hours"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // PAYMENT INFORMATION
            Text(
                text = "PAYMENT INFORMATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ParkwayGray
            )

            Spacer(modifier = Modifier.height(9.dp))

            BookingInfoRow(
                label = "Payment method",
                value = booking.paymentMethod
            )

            BookingInfoRow(
                label = "Hourly price",
                value = "৳${formatAmount(booking.pricePerHour)}"
            )

            BookingInfoRow(
                label = "Total amount",
                value = "৳${formatAmount(booking.totalAmount)}",
                valueColor = ParkwayGreen,
                bold = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TRANSACTION ID
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        ParkwayLightGray,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "BKASH TRANSACTION ID",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ParkwayGray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = booking.transactionId.ifBlank {
                            "No transaction ID"
                        },
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ParkwayBlack
                    )

                    TextButton(
                        enabled = booking.transactionId.isNotBlank(),
                        onClick = {
                            clipboardManager.setText(
                                AnnotatedString(booking.transactionId)
                            )
                        }
                    ) {
                        Text(
                            text = "COPY",
                            color = ParkwayBlack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            BookingInfoRow(
                label = "Submitted",
                value = formatBookingDate(booking.createdAt)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // APPROVE / REJECT BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ParkwayRed
                    )
                ) {
                    Text("Reject")
                }

                Button(
                    onClick = onApprove,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParkwayBlack
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(17.dp),
                            color = ParkwayLime,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Approve",
                            color = ParkwayLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingInfoRow(
    label: String,
    value: String,
    valueColor: Color = ParkwayBlack,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = ParkwayGray,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = if (bold) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            modifier = Modifier.weight(1.4f)
        )
    }
}

private fun formatBookingDate(timestamp: Long): String {
    if (timestamp <= 0L) return "Not available"

    return try {
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date(timestamp))
    } catch (e: Exception) {
        "Not available"
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%.2f", amount)
}
