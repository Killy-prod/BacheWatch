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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteScreen(vm: ReporteViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados de ubicación en la interfaz
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var direccionTexto by remember { mutableStateOf("Buscando tu ubicación...") }
    var permisosConcedidos by remember { mutableStateOf(false) }

    // Cliente de ubicación de Google Play Services
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Lanzador para solicitar permisos de GPS
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

    // Lanzador para seleccionar/tomar una foto desde la galería del sistema
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        vm.fotoUri = uri
    }

    // Solicitar ubicación al abrir la pantalla
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Si los permisos cambian a verdadero, obtenemos las coordenadas exactas
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
                title = { Text("Nuevo Reporte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Tarjeta informativa de Ubicación Geográfica
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Ubicación Detectada", style = MaterialTheme.typography.labelLarge)
                        Text(direccionTexto, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Sección para seleccionar la evidencia fotográfica
            Text("Evidencia Fotográfica", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (vm.fotoUri != null) "Cambiar Fotografía" else "Adjuntar Foto del Bache")
            }

            // Muestra confirmación visual de la foto seleccionada
            vm.fotoUri?.let { uri ->
                Text(
                    text = "Archivo seleccionado listo para subida",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // Cuadro de texto para la descripción
            OutlinedTextField(
                value = vm.descripcion,
                onValueChange = { vm.descripcion = it },
                label = { Text("Describe las condiciones del bache") },
                placeholder = { Text("Ej. Afecta el carril de alta velocidad...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Selector de nivel de peligro
            Text("Nivel de gravedad", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Gravedad.values().forEach { nivel ->
                    FilterChip(
                        selected = vm.gravedad == nivel,
                        onClick = { vm.gravedad = nivel },
                        label = { Text(nivel.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Botón de Enviar Acción
            if (vm.estaCargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = { vm.guardarReporte(latitud, longitud, direccionTexto) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = vm.descripcion.isNotBlank() && latitud != 0.0
                ) {
                    Text("Enviar Reporte")
                }
            }

            vm.mensajeError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
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