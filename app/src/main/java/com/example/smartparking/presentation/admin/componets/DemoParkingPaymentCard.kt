
package com.example.smartparking.presentation.admin.componets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartparking.data.repository.AdminParkingSession
import com.example.smartparking.ui.theme.ParkwayBlack
import com.example.smartparking.ui.theme.ParkwayGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DemoPaymentGreen = Color(0xFF238636)

@Composable
fun DemoParkingPaymentCard(
    session: AdminParkingSession
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp),
        color = Color.White,
        shape = RoundedCornerShape(15.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = session.parkingName.ifBlank {
                            "Parking facility"
                        },
                        color = ParkwayBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Session ID: ${session.id}",
                        color = ParkwayGray,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                DemoPaymentStatusPill(
                    text = "DEMO PAID"
                )
            }

            Spacer(Modifier.height(14.dp))

            DemoPaymentDetailLine(
                label = "Driver ID",
                value = session.driverId.ifBlank { "Not available" }
            )

            DemoPaymentDetailLine(
                label = "Parking ID",
                value = session.parkingId.ifBlank { "Not available" }
            )

            DemoPaymentDetailLine(
                label = "Hourly rate",
                value = "${formatDemoMoney(session.hourlyRate)}/hour"
            )

            DemoPaymentDetailLine(
                label = "Duration",
                value = formatDemoDuration(session.durationMinutes)
            )

            DemoPaymentDetailLine(
                label = "Total paid",
                value = formatDemoMoney(session.totalPaid),
                highlight = true
            )

            DemoPaymentDetailLine(
                label = "Payment date",
                value = formatDemoDateTime(session.paidAt)
            )

            DemoPaymentDetailLine(
                label = "Session status",
                value = session.status
            )

            DemoPaymentDetailLine(
                label = "Extensions",
                value = session.extensionCount.toString()
            )

            DemoPaymentDetailLine(
                label = "Expires at",
                value = formatDemoDateTime(session.expiresAt)
            )

            Spacer(Modifier.height(10.dp))

            // Successful demo checkout indicator
            Surface(
                color = DemoPaymentGreen.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        color = DemoPaymentGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.width(9.dp))

                    Text(
                        text = "Demo checkout completed successfully",
                        color = DemoPaymentGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoPaymentStatusPill(
    text: String
) {
    Surface(
        color = DemoPaymentGreen.copy(alpha = 0.10f),
        shape = RoundedCornerShape(50.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
            color = DemoPaymentGreen,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DemoPaymentDetailLine(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = ParkwayGray,
            fontSize = 11.sp
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = value,
            modifier = Modifier.weight(1.2f),
            color = if (highlight) DemoPaymentGreen else ParkwayBlack,
            fontSize = 11.sp,
            fontWeight = if (highlight) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDemoMoney(amount: Double): String {
    return "৳${String.format(Locale.US, "%.2f", amount)}"
}

private fun formatDemoDuration(minutes: Int): String {
    if (minutes <= 0) return "Not available"

    val hours = minutes / 60
    val remainingMinutes = minutes % 60

    return when {
        hours > 0 && remainingMinutes > 0 ->
            "${hours}h ${remainingMinutes}m"

        hours > 0 ->
            "${hours}h"

        else ->
            "${remainingMinutes}m"
    }
}

private fun formatDemoDateTime(timestamp: Long): String {
    if (timestamp <= 0L) return "Not available"

    return try {
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date(timestamp))
    } catch (_: Exception) {
        "Not available"
    }
}
