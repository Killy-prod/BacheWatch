package com.prodkilly.bachewatch

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prodkilly.bachewatch.ui.theme.BacheWatchTheme
import java.text.SimpleDateFormat
import java.util.Locale

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

// ─── Pantalla principal ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val uiState by vm.uiState.observeAsState(HomeUiState.Loading)
    val navegarAReporte by vm.navegarAReporte.observeAsState(false)
    val context = LocalContext.current

    // Navegar a ReporteActivity cuando el ViewModel lo indique
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
                    Text(
                        text = "BacheWatch",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.onReportarBacheClick() },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Reportar bache") }
            )
        }
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
            top = 12.dp, bottom = 96.dp   // espacio para el FAB
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Mis reportes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(reportes, key = { it.id }) { reporte ->
            ReporteCard(reporte)
        }
    }
}

// ─── Card de cada reporte ─────────────────────────────────────────────────────

@Composable
fun ReporteCard(reporte: BacheReport) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "MX")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Dirección + fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = reporte.direccion,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = dateFormat.format(reporte.fechaCreacion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            // Descripción
            Text(
                text = reporte.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(10.dp))

            // Chips: gravedad + status
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GravedadChip(reporte.gravedad)
                StatusChip(reporte.status)
            }
        }
    }
}

// ─── Chips ────────────────────────────────────────────────────────────────────

@Composable
fun GravedadChip(gravedad: Gravedad) {
    val (label, color) = when (gravedad) {
        Gravedad.LEVE     -> "Leve"     to MaterialTheme.colorScheme.tertiary
        Gravedad.MODERADO -> "Moderado" to MaterialTheme.colorScheme.secondary
        Gravedad.GRAVE    -> "Grave"    to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StatusChip(status: ReportStatus) {
    val (label, color) = when (status) {
        ReportStatus.PENDIENTE   -> "Pendiente"   to MaterialTheme.colorScheme.onSurfaceVariant
        ReportStatus.EN_REVISION -> "En revisión" to MaterialTheme.colorScheme.primary
        ReportStatus.RESUELTO    -> "Resuelto"    to MaterialTheme.colorScheme.tertiary
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

// ─── Estados alternativos ─────────────────────────────────────────────────────

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState() {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "Aún no tienes reportes.\nPresiona el botón para reportar tu primer bache.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun ErrorState(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onReintentar) {
            Text("Reintentar")
        }
    }
}