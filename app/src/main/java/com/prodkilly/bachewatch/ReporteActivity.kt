package com.prodkilly.bachewatch

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.prodkilly.bachewatch.ui.theme.BacheWatchTheme
import java.util.Locale

class ReporteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BacheWatchTheme {
                ReporteScreen(onBack = { finish() })
            }
        }
    }
}

// ─── Colores locales ──────────────────────────────────────────────────────────

private val ColorLeve     = Color(0xFF22C55E)
private val ColorModerado = Color(0xFFF59E0B)
private val ColorGrave    = Color(0xFFEF4444)

// ─── Pantalla de reporte ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteScreen(vm: ReporteViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var direccionTexto by remember { mutableStateOf("Detectando ubicación…") }
    var permisosConcedidos by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val concedido = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        permisosConcedidos = concedido
        if (!concedido) {
            direccionTexto = "Permiso de ubicación denegado."
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        vm.fotoUri = uri
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    if (permisosConcedidos) {
        LaunchedEffect(permisosConcedidos) {
            obtenerUbicacionReal(context, fusedLocationClient) { lat, lng, addr ->
                latitud = lat
                longitud = lng
                direccionTexto = addr
            }
        }
    }

    if (vm.reporteGuardado) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Reporte enviado exitosamente", Toast.LENGTH_LONG).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nuevo reporte",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "BacheWatch",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Sección: Ubicación ──────────────────────────────────────────
            SectionLabel("Ubicación detectada")
            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = direccionTexto,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp,
                            letterSpacing = (-0.2).sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Sección: Foto ───────────────────────────────────────────────
            SectionLabel("Evidencia fotográfica")
            Spacer(Modifier.height(8.dp))

            if (vm.fotoUri != null) {
                // Estado: foto seleccionada
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = ColorLeve.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = ColorLeve,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Foto lista para enviar",
                                style = MaterialTheme.typography.bodySmall,
                                color = ColorLeve,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "Cambiar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Estado: sin foto
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    )
                ) {
                    Icon(
                        Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Adjuntar foto del bache",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Sección: Descripción ────────────────────────────────────────
            SectionLabel("Descripción")
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = vm.descripcion,
                onValueChange = { vm.descripcion = it },
                placeholder = {
                    Text(
                        "Ej. Afecta el carril de alta velocidad, profundidad aproximada de 15 cm…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(14.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(24.dp))

            // ── Sección: Gravedad ───────────────────────────────────────────
            SectionLabel("Nivel de gravedad")
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Gravedad.values().forEach { nivel ->
                    val isSelected = vm.gravedad == nivel
                    val chipColor = when (nivel) {
                        Gravedad.LEVE     -> ColorLeve
                        Gravedad.MODERADO -> ColorModerado
                        Gravedad.GRAVE    -> ColorGrave
                    }
                    val label = when (nivel) {
                        Gravedad.LEVE     -> "Leve"
                        Gravedad.MODERADO -> "Moderado"
                        Gravedad.GRAVE    -> "Grave"
                    }

                    Surface(
                        onClick = { vm.gravedad = nivel },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) chipColor.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected)
                            ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(chipColor.copy(alpha = 0.8f)),
                                width = 1.dp
                            )
                        else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) chipColor
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Botón de envío ──────────────────────────────────────────────
            if (vm.estaCargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Enviando reporte…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Button(
                    onClick = { vm.guardarReporte(latitud, longitud, direccionTexto) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = vm.descripcion.isNotBlank() && latitud != 0.0,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Enviar reporte",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.sp
                    )
                }
            }

            vm.mensajeError?.let { error ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Etiqueta de sección ──────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        fontSize = 10.sp
    )
}

/**
 * Consulta las coordenadas GPS del sensor del dispositivo y realiza ingeniería
 * inversa de coordenadas (Geocoding) para obtener una calle legible.
 */
@SuppressLint("MissingPermission")
private fun obtenerUbicacionReal(
    context: Context,
    client: com.google.android.gms.location.FusedLocationProviderClient,
    onResultado: (Double, Double, String) -> Unit
) {
    client.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val lat = location.latitude
            val lng = location.longitude
            var direccionLegible = "Coordenadas: $lat, $lng"

            try {
                val geocoder = Geocoder(context, Locale("es", "MX"))
                val direcciones = geocoder.getFromLocation(lat, lng, 1)
                if (!direcciones.isNullOrEmpty()) {
                    direccionLegible = direcciones[0].getAddressLine(0)
                }
            } catch (e: Exception) {
                // Falla el Geocoder (red ausente), mantenemos el texto con coordenadas crudas
            }
            onResultado(lat, lng, direccionLegible)
        } else {
            onResultado(19.4326, -99.1332, "Ubicación por defecto (CDMX)")
        }
    }.addOnFailureListener {
        onResultado(19.4326, -99.1332, "Error al acceder a sensores GPS")
    }
}