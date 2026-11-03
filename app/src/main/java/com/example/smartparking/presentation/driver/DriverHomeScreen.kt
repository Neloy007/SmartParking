
package com.example.smartparking.presentation.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF0B1220)
private val SurfaceColor = Color(0xFF151F30)
private val PrimaryBlue = Color(0xFF3985FF)
private val MainText = Color(0xFFF1F5F9)
private val SecondaryText = Color(0xFF94A3B8)
private val Green = Color(0xFF22C55E)

private data class ParkingPlace(
    val name: String,
    val address: String,
    val distance: String,
    val price: Int,
    val availableSlots: Int,
    val rating: Double,
    val category: String
)

private val sampleParkingPlaces = listOf(
    ParkingPlace(
        "Dhanmondi City Parking",
        "Dhanmondi 27, Dhaka",
        "0.8 km",
        40,
        12,
        4.8,
        "Popular"
    ),
    ParkingPlace(
        "Bashundhara Parking Hub",
        "Bashundhara R/A, Dhaka",
        "2.1 km",
        50,
        8,
        4.6,
        "Nearby"
    ),
    ParkingPlace(
        "Mirpur Smart Parking",
        "Mirpur 10, Dhaka",
        "3.5 km",
        30,
        20,
        4.5,
        "Affordable"
    ),
    ParkingPlace(
        "Gulshan Central Parking",
        "Gulshan 2, Dhaka",
        "4.2 km",
        60,
        5,
        4.9,
        "Popular"
    )
)

@Composable
fun DriverHomeScreen() {

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedParking by remember { mutableStateOf<ParkingPlace?>(null) }

    val categories = listOf("All", "Popular", "Nearby", "Affordable")

    val filteredParking = sampleParkingPlaces.filter { parking ->
        val matchesSearch =
            parking.name.contains(searchQuery, ignoreCase = true) ||
                    parking.address.contains(searchQuery, ignoreCase = true)

        val matchesCategory =
            selectedCategory == "All" ||
                    parking.category == selectedCategory

        matchesSearch && matchesCategory
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceColor,
                contentColor = MainText
            ) {
                val tabs = listOf(
                    Triple("Home", Icons.Default.Home, 0),
                    Triple("Bookings", Icons.Default.CalendarMonth, 1),
                    Triple("Saved", Icons.Default.Favorite, 2),
                    Triple("Profile", Icons.Default.Person, 3)
                )

                tabs.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label
                            )
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            unselectedIconColor = SecondaryText,
                            unselectedTextColor = SecondaryText,
                            indicatorColor = Color(0xFF233B5D)
                        )
                    )
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {
                DriverHeader()
            }

            item {
                LocationSection()
            }

            item {
                SearchSection(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }

            item {
                ParkingMap()
            }

            item {
                Column {
                    Text(
                        text = "Find Your Parking",
                        color = MainText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Choose a parking spot near you",
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category
                            },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                labelColor = SecondaryText
                            )
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Parking",
                        color = MainText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Text(
                        text = "${filteredParking.size} places",
                        color = PrimaryBlue,
                        fontSize = 13.sp
                    )
                }
            }

            if (filteredParking.isEmpty()) {
                item {
                    Text(
                        text = "No parking places found.",
                        color = SecondaryText,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(filteredParking) { parking ->
                    ParkingCard(
                        parking = parking,
                        onClick = {
                            selectedParking = parking
                        }
                    )
                }
            }
        }
    }

    selectedParking?.let { parking ->
        ParkingDetailsDialog(
            parking = parking,
            onDismiss = { selectedParking = null }
        )
    }
}

@Composable
private fun DriverHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(14.dp),
                color = PrimaryBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalParking,
                        contentDescription = "Smart Parking",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "SMART PARKING",
                    color = MainText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )

                Text(
                    text = "Find your perfect spot",
                    color = SecondaryText,
                    fontSize = 11.sp
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = SurfaceColor,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = MainText
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationSection() {
    Column {
        Text(
            text = "YOUR LOCATION",
            color = SecondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(7.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(21.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = "Dhaka, Bangladesh",
                color = MainText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.width(5.dp))

            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = SecondaryText
            )
        }
    }
}

@Composable
private fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                SurfaceColor,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 15.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = "Search",
            tint = PrimaryBlue
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = MainText,
                fontSize = 14.sp
            ),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            "Search parking locations...",
                            color = SecondaryText,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Clear search",
                    tint = SecondaryText
                )
            }
        }
    }
}

@Composable
private fun ParkingMapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .background(
                Color(0xFF172A42),
                RoundedCornerShape(22.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Map,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Explore Parking Around You",
                color = MainText,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Google Maps integration coming next",
                color = SecondaryText,
                fontSize = 12.sp
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(10.dp),
            color = SurfaceColor
        ) {
            Text(
                text = "MAP VIEW",
                color = PrimaryBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                )
            )
        }
    }
}

@Composable
private fun ParkingCard(
    parking: ParkingPlace,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF233B5D)
                ) {
                    Icon(
                        Icons.Default.LocalParking,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(27.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC857),
                            modifier = Modifier.size(15.dp)
                        )

                        Text(
                            text = " ${parking.rating}",
                            color = MainText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = parking.distance,
                        color = SecondaryText,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = parking.name,
                color = MainText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(15.dp)
                )

                Text(
                    text = parking.address,
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            HorizontalDivider(color = Color(0xFF29374A))

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "৳${parking.price}/hour",
                        color = PrimaryBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${parking.availableSlots} slots available",
                        color = Green,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    Text("View Details")
                }
            }
        }
    }
}

@Composable
private fun ParkingDetailsDialog(
    parking: ParkingPlace,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        titleContentColor = MainText,
        textContentColor = SecondaryText,
        title = {
            Text(
                text = parking.name,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Location: ${parking.address}")
                Text("Distance: ${parking.distance}")
                Text("Rating: ${parking.rating} / 5")
                Text("Available slots: ${parking.availableSlots}")
                Text("Price: ৳${parking.price} per hour")

                Text(
                    text = "Sample parking information. Live availability and booking will be connected to Firebase later.",
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Close",
                    color = PrimaryBlue
                )
            }
        }
    )
}
