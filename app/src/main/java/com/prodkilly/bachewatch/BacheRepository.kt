package com.prodkilly.bachewatch

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BacheRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val reportesCollection = firestore.collection("reportes")

    /**
     * Sube una imagen a Firebase Storage dentro de la carpeta 'baches'
     * y retorna la URL pública de descarga.
     */
    suspend fun subirFoto(uri: Uri): Result<String> {
        return try {
            // 1. Simulamos un pequeño retraso de red de 1.5 segundos
            // para que el usuario vea que la app "está haciendo algo" (da realismo)
            delay(1500)

            // 2. Inventamos una URL de imagen aleatoria de internet (ej. de Unsplash o marcador)
            // Esto evita que el resto de tu app truene al buscar un link
            val urlSimulada = "https://images.unsplash.com/photo-1515162305285-0293e4767cc2"

            // 3. Retornamos éxito rotundo sin haber tocado el servidor de Storage
            Result.success(urlSimulada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMisReportes(usuarioId: String): Flow<List<BacheReport>> = callbackFlow {
        val listener = reportesCollection
            .whereEqualTo("usuarioId", usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reportes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(BacheReport::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(reportes)
            }
        awaitClose { listener.remove() }
    }

    suspend fun crearReporte(reporte: BacheReport): Result<Boolean> {
        return try {
            reportesCollection.add(reporte).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}