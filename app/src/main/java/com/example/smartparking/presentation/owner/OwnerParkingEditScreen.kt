package com.example.smartparking.presentation.owner



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartparking.data.model.Parking
import kotlinx.coroutines.launch

private val EditBlack = Color(0xFF080808)
private val EditLime = Color(0xFFB8F34A)
private val EditGray = Color(0xFF777777)
private val EditBorder = Color(0xFFEEEEEE)
private val EditBackground = Color(0xFFF4F4F4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerParkingEditScreen(
    parkingId: String,
    onBack: () -> Unit,
    onUpdated: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: OwnerParkingListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val parking = uiState.parkings.firstOrNull { it.id == parkingId }

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var totalSlots by remember { mutableStateOf("") }
    var availableSlots by remember { mutableStateOf("") }
    var pricePerHour by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }

    var initialized by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(parkingId) {
        initialized = false
        viewModel.loadParkings()
    }

    LaunchedEffect(parking) {
        if (parking != null && !initialized) {
            name = parking.name
            address = parking.address
            totalSlots = parking.totalSlots.toString()
            availableSlots = parking.availableSlots.toString()
            pricePerHour = parking.pricePerHour.toString()
            active = parking.active
            initialized = true
        }
    }

    val saving = uiState.isSaving
    val deleting = uiState.isDeleting

    Scaffold(
        containerColor = EditBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit parking",
                        fontWeight = FontWeight.Bold,
                        color = EditBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->

        when {
            uiState.isLoading && parking == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EditLime)
                }
            }

            parking == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        uiState.error ?: "Parking facility not found.",
                        color = EditGray
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadParkings() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EditBlack
                        )
                    ) {
                        Text("Try again", color = Color.White)
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Facility information
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalParking,
                                    contentDescription = null,
                                    tint = EditLime
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Facility information",
                                    fontWeight = FontWeight.Bold,
                                    color = EditBlack
                                )
                            }

                            EditField(
                                label = "Parking name",
                                value = name,
                                onValueChange = { name = it }
                            )

                            EditField(
                                label = "Address",
                                value = address,
                                onValueChange = { address = it }
                            )
                        }
                    }

                    // Slot management
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                "Slot management",
                                fontWeight = FontWeight.Bold,
                                color = EditBlack
                            )

                            EditField(
                                label = "Total parking slots",
                                value = totalSlots,
                                onValueChange = { totalSlots = it },
                                numeric = true
                            )

                            EditField(
                                label = "Available slots",
                                value = availableSlots,
                                onValueChange = { availableSlots = it },
                                numeric = true
                            )

                            Text(
                                "Available slots cannot exceed total slots.",
                                color = EditGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Pricing
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                "Pricing",
                                fontWeight = FontWeight.Bold,
                                color = EditBlack
                            )

                            EditField(
                                label = "Price per hour (৳)",
                                value = pricePerHour,
                                onValueChange = { pricePerHour = it },
                                numeric = true
                            )
                        }
                    }

                    // Availability
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Facility status",
                                    fontWeight = FontWeight.Bold,
                                    color = EditBlack
                                )
                                Text(
                                    if (active) "Currently active" else "Currently inactive",
                                    color = EditGray
                                )
                            }

                            Switch(
                                checked = active,
                                onCheckedChange = { active = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EditBlack,
                                    checkedTrackColor = EditLime
                                )
                            )
                        }
                    }

                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    uiState.actionMessage?.let {
                        Text(it, color = Color(0xFF388E3C))
                    }

                    // Save
                    Button(
                        onClick = {
                            val total = totalSlots.toIntOrNull()
                            val available = availableSlots.toIntOrNull()
                            val price = pricePerHour.toDoubleOrNull()

                            when {
                                name.isBlank() ->
                                    viewModel.showValidationError("Enter a parking name.")

                                address.isBlank() ->
                                    viewModel.showValidationError("Enter a parking address.")

                                total == null || total <= 0 ->
                                    viewModel.showValidationError("Enter a valid total slot count.")

                                available == null || available < 0 ->
                                    viewModel.showValidationError("Enter a valid available slot count.")

                                available > total ->
                                    viewModel.showValidationError("Available slots cannot exceed total slots.")

                                price == null || price < 0 ->
                                    viewModel.showValidationError("Enter a valid hourly price.")

                                else -> {
                                    viewModel.updateParking(
                                        parking.copy(
                                            name = name.trim(),
                                            address = address.trim(),
                                            totalSlots = total,
                                            availableSlots = available,
                                            pricePerHour = price,
                                            active = active
                                        )
                                    )
                                }
                            }
                        },
                        enabled = !saving && !deleting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EditBlack
                        )
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = EditLime,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Save changes",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Delete
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !saving && !deleting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        if (deleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Delete facility")
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    LaunchedEffect(uiState.actionMessage) {
        if (uiState.actionMessage == "Parking facility updated successfully.") {
            onUpdated()
        }
        if (uiState.actionMessage == "Parking facility deleted successfully.") {
            onDeleted()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete parking facility?") },
            text = {
                Text(
                    "This action will remove this facility. Make sure it has no active parking sessions."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteParking(parkingId)
                    }
                ) {
                    Text("Delete", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    numeric: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(
                if (numeric) input.filter { it.isDigit() || it == '.' } else input
            )
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = if (numeric)
                androidx.compose.ui.text.input.KeyboardType.Decimal
            else
                androidx.compose.ui.text.input.KeyboardType.Text
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EditLime,
            focusedLabelColor = EditBlack,
            cursorColor = EditBlack
        )
    )
}
