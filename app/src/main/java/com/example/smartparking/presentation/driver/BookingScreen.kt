
package com.example.smartparking.presentation.driver

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartparking.data.model.Booking
import com.example.smartparking.data.model.Parking
import com.example.smartparking.data.repository.BookingRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

private enum class BookingStep {
    SCHEDULE,
    SUMMARY,
    PAYMENT,
    WAITING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    parking: Parking,
    onBack: () -> Unit,
    onBookingCreated: (Booking) -> Unit,
    driverId: String
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val repository = remember { BookingRepository() }

    val dateFormat = remember {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    }

    val timeFormat = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    var step by remember { mutableStateOf(BookingStep.SCHEDULE) }

    var selectedDate by remember { mutableLongStateOf(0L) }
    var startTime by remember { mutableLongStateOf(0L) }
    var endTime by remember { mutableLongStateOf(0L) }

    var ownerBkashNumber by remember { mutableStateOf("") }
    var isLoadingBkash by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }

    var transactionId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var createdBooking by remember { mutableStateOf<Booking?>(null) }
    var copiedNumber by remember { mutableStateOf(false) }

    val scheduleSelected =
        selectedDate > 0L &&
                startTime > 0L &&
                endTime > startTime

    val billableHours = if (scheduleSelected) {
        ceil((endTime - startTime) / 3_600_000.0)
    } else {
        0.0
    }

    val totalAmount = billableHours * parking.pricePerHour

    // Load the owner's bKash number from owner_payment_profiles.

    LaunchedEffect(parking.ownerId) {
        isLoadingBkash = true
        ownerBkashNumber = ""
        errorMessage = null
        copiedNumber = false

        try {
            val result = repository.getOwnerBkashNumber(parking.ownerId)

            ownerBkashNumber = result.getOrThrow()

        } catch (e: Exception) {
            errorMessage = e.message
                ?: "Unable to load the owner's bKash number."
        } finally {
            isLoadingBkash = false
        }
    }


    fun chooseDate() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                selectedDate = selected.timeInMillis
                startTime = 0L
                endTime = 0L
                errorMessage = null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    fun chooseTime(isStart: Boolean) {
        if (selectedDate == 0L) {
            errorMessage = "Please select a booking date first."
            return
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = if (isStart || startTime == 0L) {
                selectedDate
            } else {
                startTime
            }
        }

        TimePickerDialog(
            context,
            { _, hour, minute ->
                val chosen = Calendar.getInstance().apply {
                    timeInMillis = selectedDate
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                if (chosen <= System.currentTimeMillis()) {
                    errorMessage = "Please select a future time."
                    return@TimePickerDialog
                }

                if (isStart) {
                    startTime = chosen
                    endTime = 0L
                } else {
                    if (chosen <= startTime) {
                        errorMessage = "End time must be after start time."
                        return@TimePickerDialog
                    }
                    endTime = chosen
                }

                errorMessage = null
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    fun goBack() {
        when (step) {
            BookingStep.SCHEDULE -> onBack()
            BookingStep.SUMMARY -> step = BookingStep.SCHEDULE
            BookingStep.PAYMENT -> step = BookingStep.SUMMARY
            BookingStep.WAITING -> {
                createdBooking?.let(onBookingCreated)
            }
        }
    }

    BackHandler(enabled = step != BookingStep.SCHEDULE) {
        goBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (step) {
                            BookingStep.SCHEDULE -> "Book Parking"
                            BookingStep.SUMMARY -> "Booking Summary"
                            BookingStep.PAYMENT -> "bKash Payment"
                            BookingStep.WAITING -> "Payment Submitted"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (step != BookingStep.WAITING) {
                LinearProgressIndicator(
                    progress = {
                        when (step) {
                            BookingStep.SCHEDULE -> 0.33f
                            BookingStep.SUMMARY -> 0.66f
                            BookingStep.PAYMENT -> 1f
                            BookingStep.WAITING -> 1f
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color(0xFF155EEF),
                    trackColor = Color(0xFFE2E8F0)
                )
            }

            when (step) {

                // ---------------------------------
                // STEP 1: SELECT BOOKING SCHEDULE
                // ---------------------------------

                BookingStep.SCHEDULE -> {

                    ParkingBookingCard(parking)

                    Text(
                        "Choose your schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { chooseDate() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, null)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (selectedDate > 0L) {
                                dateFormat.format(Date(selectedDate))
                            } else {
                                "Select booking date"
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = { chooseTime(true) },
                        enabled = selectedDate > 0L,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccessTime, null)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (startTime > 0L) {
                                "Start: ${timeFormat.format(Date(startTime))}"
                            } else {
                                "Select start time"
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = { chooseTime(false) },
                        enabled = startTime > 0L,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccessTime, null)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (endTime > 0L) {
                                "End: ${timeFormat.format(Date(endTime))}"
                            } else {
                                "Select end time"
                            }
                        )
                    }

                    if (scheduleSelected) {
                        InfoCard {
                            SummaryRow("Duration", "$billableHours billable hours")
                            SummaryRow("Estimated total", money(totalAmount))
                        }
                    }

                    errorMessage?.let {
                        ErrorText(it)
                    }

                    PrimaryBookingButton(
                        text = "Continue",
                        enabled = scheduleSelected
                    ) {
                        errorMessage = null
                        step = BookingStep.SUMMARY
                    }
                }

                // ---------------------------------
                // STEP 2: BOOKING SUMMARY
                // ---------------------------------

                BookingStep.SUMMARY -> {

                    ParkingBookingCard(parking)

                    Text(
                        "Review your booking",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    InfoCard {
                        SummaryRow("Parking", parking.name)
                        SummaryRow("Address", parking.address)
                        HorizontalDivider()

                        SummaryRow(
                            "Date",
                            dateFormat.format(Date(selectedDate))
                        )

                        SummaryRow(
                            "Start time",
                            timeFormat.format(Date(startTime))
                        )

                        SummaryRow(
                            "End time",
                            timeFormat.format(Date(endTime))
                        )

                        HorizontalDivider()

                        SummaryRow("Duration", "$billableHours hours")
                        SummaryRow("Hourly rate", money(parking.pricePerHour))
                        HorizontalDivider()

                        SummaryRow(
                            "Total amount",
                            money(totalAmount),
                            true
                        )
                    }

                    Text(
                        "Your booking request will be sent to the parking owner after you submit your bKash Transaction ID.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    PrimaryBookingButton("Continue to bKash Payment") {
                        errorMessage = null
                        step = BookingStep.PAYMENT
                    }
                }

                // ---------------------------------
                // STEP 3: BKASH PAYMENT
                // ---------------------------------

                BookingStep.PAYMENT -> {

                    Text(
                        "Pay using bKash",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    InfoCard {
                        SummaryRow("Parking", parking.name)
                        SummaryRow("Amount to pay", money(totalAmount), true)
                    }

                    InfoCard {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFFE2136E),
                                modifier = Modifier.size(28.dp)
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                "Owner's bKash number",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isLoadingBkash) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        } else if (ownerBkashNumber.isNotBlank()) {

                            Text(
                                ownerBkashNumber,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2136E)
                            )

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(
                                        AnnotatedString(ownerBkashNumber)
                                    )
                                    copiedNumber = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    if (copiedNumber) {
                                        Icons.Default.Check
                                    } else {
                                        Icons.Default.ContentCopy
                                    },
                                    contentDescription = null
                                )

                                Spacer(Modifier.width(8.dp))

                                Text(
                                    if (copiedNumber) "Number copied" else "Copy bKash number"
                                )
                            }
                        }
                    }

                    InfoCard {
                        Text(
                            "Payment instructions",
                            fontWeight = FontWeight.Bold
                        )

                        Text("1. Open your bKash app.")
                        Text("2. Send ৳${money(totalAmount).removePrefix("৳")} to the owner's number.")
                        Text("3. Complete the payment.")
                        Text("4. Copy the Transaction ID (TrxID) from your bKash confirmation.")
                        Text("5. Enter the TrxID below and submit your request.")
                    }

                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = {
                            transactionId = it.trim()
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("bKash Transaction ID (TrxID)") },
                        placeholder = { Text("Enter your TrxID") },
                        leadingIcon = {
                            Icon(Icons.Default.ReceiptLong, null)
                        },
                        singleLine = true,
                        enabled = !isSubmitting
                    )

                    Text(
                        "The owner will manually verify your TrxID in bKash. Your parking session will begin only after the owner approves the payment.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    errorMessage?.let {
                        ErrorText(it)
                    }

                    PrimaryBookingButton(
                        text = if (isSubmitting) {
                            "Submitting..."
                        } else {
                            "Submit Payment for Approval"
                        },
                        enabled = !isSubmitting &&
                                !isLoadingBkash &&
                                ownerBkashNumber.isNotBlank() &&
                                transactionId.isNotBlank() &&
                                totalAmount > 0.0
                    ) {

                        val cleanTrxId = transactionId.trim()

                        if (cleanTrxId.isBlank()) {
                            errorMessage = "Please enter your bKash Transaction ID."
                            return@PrimaryBookingButton
                        }

                        scope.launch {
                            isSubmitting = true
                            errorMessage = null

                            try {
                                val driverId = FirebaseAuth
                                    .getInstance()
                                    .currentUser
                                    ?.uid
                                    ?: throw Exception("Please log in again.")

                                val result = repository.createBooking(
                                    parking = parking,
                                    driverId = driverId,
                                    startTime = startTime,
                                    endTime = endTime,
                                    transactionId = cleanTrxId
                                )

                                createdBooking = result.getOrThrow()
                                step = BookingStep.WAITING

                            } catch (e: Exception) {
                                errorMessage = e.message
                                    ?: "Unable to submit your booking."
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                }

                // ---------------------------------
                // STEP 4: WAITING FOR OWNER APPROVAL
                // ---------------------------------

                BookingStep.WAITING -> {

                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = Color(0xFFE69A16),
                        modifier = Modifier
                            .size(82.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Text(
                        "Payment Submitted!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Text(
                        "Your booking request is waiting for the parking owner's approval.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    createdBooking?.let { booking ->

                        InfoCard {
                            SummaryRow("Booking ID", booking.id)
                            SummaryRow("Parking", booking.parkingName)

                            SummaryRow(
                                "Date",
                                dateFormat.format(Date(booking.startTime))
                            )

                            SummaryRow(
                                "Scheduled time",
                                "${timeFormat.format(Date(booking.startTime))} - " +
                                        timeFormat.format(Date(booking.endTime))
                            )

                            SummaryRow("Amount", money(booking.totalAmount), true)
                            SummaryRow("Payment method", "bKash")
                            SummaryRow("Transaction ID", booking.transactionId)
                            SummaryRow("Payment status", "Waiting for verification")
                            SummaryRow("Booking status", "Waiting for approval")
                        }
                    }

                    Text(
                        "Important: Your parking timer has not started. It will start immediately when the owner approves your payment.",
                        color = Color(0xFFB7791F),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    PrimaryBookingButton("View My Bookings") {
                        createdBooking?.let(onBookingCreated)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParkingBookingCard(parking: Parking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalParking,
                    null,
                    tint = Color(0xFF155EEF)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    parking.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(parking.address, color = Color.Gray)

            SummaryRow(
                "Available slots (latest listing)",
                parking.availableSlots.toString()
            )

            SummaryRow("Price per hour", money(parking.pricePerHour))
        }
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    emphasize: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            value,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasize) {
                Color(0xFF155EEF)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PrimaryBookingButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF155EEF)
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
}

private fun money(amount: Double): String {
    return "৳" + String.format(Locale.US, "%.2f", amount)
}
