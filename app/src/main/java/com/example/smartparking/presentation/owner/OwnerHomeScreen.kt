package com.example.smartparking.presentation.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    onRegisterParking: () -> Unit = {},
    viewModel: OwnerParkingListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadParkings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Owner Dashboard") },
                actions = {
                    IconButton(onClick = { viewModel.loadParkings() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onRegisterParking) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Register Parking"
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "My Parking Facilities",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = { viewModel.loadParkings() }) {
                        Text("Try Again")
                    }
                }

                uiState.parkings.isEmpty() -> {
                    Text("You haven't registered any parking facilities yet.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onRegisterParking) {
                        Text("Register Your First Parking")
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.parkings,
                            key = { it.id }
                        ) { parking ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        parking.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(parking.address)

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        "Available: ${parking.availableSlots}/${parking.totalSlots}"
                                    )

                                    Text(
                                        "Price: ৳${parking.pricePerHour}/hour"
                                    )

                                    Text(
                                        if (parking.active) "Active" else "Inactive",
                                        color = if (parking.active)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}