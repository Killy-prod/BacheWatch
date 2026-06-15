package com.prodkilly.bachewatch

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.prodkilly.bachewatch.ui.theme.BacheWatchTheme

class LoginActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (auth.currentUser != null) {
            irAlHome()
            return
        }

        setContent {
            BacheWatchTheme {
                LoginScreen(
                    onLoginClick = { onFailure -> loginAnonimo(onFailure) }
                )
            }
        }
    }

    private fun loginAnonimo(onFailure: () -> Unit) {
        auth.signInAnonymously()
            .addOnSuccessListener {
                irAlHome()
            }
            .addOnFailureListener { exception ->
                onFailure()
                Toast.makeText(
                    this,
                    "Error al conectar: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun irAlHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

// ─── Paleta ───────────────────────────────────────────────────────────────────
private val BwMorado    = Color(0xFF520943)
private val BwMagenta   = Color(0xFFAC0E4F)
private val BwCyan      = Color(0xFF209CD8)
private val BwCyanClaro = Color(0xFF20DAD8)
private val BwMenta     = Color(0xFFA1EBE9)

@Composable
fun LoginScreen(onLoginClick: (onFailure: () -> Unit) -> Unit) {
    var cargando by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Fondo con gradiente morado → magenta oscuro
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BwMorado,
                        Color(0xFF7A0A35),
                        Color(0xFF3D0630)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ─── Logo / ícono ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = BwMagenta.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = BwMenta,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "BacheWatch",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
                color = Color.White
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Ingresa tus datos para continuar",
                fontSize = 14.sp,
                color = BwMenta.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // ─── Tarjeta de formulario ────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.10f),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Campo de correo
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = {
                            Text(
                                "Correo electrónico",
                                color = BwMenta.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        },
                        placeholder = {
                            Text(
                                "ejemplo@correo.com",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 13.sp
                            )
                        },
                        isError = emailError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BwCyanClaro,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            errorBorderColor = BwMagenta,
                            cursorColor = BwCyanClaro,
                            focusedLabelColor = BwCyanClaro,
                            unfocusedLabelColor = BwMenta.copy(alpha = 0.7f)
                        )
                    )
                    if (emailError != null) {
                        Text(
                            text = emailError!!,
                            color = BwCyanClaro,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Campo de contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = {
                            Text(
                                "Contraseña",
                                color = BwMenta.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        },
                        isError = passwordError != null,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BwCyanClaro,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            errorBorderColor = BwMagenta,
                            cursorColor = BwCyanClaro,
                            focusedLabelColor = BwCyanClaro,
                            unfocusedLabelColor = BwMenta.copy(alpha = 0.7f)
                        )
                    )
                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            color = BwCyanClaro,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─── Botones ──────────────────────────────────────────────────
            if (cargando) {
                CircularProgressIndicator(
                    color = BwCyanClaro,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón principal: gradiente cyan → magenta
                    Button(
                        onClick = {
                            if (validarFormulario(email, password, { emailError = it }, { passwordError = it })) {
                                cargando = true
                                onLoginClick { cargando = false }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(BwCyan, BwMagenta)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Ingresar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    TextButton(
                        onClick = {
                            if (validarFormulario(email, password, { emailError = it }, { passwordError = it })) {
                                cargando = true
                                onLoginClick { cargando = false }
                            }
                        }
                    ) {
                        Text(
                            "¿No tienes cuenta? Regístrate",
                            color = BwMenta,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun validarFormulario(
    email: String,
    password: String,
    onEmailError: (String?) -> Unit,
    onPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (email.isBlank()) {
        onEmailError("El correo no puede estar vacío")
        isValid = false
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError("Ingresa un correo válido")
        isValid = false
    }

    if (password.isBlank()) {
        onPasswordError("La contraseña no puede estar vacía")
        isValid = false
    } else if (password.length < 6) {
        onPasswordError("Mínimo 6 caracteres")
        isValid = false
    }

    return isValid
}