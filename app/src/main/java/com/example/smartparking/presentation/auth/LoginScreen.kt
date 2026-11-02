package com.example.smartparking.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartparking.presentation.navigation.Routes

private val Navy = Color(0xFF0B1220)
private val CardColor = Color(0xFF151F30)
private val AccentBlue = Color(0xFF3985FF)
private val MutedText = Color(0xFF9BA9BD)

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            val destination = when (user.role.uppercase()) {
                "DRIVER" -> Routes.DriverHome.route
                "OWNER" -> Routes.OwnerHome.route
                else -> Routes.Login.route
            }

            if (destination != Routes.Login.route) {
                navController.navigate(destination) {
                    popUpTo(Routes.Login.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(65.dp))

            // Branding
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        AccentBlue.copy(alpha = 0.15f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentBlue
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Smart Parking",
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your space. Your time. Your convenience.",
                fontSize = 13.sp,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Login Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        CardColor,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(22.dp)
            ) {

                Text(
                    text = "Welcome back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "Sign in to continue to your account.",
                    fontSize = 13.sp,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Email address",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(9.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        validationError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Enter your email",
                            color = MutedText
                        )
                    },
                    leadingIcon = {
                        Text("✉️")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(13.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color(0xFF344258),
                        cursorColor = AccentBlue,
                        focusedContainerColor = Navy,
                        unfocusedContainerColor = Navy
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Password",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(9.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        validationError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Enter your password",
                            color = MutedText
                        )
                    },
                    leadingIcon = {
                        Text("🔒")
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                passwordVisible = !passwordVisible
                            }
                        ) {
                            Text(
                                if (passwordVisible) "Hide" else "Show",
                                color = AccentBlue
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(13.dp),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color(0xFF344258),
                        cursorColor = AccentBlue,
                        focusedContainerColor = Navy,
                        unfocusedContainerColor = Navy
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        validationError =
                            "Password reset will be available in the next authentication step."
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        "Forgot password?",
                        color = AccentBlue,
                        fontSize = 13.sp
                    )
                }

                val errorMessage = validationError ?: uiState.error

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF7777),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Login Button
                Button(
                    onClick = {
                        when {
                            email.isBlank() -> {
                                validationError = "Please enter your email."
                            }

                            password.isBlank() -> {
                                validationError = "Please enter your password."
                            }

                            else -> {
                                validationError = null
                                authViewModel.login(
                                    email.trim(),
                                    password
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        disabledContainerColor = AccentBlue.copy(alpha = 0.5f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(23.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Don't have an account? ",
                        color = MutedText,
                        fontSize = 13.sp
                    )

                    TextButton(
                        onClick = {
                            navController.navigate(Routes.Register.route)
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Create account",
                            color = AccentBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "SMART PARKING SYSTEM",
                color = MutedText,
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(25.dp))
        }
    }
}