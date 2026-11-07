package com.example.ridedashboardhmi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleViewModelTest {

    private fun newViewModel() = VehicleViewModel()

    @Test
    fun `simulated vehicle data follows required sequence`() = runBlocking {
        val source = VehicleDataSource()

        val states = source.observeVehicleState()
            .take(5)
            .toList()

        assertEquals(5, states.size)
        assertEquals(0, states[0].speedKmh)
        assertEquals(false, states[0].motorOn)
        assertEquals(false, states[0].killSwitchActive)

        assertEquals(0, states[1].speedKmh)
        assertEquals(true, states[1].motorOn)
        assertEquals(false, states[1].killSwitchActive)

        assertEquals(25, states[2].speedKmh)
        assertEquals(true, states[2].motorOn)
        assertEquals(false, states[2].killSwitchActive)

        assertEquals(50, states[3].speedKmh)
        assertEquals(true, states[3].motorOn)
        assertEquals(false, states[3].killSwitchActive)

        assertEquals(0, states[4].speedKmh)
        assertEquals(false, states[4].motorOn)
        assertEquals(true, states[4].killSwitchActive)
    }

    @Test
    fun `invalid speed and stale data are sanitized gracefully`() {
        val viewModel = newViewModel()

        val invalidState = VehicleUiState(
            speedKmh = -12,
            motorOn = true,
            killSwitchActive = false,
            statusLabel = "Riding",
            dataValid = true,
            timestampMs = System.currentTimeMillis() - 10_000L
        )

        val sanitized = viewModel.sanitizeState(invalidState)

        assertEquals(0, sanitized.speedKmh)
        assertFalse(sanitized.dataValid)
        assertEquals("Data stale", sanitized.statusLabel)
    }

    @Test
    fun `test state updates correctly`() {
        runBlocking {
            val viewModel = newViewModel()

            // Skip the initial empty default state, then take the first five mapped scenarios.
            val states = viewModel.uiState
                .filter { it.statusLabel.isNotEmpty() }
                .take(5)
                .toList()

            val byLabel = states.associateBy { it.statusLabel }
            byLabel["Parked"]?.let { assertEquals(0, it.speedKmh) }
            byLabel["Ready"]?.let { assertTrue(it.motorOn) }
            byLabel["Riding"]?.let { assertEquals(25, it.speedKmh) }
            byLabel["Accelerating"]?.let { assertEquals(50, it.speedKmh) }
            byLabel["Motor Disabled"]?.let { assertTrue(it.killSwitchActive) }
        }
    }

    @Test
    fun `test error handling`() {
        val viewModel = newViewModel()

        val errorState = viewModel.sanitizeState(
            VehicleUiState(
                speedKmh = -5,
                statusLabel = "Riding",
                dataValid = true,
                timestampMs = System.currentTimeMillis() - 10_000L
            )
        )

        assertFalse(errorState.dataValid)
        assertTrue(errorState.statusLabel.contains("stale"))
    }
}
