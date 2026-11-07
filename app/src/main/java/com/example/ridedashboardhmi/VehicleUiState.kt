package com.example.ridedashboardhmi

/**
 * Full HMI state model. Field numbers in comments map to the 36 reference
 * components.
 */
data class VehicleUiState(
    // Core / speedometer
    val speedKmh: Int = 0,                 // 20 Speedometer value
    val motorOn: Boolean = false,          // 36 Motor Status
    val killSwitchActive: Boolean = false,
    val statusLabel: String = "",
    val dataValid: Boolean = true,
    val timestampMs: Long = System.currentTimeMillis(),

    // Battery / range / power
    val batteryPercent: Int = 100,         // 35 Battery pill with Battery SOCs
    val altBatteryPercent: Int = 92,       // 11 Alternate battery SOC visual bar
    val rangeKm: Int = 62,                 // 19 Range as per SOC and mode
    val motorTempC: Int = 34,
    val powerPercent: Int = 0,

    // Ride mode
    val gear: String = "D",
    val rideMode: String = "SONIC",        // 5 Ride mode badge, 18 Ride mode pill
    val rideModeColor: Long = 0xFFFF6A2B,

    // Indicators / lights
    val leftTurn: Boolean = false,         // 30 Left indicator
    val rightTurn: Boolean = false,        // 33 Right indicator
    val headlightOn: Boolean = false,
    val highBeamOn: Boolean = false,       // 28 High beam
    val sideStandDown: Boolean = false,    // 29 Side Stand

    // Trip data
    val odometerKm: Int = 1324,            // 22 Odometer
    val tripKm: Double = 12.4,             // trip meter in slider
    val avgSpeedKmh: Int = 42,             // avg. speed in slider

    // Top status bar
    val clock: String = "10:22 PM",        // 12 Date and Time
    val dateLabel: String = "Fri 24 Oct",  // 12 Date and Time
    val signalType: String = "LTE",        // 13 Signal Strength
    val signalBars: Int = 3,               // 13 Signal Strength (0..4)
    val mobilePaired: Boolean = true,      // 2 Mobile Paired Status
    val pairedDeviceName: String = "Pixel 9 Pro",
    val pairedDeviceBattery: Int = 18,
    val incognito: Boolean = true,         // 3 Incognito
    val dnd: Boolean = true,               // 4 DND
    val mobileNotifications: Int = 4,      // 6 Mobile notifications
    val systemNotifications: Int = 2,      // 7 System notifications
    val callActive: Boolean = true,        // 8 Active Call pill
    val callDuration: String = "01:48",    // 8 Active Call pill
    val liveLocationSharing: Boolean = true, // 9 Live Location sharing
    val activeGeofence: Boolean = true,    // 10 Active Geofence

    // Parking
    val parkingActive: Boolean = true,     // 1 Parking Slider

    // Right alerts
    val rapidBrakeAlert: Boolean = true,   // 14 Rapid Brake Alert
    val superHoldActive: Boolean = true,   // 15 SuperHold
    val overSpeed: Boolean = true,         // 16 OverSpeed / Speed
    val overSpeedLimit: Int = 80,          // 16 OverSpeed value
    val cruiseActive: Boolean = true,      // 17 Cruise control
    val cruiseSpeedKmh: Int = 72,          // 17 Cruise control value
    val harshAccel: Boolean = false,       // 21 harsh acceleration blinker
    val rapidBrakeBlink: Boolean = false,  // 21 rapid brake blinker

    // Thruster / regen
    val thrusterActive: Boolean = false,   // 24 Thruster visualisation (>80 km/h)
    val regenLevel: Int = 2,               // 25 Regen indication (0..3)

    // Bottom indicators
    val serviceDue: Boolean = true,        // 31 Service Due
    val tractionControlOn: Boolean = true, // 32 Traction Control
    val tpmsAlert: Boolean = false,        // 34 TPMS

    // Navigation
    val navigationActive: Boolean = true,  // 27 Maps navigation
    val navInstruction: String = "Turn right",   // 27 Maps navigation hint
    val navDistanceM: Int = 120,                 // 27 Maps navigation distance
    val navEta: String = "8 min",                // 27 Maps navigation ETA

    // Settings
    val settingsOpen: Boolean = false            // 26 Settings menu
)
