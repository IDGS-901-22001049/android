package com.example.aguainteligente.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aguainteligente.data.model.WaterConsumption
import com.example.aguainteligente.data.repository.AuthRepository
import com.example.aguainteligente.data.repository.FirestoreRepository
import com.example.aguainteligente.data.service.MqttManager
import com.example.aguainteligente.data.service.NotificationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

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

    private var mqttManager: MqttManager? = null
    private var notificationService: NotificationService? = null

    private val _currentFlowLiters = MutableStateFlow(0f)
    val currentFlowLiters: StateFlow<Float> = _currentFlowLiters
    private val _leakAlert = MutableStateFlow(false)
    val leakAlert: StateFlow<Boolean> = _leakAlert
    private val _valveState = MutableStateFlow(false)
    val valveState: StateFlow<Boolean> = _valveState

    private var savingJob: Job? = null

    fun initialize(context: Context) {
        if (mqttManager == null) {
            val userId = authRepository.getCurrentUser()?.uid
            notificationService = NotificationService(context)
            mqttManager = MqttManager(userId).apply {
                connect()
                this.currentFlowLiters.onEach { litersInInterval ->
                    _currentFlowLiters.value = litersInInterval
                    if (litersInInterval > 0) {
                        savingJob?.cancel()
                        savingJob = viewModelScope.launch {
                            delay(6000)
                            addWaterConsumption(litersInInterval)
                        }
                    }
                }.launchIn(viewModelScope)

                this.leakAlert.onEach { hasLeak ->
                    _leakAlert.value = hasLeak
                    if (hasLeak) {
                        notificationService?.showLeakAlertNotification()
                    }
                }.launchIn(viewModelScope)
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

    private fun addWaterConsumption(liters: Float) {
        authRepository.getCurrentUser()?.uid?.let { userId ->
            viewModelScope.launch {
                _historyError.value = null
                try {
                    val newConsumption = WaterConsumption(userId = userId, liters = liters, timestamp = System.currentTimeMillis())
                    firestoreRepository.addWaterConsumption(newConsumption)
                    fetchConsumptionData()
                } catch (e: Exception) {
                    _historyError.value = "Error al agregar consumo: ${e.localizedMessage}"
                }
            }
        }
    }

    fun setValveState(isOn: Boolean) {
        _valveState.value = isOn
        mqttManager?.publishValveState(if (isOn) "ON" else "OFF")
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