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

// ─── Paleta BacheWatch ────────────────────────────────────────────────────────
private val BwMorado    = Color(0xFF520943)   // color1 — fondo oscuro / TopBar
private val BwMagenta   = Color(0xFFAC0E4F)   // color2 — acento principal / FAB
private val BwCyan      = Color(0xFF209CD8)   // color3 — acento secundario / links
private val BwCyanClaro = Color(0xFF20DAD8)   // color4 — estados activos
private val BwMenta     = Color(0xFFA1EBE9)   // color5 — chips / badges suaves

// Colores de gravedad adaptados a la paleta
private val ColorLeve     = Color(0xFF20DAD8)   // cyan claro (leve = frío)
private val ColorModerado = Color(0xFFAC0E4F)   // magenta (moderado = alerta)
private val ColorGrave    = Color(0xFF520943)   // morado oscuro (grave = crítico)

// Colores de status
private val ColorPendiente   = Color(0xFF209CD8)   // cyan
private val ColorEnRevision  = Color(0xFFAC0E4F)   // magenta
private val ColorResuelto    = Color(0xFF20DAD8)   // cyan claro

// Gradiente de header
private val GradienteHeader = Brush.horizontalGradient(
    listOf(Color(0xFF520943), Color(0xFF7A0A35))
)

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BwMorado)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ícono decorativo
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = BwMagenta.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = BwMenta,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "BacheWatch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp,
                            color = Color.White
                        )
                        Text(
                            text = "Reportes ciudadanos",
                            fontSize = 11.sp,
                            color = BwMenta.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.onReportarBacheClick() },
                containerColor = BwMagenta,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
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
        containerColor = Color(0xFFF5F0F4)
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
            top = 12.dp, bottom = 104.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BwMorado
                )
                // Badge contador con color de la paleta
                Box(
                    modifier = Modifier
                        .background(
                            color = BwMagenta,
                            shape = CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${reportes.size}",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
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
fun ReporteCard(reporte: BacheReport) {
    val dateFormat = remember { SimpleDateFormat("d MMM · yyyy", Locale("es", "MX")) }

    val accentColor = when (reporte.gravedad) {
        Gravedad.LEVE     -> ColorLeve
        Gravedad.MODERADO -> ColorModerado
        Gravedad.GRAVE    -> ColorGrave
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Barra lateral con gradiente de la paleta
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                                tint = BwCyan
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = reporte.direccion,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BwMorado,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = (-0.2).sp
                            )
                        }
                        Text(
                            text = dateFormat.format(reporte.fechaCreacion),
                            fontSize = 10.sp,
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = reporte.descripcion,
                        fontSize = 12.sp,
                        color = Color(0xFF6B6B6B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color(0xFFEEEEEE)
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GravedadChip(reporte.gravedad)
                        StatusChip(reporte.status)
                    }
                }

                reporte.fotoUrl?.let { url ->
                    if (url.isNotBlank() && url != "sin_foto") {
                        Spacer(Modifier.width(12.dp))
                        coil.compose.AsyncImage(
                            model = url,
                            contentDescription = "Evidencia del bache",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF0E8EF)),
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
        color = color.copy(alpha = 0.13f)
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
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.SemiBold
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
            fontSize = 10.sp,
            color = color,
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
                color = BwMagenta,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "Cargando reportes…",
                fontSize = 13.sp,
                color = BwMorado.copy(alpha = 0.6f)
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
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(BwMagenta.copy(alpha = 0.15f), BwMorado.copy(alpha = 0.08f))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = BwMagenta,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Sin reportes aún",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = BwMorado
            )
            Text(
                text = "Usa el botón inferior para reportar\nel primer bache de tu zona.",
                fontSize = 13.sp,
                color = Color(0xFF888888),
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
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        color = BwMagenta.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = BwMagenta,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Algo salió mal",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = BwMorado
            )
            Text(
                text = mensaje,
                fontSize = 13.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onReintentar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BwMagenta)
            ) {
                Text("Reintentar", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}