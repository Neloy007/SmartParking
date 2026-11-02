
package com.example.smartparking.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartparking.presentation.navigation.Routes

private val RegisterBackground = Color(0xFF0B1220)
private val RegisterCard = Color(0xFF151F30)
private val RegisterBlue = Color(0xFF3985FF)
private val RegisterText = Color(0xFFF1F5F9)
private val RegisterMuted = Color(0xFF94A3B8)

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var selectedRole by remember { mutableStateOf("DRIVER") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.user) {
        val user = uiState.user
        if (user != null) {
            val destination = if (user.role == "OWNER") {
                Routes.OwnerHome.route
            } else {
                Routes.DriverHome.route
            }

            navController.navigate(destination) {
                popUpTo(Routes.Login.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RegisterBackground)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(12.dp))

        // App logo
        Surface(
            modifier = Modifier.size(66.dp),
            shape = CircleShape,
            color = RegisterBlue
        ) {
            BoxContent()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RegisterText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Join Smart Parking and make parking easier.",
            color = RegisterMuted,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = RegisterCard
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Personal Information",
                    color = RegisterText,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )

                RegisterTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = "Full name",
                    keyboardType = KeyboardType.Text
                )

                RegisterTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        validationError = null
                    },
                    label = "Email address",
                    keyboardType = KeyboardType.Email
                )

                RegisterTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        validationError = null
                    },
                    label = "Phone number",
                    keyboardType = KeyboardType.Phone
                )

                Text(
                    text = "Account Type",
                    color = RegisterText,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = selectedRole == "DRIVER",
                        onClick = {
                            selectedRole = "DRIVER"
                        },
                        label = {
                            Text("Driver")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedRole == "OWNER",
                        onClick = {
                            selectedRole = "OWNER"
                        },
                        label = {
                            Text("Parking Owner")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Create Password",
                    color = RegisterText,
                    fontWeight = FontWeight.SemiBold
                )

                RegisterTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        validationError = null
                    },
                    label = "Password",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = {
                        passwordVisible = !passwordVisible
                    }
                )

                RegisterTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        validationError = null
                    },
                    label = "Confirm password",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    passwordVisible = confirmPasswordVisible,
                    onTogglePassword = {
                        confirmPasswordVisible = !confirmPasswordVisible
                    }
                )

                validationError?.let {
                    Text(
                        text = it,
                        color = Color(0xFFFF7777),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                uiState.error?.let {
                    Text(
                        text = it,
                        color = Color(0xFFFF7777),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        val cleanName = name.trim()
                        val cleanEmail = email.trim()
                        val cleanPhone = phone.trim()

                        validationError = when {
                            cleanName.length < 2 ->
                                "Please enter your full name."

                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(cleanEmail).matches() ->
                                "Please enter a valid email address."

                            cleanPhone.count { it.isDigit() } !in 7..15 ->
                                "Please enter a valid phone number."

                            password.length < 8 ->
                                "Password must contain at least 8 characters."

                            confirmPassword != password ->
                                "Passwords do not match."

                            else -> null
                        }

                        if (validationError == null) {
                            authViewModel.register(
                                name = cleanName,
                                email = cleanEmail,
                                password = password,
                                phone = cleanPhone,
                                role = selectedRole
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RegisterBlue
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                color = RegisterMuted
            )

            TextButton(
                onClick = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Register.route) {
                            inclusive = true
                        }
                    }
                }
            ) {
                Text(
                    text = "Sign In",
                    color = RegisterBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun BoxContent() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "P",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(label)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            if (isPassword) {
                TextButton(
                    onClick = { onTogglePassword?.invoke() }
                ) {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        color = RegisterBlue
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = RegisterText,
            unfocusedTextColor = RegisterText,
            focusedBorderColor = RegisterBlue,
            unfocusedBorderColor = Color(0xFF334155),
            focusedLabelColor = RegisterBlue,
            unfocusedLabelColor = RegisterMuted,
            cursorColor = RegisterBlue
        )
    )
}
