
package com.example.smartparking.presentation.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartparking.presentation.navigation.Routes
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.milliseconds

private val ParkwayBlack = Color(0xFF080808)
private val ParkwayLime = Color(0xFFB8F34A)
private val ParkwayGray = Color(0xFF888888)

@Composable
fun SplashScreen(
    navController: NavController
) {

    LaunchedEffect(Unit) {
        delay(2500.milliseconds)

        val currentUser = FirebaseAuth.getInstance().currentUser

        val destination = if (currentUser == null) {
            Routes.Login.route
        } else {
            try {
                val userDocument = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val role = userDocument.getString("role")
                    ?.uppercase()

                when (role) {
                    "DRIVER" -> Routes.DriverHome.route
                    "OWNER" -> Routes.OwnerHome.route
                    "ADMIN" -> Routes.AdminDashboard.route
                    else -> Routes.Login.route
                }
            } catch (e: Exception) {
                // Keep the Firebase session. Do not sign the user out here.
                Routes.Login.route
            }
        }

        navController.navigate(destination) {
            popUpTo(Routes.Splash.route) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(
        label = "splash_animation"
    )

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val loadingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = FastOutSlowInEasing
            )
        ),
        label = "loading_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ParkwayBlack),
        contentAlignment = Alignment.Center
    ) {

        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {

            // Animated logo container
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .scale(logoScale)
                    .background(
                        color = ParkwayLime,
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                // Parking logo
                Text(
                    text = "P",
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Black,
                    color = ParkwayBlack,
                    letterSpacing = (-5).sp
                )

                // Small location indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(9.dp)
                        .size(19.dp)
                        .background(
                            ParkwayBlack,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                ParkwayLime,
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand name
            Text(
                text = "parkway",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1.5).sp
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "SMART PARKING, SIMPLIFIED.",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ParkwayLime,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(55.dp))

            // Animated loading indicator
            Box(
                modifier = Modifier.size(38.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(loadingRotation)
                ) {
                    drawArc(
                        color = Color(0xFF303030),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(
                            width = 3.dp.toPx()
                        )
                    )

                    drawArc(
                        color = ParkwayLime,
                        startAngle = 0f,
                        sweepAngle = 95f,
                        useCenter = false,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Finding your way to better parking...",
                fontSize = 12.sp,
                color = ParkwayGray,
                fontWeight = FontWeight.Medium
            )
        }

        // Bottom branding
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 35.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "FIND  •  BOOK  •  PARK",
                fontSize = 10.sp,
                color = Color(0xFF777777),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = "MADE FOR SMARTER CITIES",
                fontSize = 9.sp,
                color = Color(0xFF444444),
                letterSpacing = 1.5.sp
            )
        }
    }
}
