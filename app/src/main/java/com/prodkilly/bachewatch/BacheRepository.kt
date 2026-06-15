package com.prodkilly.bachewatch

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BacheRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val reportesCollection = firestore.collection("reportes")

    /**
     * Sube una imagen a Firebase Storage dentro de la carpeta 'baches'
     * y retorna la URL pública de descarga.
     */
    suspend fun subirFoto(imageUri: Uri): Result<String> {
        return try {
            // Nombre único para el archivo usando UUID
            val nombreArchivo = "${UUID.randomUUID()}.jpg"
            val referencia = storage.reference.child("baches/$nombreArchivo")

            // Sube el archivo y espera a que termine
            referencia.putFile(imageUri).await()

            // Obtiene la URL de acceso pública
            val urlDescarga = referencia.downloadUrl.await().toString()
            Result.success(urlDescarga)
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