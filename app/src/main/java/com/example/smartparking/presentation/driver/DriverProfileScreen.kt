
package com.example.smartparking.presentation.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private val ProfileBlue = Color(0xFF155EEF)
private val ProfileBackground = Color(0xFFF8FAFC)
private val ProfileText = Color(0xFF0F172A)
private val ProfileGray = Color(0xFF64748B)
private val ProfileBorder = Color(0xFFE2E8F0)

@Composable
fun DriverProfileScreen(
    onLogout: () -> Unit,
    onVerificationClick: () -> Unit = {}
) {

    val authUser = FirebaseAuth.getInstance().currentUser

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    var databaseName by remember {
        mutableStateOf<String?>(null)
    }

    var isNameLoading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(authUser?.uid) {

        val uid = authUser?.uid

        if (uid == null) {
            isNameLoading = false
            return@LaunchedEffect
        }

        try {

            val document = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            databaseName = document.getString("name")
                ?.trim()
                ?.takeIf { it.isNotBlank() }

        } catch (e: Exception) {

            databaseName = null

        } finally {

            isNameLoading = false
        }
    }

    val displayName = when {
        isNameLoading -> "Loading..."
        databaseName != null -> databaseName!!
        else -> "Name unavailable"
    }

    val email = authUser?.email ?: "Email not available"
    val phone = authUser?.phoneNumber ?: "Not provided"
    val uid = authUser?.uid ?: "Not available"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            )
    ) {

        // ================================================
        // HEADER
        // ================================================

        Text(
            text = "My Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = ProfileText
        )

        Text(
            text = "Manage your account and verification details",
            style = MaterialTheme.typography.bodyMedium,
            color = ProfileGray,
            modifier = Modifier.padding(top = 5.dp)
        )

        Spacer(Modifier.height(24.dp))

        // ================================================
        // PROFILE HERO CARD
        // ================================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = ProfileBlue
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    modifier = Modifier.size(86.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = displayName
                                .firstOrNull()
                                ?.uppercaseChar()
                                ?.toString() ?: "D",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = ProfileBlue
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = Color.White.copy(alpha = 0.18f)
                ) {

                    Row(
                        modifier = Modifier.padding(
                            horizontal = 15.dp,
                            vertical = 7.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(Modifier.width(7.dp))

                        Text(
                            text = "DRIVER ACCOUNT",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // ================================================
        // ACCOUNT INFORMATION
        // ================================================

        ProfileSectionTitle(
            title = "Account Information",
            subtitle = "Your registered account details"
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                ProfileBorder
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                ProfileInfoRow(
                    icon = Icons.Default.Person,
                    title = "Full Name",
                    value = displayName
                )

                HorizontalDivider(color = ProfileBorder)

                ProfileInfoRow(
                    icon = Icons.Default.Email,
                    title = "Email Address",
                    value = email
                )

                HorizontalDivider(color = ProfileBorder)

                ProfileInfoRow(
                    icon = Icons.Default.Phone,
                    title = "Phone Number",
                    value = phone
                )

                HorizontalDivider(color = ProfileBorder)

                ProfileInfoRow(
                    icon = Icons.Default.Badge,
                    title = "Firebase User ID",
                    value = uid
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // ================================================
        // DRIVER VERIFICATION
        // ================================================

        ProfileSectionTitle(
            title = "Driver Verification",
            subtitle = "Verify your identity and vehicle"
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                ProfileBorder
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEFF6FF)
                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = ProfileBlue,
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(13.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Verify your driver account",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ProfileText
                        )

                        Text(
                            text = "Complete your personal and vehicle details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProfileGray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                HorizontalDivider(color = ProfileBorder)

                Spacer(Modifier.height(14.dp))

                VerificationDetailRow(
                    icon = Icons.Default.DirectionsCar,
                    text = "Vehicle registration number"
                )

                Spacer(Modifier.height(10.dp))

                VerificationDetailRow(
                    icon = Icons.Default.Badge,
                    text = "National ID (NID) number"
                )

                Spacer(Modifier.height(10.dp))

                VerificationDetailRow(
                    icon = Icons.Default.CreditCard,
                    text = "Driving licence number"
                )

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = onVerificationClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileBlue
                    )
                ) {

                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(Modifier.width(9.dp))

                    Text(
                        text = "Open Verification",
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))


        Spacer(Modifier.height(28.dp))

        // ================================================
        // LOGOUT BUTTON
        // ================================================

        OutlinedButton(
            onClick = {
                showLogoutDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFDC2626)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFFFECACA)
            )
        ) {

            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = "Log Out",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(70.dp))
    }

    // ================================================
    // LOGOUT CONFIRMATION
    // ================================================

    if (showLogoutDialog) {

        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFFDC2626)
                )
            },

            title = {
                Text(
                    text = "Log out?",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    text = "Are you sure you want to log out of your Parkway account?"
                )
            },

            confirmButton = {

                Button(
                    onClick = {
                        showLogoutDialog = false
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626)
                    )
                ) {
                    Text("Log Out")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ================================================
// PROFILE SECTION TITLE
// ================================================

@Composable
private fun ProfileSectionTitle(
    title: String,
    subtitle: String
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProfileText
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = ProfileGray
        )
    }
}

// ================================================
// PROFILE INFORMATION ROW
// ================================================

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEFF6FF)
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ProfileBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = ProfileGray,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = value,
                fontWeight = FontWeight.Medium,
                color = ProfileText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ================================================
// VERIFICATION DETAIL ROW
// ================================================

@Composable
private fun VerificationDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ProfileBlue,
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = ProfileText
        )
    }
}
