
package com.example.smartparking.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartparking.presentation.navigation.Routes
import kotlinx.coroutines.launch

private val ParkwayBlack = Color(0xFF080808)
private val ParkwayWhite = Color(0xFFFFFFFF)
private val ParkwayGreen = Color(0xFFB8F34A)
private val ParkwayGray = Color(0xFF777777)
private val ParkwayInput = Color(0xFFF4F4F4)
private val ParkwayError = Color(0xFFD32F2F)


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

    // Keyboard visibility and automatic scrolling
    val emailRequester = remember { BringIntoViewRequester() }
    val passwordRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Normal Firebase user navigation
    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            val destination = when (user.role.trim().uppercase()) {
                "DRIVER" -> Routes.DriverHome.route
                "OWNER" -> Routes.OwnerHome.route
                "ADMIN" -> Routes.AdminDashboard.route
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ParkwayWhite)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 25.dp)
    ) {
        Spacer(modifier = Modifier.height(65.dp))

        // PARKWAY BRAND
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        ParkwayBlack,
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P",
                    color = ParkwayGreen,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "parkway",
                    color = ParkwayBlack,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = "SMART PARKING, SIMPLIFIED",
                    color = ParkwayGray,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(65.dp))

        // INTRODUCTION
        Text(
            text = "PARK SMARTER.",
            fontSize = 35.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ParkwayBlack,
            letterSpacing = (-1.5).sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "LIVE BETTER.",
                fontSize = 35.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ParkwayBlack,
                letterSpacing = (-1.5).sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        ParkwayGreen,
                        RoundedCornerShape(50)
                    )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Find your space. Save your time.\nYour parking journey starts here.",
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = ParkwayGray
        )

        Spacer(modifier = Modifier.height(42.dp))

        // LOGIN SECTION
        Text(
            text = "Welcome back",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ParkwayBlack
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = "Sign in to your Parkway account.",
            fontSize = 13.sp,
            color = ParkwayGray
        )

        Spacer(modifier = Modifier.height(28.dp))

        // EMAIL
        Text(
            text = "EMAIL ADDRESS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = ParkwayBlack
        )

        Spacer(modifier = Modifier.height(9.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                validationError = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(emailRequester)
                .onFocusEvent {
                    if (it.isFocused) {
                        coroutineScope.launch {
                            emailRequester.bringIntoView()
                        }
                    }
                },
            placeholder = {
                Text(
                    "Enter your email",
                    color = ParkwayGray
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(13.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ParkwayBlack,
                unfocusedTextColor = ParkwayBlack,
                focusedBorderColor = ParkwayBlack,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = ParkwayInput,
                unfocusedContainerColor = ParkwayInput,
                cursorColor = ParkwayBlack
            )
        )

        Spacer(modifier = Modifier.height(21.dp))

        // PASSWORD
        Text(
            text = "PASSWORD",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = ParkwayBlack
        )

        Spacer(modifier = Modifier.height(9.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                validationError = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(passwordRequester)
                .onFocusEvent {
                    if (it.isFocused) {
                        coroutineScope.launch {
                            passwordRequester.bringIntoView()
                        }
                    }
                },
            placeholder = {
                Text(
                    "Enter your password",
                    color = ParkwayGray
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(13.dp),
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Text(
                        if (passwordVisible) "HIDE" else "SHOW",
                        color = ParkwayBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ParkwayBlack,
                unfocusedTextColor = ParkwayBlack,
                focusedBorderColor = ParkwayBlack,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = ParkwayInput,
                unfocusedContainerColor = ParkwayInput,
                cursorColor = ParkwayBlack
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = {
                validationError =
                    "Password reset will be available in the next authentication step."
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                "Forgot password?",
                color = ParkwayBlack,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ERRORS
        val errorMessage = validationError ?: uiState.error

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = ParkwayError,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // SIGN IN BUTTON
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
                .height(58.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ParkwayBlack,
                contentColor = ParkwayWhite,
                disabledContainerColor = ParkwayGray
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(23.dp),
                    color = ParkwayGreen,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        "→",
                        color = ParkwayGreen,
                        fontSize = 22.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(27.dp))

        // REGISTER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "New to Parkway?",
                color = ParkwayGray,
                fontSize = 13.sp
            )

            TextButton(
                onClick = {
                    navController.navigate(Routes.Register.route)
                },
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Text(
                    "Create account",
                    color = ParkwayBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))


        // FOOTER
        HorizontalDivider(color = Color(0xFFEEEEEE))

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "PARKWAY",
                color = ParkwayBlack,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Text(
                "PARKING MADE SIMPLE.",
                color = ParkwayGray,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(25.dp))
    }

}
