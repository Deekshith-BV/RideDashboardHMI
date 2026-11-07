package com.example.ridedashboardhmi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val vehicleDataSource: VehicleDataSource = VehicleDataSource(),
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    // Own scope built from the injected dispatcher so the class is unit-testable
    // without requiring the Android Main dispatcher.
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState: StateFlow<VehicleUiState> = _uiState

    init {
        scope.launch {
            vehicleDataSource.getVehicleData()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        dataValid = false,
                        statusLabel = "Error: ${e.message}"
                    )
                }
                .collect { data ->
                    _uiState.value = sanitizeState(
                        VehicleUiState(
                            speedKmh = data.speedKmh,
                            motorOn = data.motorOn,
                            killSwitchActive = data.killSwitchActive,
                            statusLabel = data.statusLabel,
                            dataValid = true,
                            batteryPercent = data.batteryPercent,
                            rangeKm = data.rangeKm,
                            motorTempC = data.motorTempC,
                            powerPercent = data.powerPercent,
                            gear = data.gear,
                            rideMode = data.rideMode,
                            leftTurn = data.leftTurn,
                            rightTurn = data.rightTurn,
                            headlightOn = data.headlightOn,
                            highBeamOn = data.highBeamOn,
                            sideStandDown = data.sideStandDown,
                            // Alerts
                            rapidBrakeAlert = data.rapidBrakeAlert,
                            rapidBrakeBlink = data.rapidBrakeBlink,
                            harshAccel = data.harshAccel,
                            superHoldActive = data.superHoldActive,
                            overSpeed = data.overSpeed,
                            overSpeedLimit = data.overSpeedLimit,
                            cruiseActive = data.cruiseActive,
                            cruiseSpeedKmh = data.cruiseSpeedKmh,
                            // Thruster / regen
                            thrusterActive = data.thrusterActive,
                            regenLevel = data.regenLevel,
                            // Bottom indicators
                            serviceDue = data.serviceDue,
                            tractionControlOn = data.tractionControlOn,
                            tpmsAlert = data.tpmsAlert,
                            // Parking
                            parkingActive = data.parkingActive,
                            // Navigation
                            navigationActive = data.navigationActive,
                            navInstruction = data.navInstruction,
                            navDistanceM = data.navDistanceM,
                            navEta = data.navEta
                        )
                    )
                }
        }
    }

    /**
     * Sanitizes an incoming UI state:
     * - Negative speeds are clamped to 0.
     * - Data older than [STALE_THRESHOLD_MS] is marked invalid and labelled "Data stale".
     */
    fun sanitizeState(state: VehicleUiState): VehicleUiState {
        val safeSpeed = state.speedKmh.coerceAtLeast(0)
        val isStale = System.currentTimeMillis() - state.timestampMs > STALE_THRESHOLD_MS

        return if (isStale) {
            state.copy(
                speedKmh = safeSpeed,
                dataValid = false,
                statusLabel = "Data stale"
            )
        } else {
            state.copy(speedKmh = safeSpeed)
        }
    }

    companion object {
        private const val STALE_THRESHOLD_MS = 2_000L
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}
