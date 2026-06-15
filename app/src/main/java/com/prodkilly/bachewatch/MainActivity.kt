package com.prodkilly.bachewatch

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prodkilly.bachewatch.ui.theme.BacheWatchTheme
import java.text.SimpleDateFormat
import java.util.Locale
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BacheWatchTheme {
                HomeScreen()
            }
        }
    }
}

// ─── Colores de diseño ────────────────────────────────────────────────────────

private val ColorLeve     = Color(0xFF22C55E)   // verde
private val ColorModerado = Color(0xFFF59E0B)   // ámbar
private val ColorGrave    = Color(0xFFEF4444)   // rojo
private val ColorPendiente   = Color(0xFF94A3B8)
private val ColorEnRevision  = Color(0xFF3B82F6)
private val ColorResuelto    = Color(0xFF22C55E)

// ─── Pantalla principal ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val uiState by vm.uiState.observeAsState(HomeUiState.Loading)
    val navegarAReporte by vm.navegarAReporte.observeAsState(false)
    val context = LocalContext.current

    LaunchedEffect(navegarAReporte) {
        if (navegarAReporte) {
            val intent = Intent(context, ReporteActivity::class.java)
            context.startActivity(intent)
            vm.onNavegacionCompletada()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BacheWatch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Reportes ciudadanos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.onReportarBacheClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(
                        "Reportar bache",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> LoadingState()
                is HomeUiState.Empty   -> EmptyState()
                is HomeUiState.Error   -> ErrorState(state.mensaje) { vm.cargarReportes() }
                is HomeUiState.Success -> ReportesList(state.reportes)
            }
        }
    }
}

// ─── Lista de reportes ────────────────────────────────────────────────────────

@Composable
fun ReportesList(reportes: List<BacheReport>) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 8.dp, bottom = 104.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis reportes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${reportes.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
        items(reportes, key = { it.id }) { reporte ->
            ReporteCard(reporte)
        }
    }
}

// ─── Card de cada reporte ─────────────────────────────────────────────────────

@Composable
// Asegúrate de importar la función AsyncImage de Coil en la parte superior:
// import io.coil-kt.compose.AsyncImage
fun ReporteCard(reporte: BacheReport) {
    val dateFormat = remember { SimpleDateFormat("d MMM · yyyy", Locale("es", "MX")) }

    val accentColor = when (reporte.gravedad) {
        Gravedad.LEVE     -> ColorLeve
        Gravedad.MODERADO -> ColorModerado
        Gravedad.GRAVE    -> ColorGrave
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Barra lateral de color según gravedad
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            // Contenedor principal que divide el texto (izquierda) de la foto (derecha)
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // COLUMNA IZQUIERDA: Textos y Datos
                Column(modifier = Modifier.weight(1f)) {
                    // Dirección + fecha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = accentColor
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = reporte.direccion,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = (-0.2).sp
                            )
                        }
                        Text(
                            text = dateFormat.format(reporte.fechaCreacion),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 10.sp
                        )
                    }

                    Spacer(Modifier.height(5.dp))

                    // Descripción
                    Text(
                        text = reporte.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(8.dp))

                    // Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GravedadChip(reporte.gravedad)
                        StatusChip(reporte.status)
                    }
                }

                // COLUMNA DERECHA: Evidencia Visual (Carga la foto simulada o real)
                // Usamos ?.let para asegurar que si fotoUrl es nulo, este bloque simplemente se ignore
                reporte.fotoUrl?.let { url ->
                    if (url.isNotBlank() && url != "sin_foto") {
                        Spacer(Modifier.width(12.dp))

                        // Usamos la clase directa de Coil sin el prefijo "io.coil3" que causaba el conflicto
                        coil.compose.AsyncImage(
                            model = url,
                            contentDescription = "Evidencia del bache",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
// ─── Chips ────────────────────────────────────────────────────────────────────

@Composable
fun GravedadChip(gravedad: Gravedad) {
    val (label, color) = when (gravedad) {
        Gravedad.LEVE     -> "Leve"     to ColorLeve
        Gravedad.MODERADO -> "Moderado" to ColorModerado
        Gravedad.GRAVE    -> "Grave"    to ColorGrave
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun StatusChip(status: ReportStatus) {
    val (label, color) = when (status) {
        ReportStatus.PENDIENTE   -> "Pendiente"   to ColorPendiente
        ReportStatus.EN_REVISION -> "En revisión" to ColorEnRevision
        ReportStatus.RESUELTO    -> "Resuelto"    to ColorResuelto
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.10f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ─── Estados alternativos ─────────────────────────────────────────────────────

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Cargando reportes…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Sin reportes aún",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Usa el botón inferior para reportar\nel primer bache de tu zona.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ErrorState(mensaje: String, onReintentar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Algo salió mal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            FilledTonalButton(
                onClick = onReintentar,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reintentar", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}