package com.example.aguainteligente.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aguainteligente.data.repository.AuthRepository
import com.example.aguainteligente.data.repository.FirestoreRepository
import com.example.aguainteligente.data.service.MqttManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private var mqttManager: MqttManager? = null

    // --- ESTADOS DE FIRESTORE ---
    private val _dailyConsumption = MutableStateFlow(0f)
    val dailyConsumption: StateFlow<Float> = _dailyConsumption
    private val _weeklyConsumption = MutableStateFlow(0f)
    val weeklyConsumption: StateFlow<Float> = _weeklyConsumption
    private val _monthlyConsumption = MutableStateFlow(0f)
    val monthlyConsumption: StateFlow<Float> = _monthlyConsumption
    private val _previousMonthConsumption = MutableStateFlow(0f)
    val previousMonthConsumption: StateFlow<Float> = _previousMonthConsumption
    private val _monthlyHistory = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val monthlyHistory: StateFlow<List<Pair<String, Float>>> = _monthlyHistory
    private val _peakConsumptionData = MutableStateFlow<Map<String, Float>>(emptyMap())
    val peakConsumptionData: StateFlow<Map<String, Float>> = _peakConsumptionData
    private val _isLoadingHistory = MutableStateFlow(true)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory
    private val _historyError = MutableStateFlow<String?>(null)
    val historyError: StateFlow<String?> = _historyError

    // --- ESTADOS DE MQTT Y CONTROL ---
    val currentFlowRate: StateFlow<Float> get() = mqttManager?.currentFlowRate ?: MutableStateFlow(0f)
    private val _valveState = MutableStateFlow(false)
    val valveState: StateFlow<Boolean> = _valveState
    private val _dispenseAmount = MutableStateFlow(0f)
    val dispenseAmount: StateFlow<Float> = _dispenseAmount
    private val _dispensedProgress = MutableStateFlow(0f)
    val dispensedProgress: StateFlow<Float> = _dispensedProgress
    private var dispenseJob: Job? = null

    fun initialize(context: Context) {
        if (mqttManager == null) {
            mqttManager = MqttManager(authRepository.getCurrentUser()?.uid).apply {
                connect()
            }
        }
    }

    fun fetchConsumptionData() {
        authRepository.getCurrentUser()?.uid?.let { userId ->
            viewModelScope.launch {
                _isLoadingHistory.value = true
                _historyError.value = null
                try {
                    _dailyConsumption.value = firestoreRepository.getDailyWaterConsumption(userId)
                    _weeklyConsumption.value = firestoreRepository.getWeeklyWaterConsumption(userId)
                    _monthlyConsumption.value = firestoreRepository.getMonthlyWaterConsumption(userId)
                    _previousMonthConsumption.value = firestoreRepository.getPreviousMonthWaterConsumption(userId)
                    _monthlyHistory.value = firestoreRepository.getMonthlyWaterHistory(userId, 6)
                    _peakConsumptionData.value = firestoreRepository.getPeakConsumptionData(userId)
                } catch (e: Exception) {
                    _historyError.value = "Error al cargar datos: ${e.localizedMessage}"
                } finally {
                    _isLoadingHistory.value = false
                }
            }
        }
    }

    fun toggleValve() {
        mqttManager?.sendValvePulse()
        _valveState.value = !_valveState.value
    }

    fun startDispensing(liters: Float) {
        if (liters <= 0f) return

        dispenseJob?.cancel()
        _dispenseAmount.value = liters
        _dispensedProgress.value = 0f

        dispenseJob = viewModelScope.launch {
            if (!_valveState.value) {
                toggleValve()
            }

            var accumulatedLiters = 0f

            currentFlowRate.collect { flowRateLitersPerMinute ->
                val litersPerSecond = flowRateLitersPerMinute / 60f
                accumulatedLiters += litersPerSecond
                _dispensedProgress.value = accumulatedLiters

                if (accumulatedLiters >= liters) {
                    if (_valveState.value) {
                        toggleValve()
                    }
                    _dispenseAmount.value = 0f
                    this.coroutineContext[Job]?.cancel()
                }
            }
        }
    }

    fun cancelDispensing() {
        dispenseJob?.cancel()
        if (_valveState.value) {
            toggleValve()
        }
        _dispenseAmount.value = 0f
        _dispensedProgress.value = 0f
    }

    fun logout() {
        authRepository.logout()
        mqttManager?.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        mqttManager?.disconnect()
    }
}

class DashboardViewModelFactory(
    private val firestoreRepo: FirestoreRepository,
    private val authRepo: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(firestoreRepo, authRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}