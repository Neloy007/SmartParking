package com.example.smartparking.presentation.driver.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.smartparking.presentation.driver.utils.DriverColors

@Composable
fun DriverHeader(
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = DriverColors.Black
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "P",
                        color = DriverColors.Lime,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "parkway",
                    color = DriverColors.Black,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = "PARK SMARTER. LIVE BETTER.",
                    color = DriverColors.SecondaryText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onRefresh),
            shape = CircleShape,
            color = DriverColors.Input
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh parking",
                    tint = DriverColors.Black
                )
            }
        }
    }
}