package com.prodkilly.bachewatch

import java.util.Date

enum class ReportStatus { PENDIENTE, EN_REVISION, RESUELTO }

enum class Gravedad { LEVE, MODERADO, GRAVE }

// Agrega el campo usuarioId a tu data class BacheReport
data class BacheReport(
    val id: String = "",
    val usuarioId: String = "", // <--- Campo indispensable para Firestore
    val descripcion: String = "",
    val gravedad: Gravedad = Gravedad.LEVE,
    val status: ReportStatus = ReportStatus.PENDIENTE,
    val direccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fotoUrl: String? = null,
    val fechaCreacion: java.util.Date = java.util.Date()
)