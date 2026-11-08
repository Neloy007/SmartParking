package com.example.smartparking.presentation.driver.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.smartparking.presentation.driver.utils.DriverColors

@Composable
fun PlaceholderDriverTab(
    tab: Int
) {
    val title = when (tab) {
        1 -> "My Bookings"
        3 -> "Saved Parking"
        4 -> "My Profile"
        else -> "Parkway"
    }

    val subtitle = when (tab) {
        1 -> "Your parking reservations will appear here."
        3 -> "Your favourite parking spaces will appear here."
        4 -> "Manage your personal account information."
        else -> "Your smart parking experience."
    }

    val icon = when (tab) {
        1 -> Icons.Default.CalendarMonth
        3 -> Icons.Default.Favorite
        4 -> Icons.Default.Person
        else -> Icons.Default.DirectionsCar
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DriverColors.Background)
            .padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(30.dp)
        ) {
            Surface(
                color = DriverColors.Input,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DriverColors.Black,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = title,
                color = DriverColors.Black,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = subtitle,
                color = DriverColors.SecondaryText,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(20.dp))

            Surface(
                color = DriverColors.Lime,
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "COMING SOON",
                    color = DriverColors.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(
                        horizontal = 17.dp,
                        vertical = 9.dp
                    )
                )
            }
        }
    }
}