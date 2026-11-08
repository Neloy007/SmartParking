
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartparking.data.model.Parking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil

data class DemoParkingBooking(
    val id: String,
    val parkingId: String,
    val parkingName: String,
    val parkingAddress: String,
    val startTime: Long,
    val endTime: Long,
    val durationHours: Double,
    val pricePerHour: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val bookingStatus: String,
    val paymentStatus: String,
    val createdAt: Long = System.currentTimeMillis()
)

private enum class BookingStep {
    SCHEDULE,
    SUMMARY,
    PAYMENT_METHOD,
    PAYMENT_CONFIRMATION,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    parking: Parking,
    onBack: () -> Unit,
    onBookingCreated: (DemoParkingBooking) -> Unit
) {
    val context = LocalContext.current

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

    var selectedPayment by remember { mutableStateOf("bKash") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var demoBooking by remember { mutableStateOf<DemoParkingBooking?>(null) }

    val scheduleSelected =
        selectedDate > 0L && startTime > 0L && endTime > startTime

    val billableHours = if (scheduleSelected) {
        ceil((endTime - startTime) / 3_600_000.0)
    } else {
        0.0
    }

    val totalAmount = billableHours * parking.pricePerHour

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
            BookingStep.PAYMENT_METHOD -> step = BookingStep.SUMMARY
            BookingStep.PAYMENT_CONFIRMATION -> step = BookingStep.PAYMENT_METHOD
            BookingStep.SUCCESS -> {
                demoBooking?.let(onBookingCreated)
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
                            BookingStep.PAYMENT_METHOD -> "Payment Method"
                            BookingStep.PAYMENT_CONFIRMATION -> "Payment Confirmation"
                            BookingStep.SUCCESS -> "Booking Successful"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { goBack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
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

            if (step != BookingStep.SUCCESS) {
                LinearProgressIndicator(
                    progress = {
                        when (step) {
                            BookingStep.SCHEDULE -> 0.20f
                            BookingStep.SUMMARY -> 0.40f
                            BookingStep.PAYMENT_METHOD -> 0.60f
                            BookingStep.PAYMENT_CONFIRMATION -> 0.80f
                            BookingStep.SUCCESS -> 1f
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
                            SummaryRow(
                                "Estimated total",
                                money(totalAmount)
                            )
                        }
                    }

                    errorMessage?.let {
                        ErrorText(it)
                    }

                    PrimaryBookingButton(
                        text = "Continue",
                        enabled = scheduleSelected,
                        onClick = {
                            errorMessage = null
                            step = BookingStep.SUMMARY
                        }
                    )
                }

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
                        SummaryRow("Date", dateFormat.format(Date(selectedDate)))
                        SummaryRow("Start time", timeFormat.format(Date(startTime)))
                        SummaryRow("End time", timeFormat.format(Date(endTime)))
                        HorizontalDivider()
                        SummaryRow("Duration", "$billableHours hours")
                        SummaryRow("Hourly rate", money(parking.pricePerHour))
                        HorizontalDivider()
                        SummaryRow("Total amount", money(totalAmount), true)
                    }

                    Text(
                        "This is a demo booking preview. No parking slot is reserved yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    PrimaryBookingButton("Continue to Payment") {
                        step = BookingStep.PAYMENT_METHOD
                    }
                }

                BookingStep.PAYMENT_METHOD -> {

                    Text(
                        "Select payment method",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    InfoCard {
                        SummaryRow("Booking amount", money(totalAmount), true)
                    }

                    PaymentOption(
                        title = "bKash",
                        subtitle = "Demo mobile payment",
                        icon = Icons.Default.AccountBalanceWallet,
                        selected = selectedPayment == "bKash"
                    ) {
                        selectedPayment = "bKash"
                    }

                    PaymentOption(
                        title = "Nagad",
                        subtitle = "Demo mobile payment",
                        icon = Icons.Default.AccountBalanceWallet,
                        selected = selectedPayment == "Nagad"
                    ) {
                        selectedPayment = "Nagad"
                    }

                    PaymentOption(
                        title = "Cash",
                        subtitle = "Pay at the parking location",
                        icon = Icons.Default.Payments,
                        selected = selectedPayment == "Cash"
                    ) {
                        selectedPayment = "Cash"
                    }

                    Text(
                        "Payment is simulated. No real payment will be collected.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    PrimaryBookingButton("Continue") {
                        step = BookingStep.PAYMENT_CONFIRMATION
                    }
                }

                BookingStep.PAYMENT_CONFIRMATION -> {

                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF155EEF),
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Text(
                        "Confirm your demo payment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    InfoCard {
                        SummaryRow("Payment method", selectedPayment)
                        SummaryRow("Parking", parking.name)
                        SummaryRow("Amount", money(totalAmount), true)
                    }

                    Text(
                        if (selectedPayment == "Cash") {
                            "This demo will record Cash on Arrival as the selected payment method. No cash transaction is being processed."
                        } else {
                            "This demo will simulate a successful $selectedPayment payment. No wallet account or real transaction is involved."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    PrimaryBookingButton("Confirm Demo Booking") {
                        val booking = DemoParkingBooking(
                            id = "SPK" + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .take(8)
                                .uppercase(),
                            parkingId = parking.id,
                            parkingName = parking.name,
                            parkingAddress = parking.address,
                            startTime = startTime,
                            endTime = endTime,
                            durationHours = billableHours,
                            pricePerHour = parking.pricePerHour,
                            totalAmount = totalAmount,
                            paymentMethod = selectedPayment,
                            bookingStatus = "CONFIRMED_DEMO",
                            paymentStatus = if (selectedPayment == "Cash") {
                                "PAY_ON_ARRIVAL"
                            } else {
                                "DEMO_PAID"
                            }
                        )

                        demoBooking = booking
                        step = BookingStep.SUCCESS
                    }
                }

                BookingStep.SUCCESS -> {

                    val booking = demoBooking

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier
                            .size(88.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Text(
                        "Demo Booking Created!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Text(
                        "Your demo booking is ready. It has not reserved a real parking slot.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (booking != null) {
                        InfoCard {
                            SummaryRow("Booking ID", booking.id)
                            SummaryRow("Parking", booking.parkingName)
                            SummaryRow(
                                "Date",
                                dateFormat.format(Date(booking.startTime))
                            )
                            SummaryRow(
                                "Time",
                                "${timeFormat.format(Date(booking.startTime))} - " +
                                        timeFormat.format(Date(booking.endTime))
                            )
                            SummaryRow("Amount", money(booking.totalAmount), true)
                            SummaryRow("Payment", booking.paymentMethod)
                            SummaryRow("Payment status", booking.paymentStatus)
                        }
                    }

                    PrimaryBookingButton("View My Bookings") {
                        demoBooking?.let(onBookingCreated)
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
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
            color = if (emphasize) Color(0xFF155EEF) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PaymentOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) Color(0xFF155EEF) else Color(0xFFE2E8F0)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFEFF6FF) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color(0xFF155EEF))
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
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
