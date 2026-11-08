
package com.example.smartparking.presentation.driver.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartparking.data.model.Parking
import com.example.smartparking.presentation.driver.utils.DriverColors

@Composable
fun ParkingDetailsDialog(
    parking: Parking,
    distance: String,
    onDismiss: () -> Unit,
    onGoThere: () -> Unit,
    onBookParking: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DriverColors.White,
        titleContentColor = DriverColors.Black,
        textContentColor = DriverColors.SecondaryText,
        shape = RoundedCornerShape(24.dp),

        title = {
            Column {
                Surface(
                    color = DriverColors.Input,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = DriverColors.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(15.dp))

                Text(
                    text = parking.name,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = DriverColors.Black
                )
            }
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DetailRow(
                    icon = Icons.Default.LocationOn,
                    label = "Address",
                    value = parking.address
                )

                DetailRow(
                    icon = Icons.Default.NearMe,
                    label = "Distance",
                    value = distance
                )

                DetailRow(
                    icon = Icons.Default.EventAvailable,
                    label = "Available slots",
                    value = "${parking.availableSlots}"
                )

                DetailRow(
                    icon = Icons.Default.LocalParking,
                    label = "Total slots",
                    value = "${parking.totalSlots}"
                )

                DetailRow(
                    icon = Icons.Default.Payments,
                    label = "Hourly price",
                    value = "৳${parking.pricePerHour}/hour"
                )
            }
        },

        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Button(
                    onClick = onBookParking,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(7.dp))

                    Text(
                        text = "Book Parking",
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onGoThere,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DriverColors.Black,
                        contentColor = DriverColors.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(7.dp))

                    Text(
                        text = "Navigate",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = DriverColors.SecondaryText
                )
            }
        }
    )
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = DriverColors.Input,
            shape = RoundedCornerShape(9.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DriverColors.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.width(11.dp))

        Column {
            Text(
                text = label,
                color = DriverColors.SecondaryText,
                fontSize = 10.sp
            )

            Text(
                text = value,
                color = DriverColors.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
