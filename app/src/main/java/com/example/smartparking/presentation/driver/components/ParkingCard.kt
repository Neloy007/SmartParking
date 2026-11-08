package com.example.smartparking.presentation.driver.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartparking.data.model.Parking
import com.example.smartparking.presentation.driver.utils.DriverColors

@Composable
fun ParkingCard(
    parking: Parking,
    distance: String,
    onClick: () -> Unit,
    onNavigate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DriverColors.Surface
        ),
        border = BorderStroke(1.dp, DriverColors.Border),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(17.dp)
        ) {

            // Parking icon and distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = DriverColors.Input,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = DriverColors.Black,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DriverColors.SecondaryText,
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(Modifier.width(3.dp))

                    Text(
                        text = distance,
                        color = DriverColors.SecondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(15.dp))

            // Parking name
            Text(
                text = parking.name,
                color = DriverColors.MainText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            // Parking address
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = DriverColors.SecondaryText,
                    modifier = Modifier.size(15.dp)
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = parking.address,
                    color = DriverColors.SecondaryText,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(
                color = DriverColors.Border,
                thickness = 1.dp
            )

            Spacer(Modifier.height(14.dp))

            // Price and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Price
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "৳${parking.pricePerHour}",
                            color = DriverColors.Black,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = "/ hour",
                            color = DriverColors.SecondaryText,
                            fontSize = 11.sp
                        )
                    }
                }

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DriverColors.Black,
                            contentColor = DriverColors.White
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 12.dp
                        )
                    ) {
                        Text(
                            text = "View details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.width(5.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigate
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Navigate",
                            tint = DriverColors.Black
                        )
                    }
                }
            }
        }
    }
}