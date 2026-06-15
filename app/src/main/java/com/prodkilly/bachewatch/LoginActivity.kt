package com.prodkilly.bachewatch

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    // BUG 6 FIX: se pasa el contexto para manejar errores en la Activity
                    onLoginClick = { onFailure -> loginAnonimo(onFailure) }
                )
            }
        }
    }

    // BUG 6 FIX: recibe un callback para resetear el estado de carga si falla
    private fun loginAnonimo(onFailure: () -> Unit) {
        auth.signInAnonymously()
            .addOnSuccessListener {
                irAlHome()
            }
            .addOnFailureListener { exception ->
                onFailure() // Avisa a la UI para que reactive el botón
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

@Composable
fun LoginScreen(onLoginClick: (onFailure: () -> Unit) -> Unit) {
    var cargando by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "BacheWatch",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Ingresa tus datos para continuar",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Campo de correo
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Correo electrónico") },
                placeholder = { Text("ejemplo@correo.com") },
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // BUG 1 FIX: campo de contraseña ahora va ANTES de los botones
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text("Contraseña") },
                isError = passwordError != null,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // BUG 2 y 4 FIX: un solo bloque if/else, un solo indicador de carga
            if (cargando) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Button(
                        onClick = {
                            if (validarFormulario(email, password, { emailError = it }, { passwordError = it })) {
                                cargando = true
                                // BUG 6 FIX: si falla, cargando vuelve a false
                                onLoginClick { cargando = false }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Ingresar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(8.dp))

                    // BUG 3 y 5 FIX: contexto eliminado del Toast; registro diferenciado del login
                    TextButton(
                        onClick = {
                            if (validarFormulario(email, password, { emailError = it }, { passwordError = it })) {
                                cargando = true
                                // Aquí iría lógica real de registro (createUserWithEmailAndPassword)
                                // Por ahora reutiliza el login anónimo como placeholder
                                onLoginClick { cargando = false }
                            }
                        }
                    ) {
                        Text("¿No tienes cuenta? Regístrate")
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