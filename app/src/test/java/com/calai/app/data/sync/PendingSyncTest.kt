package com.calai.app.data.sync

import com.calai.app.data.local.entity.PendingRequestEntity
import com.calai.app.data.remote.dto.CreateMealItemDto
import com.calai.app.data.remote.dto.CreateMealRequest
import com.google.gson.Gson
import java.io.IOException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class PendingSyncTest {
    private fun meal(date: String, kcal: Float, qty: Float = 1f, status: String = PendingRequestEntity.STATUS_PENDING): PendingRequestEntity {
        val req = CreateMealRequest(
            mealType = "LUNCH", date = date,
            items = listOf(CreateMealItemDto(name = "Cơm", servingSize = "1 chén", quantity = qty, calories = kcal, protein = 3f, carb = 30f, fat = 1f, source = "manual"))
        )
        return PendingRequestEntity(kind = PendingRequestEntity.KIND_MEAL, clientRequestId = "id$date$kcal", payload = Gson().toJson(req), status = status)
    }

    private fun http(code: Int) = HttpException(Response.error<Any>(code, "{}".toResponseBody()))

    @Test fun sumsOnlyPendingMealsOfThatDay() {
        val items = listOf(meal("2026-10-13", 200f, 1.5f), meal("2026-10-13", 100f), meal("2026-10-12", 999f),
            meal("2026-10-13", 500f, status = PendingRequestEntity.STATUS_FAILED))
        val t = pendingMealTotalsFor(items, "2026-10-13")
        assertEquals(400f, t.calories, 0.01f)
        assertEquals(7.5f, t.protein, 0.01f)
    }

    @Test fun ignoresWorkoutsAndBrokenPayload() {
        val workout = PendingRequestEntity(kind = PendingRequestEntity.KIND_WORKOUT, clientRequestId = "w", payload = "{}")
        val broken = PendingRequestEntity(kind = PendingRequestEntity.KIND_MEAL, clientRequestId = "b", payload = "not json")
        assertEquals(0f, pendingMealTotalsFor(listOf(workout, broken), "2026-10-13").calories, 0f)
    }

    @Test fun classifiesFailures() {
        assertTrue(classifyFailure(IOException("offline")) is SendOutcome.Retry)
        assertTrue(classifyFailure(http(503)) is SendOutcome.Retry)
        assertTrue(classifyFailure(http(429)) is SendOutcome.Retry)
        assertTrue(classifyFailure(http(400)) is SendOutcome.Rejected)
        assertTrue(classifyFailure(http(409)) is SendOutcome.Rejected)
    }
}
