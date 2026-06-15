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
                var urlFinalFoto: String? = null

                // 1. Si el usuario seleccionó una foto, la subimos primero a Storage
                fotoUri?.let { uri ->
                    val uploadResult = repository.subirFoto(uri)
                    if (uploadResult.isSuccess) {
                        urlFinalFoto = uploadResult.getOrNull()
                    } else {
                        estaCargando = false
                        mensajeError = "Error al subir la imagen: ${uploadResult.exceptionOrNull()?.message}"
                        return@launch
                    }
                }

                // 2. Construimos el reporte con la ubicación real y la URL de la foto
                val nuevoReporte = BacheReport(
                    id = UUID.randomUUID().toString(),
                    usuarioId = userId,
                    descripcion = descripcion,
                    gravedad = gravedad,
                    direccion = direccion,
                    latitud = latitud,
                    longitud = longitud,
                    fotoUrl = urlFinalFoto
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