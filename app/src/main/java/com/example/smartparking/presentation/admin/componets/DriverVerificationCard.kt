
package com.example.smartparking.presentation.admin.componets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartparking.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AdminBlack = Color(0xFF080808)
private val AdminLime = Color(0xFFB8F34A)
private val AdminGray = Color(0xFF777777)
private val AdminRed = Color(0xFFD32F2F)
private val AdminGreen = Color(0xFF238636)

@Composable
fun DriverVerificationCard(
    driver: User,
    isLoading: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        color = Color.White,
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.width(43.dp).height(43.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = AdminLime.copy(alpha = 0.35f)
                ) {
                    androidx.compose.foundation.layout.Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = driver.name.firstOrNull()?.uppercase() ?: "?",
                            color = AdminBlack,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = driver.name.ifBlank { "Unnamed driver" },
                        color = AdminBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = driver.email,
                        color = AdminGray,
                        fontSize = 11.sp
                    )
                }

                VerificationStatusPill("PENDING")
            }

            Spacer(Modifier.height(15.dp))

            Text(
                text = "IDENTITY & VEHICLE DETAILS",
                color = AdminGray,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )

            Spacer(Modifier.height(8.dp))

            VerificationDetailLine("Driver ID", driver.uid)
            VerificationDetailLine("Phone", driver.phone)
            VerificationDetailLine("Car number", driver.carNumber)
            VerificationDetailLine("NID number", driver.nidNumber)
            VerificationDetailLine(
                "Driving licence",
                driver.drivingLicenseNumber
            )
            VerificationDetailLine(
                "Submitted",
                formatVerificationDate(driver.verificationSubmittedAt)
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Button(
                    onClick = onApprove,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminBlack
                    )
                ) {
                    Text(
                        text = "Approve",
                        color = AdminLime,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onReject,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Reject",
                        color = AdminRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isLoading) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Processing admin action...",
                    color = AdminGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun VerificationDetailLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.width(105.dp),
            color = AdminGray,
            fontSize = 11.sp
        )

        Text(
            text = value.ifBlank { "Not provided" },
            modifier = Modifier.weight(1f),
            color = AdminBlack,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun VerificationStatusPill(text: String) {
    Surface(
        color = Color(0xFFFFF1D6),
        shape = CircleShape
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 5.dp
            ),
            color = Color(0xFF9A6500),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatVerificationDate(timestamp: Long): String {
    if (timestamp <= 0L) return "Not available"

    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date(timestamp))
}
