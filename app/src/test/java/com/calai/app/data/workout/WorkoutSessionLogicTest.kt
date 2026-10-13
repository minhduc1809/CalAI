package com.calai.app.data.workout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutSessionLogicTest {
    private fun draft(vararg sets: DraftSet) = WorkoutDraft(
        name = "Ngực", exercises = listOf(DraftExercise(name = "Đẩy ngực", sets = sets.toList()))
    )

    @Test fun netCaloriesSubtractsResting() {
        assertEquals(350f, WorkoutSessionLogic.netCalories(6f, 60, 70f), 0.1f)
        assertEquals(0f, WorkoutSessionLogic.netCalories(0.5f, 60, 70f), 0.01f)
    }

    @Test fun exerciseKeyMatchesBackend() {
        assertEquals("CATALOG:abc", WorkoutSessionLogic.exerciseKey("CATALOG", "abc", "x"))
        assertEquals("name:day nguc", WorkoutSessionLogic.exerciseKey(null, null, "Đẩy  ngực"))
    }

    @Test fun requestOnlyContainsCompletedSetsAndRenumbers() {
        val d = draft(DraftSet(reps = 8, weightKg = 60f), DraftSet(reps = 8, weightKg = 60f, isCompleted = true))
        val r = WorkoutSessionLogic.toRequest(d)
        assertEquals(1, r.exercises!!.size)
        assertEquals(1, r.exercises!![0].sets.size)
        assertEquals(1, r.exercises!![0].sets[0].setNumber)
        assertNull(r.caloriesBurned)
        assertEquals(d.clientRequestId, r.clientRequestId)
    }

    @Test fun validateRequiresCompletedWorkingSet() {
        assertNotNull(WorkoutSessionLogic.validate(draft(DraftSet(isCompleted = true, isWarmup = true))))
        assertNull(WorkoutSessionLogic.validate(draft(DraftSet(isCompleted = true))))
    }

    @Test fun weightJumpWarning() {
        assertTrue(WorkoutSessionLogic.isWeightJump(130f, 60f))
        assertFalse(WorkoutSessionLogic.isWeightJump(100f, 60f))
        assertFalse(WorkoutSessionLogic.isWeightJump(100f, null))
    }

    @Test fun volumeIgnoresWarmupAndUndone() {
        val d = draft(
            DraftSet(reps = 10, weightKg = 50f, isCompleted = true),
            DraftSet(reps = 10, weightKg = 20f, isCompleted = true, isWarmup = true),
            DraftSet(reps = 10, weightKg = 50f)
        )
        assertEquals(500f, WorkoutSessionLogic.volumeKg(d), 0.01f)
    }
}
