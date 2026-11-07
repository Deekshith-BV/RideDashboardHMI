package com.example.ridedashboardhmi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridedashboardhmi.ui.theme.RideDashboardHMITheme
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val ScreenBg = Color(0xFF05080D)
private val Bezel = Color(0xFF2A2F37)
private val PanelBg = Color(0xFF0E141C)
private val Accent = Color(0xFF2DE2A6)
private val AccentDim = Color(0xFF0F3C32)
private val Warn = Color(0xFFFFA726)
private val Danger = Color(0xFFFF5252)
private val TextPrimary = Color(0xFFF2F5F8)
private val TextSecondary = Color(0xFF9AA4AF)
private val Inactive = Color(0xFF3A434E)

private const val MAX_SPEED = 120f

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RideDashboardHMITheme {
                val viewModel = remember { VehicleViewModel() }
                val uiState by viewModel.uiState.collectAsState()
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    DashboardScreen(uiState = uiState)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(uiState: VehicleUiState) {
    var settingsOpen by remember { mutableStateOf(uiState.settingsOpen) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(10.dp)
            .clip(RoundedCornerShape(26.dp))
            .border(3.dp, Bezel, RoundedCornerShape(26.dp))
            .background(ScreenBg)
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopStatusBar(uiState, onSettingsClick = { settingsOpen = true })
            Spacer(Modifier.height(6.dp))
            ConnectivityBar(uiState)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeftInfoColumn(uiState, Modifier.weight(1f))
                CenterSpeedometer(uiState, Modifier.weight(1.4f))
                RightStatusColumn(uiState, Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            IndicatorRow(uiState)
            Spacer(Modifier.height(6.dp))
            ParkingSlider(uiState)
            Spacer(Modifier.height(6.dp))
            BottomStatusBar(uiState)
        }

        if (settingsOpen) {
            SettingsMenu(onDismiss = { settingsOpen = false })
        }
    }
}

@Composable
private fun TopStatusBar(s: VehicleUiState, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(s.clock, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(s.dateLabel, color = TextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.width(14.dp))
            SignalStrength(s.signalType, s.signalBars)
            Spacer(Modifier.width(14.dp))
            Text(
                text = if (s.motorOn) "READY" else "OFF",
                color = if (s.motorOn) Accent else TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            GlyphIcon("\u25C0", active = s.leftTurn, activeColor = Accent)
            Spacer(Modifier.width(18.dp))
            GearIndicator(s.gear)
            Spacer(Modifier.width(10.dp))
            GlyphIcon("\u2600", active = s.headlightOn, activeColor = Color(0xFF4FC3F7))
            Spacer(Modifier.width(18.dp))
            GlyphIcon("\u25B6", active = s.rightTurn, activeColor = Accent)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${s.motorTempC}\u00B0C", color = TextSecondary, fontSize = 15.sp)
            Spacer(Modifier.width(14.dp))
            BatteryGauge(s.batteryPercent)
            Spacer(Modifier.width(14.dp))
            GlyphIcon(
                "\u2699",
                active = true,
                activeColor = TextPrimary,
                modifier = Modifier.clickable { onSettingsClick() }
            )
        }
    }
}

@Composable
private fun SignalStrength(type: String, bars: Int) {
    Row(verticalAlignment = Alignment.Bottom) {
        for (i in 1..4) {
            Box(
                modifier = Modifier
                    .padding(end = 2.dp)
                    .width(4.dp)
                    .height((5 + i * 3).dp)
                    .background(
                        if (i <= bars) Accent else Inactive,
                        RoundedCornerShape(1.dp)
                    )
            )
        }
        Spacer(Modifier.width(5.dp))
        Text(type, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GlyphIcon(symbol: String, active: Boolean, activeColor: Color, modifier: Modifier = Modifier) {
    Text(
        text = symbol,
        color = if (active) activeColor else Inactive,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun GearIndicator(gear: String) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AccentDim),
        contentAlignment = Alignment.Center
    ) {
        Text(gear, color = Accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BatteryGauge(percent: Int) {
    val color = when {
        percent <= 15 -> Danger
        percent <= 35 -> Warn
        else -> Accent
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(2.dp, TextSecondary, RoundedCornerShape(4.dp))
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent / 100f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 1.dp)
                .width(3.dp)
                .height(8.dp)
                .background(TextSecondary, RoundedCornerShape(1.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text("$percent%", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LeftInfoColumn(s: VehicleUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        AlertChip("RAPID BRAKE", "\u26A0", active = s.rapidBrakeAlert, color = Danger)
        Spacer(Modifier.height(8.dp))
        AlertChip("SUPER HOLD", "\u270B", active = s.superHoldActive, color = Accent)
        Spacer(Modifier.height(8.dp))
        AlertChip(
            if (s.overSpeed) "SPEED EXCEEDED" else "SPEED OK",
            "${s.overSpeedLimit}",
            active = s.overSpeed,
            color = Warn
        )
        Spacer(Modifier.height(8.dp))
        AlertChip("CRUISE ${s.cruiseSpeedKmh}", "\u27F3", active = s.cruiseActive, color = Color(0xFF4FC3F7))
        Spacer(Modifier.height(12.dp))
        InfoTile("RANGE", "${s.rangeKm}", "km")
        Spacer(Modifier.height(10.dp))
        InfoTile("ODO", "${s.odometerKm}", "km")
        Spacer(Modifier.height(10.dp))
        InfoTile("TRIP", String.format(Locale.US, "%.1f", s.tripKm), "km")
    }
}

@Composable
private fun AlertChip(label: String, glyph: String, active: Boolean, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) color.copy(alpha = 0.16f) else PanelBg)
            .border(
                1.dp,
                if (active) color else Inactive,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(glyph, color = if (active) color else Inactive, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            color = if (active) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InfoTile(label: String, value: String, unit: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(14.dp))
            .background(PanelBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(label, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(5.dp))
                Text(unit, color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CenterSpeedometer(s: VehicleUiState, modifier: Modifier = Modifier) {
    val speedText = if (s.dataValid) s.speedKmh.toString() else "--"
    val progress = (s.speedKmh.coerceIn(0, MAX_SPEED.toInt())) / MAX_SPEED

    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val stroke = 18f
            val diameter = minOf(size.width, size.height) - stroke
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)
            val startAngle = 135f
            val sweep = 270f

            drawArc(
                color = Inactive,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Accent, Color(0xFF4FC3F7), Accent)
                ),
                startAngle = startAngle,
                sweepAngle = sweep * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rOuter = diameter / 2f + stroke / 2f + 6f
            val ticks = 12
            for (i in 0..ticks) {
                val angle = startAngle + sweep * i / ticks
                val rad = Math.toRadians(angle.toDouble())
                val len = if (i % 3 == 0) 20f else 11f
                val sx = cx + rOuter * cos(rad).toFloat()
                val sy = cy + rOuter * sin(rad).toFloat()
                val ex = cx + (rOuter + len) * cos(rad).toFloat()
                val ey = cy + (rOuter + len) * sin(rad).toFloat()
                drawLine(
                    color = TextSecondary,
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = if (i % 3 == 0) 4f else 2f
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = s.statusLabel.ifEmpty { "\u2014" },
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = speedText,
                color = if (s.dataValid) TextPrimary else Warn,
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 64.sp,
                textAlign = TextAlign.Center
            )
            Text("km / h", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            PowerBar(s.powerPercent)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RegenIndicator(s.regenLevel)
                Spacer(Modifier.width(16.dp))
                ThrusterIndicator(s.thrusterActive)
            }
        }
    }
}

@Composable
private fun RegenIndicator(level: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("REGEN", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(6.dp))
        for (i in 1..3) {
            Box(
                modifier = Modifier
                    .padding(end = 3.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (i <= level) Accent else Inactive)
            )
        }
    }
}

@Composable
private fun ThrusterIndicator(active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "\u25B6\u25B6",
            color = if (active) Warn else Inactive,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(5.dp))
        Text(
            "THRUST",
            color = if (active) TextPrimary else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PowerBar(power: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("POWER", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Inactive)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(power.coerceIn(0, 100) / 100f)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Accent, Warn)))
            )
        }
    }
}

@Composable
private fun RightStatusColumn(s: VehicleUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        StatusDot(
            label = if (s.motorOn) "MOTOR ON" else "MOTOR OFF",
            active = s.motorOn,
            color = Accent
        )
        Spacer(Modifier.height(16.dp))
        StatusDot(
            label = if (s.killSwitchActive) "KILL ACTIVE" else "KILL INACTIVE",
            active = s.killSwitchActive,
            color = Danger
        )
        Spacer(Modifier.height(16.dp))
        StatusDot(
            label = s.rideMode,
            active = true,
            color = if (s.rideMode == "SPORT") Warn else Accent
        )
        Spacer(Modifier.height(12.dp))
        NavigationCard(s)
    }
}

@Composable
private fun NavigationCard(s: VehicleUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .clip(RoundedCornerShape(14.dp))
            .background(PanelBg)
            .border(
                1.dp,
                if (s.navigationActive) Accent else Inactive,
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "\u2197",
                color = if (s.navigationActive) Accent else Inactive,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Text("NAV", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        if (s.navigationActive) {
            Spacer(Modifier.height(6.dp))
            Text(s.navInstruction, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("${s.navDistanceM} m  \u2022  ${s.navEta}", color = TextSecondary, fontSize = 12.sp)
        } else {
            Spacer(Modifier.height(6.dp))
            Text("No route", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun StatusDot(label: String, active: Boolean, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .clip(RoundedCornerShape(14.dp))
            .background(PanelBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(if (active) color else Inactive, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            color = if (active) TextPrimary else TextSecondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BottomStatusBar(s: VehicleUiState) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Bezel)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (s.dataValid) "DATA OK" else "DATA INVALID",
                color = if (s.dataValid) Accent else Warn,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text("MODE  ${s.rideMode}", color = TextSecondary, fontSize = 14.sp)
            Text(
                text = s.statusLabel.ifEmpty { "\u2014" },
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("TRIP ${String.format(Locale.US, "%.1f", s.tripKm)} km", color = TextSecondary, fontSize = 14.sp)
            Text("ODO ${s.odometerKm} km", color = TextPrimary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ConnectivityBar(s: VehicleUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConnChip(
            "\uD83D\uDCF1",
            if (s.mobilePaired) "${s.pairedDeviceName}  ${s.pairedDeviceBattery}%" else "No device",
            active = s.mobilePaired,
            color = Accent
        )
        ConnChip("\uD83D\uDD76", "INCOGNITO", active = s.incognito, color = Color(0xFF9C7BFF))
        ConnChip("\uD83D\uDD15", "DND", active = s.dnd, color = Warn)
        ConnChip("\u2709", "${s.mobileNotifications}", active = s.mobileNotifications > 0, color = Color(0xFF4FC3F7))
        ConnChip("\u2699", "${s.systemNotifications}", active = s.systemNotifications > 0, color = TextSecondary)
        if (s.callActive) ConnChip("\uD83D\uDCDE", s.callDuration, active = true, color = Accent)
        ConnChip("\uD83D\uDCCD", "LIVE", active = s.liveLocationSharing, color = Danger)
        ConnChip("\u2B55", "GEOFENCE", active = s.activeGeofence, color = Color(0xFF4FC3F7))
    }
}

@Composable
private fun ConnChip(glyph: String, label: String, active: Boolean, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) color.copy(alpha = 0.16f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(glyph, color = if (active) color else Inactive, fontSize = 14.sp)
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            color = if (active) TextPrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun IndicatorRow(s: VehicleUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IndicatorPip("\u25C0", "LEFT", s.leftTurn, Accent)             // 30 Left indicator
        IndicatorPip("\uD83D\uDCA1", "HIGH BEAM", s.highBeamOn, Color(0xFF4FC3F7)) // 28 High beam
        IndicatorPip("\u26A0", "SIDE STAND", s.sideStandDown, Danger)  // 29 Side stand
        IndicatorPip("\uD83D\uDD27", "SERVICE", s.serviceDue, Warn)    // 31 Service due
        IndicatorPip("\u2707", "TRACTION", s.tractionControlOn, Accent)// 32 Traction control
        IndicatorPip("\u26A0", "TPMS", s.tpmsAlert, Danger)           // 34 TPMS
        IndicatorPip("\u25B6", "RIGHT", s.rightTurn, Accent)           // 33 Right indicator
    }
}

@Composable
private fun IndicatorPip(glyph: String, label: String, active: Boolean, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(glyph, color = if (active) color else Inactive, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            label,
            color = if (active) TextPrimary else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ParkingSlider(s: VehicleUiState) {
    val color = if (s.parkingActive) Accent else Inactive
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(PanelBg)
            .border(1.dp, color, RoundedCornerShape(50))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text("P", color = ScreenBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            if (s.parkingActive) "PARKING ENGAGED \u2014 slide to release" else "SLIDE TO PARK",
            color = if (s.parkingActive) TextPrimary else TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        Text("\u25B6", color = color, fontSize = 16.sp)
    }
}

@Composable
private fun SettingsMenu(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .clip(RoundedCornerShape(20.dp))
                .background(PanelBg)
                .border(1.dp, Accent, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SETTINGS", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "\u2715",
                    color = TextSecondary,
                    fontSize = 20.sp,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }
            Spacer(Modifier.height(16.dp))
            listOf(
                "Display Brightness",
                "Ride Modes",
                "Bluetooth & Devices",
                "Navigation",
                "Units & Language",
                "Software Update"
            ).forEach { item ->
                Text(
                    item,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Bezel)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun DashboardPreview() {
    RideDashboardHMITheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            DashboardScreen(
                uiState = VehicleUiState(
                    speedKmh = 50,
                    motorOn = true,
                    killSwitchActive = false,
                    statusLabel = "Accelerating",
                    dataValid = true,
                    gear = "D",
                    rideMode = "SPORT",
                    rightTurn = true,
                    headlightOn = true,
                    powerPercent = 80,
                    batteryPercent = 82,
                    rangeKm = 64,
                    motorTempC = 55
                )
            )
        }
    }
}

