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
import androidx.compose.ui.graphics.Brush
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

// ─── Paleta BacheWatch ────────────────────────────────────────────────────────
private val BwMorado    = Color(0xFF520943)
private val BwMagenta   = Color(0xFFAC0E4F)
private val BwCyan      = Color(0xFF209CD8)
private val BwCyanClaro = Color(0xFF20DAD8)
private val BwMenta     = Color(0xFFA1EBE9)

// Chips de gravedad con la nueva paleta
private val ColorLeve     = Color(0xFF20DAD8)   // cyan claro
private val ColorModerado = Color(0xFFAC0E4F)   // magenta
private val ColorGrave    = Color(0xFF520943)   // morado

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
            // TopBar con el mismo fondo morado que MainActivity
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BwMorado)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = BwMenta,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Nuevo reporte",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = (-0.5).sp,
                            color = Color.White
                        )
                        Text(
                            text = "BacheWatch",
                            fontSize = 11.sp,
                            color = BwMenta.copy(alpha = 0.75f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F0F4)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Sección: Ubicación ──────────────────────────────────────────
            SectionLabel("Ubicación detectada")
            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícono con fondo cyan
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = BwCyan.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = BwCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = direccionTexto,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = BwMorado,
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
                // Estado: foto seleccionada — fondo con tono cyan claro
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = BwCyanClaro.copy(alpha = 0.10f)
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
                                tint = BwCyanClaro,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Foto lista para enviar",
                                fontSize = 13.sp,
                                color = Color(0xFF0D6E6E),  // versión oscura del cyan para legibilidad
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "Cambiar",
                                fontSize = 12.sp,
                                color = BwCyan
                            )
                        }
                    }
                }
            } else {
                // Estado: sin foto — botón con borde cyan
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BwCyan
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BwCyan.copy(alpha = 0.5f)),
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
                        color = Color(0xFFBBBBBB),
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = BwMorado
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFDDD0DA),
                    focusedBorderColor = BwCyan,
                    cursorColor = BwCyan,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
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
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) chipColor.copy(alpha = 0.14f)
                        else Color.White,
                        border = if (isSelected)
                            ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(chipColor),
                                width = 1.5.dp
                            )
                        else
                            ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE0D0DC)),
                                width = 0.8.dp
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) chipColor else Color(0xFF888888)
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
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = BwMagenta
                        )
                        Text(
                            text = "Enviando reporte…",
                            fontSize = 13.sp,
                            color = BwMorado.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Botón con gradiente magenta → morado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = if (vm.descripcion.isNotBlank() && latitud != 0.0)
                                Brush.horizontalGradient(listOf(BwMagenta, BwMorado))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFFCCCCCC), Color(0xFFBBBBBB)))
                        )
                ) {
                    Button(
                        onClick = { vm.guardarReporte(latitud, longitud, direccionTexto) },
                        modifier = Modifier.fillMaxSize(),
                        enabled = vm.descripcion.isNotBlank() && latitud != 0.0,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Enviar reporte",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            color = Color.White
                        )
                    }
                }
            }

            vm.mensajeError?.let { error ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BwMagenta.copy(alpha = 0.10f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(BwMagenta, shape = RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = error,
                            color = BwMagenta,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Etiqueta de sección ──────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(12.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(BwCyan, BwMagenta)),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text.uppercase(),
            fontSize = 10.sp,
            color = BwMorado,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
    }
}

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