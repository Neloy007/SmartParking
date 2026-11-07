
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
import androidx.compose.ui.text.input.*
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
private val ParkwayBorder = Color(0xFFEAEAEA)
private val ParkwayError = Color(0xFFD32F2F)

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
            val destination = when (user.role.uppercase()) {
                "OWNER" -> Routes.OwnerHome.route
                else -> Routes.DriverHome.route
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
            .background(ParkwayWhite)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Spacer(modifier = Modifier.height(38.dp))

        // PARKWAY BRAND
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        ParkwayBlack,
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "P",
                    color = ParkwayGreen,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(11.dp))

            Column {
                Text(
                    "parkway",
                    color = ParkwayBlack,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )

                Text(
                    "SMART PARKING, SIMPLIFIED",
                    color = ParkwayGray,
                    fontSize = 8.sp,
                    letterSpacing = 1.3.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(43.dp))

        // HEADING
        Text(
            "LET'S GET YOU",
            fontSize = 31.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ParkwayBlack,
            letterSpacing = (-1).sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "STARTED.",
                fontSize = 31.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ParkwayBlack,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(11.dp)
                    .background(
                        ParkwayGreen,
                        RoundedCornerShape(50)
                    )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Create your Parkway account and enjoy\nsmarter, simpler parking.",
            color = ParkwayGray,
            fontSize = 13.sp,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // PERSONAL INFORMATION
        Text(
            "PERSONAL INFORMATION",
            color = ParkwayBlack,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        ParkwayRegisterField(
            value = name,
            onValueChange = {
                name = it
                validationError = null
            },
            label = "Full name",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(15.dp))

        ParkwayRegisterField(
            value = email,
            onValueChange = {
                email = it
                validationError = null
            },
            label = "Email address",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(15.dp))

        ParkwayRegisterField(
            value = phone,
            onValueChange = {
                phone = it
                validationError = null
            },
            label = "Phone number",
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(29.dp))

        // ACCOUNT TYPE
        Text(
            "CHOOSE ACCOUNT TYPE",
            color = ParkwayBlack,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(13.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ParkwayRoleCard(
                title = "Driver",
                subtitle = "Find parking spaces",
                selected = selectedRole == "DRIVER",
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedRole = "DRIVER"
                }
            )

            ParkwayRoleCard(
                title = "Owner",
                subtitle = "Manage parking",
                selected = selectedRole == "OWNER",
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedRole = "OWNER"
                }
            )
        }

        Spacer(modifier = Modifier.height(29.dp))

        // PASSWORD
        Text(
            "SECURITY",
            color = ParkwayBlack,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(17.dp))

        ParkwayRegisterField(
            value = password,
            onValueChange = {
                password = it
                validationError = null
            },
            label = "Create password",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePassword = {
                passwordVisible = !passwordVisible
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        ParkwayRegisterField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                validationError = null
            },
            label = "Confirm password",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onTogglePassword = {
                confirmPasswordVisible = !confirmPasswordVisible
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Use at least 8 characters for your password.",
            color = ParkwayGray,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(19.dp))

        // ERRORS
        val errorMessage = validationError ?: uiState.error

        errorMessage?.let {
            Text(
                text = it,
                color = ParkwayError,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // CREATE ACCOUNT
        Button(
            onClick = {
                val cleanName = name.trim()
                val cleanEmail = email.trim()
                val cleanPhone = phone.trim()

                val error = when {
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

                validationError = error

                if (error == null) {
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
                .height(57.dp),
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
                    modifier = Modifier.size(22.dp),
                    color = ParkwayGreen,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Create Account",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        "→",
                        color = ParkwayGreen,
                        fontSize = 21.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(23.dp))

        // LOGIN NAVIGATION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Already have an account?",
                color = ParkwayGray,
                fontSize = 12.sp
            )

            TextButton(
                onClick = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Register.route) {
                            inclusive = true
                        }
                    }
                },
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Text(
                    "Sign In",
                    color = ParkwayBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        HorizontalDivider(color = ParkwayBorder)

        Spacer(modifier = Modifier.height(17.dp))

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

@Composable
private fun ParkwayRoleCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(91.dp),
        shape = RoundedCornerShape(13.dp),
        color = if (selected) ParkwayBlack else ParkwayInput,
        border = if (selected) null else
            androidx.compose.foundation.BorderStroke(
                1.dp,
                ParkwayBorder
            )
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    color = if (selected) ParkwayWhite else ParkwayBlack,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(
                                ParkwayGreen,
                                RoundedCornerShape(50)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                subtitle,
                color = if (selected) Color.LightGray else ParkwayGray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ParkwayRegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        placeholder = {
            Text(
                label,
                color = ParkwayGray,
                fontSize = 13.sp
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            if (isPassword) {
                TextButton(
                    onClick = {
                        onTogglePassword?.invoke()
                    }
                ) {
                    Text(
                        if (passwordVisible) "HIDE" else "SHOW",
                        color = ParkwayBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        shape = RoundedCornerShape(13.dp),
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
}
