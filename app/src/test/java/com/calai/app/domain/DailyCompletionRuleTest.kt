package com.calai.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DailyCompletionRuleTest {

    @Test fun `co bua va chua danh dau thi nhac`() {
        assertTrue(DailyCompletionRule.shouldRemind(1, "AUTO"))
        assertTrue(DailyCompletionRule.shouldRemind(4, null))
    }

    @Test fun `chua co bua nao thi khong nhac`() {
        assertFalse(DailyCompletionRule.shouldRemind(0, "AUTO"))
        assertFalse(DailyCompletionRule.shouldRemind(0, null))
    }

    @Test fun `da danh dau roi thi khong nhac`() {
        assertFalse(DailyCompletionRule.shouldRemind(3, "COMPLETE"))
        assertFalse(DailyCompletionRule.shouldRemind(3, "INCOMPLETE"))
    }

    @Test fun `doc gio hop le va chuoi sai ve mac dinh 21h`() {
        assertEquals(7 to 30, DailyCompletionRule.parseTime("07:30"))
        assertEquals(21 to 0, DailyCompletionRule.parseTime(null))
        assertEquals(21 to 0, DailyCompletionRule.parseTime("abc"))
        assertEquals(21 to 0, DailyCompletionRule.parseTime("25:00"))
        assertEquals(21 to 0, DailyCompletionRule.parseTime("12:75"))
        assertEquals("09:05", DailyCompletionRule.formatTime(9, 5))
    }

    private fun at(hour: Int, minute: Int) = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, 12, hour, minute, 0)
    }

    @Test fun `truoc 21h thi nhac trong hom nay`() {
        assertEquals(60L * 60 * 1000, DailyCompletionRule.delayUntilNext("21:00", at(20, 0)))
    }

    @Test fun `sau 21h thi nhac vao ngay mai`() {
        assertEquals(23L * 60 * 60 * 1000, DailyCompletionRule.delayUntilNext("21:00", at(22, 0)))
    }

    @Test fun `dung 21h thi tinh sang ngay mai de khong bao hai lan`() {
        assertEquals(24L * 60 * 60 * 1000, DailyCompletionRule.delayUntilNext("21:00", at(21, 0)))
    }
}
