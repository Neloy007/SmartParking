
package com.example.smartparking.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartparking.data.model.Booking
import com.example.smartparking.data.model.Parking
import com.example.smartparking.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.smartparking.data.repository.AdminDashboardStats

private val ParkwayBlack = Color(0xFF080808)
private val ParkwayLime = Color(0xFFB8F34A)
private val ParkwayGray = Color(0xFF777777)
private val ParkwayBackground = Color(0xFFF5F5F5)
private val ParkwayBorder = Color(0xFFEEEEEE)
private val ParkwayGreen = Color(0xFF238636)
private val ParkwayRed = Color(0xFFD32F2F)
private val ParkwayOrange = Color(0xFFFFA000)

private enum class AdminTab(val label: String, val symbol: String) {
    OVERVIEW("Overview", "⌂"),
    OWNERS("Owners", "♙"),
    DRIVERS("Drivers", "♧"),
    APPROVALS("Approvals", "✓"),
    FINANCE("Finance", "৳")
}

@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }
    var searchQuery by remember { mutableStateOf("") }
    var rejectingFacility by remember { mutableStateOf<Parking?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.startListening()
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null && rejectingFacility != null) {
            rejectingFacility = null
            rejectionReason = ""
        }
    }

    Scaffold(
        containerColor = ParkwayBackground,
        topBar = {
            AdminTopBar(
                onLogout = {
                    viewModel.logout()
                    onLogout()
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 5.dp
            ) {
                AdminTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            searchQuery = ""
                        },
                        icon = {
                            Text(
                                text = tab.symbol,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        label = {
                            Text(tab.label, fontSize = 9.sp)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ParkwayBlack,
                            selectedTextColor = ParkwayBlack,
                            indicatorColor = ParkwayLime,
                            unselectedIconColor = ParkwayGray,
                            unselectedTextColor = ParkwayGray
                        )
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            LiveDataBanner()

            Spacer(Modifier.height(15.dp))

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = ParkwayBlack
                )
                Spacer(Modifier.height(16.dp))
            }

            uiState.errorMessage?.let { message ->
                MessageCard(
                    message = message,
                    isError = true,
                    onDismiss = viewModel::clearMessages
                )
                Spacer(Modifier.height(12.dp))
            }

            uiState.successMessage?.let { message ->
                MessageCard(
                    message = message,
                    isError = false,
                    onDismiss = viewModel::clearMessages
                )
                Spacer(Modifier.height(12.dp))
            }

            when (selectedTab) {
                AdminTab.OVERVIEW -> {
                    OverviewSection(
                        stats = uiState.stats,
                        onViewApprovals = {
                            selectedTab = AdminTab.APPROVALS
                        }
                    )
                }

                AdminTab.OWNERS -> {
                    SectionHeading(
                        title = "Parking Owners",
                        subtitle = "${uiState.owners.size} registered owner accounts"
                    )

                    SearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search owners..."
                    )

                    val filteredOwners = uiState.owners.filter { owner ->
                        owner.name.contains(searchQuery, true) ||
                                owner.email.contains(searchQuery, true) ||
                                owner.phone.contains(searchQuery, true)
                    }

                    if (filteredOwners.isEmpty()) {
                        EmptyState("No matching owners found.")
                    } else {
                        filteredOwners.forEach { owner ->
                            OwnerCard(
                                owner = owner,
                                facilityCount = uiState.facilities.count {
                                    it.ownerId == owner.uid
                                },
                                bookingCount = uiState.bookings.count {
                                    it.ownerId == owner.uid
                                }
                            )
                        }
                    }
                }

                AdminTab.DRIVERS -> {
                    SectionHeading(
                        title = "Registered Drivers",
                        subtitle = "${uiState.drivers.size} registered driver accounts"
                    )

                    SearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search drivers..."
                    )

                    val filteredDrivers = uiState.drivers.filter { driver ->
                        driver.name.contains(searchQuery, true) ||
                                driver.email.contains(searchQuery, true) ||
                                driver.phone.contains(searchQuery, true) ||
                                driver.carNumber.contains(searchQuery, true)
                    }

                    if (filteredDrivers.isEmpty()) {
                        EmptyState("No matching drivers found.")
                    } else {
                        filteredDrivers.forEach { driver ->
                            DriverCard(
                                driver = driver,
                                bookingCount = uiState.bookings.count {
                                    it.driverId == driver.uid
                                }
                            )
                        }
                    }
                }

                AdminTab.APPROVALS -> {
                    val pending = uiState.facilities.count {
                        it.approvalStatus == "PENDING"
                    }
                    val approved = uiState.facilities.count {
                        it.approvalStatus == "APPROVED"
                    }
                    val rejected = uiState.facilities.count {
                        it.approvalStatus == "REJECTED"
                    }

                    SectionHeading(
                        title = "Facility Approvals",
                        subtitle = "$pending facilities waiting for review"
                    )

                    StatusSummaryRow(
                        pending = pending,
                        approved = approved,
                        rejected = rejected
                    )

                    SearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search facilities..."
                    )

                    val visibleFacilities = uiState.facilities.filter { facility ->
                        val ownerName = uiState.owners
                            .firstOrNull { it.uid == facility.ownerId }
                            ?.name.orEmpty()

                        facility.approvalStatus == "PENDING" &&
                                (
                                        facility.name.contains(searchQuery, true) ||
                                                ownerName.contains(searchQuery, true) ||
                                                facility.address.contains(searchQuery, true)
                                        )
                    }

                    if (visibleFacilities.isEmpty()) {
                        EmptyState("No matching facilities found.")
                    } else {
                        visibleFacilities.forEach { facility ->
                            val ownerName = uiState.owners
                                .firstOrNull { it.uid == facility.ownerId }
                                ?.name ?: "Unknown owner"

                            FacilityApprovalCard(
                                facility = facility,
                                ownerName = ownerName,
                                isLoading = uiState.isActionLoading,
                                onApprove = {
                                    viewModel.approveFacility(facility.id)
                                },
                                onReject = {
                                    viewModel.clearMessages()
                                    rejectingFacility = facility
                                    rejectionReason = ""
                                }
                            )
                        }
                    }
                }

                AdminTab.FINANCE -> {
                    FinanceSection(
                        bookings = uiState.bookings,
                        stats = uiState.stats,
                        isActionLoading = uiState.isActionLoading,
                        onVerifyPayment = { booking ->
                            viewModel.verifyBookingPayment(
                                booking.id,
                                booking.transactionId
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    rejectingFacility?.let { facility ->
        AlertDialog(
            onDismissRequest = {
                rejectingFacility = null
            },
            title = {
                Text(
                    "Reject facility",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Provide a reason for rejecting ${facility.name}.",
                        fontSize = 13.sp,
                        color = ParkwayGray
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Enter rejection reason")
                        },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = rejectionReason.isNotBlank() &&
                            !uiState.isActionLoading,
                    onClick = {
                        viewModel.rejectFacility(
                            facility.id,
                            rejectionReason.trim()
                        )
                    }
                ) {
                    Text(
                        "Reject facility",
                        color = ParkwayRed
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        rejectingFacility = null
                    }
                ) {
                    Text("Cancel", color = ParkwayGray)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "parkway",
                    color = ParkwayBlack,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Text(
                    "ADMIN CONTROL CENTER",
                    color = ParkwayGray,
                    fontSize = 9.sp,
                    letterSpacing = 1.3.sp
                )
            }
        },
        actions = {
            TextButton(onClick = onLogout) {
                Text(
                    "Logout",
                    color = ParkwayBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
private fun LiveDataBanner() {
    Surface(
        color = ParkwayGreen.copy(alpha = 0.10f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(ParkwayGreen, RoundedCornerShape(50))
            )

            Spacer(Modifier.width(9.dp))

            Text(
                "FIREBASE DATA",
                color = ParkwayGreen,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Dashboard connected to live records",
                color = ParkwayBlack,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isError) {
            ParkwayRed.copy(alpha = 0.08f)
        } else {
            ParkwayGreen.copy(alpha = 0.08f)
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                message,
                modifier = Modifier.weight(1f),
                color = if (isError) ParkwayRed else ParkwayGreen,
                fontSize = 12.sp
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {
    Text(
        title,
        fontSize = 23.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ParkwayBlack
    )
    Spacer(Modifier.height(4.dp))
    Text(
        subtitle,
        color = ParkwayGray,
        fontSize = 12.sp
    )
    Spacer(Modifier.height(17.dp))
}

@Composable
private fun OverviewSection(
    stats: AdminDashboardStats,
    onViewApprovals: () -> Unit
) {
    Text(
        "Dashboard Overview",
        fontSize = 23.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ParkwayBlack
    )

    Spacer(Modifier.height(5.dp))

    Text(
        "Monitor Parkway activity at a glance.",
        color = ParkwayGray,
        fontSize = 12.sp
    )

    Spacer(Modifier.height(20.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SummaryCard(
            Modifier.weight(1f),
            "Owners",
            stats.ownerCount.toString(),
            "♙"
        )
        SummaryCard(
            Modifier.weight(1f),
            "Drivers",
            stats.driverCount.toString(),
            "♧"
        )
    }

    Spacer(Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SummaryCard(
            Modifier.weight(1f),
            "Facilities",
            stats.facilityCount.toString(),
            "▦"
        )
        SummaryCard(
            Modifier.weight(1f),
            "Approved",
            stats.approvedFacilities.toString(),
            "✓"
        )
    }

    Spacer(Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SummaryCard(
            Modifier.weight(1f),
            "Bookings",
            stats.bookingCount.toString(),
            "▣"
        )
        SummaryCard(
            Modifier.weight(1f),
            "Pending review",
            stats.pendingFacilities.toString(),
            "!"
        )
    }

    Spacer(Modifier.height(22.dp))

    SectionHeading(
        "Facility Review",
        "Review parking facilities submitted by owners."
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(Modifier.padding(17.dp)) {
            Text(
                "${stats.pendingFacilities} pending approvals",
                color = ParkwayBlack,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(7.dp))

            Text(
                "Facilities must be approved before becoming available to drivers.",
                color = ParkwayGray,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(15.dp))

            Button(
                onClick = onViewApprovals,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParkwayBlack
                )
            ) {
                Text("Review Facilities", color = Color.White)
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    SectionHeading(
        "Financial Snapshot",
        "Based on completed and paid bookings only."
    )

    FinanceMetricCard(
        "Gross booking value",
        formatMoney(stats.totalBookingValue)
    )

    Spacer(Modifier.height(9.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FinanceMetricCard(
            "Parkway fee (2.5%)",
            formatMoney(stats.totalPlatformFee),
            Modifier.weight(1f)
        )

        FinanceMetricCard(
            "Owner earnings",
            formatMoney(stats.totalOwnerEarnings),
            Modifier.weight(1f)
        )
    }

    Spacer(Modifier.height(9.dp))

    Text(
        "${stats.completedBookingCount} completed and paid bookings",
        color = ParkwayGray,
        fontSize = 11.sp
    )
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    symbol: String
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                symbol,
                fontSize = 21.sp,
                color = ParkwayBlack,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                value,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ParkwayBlack
            )
            Text(
                title,
                fontSize = 11.sp,
                color = ParkwayGray
            )
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        placeholder = {
            Text(placeholder, fontSize = 13.sp)
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ParkwayBlack,
            unfocusedBorderColor = ParkwayBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = ParkwayBlack
        )
    )
}

@Composable
private fun OwnerCard(
    owner: User,
    facilityCount: Int,
    bookingCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp),
        shape = RoundedCornerShape(15.dp),
        color = Color.White
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarLetter(owner.name)
                Spacer(Modifier.width(11.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        owner.name.ifBlank { "Unnamed owner" },
                        color = ParkwayBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        owner.uid,
                        color = ParkwayGray,
                        fontSize = 10.sp
                    )
                }

                StatusPill(
                    text = if (owner.isVerified) "VERIFIED" else "UNVERIFIED",
                    positive = owner.isVerified
                )
            }

            Spacer(Modifier.height(13.dp))

            DetailLine("Email", owner.email)
            DetailLine("Phone", owner.phone)
            DetailLine("Facilities", facilityCount.toString())
            DetailLine("Bookings", bookingCount.toString())
            DetailLine(
                "Joined",
                formatDate(owner.createdAt)
            )
        }
    }
}

@Composable
private fun DriverCard(
    driver: User,
    bookingCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp),
        shape = RoundedCornerShape(15.dp),
        color = Color.White
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarLetter(driver.name)
                Spacer(Modifier.width(11.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        driver.name.ifBlank { "Unnamed driver" },
                        color = ParkwayBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        driver.uid,
                        color = ParkwayGray,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(Modifier.height(13.dp))

            DetailLine("Email", driver.email)
            DetailLine("Phone", driver.phone)
            DetailLine("Car number", driver.carNumber.ifBlank { "Not provided" })
            DetailLine("Bookings", bookingCount.toString())
            DetailLine(
                "License",
                if (driver.drivingLicenseNumber.isBlank()) {
                    "Not provided"
                } else {
                    driver.drivingLicenseNumber
                }
            )
            DetailLine(
                "Verification",
                if (driver.isVerified) "Verified" else "Not verified"
            )
        }
    }
}

@Composable
private fun FacilityApprovalCard(
    facility: Parking,
    ownerName: String,
    isLoading: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(15.dp),
        color = Color.White
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        facility.name,
                        color = ParkwayBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        facility.id,
                        color = ParkwayGray,
                        fontSize = 10.sp
                    )
                }

                StatusPill(
                    text = facility.approvalStatus,
                    positive = facility.approvalStatus == "APPROVED"
                )
            }

            Spacer(Modifier.height(12.dp))

            DetailLine("Owner", ownerName)
            DetailLine("Address", facility.address)
            DetailLine("Total slots", facility.totalSlots.toString())
            DetailLine("Available", facility.availableSlots.toString())
            DetailLine(
                "Hourly rate",
                formatMoney(facility.pricePerHour)
            )

            if (facility.rejectionReason.isNotBlank()) {
                DetailLine("Reason", facility.rejectionReason)
            }

            if (facility.approvalStatus == "PENDING") {
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Button(
                        onClick = onApprove,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParkwayBlack
                        )
                    ) {
                        Text(
                            "Approve",
                            color = ParkwayLime,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Reject",
                            color = ParkwayRed,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSummaryRow(
    pending: Int,
    approved: Int,
    rejected: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        SmallStatusCard(
            Modifier.weight(1f),
            "Pending",
            pending,
            ParkwayOrange
        )
        SmallStatusCard(
            Modifier.weight(1f),
            "Approved",
            approved,
            ParkwayGreen
        )
        SmallStatusCard(
            Modifier.weight(1f),
            "Rejected",
            rejected,
            ParkwayRed
        )
    }

    Spacer(Modifier.height(15.dp))
}

@Composable
private fun SmallStatusCard(
    modifier: Modifier,
    label: String,
    count: Int,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                label,
                fontSize = 10.sp,
                color = ParkwayGray
            )
        }
    }
}

@Composable
private fun FinanceSection(
    bookings: List<Booking>,
    stats: AdminDashboardStats,
    isActionLoading: Boolean,
    onVerifyPayment: (Booking) -> Unit
) {
    val pendingPayments = bookings
        .filter {
            it.paymentStatus == "PENDING" &&
                    it.transactionId.isNotBlank()
        }
        .sortedByDescending { it.createdAt }

    val completedPaidBookings = bookings
        .filter {
            it.bookingStatus == "COMPLETED" &&
                    it.paymentStatus == "PAID"
        }
        .sortedByDescending { it.createdAt }

    Text(
        "Financial Overview",
        fontSize = 23.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ParkwayBlack
    )

    Spacer(Modifier.height(5.dp))

    Text(
        "Review submitted payments and monitor Parkway revenue.",
        fontSize = 12.sp,
        color = ParkwayGray
    )

    Spacer(Modifier.height(20.dp))

    FinanceMetricCard(
        "Gross booking value",
        formatMoney(stats.totalBookingValue)
    )

    Spacer(Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FinanceMetricCard(
            "Parkway fee (2.5%)",
            formatMoney(stats.totalPlatformFee),
            Modifier.weight(1f)
        )

        FinanceMetricCard(
            "Owner earnings",
            formatMoney(stats.totalOwnerEarnings),
            Modifier.weight(1f)
        )
    }

    Spacer(Modifier.height(12.dp))

    Text(
        "${stats.completedBookingCount} completed and paid bookings",
        color = ParkwayGray,
        fontSize = 11.sp
    )

    Spacer(Modifier.height(25.dp))

    // PENDING PAYMENT VERIFICATION
    SectionHeading(
        "Pending Payment Verification",
        "${pendingPayments.size} payments waiting for admin review"
    )

    if (pendingPayments.isEmpty()) {
        EmptyState(
            "No pending payments with submitted transaction IDs."
        )
    } else {
        pendingPayments.forEach { booking ->
            PendingPaymentCard(
                booking = booking,
                isLoading = isActionLoading,
                onVerify = {
                    onVerifyPayment(booking)
                }
            )
        }
    }

    Spacer(Modifier.height(22.dp))

    // COMPLETED TRANSACTIONS
    SectionHeading(
        "Recent Transactions",
        "Completed and paid booking records."
    )

    if (completedPaidBookings.isEmpty()) {
        EmptyState(
            "No completed and paid bookings are available yet."
        )
    } else {
        completedPaidBookings.forEach { booking ->
            FinanceTransactionRow(booking)
        }
    }

    Spacer(Modifier.height(12.dp))

    Text(
        "Parkway's platform fee is calculated at 2.5% of each completed and paid booking. Payment verification records the admin's review of the submitted transaction ID; it does not independently confirm a bank transfer.",
        color = ParkwayGray,
        fontSize = 11.sp,
        lineHeight = 17.sp
    )
}

@Composable
private fun PendingPaymentCard(
    booking: Booking,
    isLoading: Boolean,
    onVerify: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp),
        color = Color.White,
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        booking.parkingName.ifBlank {
                            "Parking booking"
                        },
                        color = ParkwayBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(
                        "Booking ID: ${booking.id}",
                        color = ParkwayGray,
                        fontSize = 10.sp
                    )
                }

                StatusPill(
                    text = "PENDING",
                    positive = false
                )
            }

            Spacer(Modifier.height(12.dp))

            DetailLine(
                "Driver ID",
                booking.driverId
            )

            DetailLine(
                "Owner ID",
                booking.ownerId
            )

            DetailLine(
                "Payment method",
                booking.paymentMethod
            )

            DetailLine(
                "Transaction ID",
                booking.transactionId
            )

            DetailLine(
                "Amount",
                formatMoney(booking.totalAmount)
            )

            DetailLine(
                "Booking date",
                formatDate(booking.createdAt)
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onVerify,
                enabled = !isLoading &&
                        booking.transactionId.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParkwayBlack
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(17.dp),
                        color = ParkwayLime,
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(9.dp))
                }

                Text(
                    "Verify Payment",
                    color = ParkwayLime,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FinanceMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                title,
                fontSize = 11.sp,
                color = ParkwayGray
            )
            Spacer(Modifier.height(7.dp))
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ParkwayBlack
            )
        }
    }
}

@Composable
private fun FinanceTransactionRow(
    booking: Booking
) {
    val platformFee = booking.totalAmount * 0.025

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp),
        color = Color.White,
        shape = RoundedCornerShape(13.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        booking.parkingName.ifBlank { "Parking booking" },
                        color = ParkwayBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "Booking: ${booking.id}",
                        color = ParkwayGray,
                        fontSize = 10.sp
                    )
                }

                Text(
                    formatMoney(booking.totalAmount),
                    color = ParkwayGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(9.dp))

            DetailLine("Booking date", formatDate(booking.createdAt))
            DetailLine("Payment", booking.paymentStatus)
            DetailLine("Status", booking.bookingStatus)
            DetailLine("Platform fee", formatMoney(platformFee))
            DetailLine(
                "Owner earnings",
                formatMoney(booking.totalAmount - platformFee)
            )
        }
    }
}

@Composable
private fun AvatarLetter(name: String) {
    Box(
        modifier = Modifier
            .size(43.dp)
            .background(
                ParkwayLime.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            color = ParkwayBlack,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            modifier = Modifier.width(105.dp),
            color = ParkwayGray,
            fontSize = 11.sp
        )

        Text(
            value,
            modifier = Modifier.weight(1f),
            color = ParkwayBlack,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    positive: Boolean
) {
    val backgroundColor = when {
        positive -> ParkwayGreen.copy(alpha = 0.14f)
        text == "REJECTED" -> ParkwayRed.copy(alpha = 0.12f)
        else -> Color(0xFFFFF1D6)
    }

    val textColor = when {
        positive -> ParkwayGreen
        text == "REJECTED" -> ParkwayRed
        else -> Color(0xFF9A6500)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 5.dp
            ),
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            message,
            modifier = Modifier.padding(20.dp),
            color = ParkwayGray,
            fontSize = 13.sp
        )
    }
}

private fun formatMoney(amount: Double): String {
    return "৳${String.format(Locale.US, "%.2f", amount)}"
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return "Not available"

    return SimpleDateFormat(
        "dd MMM yyyy",
        Locale.getDefault()
    ).format(Date(timestamp))
}
