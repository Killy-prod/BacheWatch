package com.prodkilly.bachewatch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val reportes: List<BacheReport>) : HomeUiState
    data class Error(val mensaje: String) : HomeUiState
    object Empty : HomeUiState
}

class HomeViewModel : ViewModel() {

    private val repository = BacheRepository()

    // Instancia de Firebase Auth
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableLiveData<HomeUiState>(HomeUiState.Loading)
    val uiState: LiveData<HomeUiState> = _uiState

    private val _navegarAReporte = MutableLiveData(false)
    val navegarAReporte: LiveData<Boolean> = _navegarAReporte

    private var cargarJob: Job? = null

    init {
        iniciarSesionAnonima()
    }

    /**
     * Revisa si el dispositivo ya tiene una identidad.
     * Si no, crea una nueva sesión anónima en Firebase.
     */
    private fun iniciarSesionAnonima() {
        _uiState.value = HomeUiState.Loading

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // El usuario ya existe en este dispositivo, cargamos sus datos
            cargarReportes(currentUser.uid)
        } else {
            // Es la primera vez que abre la app, registramos el dispositivo de forma anónima
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val nuevoUsuario = auth.currentUser
                        nuevoUsuario?.uid?.let { uid ->
                            cargarReportes(uid)
                        }
                    } else {
                        _uiState.value = HomeUiState.Error(
                            task.exception?.message ?: "Error al asignar identidad al dispositivo"
                        )
                    }
                }
        }
    }

    /**
     * Carga los reportes usando el UID único del dispositivo.
     */
    fun cargarReportes(usuarioId: String? = auth.currentUser?.uid) {
        if (usuarioId == null) {
            iniciarSesionAnonima()
            return
        }

        cargarJob?.cancel()

        cargarJob = viewModelScope.launch {
            try {
                // Ahora le pasamos el ID al repositorio para que filtre en Firestore
                repository.getMisReportes(usuarioId).collect { reportes ->
                    _uiState.value = if (reportes.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        HomeUiState.Success(reportes)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Error al cargar reportes")
            }
        }
    }

    fun onReportarBacheClick() {
        _navegarAReporte.value = true
    }

    fun onNavegacionCompletada() {
        _navegarAReporte.value = false
    }
}