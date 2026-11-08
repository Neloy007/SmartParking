package com.example.smartparking.presentation.driver.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.smartparking.presentation.driver.utils.DriverColors

@Composable
fun FloatingDriverNavigation(
    selectedTab: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = RoundedCornerShape(23.dp),
        color = DriverColors.Black,
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DriverNavItem(
                label = "Home",
                icon = Icons.Default.Home,
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(0) }
            )

            DriverNavItem(
                label = "Bookings",
                icon = Icons.Default.CalendarMonth,
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(1) }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelect(2) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = DriverColors.Lime,
                        modifier = Modifier.size(39.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Nearby",
                                tint = DriverColors.Black,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = "Nearby",
                        color = if (selectedTab == 2) {
                            DriverColors.Lime
                        } else {
                            DriverColors.White.copy(alpha = 0.65f)
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            DriverNavItem(
                label = "Saved",
                icon = Icons.Default.Favorite,
                selected = selectedTab == 3,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(3) }
            )

            DriverNavItem(
                label = "Profile",
                icon = Icons.Default.Person,
                selected = selectedTab == 4,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(4) }
            )
        }
    }
}

@Composable
private fun DriverNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) {
                DriverColors.Lime
            } else {
                DriverColors.White.copy(alpha = 0.65f)
            },
            modifier = Modifier.size(21.dp)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = label,
            color = if (selected) {
                DriverColors.Lime
            } else {
                DriverColors.White.copy(alpha = 0.65f)
            },
            fontSize = 9.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}