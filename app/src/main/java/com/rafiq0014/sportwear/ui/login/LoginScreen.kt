package com.rafiq0014.sportwear.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq0014.sportwear.R
import com.rafiq0014.sportwear.viewmodel.AuthState
import com.rafiq0014.sportwear.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    navigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val authState by viewModel.authState.collectAsState()

    if (authState is AuthState.Authenticated) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            navigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        // Decorative sporty ambient glows
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = CircleShape)
                .blur(80.dp)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 80.dp)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), shape = CircleShape)
                .blur(70.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header / Brand Logo representation
            Text(
                text = "SPORTWEAR",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "E L E V A T E   Y O U R   G E A R",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 48.dp)
            )

            // Glassmorphic-style login card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sign in to manage and sync your sportswear product catalog easily.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isLoading || authState is AuthState.Loading || authState is AuthState.Authenticated) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        // Premium Google Sign-In Button
                        Button(
                            onClick = { viewModel.signInWithGoogle(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Google G Logo Icon Custom Drawing
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawArc(
                                        color = Color(0xFFEA4335),
                                        startAngle = 135f,
                                        sweepAngle = 90f,
                                        useCenter = true
                                    )
                                    drawArc(
                                        color = Color(0xFFFBBC05),
                                        startAngle = 225f,
                                        sweepAngle = 90f,
                                        useCenter = true
                                    )
                                    drawArc(
                                        color = Color(0xFF34A853),
                                        startAngle = 315f,
                                        sweepAngle = 90f,
                                        useCenter = true
                                    )
                                    drawArc(
                                        color = Color(0xFF4285F4),
                                        startAngle = 45f,
                                        sweepAngle = 90f,
                                        useCenter = true
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = stringResource(R.string.sign_in_with_google),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            // Footer offline note
            Text(
                text = "Supports Offline Mode & Sync",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
