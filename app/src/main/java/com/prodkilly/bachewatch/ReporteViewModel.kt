package com.prodkilly.bachewatch

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class ReporteViewModel : ViewModel() {
    private val repository = BacheRepository()

    // Estados del formulario
    var descripcion by mutableStateOf("")
    var gravedad by mutableStateOf(Gravedad.LEVE)

    // Estado de Almacenamiento (Foto seleccionada localmente)
    var fotoUri by mutableStateOf<Uri?>(null)

    // Estados de UI
    var estaCargando by mutableStateOf(false)
    var reporteGuardado by mutableStateOf(false)
    var mensajeError by mutableStateOf<String?>(null)

    fun guardarReporte(latitud: Double, longitud: Double, direccion: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        estaCargando = true
        mensajeError = null

        viewModelScope.launch {
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val userId = auth.currentUser?.uid ?: "anonimo"

                // ─── SOLO DEBE HABER UNA LÍNEA CON ESTE NOMBRE ───
                val urlFinalFoto = fotoUri?.toString() ?: "sin_foto"

                // Construcción del reporte
                val nuevoReporte = BacheReport(
                    id = java.util.UUID.randomUUID().toString(),
                    usuarioId = userId,
                    descripcion = descripcion,
                    gravedad = gravedad,
                    direccion = direccion,
                    latitud = latitud,
                    longitud = longitud,
                    fotoUrl = urlFinalFoto, // Pasamos la variable correcta
                    fechaCreacion = java.util.Date(),
                    status = ReportStatus.PENDIENTE
                )


                // 3. Guardamos el reporte final en Firestore
                val resultado = repository.crearReporte(nuevoReporte)
                estaCargando = false

                if (resultado.isSuccess) {
                    reporteGuardado = true
                } else {
                    mensajeError = resultado.exceptionOrNull()?.message ?: "Error al guardar el reporte"
                }
            } catch (e: Exception) {
                estaCargando = false
                mensajeError = e.localizedMessage ?: "Ocurrió un error inesperado"
            }
        }
    }
}