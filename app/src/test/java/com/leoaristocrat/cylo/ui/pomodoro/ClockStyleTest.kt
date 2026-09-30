package com.leoaristocrat.cylo.ui.pomodoro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests verifying clock style definitions, parsing, aliases, and state compatibility.
 */
class ClockStyleTest {

    @Test
    fun `all six clock styles are present in enum`() {
        assertEquals(6, ClockStyle.entries.size)
        assertNotNull(ClockStyle.DIAL)
        assertNotNull(ClockStyle.FLIP)
        assertNotNull(ClockStyle.ARC)
        assertNotNull(ClockStyle.ORBITAL)
        assertNotNull(ClockStyle.SLOT_MACHINE)
        assertNotNull(ClockStyle.BREATH)
    }

    @Test
    fun `fromId correctly resolves all 6 styles by canonical id`() {
        assertEquals(ClockStyle.DIAL, ClockStyle.fromId("DIAL"))
        assertEquals(ClockStyle.FLIP, ClockStyle.fromId("FLIP"))
        assertEquals(ClockStyle.ARC, ClockStyle.fromId("ARC"))
        assertEquals(ClockStyle.ORBITAL, ClockStyle.fromId("ORBITAL"))
        assertEquals(ClockStyle.SLOT_MACHINE, ClockStyle.fromId("SLOT_MACHINE"))
        assertEquals(ClockStyle.BREATH, ClockStyle.fromId("BREATH"))
    }

    @Test
    fun `fromId is case-insensitive`() {
        assertEquals(ClockStyle.ARC, ClockStyle.fromId("arc"))
        assertEquals(ClockStyle.ORBITAL, ClockStyle.fromId("orbital"))
        assertEquals(ClockStyle.SLOT_MACHINE, ClockStyle.fromId("slot_machine"))
        assertEquals(ClockStyle.BREATH, ClockStyle.fromId("breath"))
        assertEquals(ClockStyle.FLIP, ClockStyle.fromId("flip"))
        assertEquals(ClockStyle.DIAL, ClockStyle.fromId("dial"))
    }

    @Test
    fun `fromId correctly handles legacy aliases and abbreviations`() {
        assertEquals(ClockStyle.FLIP, ClockStyle.fromId("FLIP_CARD"))
        assertEquals(ClockStyle.DIAL, ClockStyle.fromId("CONCENTRIC"))
        assertEquals(ClockStyle.SLOT_MACHINE, ClockStyle.fromId("SLOT"))
    }

    @Test
    fun `fromId safely falls back to DIAL for MORPH, unknown, or null keys`() {
        assertEquals(ClockStyle.DIAL, ClockStyle.fromId(null))
        assertEquals(ClockStyle.DIAL, ClockStyle.fromId(""))
        assertEquals(ClockStyle.DIAL, ClockStyle.fromId("MORPH"))
        assertEquals(ClockStyle.DIAL, ClockStyle.fromId("morph"))
        assertEquals(ClockStyle.DIAL, ClockStyle.fromId("NON_EXISTENT_STYLE"))
    }

    @Test
    fun `clock style labels are formatted cleanly for UI`() {
        assertEquals("Dial", ClockStyle.DIAL.label)
        assertEquals("Flip", ClockStyle.FLIP.label)
        assertEquals("Arc", ClockStyle.ARC.label)
        assertEquals("Orbital", ClockStyle.ORBITAL.label)
        assertEquals("Slot Machine", ClockStyle.SLOT_MACHINE.label)
        assertEquals("Breath", ClockStyle.BREATH.label)
    }

    @Test
    fun `clock style backward-compatible companion aliases exist`() {
        assertEquals(ClockStyle.FLIP, ClockStyle.FLIP_CARD)
        assertEquals(ClockStyle.DIAL, ClockStyle.CONCENTRIC)
    }

    @Test
    fun `time formatting correctly handles all mandatory test checkpoints`() {
        assertEquals("25:00", CyloClockTokens.formatTime(25 * 60))
        assertEquals("24:59", CyloClockTokens.formatTime(24 * 60 + 59))
        assertEquals("24:30", CyloClockTokens.formatTime(24 * 60 + 30))
        assertEquals("10:00", CyloClockTokens.formatTime(10 * 60))
        assertEquals("09:59", CyloClockTokens.formatTime(9 * 60 + 59))
        assertEquals("05:00", CyloClockTokens.formatTime(5 * 60))
        assertEquals("01:00", CyloClockTokens.formatTime(60))
        assertEquals("00:59", CyloClockTokens.formatTime(59))
        assertEquals("00:10", CyloClockTokens.formatTime(10))
        assertEquals("00:01", CyloClockTokens.formatTime(1))
        assertEquals("00:00", CyloClockTokens.formatTime(0))
        // Negative edge case safety
        assertEquals("00:00", CyloClockTokens.formatTime(-5))
    }

    @Test
    fun `reel digit decomposition matches exact test points for slot machine`() {
        data class ReelDigits(val m1: Int, val m2: Int, val s1: Int, val s2: Int)
        fun decompose(remaining: Int): ReelDigits {
            val safe = remaining.coerceAtLeast(0)
            val m = safe / 60
            val s = safe % 60
            return ReelDigits(m / 10, m % 10, s / 10, s % 10)
        }

        assertEquals(ReelDigits(2, 5, 0, 0), decompose(25 * 60))
        assertEquals(ReelDigits(2, 4, 5, 9), decompose(24 * 60 + 59))
        assertEquals(ReelDigits(2, 4, 3, 0), decompose(24 * 60 + 30))
        assertEquals(ReelDigits(1, 0, 0, 0), decompose(10 * 60))
        assertEquals(ReelDigits(0, 9, 5, 9), decompose(9 * 60 + 59))
        assertEquals(ReelDigits(0, 5, 0, 0), decompose(5 * 60))
        assertEquals(ReelDigits(0, 1, 0, 0), decompose(60))
        assertEquals(ReelDigits(0, 0, 5, 9), decompose(59))
        assertEquals(ReelDigits(0, 0, 1, 0), decompose(10))
        assertEquals(ReelDigits(0, 0, 0, 9), decompose(9))
        assertEquals(ReelDigits(0, 0, 0, 1), decompose(1))
        assertEquals(ReelDigits(0, 0, 0, 0), decompose(0))
    }

    @Test
    fun `accessibility descriptions convey remaining time and timer status clearly`() {
        val descRunning = CyloClockTokens.makeAccessibilityDescription(24 * 60 + 32, TimerStatus.RUNNING)
        assertEquals("24 minutes 32 seconds remaining, Timer running", descRunning)

        val descPaused = CyloClockTokens.makeAccessibilityDescription(10 * 60, TimerStatus.PAUSED)
        assertEquals("10 minutes 0 seconds remaining, Timer paused" , descPaused)

        val descCompleted = CyloClockTokens.makeAccessibilityDescription(0, TimerStatus.RUNNING)
        assertEquals("0 minutes 0 seconds remaining, Timer running", descCompleted)

        val descFinishedIdle = CyloClockTokens.makeAccessibilityDescription(0, TimerStatus.IDLE)
        assertEquals("0 minutes 0 seconds remaining, Timer completed", descFinishedIdle)
    }

    @Test
    fun `pomodoro ui state progress is mathematically sound`() {
        val fullState = PomodoroUiState(remainingSeconds = 1500, totalSeconds = 1500)
        assertEquals(0f, fullState.progress, 0.001f)

        val halfState = PomodoroUiState(remainingSeconds = 750, totalSeconds = 1500)
        assertEquals(0.5f, halfState.progress, 0.001f)

        val doneState = PomodoroUiState(remainingSeconds = 0, totalSeconds = 1500)
        assertEquals(1.0f, doneState.progress, 0.001f)
    }
}
