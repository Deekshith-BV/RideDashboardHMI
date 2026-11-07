package com.example.ridedashboardhmi

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class VehicleDataSource {

    fun getVehicleData(): Flow<VehicleData> = flow {
        val states = listOf(
            // ---- Mandatory assignment sequence (first 5, exact spec values) ----
            // 1) Parked: 0 km/h | Motor OFF | Kill switch Inactive
            VehicleData(
                speedKmh = 0, motorOn = false, killSwitchActive = false, statusLabel = "Parked",
                batteryPercent = 86, rangeKm = 72, motorTempC = 28,
                gear = "P", rideMode = "ECO", headlightOn = false, powerPercent = 0,
                parkingActive = true, regenLevel = 0,
                superHoldActive = true, sideStandDown = true,
                tractionControlOn = true, serviceDue = true, navigationActive = false
            ),
            // 2) Ready: 0 km/h | Motor ON | Kill switch Inactive
            VehicleData(
                speedKmh = 0, motorOn = true, killSwitchActive = false, statusLabel = "Ready",
                batteryPercent = 86, rangeKm = 72, motorTempC = 30,
                gear = "N", rideMode = "ECO", headlightOn = true, powerPercent = 0,
                superHoldActive = true, leftTurn = true,
                tractionControlOn = true, serviceDue = true, navigationActive = true,
                navInstruction = "Head north", navDistanceM = 400, navEta = "11 min"
            ),
            // 3) Riding: 25 km/h | Motor ON | Kill switch Inactive
            VehicleData(
                speedKmh = 25, motorOn = true, killSwitchActive = false, statusLabel = "Riding",
                batteryPercent = 84, rangeKm = 69, motorTempC = 42,
                gear = "D", rideMode = "ECO", headlightOn = true,
                rightTurn = true, powerPercent = 35,
                regenLevel = 1, tractionControlOn = true, navigationActive = true,
                navInstruction = "Turn right", navDistanceM = 220, navEta = "9 min"
            ),
            // 4) Accelerating: 50 km/h | Motor ON | Kill switch Inactive
            VehicleData(
                speedKmh = 50, motorOn = true, killSwitchActive = false, statusLabel = "Accelerating",
                batteryPercent = 82, rangeKm = 64, motorTempC = 55,
                gear = "D", rideMode = "SPORT", headlightOn = true, powerPercent = 80,
                cruiseActive = true, cruiseSpeedKmh = 50, harshAccel = true,
                highBeamOn = true, tractionControlOn = true, navigationActive = true,
                navInstruction = "Continue straight", navDistanceM = 1200, navEta = "7 min"
            ),
            // 5) Motor disabled: 0 km/h | Motor OFF | Kill switch Active
            VehicleData(
                speedKmh = 0, motorOn = false, killSwitchActive = true, statusLabel = "Motor Disabled",
                batteryPercent = 80, rangeKm = 59, motorTempC = 48,
                gear = "N", rideMode = "ECO", headlightOn = false, powerPercent = 0,
                parkingActive = true, superHoldActive = true, sideStandDown = true,
                tractionControlOn = true, serviceDue = true, navigationActive = false
            ),

            // ---- Extra demo states to exercise the full HMI indicator set ----
            // 6) Top speed / thruster + over-speed + TPMS
            VehicleData(
                speedKmh = 98, motorOn = true, killSwitchActive = false, statusLabel = "Thruster",
                batteryPercent = 80, rangeKm = 58, motorTempC = 63,
                gear = "D", rideMode = "SPORT", headlightOn = true, powerPercent = 100,
                overSpeed = true, overSpeedLimit = 80, thrusterActive = true, harshAccel = true,
                highBeamOn = true, tractionControlOn = false, tpmsAlert = true,
                navigationActive = true,
                navInstruction = "Exit in 2 km", navDistanceM = 2000, navEta = "5 min"
            ),
            // 7) Braking hard / regen + rapid-brake alert
            VehicleData(
                speedKmh = 40, motorOn = true, killSwitchActive = false, statusLabel = "Braking",
                batteryPercent = 80, rangeKm = 59, motorTempC = 58,
                gear = "D", rideMode = "SPORT", headlightOn = true, powerPercent = 10,
                regenLevel = 3, rapidBrakeAlert = true, rapidBrakeBlink = true,
                leftTurn = true, tractionControlOn = true, navigationActive = true,
                navInstruction = "Turn left", navDistanceM = 150, navEta = "4 min"
            )
        )

        while (true) {
            for (state in states) {
                emit(state)
                delay(1000)
            }
        }
    }

    /** Alias used by tests/observers to stream the simulated vehicle state. */
    fun observeVehicleState(): Flow<VehicleData> = getVehicleData()
}

data class VehicleData(
    // Core
    val speedKmh: Int,
    val motorOn: Boolean,
    val killSwitchActive: Boolean,
    val statusLabel: String,

    // Battery / range / power
    val batteryPercent: Int = 86,
    val rangeKm: Int = 72,
    val motorTempC: Int = 34,
    val powerPercent: Int = 0,

    // Ride mode / gear
    val gear: String = "N",
    val rideMode: String = "ECO",

    // Lights & indicators
    val leftTurn: Boolean = false,
    val rightTurn: Boolean = false,
    val headlightOn: Boolean = false,
    val highBeamOn: Boolean = false,
    val sideStandDown: Boolean = false,

    // Alerts
    val rapidBrakeAlert: Boolean = false,
    val rapidBrakeBlink: Boolean = false,
    val harshAccel: Boolean = false,
    val superHoldActive: Boolean = false,
    val overSpeed: Boolean = false,
    val overSpeedLimit: Int = 80,
    val cruiseActive: Boolean = false,
    val cruiseSpeedKmh: Int = 72,

    // Thruster / regen
    val thrusterActive: Boolean = false,
    val regenLevel: Int = 0,

    // Bottom indicators
    val serviceDue: Boolean = false,
    val tractionControlOn: Boolean = true,
    val tpmsAlert: Boolean = false,

    // Parking
    val parkingActive: Boolean = false,

    // Navigation
    val navigationActive: Boolean = false,
    val navInstruction: String = "Turn right",
    val navDistanceM: Int = 120,
    val navEta: String = "8 min"
)
