
package com.example.smartparking.presentation.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val VerificationLime = Color(0xFFB8F34A)
private val VerificationBlack = Color(0xFF080808)
private val VerificationBackground = Color(0xFFF4F4F4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverVerificationScreen(
    onBack: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    val uid = auth.currentUser?.uid

    var carNumber by remember { mutableStateOf("") }
    var nidNumber by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }

    var verificationStatus by remember { mutableStateOf("NOT_SUBMITTED") }
    var rejectionReason by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var isLoadingProfile by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        if (uid == null) {
            errorMessage = "Please log in again."
            isLoadingProfile = false
            return@LaunchedEffect
        }

        try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            carNumber = document.getString("carNumber") ?: ""
            nidNumber = document.getString("nidNumber") ?: ""
            licenseNumber = document.getString("drivingLicenseNumber") ?: ""

            verificationStatus =
                document.getString("verificationStatus") ?: "NOT_SUBMITTED"

            rejectionReason =
                document.getString("rejectionReason") ?: ""

        } catch (e: Exception) {
            errorMessage = e.localizedMessage
                ?: "Could not load your information."
        } finally {
            isLoadingProfile = false
        }
    }

    fun submitVerification() {
        if (uid == null) {
            errorMessage = "Please log in again."
            return
        }

        if (
            carNumber.isBlank() ||
            nidNumber.isBlank() ||
            licenseNumber.isBlank()
        ) {
            errorMessage = "Please complete all three information fields."
            return
        }

        scope.launch {
            isLoading = true
            errorMessage = ""
            successMessage = ""

            try {
                firestore.collection("users")
                    .document(uid)
                    .update(
                        mapOf(
                            "carNumber" to carNumber.trim(),
                            "nidNumber" to nidNumber.trim(),
                            "drivingLicenseNumber" to licenseNumber.trim(),
                            "verificationStatus" to "PENDING",
                            "verificationSubmittedAt" to FieldValue.serverTimestamp(),
                            "verifiedAt" to null,
                            "rejectionReason" to ""
                        )
                    )
                    .await()

                verificationStatus = "PENDING"
                rejectionReason = ""

                successMessage =
                    "Your verification information has been submitted successfully."

            } catch (e: Exception) {
                errorMessage = e.localizedMessage
                    ?: "Verification submission failed."
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = VerificationBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Driver Verification",
                        fontWeight = FontWeight.Bold,
                        color = VerificationBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = VerificationBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerificationBackground
                )
            )
        }
    ) { padding ->

        if (isLoadingProfile) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = VerificationBlack)
            }
        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = VerificationBlack
                        )

                        Text(
                            text = "Verify your driver account",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VerificationBlack
                        )

                        Text(
                            text = "Enter your vehicle and identity information to complete your driver verification.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }

                VerificationStatusCard(
                    status = verificationStatus,
                    rejectionReason = rejectionReason
                )

                Text(
                    text = "Vehicle Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = carNumber,
                    onValueChange = { carNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Car Registration Number") },
                    placeholder = { Text("e.g. DHAKA METRO-GA-12-3456") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Identity Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = nidNumber,
                    onValueChange = { nidNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("NID Number") },
                    placeholder = { Text("Enter your NID number") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = licenseNumber,
                    onValueChange = { licenseNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Driving Licence Number") },
                    placeholder = { Text("Enter your licence number") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (successMessage.isNotBlank()) {
                    Text(
                        text = successMessage,
                        color = Color(0xFF238636),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = { submitVerification() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isLoading &&
                            verificationStatus != "VERIFIED" &&
                            verificationStatus != "PENDING",
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerificationBlack,
                        contentColor = VerificationLime
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(22.dp),
                            color = VerificationLime,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = when (verificationStatus) {
                                "VERIFIED" -> "Already Verified"
                                "PENDING" -> "Waiting for Approval"
                                else -> "Submit for Verification"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Your NID and driving licence information is private and should only be accessible to you and authorized administrators.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun VerificationStatusCard(
    status: String,
    rejectionReason: String
) {
    val statusText = when (status) {
        "PENDING" -> "Verification Pending"
        "VERIFIED" -> "Account Verified"
        "REJECTED" -> "Verification Rejected"
        else -> "Not Verified"
    }

    val statusColor = when (status) {
        "VERIFIED" -> Color(0xFF238636)
        "REJECTED" -> Color(0xFFD32F2F)
        "PENDING" -> Color(0xFFB7791F)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = statusText,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )

            when (status) {
                "PENDING" -> Text(
                    "Your information has been submitted and is waiting for admin review.",
                    style = MaterialTheme.typography.bodySmall
                )

                "VERIFIED" -> Text(
                    "Your driver account has been successfully verified.",
                    style = MaterialTheme.typography.bodySmall
                )

                "REJECTED" -> {
                    Text(
                        "Please review the reason and update your information.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (rejectionReason.isNotBlank()) {
                        Text(
                            "Reason: $rejectionReason",
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                else -> Text(
                    "Complete the form below to verify your driver account.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
