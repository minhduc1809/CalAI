package com.calai.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServingGramsTest {
    @Test fun parsesGramsInParensAndLeading() {
        assertEquals(130f, ServingGrams.parse("1 chén (130g)")!!, 0.01f)
        assertEquals(250f, ServingGrams.parse("1 ly (250ml)")!!, 0.01f)
        assertEquals(150f, ServingGrams.parse("150g")!!, 0.01f)
        assertEquals(500f, ServingGrams.parse("1 tô (500 g)")!!, 0.01f)
    }

    @Test fun doesNotGuessWhenUnknown() {
        assertNull(ServingGrams.parse("1 phần"))
        assertNull(ServingGrams.parse(null))
        assertNull(ServingGrams.parse(""))
    }

    @Test fun convertsBetweenGramsAndPortions() {
        assertEquals(1.538f, ServingGrams.portionsForGrams(130f, 200f)!!, 0.001f)
        assertEquals(0.1f, ServingGrams.portionsForGrams(130f, 1f)!!, 0.001f)
        assertEquals(50f, ServingGrams.portionsForGrams(100f, 99999f)!!, 0.001f)
        assertNull(ServingGrams.portionsForGrams(null, 100f))
        assertEquals(195f, ServingGrams.gramsForPortions(130f, 1.5f)!!, 0.01f)
    }
}
