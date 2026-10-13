package com.calai.app.data.workout

import com.calai.app.data.remote.dto.CreateWorkoutExerciseRequest
import com.calai.app.data.remote.dto.CreateWorkoutLogRequest
import com.calai.app.data.remote.dto.CreateWorkoutSetRequest
import com.calai.app.data.remote.dto.LastSetDto
import com.calai.app.data.remote.dto.TemplateExerciseDto
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Giới hạn dữ liệu buổi tập, khớp với backend (BR-10.4). */
object WorkoutLimits {
    const val REPS_MAX = 100
    const val WEIGHT_MAX_KG = 500f
    const val DURATION_MAX_MIN = 600
    const val REST_SEC_MAX = 900
    /** Tạ gấp hơn mức này so với lần trước cùng bài thì cảnh báo mềm. */
    const val WEIGHT_JUMP_FACTOR = 2f
}

/**
 * Logic thuần của một phiên tập: tính calo, khoá bài, dựng yêu cầu gửi máy chủ, kiểm tra trước khi lưu. Tách khỏi
 * ViewModel để kiểm thử bằng unit test.
 */
object WorkoutSessionLogic {

    /** Calo tập NET = (MET − 1) × cân nặng × giờ (BR-10.5). MET gộp đã gồm năng lượng lúc nghỉ nên phải trừ 1. */
    fun netCalories(met: Float, durationMinutes: Int, weightKg: Float): Float {
        val cal = max(0f, met - 1f) * weightKg * (durationMinutes / 60f)
        return (cal * 10).roundToInt() / 10f
    }

    /** Chuẩn hoá tên giống backend (`normalizeName`): bỏ dấu, chữ thường, ký tự lạ thành khoảng trắng. */
    fun normalize(input: String): String =
        Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .replace('đ', 'd').replace('Đ', 'd')
            .lowercase(Locale.ROOT)
            .replace("[^a-z0-9]+".toRegex(), " ")
            .trim()

    /** Khoá bài tập: theo mã khi có (CATALOG/CUSTOM), ngược lại theo tên không dấu. Khớp backend. */
    fun exerciseKey(source: String?, refId: String?, name: String): String =
        if (!source.isNullOrBlank() && !refId.isNullOrBlank()) "$source:$refId" else "name:${normalize(name)}"

    fun exerciseKey(ex: DraftExercise): String = exerciseKey(ex.source, ex.refId, ex.name)

    private fun fmt(v: Float): String =
        if (v == v.toInt().toFloat()) v.toInt().toString() else String.format(Locale.US, "%.1f", v)

    /** "Lần trước: 60 kg × 8" (tạ cơ thể: "Lần trước: 12 lần"). */
    fun hint(set: LastSetDto): String =
        if (set.weightKg > 0f) "Lần trước: ${fmt(set.weightKg)} kg × ${set.reps}" else "Lần trước: ${set.reps} lần"

    /**
     * Điền sẵn các set của bài từ lần tập gần nhất (số lần và tạ của set tương ứng), BR-10.4. Chưa có lịch sử thì
     * một set trống. Set điền sẵn chưa đánh dấu là đã làm.
     */
    fun prefillSets(last: List<LastSetDto>): List<DraftSet> =
        if (last.isEmpty()) listOf(DraftSet(reps = 10, weightKg = 0f))
        else last.sortedBy { it.setNumber }.map {
            DraftSet(reps = it.reps.coerceIn(1, WorkoutLimits.REPS_MAX), weightKg = it.weightKg, isWarmup = it.isWarmup, hint = hint(it))
        }

    fun lastMaxKg(last: List<LastSetDto>): Float? =
        last.filter { !it.isWarmup }.maxOfOrNull { it.weightKg }?.takeIf { it > 0f }

    /** Cảnh báo mềm khi tạ nhập gấp hơn 2 lần lần trước (hay nhầm kg và lb); không chặn việc lưu. */
    fun isWeightJump(weightKg: Float, lastMaxKg: Float?): Boolean =
        lastMaxKg != null && lastMaxKg > 0f && weightKg > lastMaxKg * WorkoutLimits.WEIGHT_JUMP_FACTOR

    /** RPE theo bước 0,5 trong khoảng 1–10. */
    fun clampRpe(value: Float): Float = (((value.coerceIn(1f, 10f)) * 2).roundToInt() / 2f)

    fun clampReps(value: Int): Int = value.coerceIn(1, WorkoutLimits.REPS_MAX)
    fun clampWeight(value: Float): Float = value.coerceIn(0f, WorkoutLimits.WEIGHT_MAX_KG)

    /** Tổng volume kg của các set đã làm, không tính khởi động. */
    fun volumeKg(draft: WorkoutDraft): Float =
        draft.exercises.sumOf { ex ->
            ex.sets.filter { it.isCompleted && !it.isWarmup }.sumOf { (it.weightKg * it.reps).toDouble() }
        }.toFloat()

    fun completedWorkingSets(draft: WorkoutDraft): Int =
        draft.exercises.sumOf { ex -> ex.sets.count { it.isCompleted && !it.isWarmup } }

    /** Thời lượng theo đồng hồ từ lúc bắt đầu, làm tròn lên phút, trong 1–600. */
    fun elapsedMinutes(draft: WorkoutDraft, nowMs: Long): Int =
        (((nowMs - draft.startedAtMs) / 60_000f).let { kotlin.math.ceil(it).toInt() }).coerceIn(1, WorkoutLimits.DURATION_MAX_MIN)

    fun effectiveDuration(draft: WorkoutDraft, nowMs: Long): Int =
        if (draft.durationManual || draft.category != "STRENGTH") draft.durationMinutes.coerceIn(1, WorkoutLimits.DURATION_MAX_MIN)
        else max(draft.durationMinutes.coerceIn(1, WorkoutLimits.DURATION_MAX_MIN), elapsedMinutes(draft, nowMs))

    /** Lý do không lưu được (hiển thị cho người dùng), hoặc null nếu hợp lệ. */
    fun validate(draft: WorkoutDraft): String? {
        if (draft.name.isBlank()) return "Vui lòng nhập tên buổi tập"
        if (draft.category == "STRENGTH") {
            if (draft.exercises.isEmpty()) return "Buổi tập tạ cần ít nhất một bài tập"
            if (completedWorkingSets(draft) == 0) return "Hãy đánh dấu ít nhất một set đã tập (không tính khởi động)"
        }
        return null
    }

    private fun isoUtc(ms: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(ms))

    fun todayKey(nowMs: Long = System.currentTimeMillis()): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(nowMs))

    /**
     * Dựng yêu cầu gửi máy chủ từ phiên tập: chỉ gửi set đã làm, bỏ bài không có set nào, đánh số lại set, giới hạn giá
     * trị đúng khoảng của backend. Calo chỉ gửi khi người dùng tự nhập; còn lại máy chủ tính theo MET.
     */
    fun toRequest(draft: WorkoutDraft, nowMs: Long = System.currentTimeMillis()): CreateWorkoutLogRequest {
        val exercises = if (draft.category == "STRENGTH") {
            draft.exercises.mapNotNull { ex ->
                val done = ex.sets.filter { it.isCompleted }
                if (done.isEmpty()) null else ex to done
            }.mapIndexed { idx, (ex, done) ->
                CreateWorkoutExerciseRequest(
                    name = ex.name.trim(),
                    order = idx + 1,
                    exerciseSource = ex.source,
                    exerciseId = ex.refId,
                    primaryMuscle = ex.primaryMuscle,
                    restSec = ex.restSec.coerceIn(0, WorkoutLimits.REST_SEC_MAX),
                    sets = done.mapIndexed { i, s ->
                        CreateWorkoutSetRequest(
                            setNumber = i + 1,
                            reps = clampReps(s.reps),
                            weightKg = clampWeight(s.weightKg),
                            rpe = s.rpe?.let { clampRpe(it) },
                            isWarmup = if (s.isWarmup) true else null
                        )
                    }
                )
            }
        } else null
        return CreateWorkoutLogRequest(
            name = draft.name.trim(),
            category = draft.category,
            date = todayKey(draft.startedAtMs),
            durationMinutes = effectiveDuration(draft, nowMs),
            caloriesBurned = draft.caloriesOverride,
            rpe = draft.rpe.coerceIn(1, 10),
            note = draft.note.ifBlank { null },
            clientRequestId = draft.clientRequestId,
            startedAt = isoUtc(draft.startedAtMs),
            endedAt = isoUtc(max(nowMs, draft.startedAtMs)),
            exercises = exercises
        )
    }

    /** Bài trong buổi mẫu/buổi gần nhất thành bài trong phiên mới, số liệu điền sẵn. */
    fun exerciseFromTemplate(t: TemplateExerciseDto): DraftExercise {
        val sets = prefillSets(t.sets)
        return DraftExercise(
            name = t.name,
            source = t.exerciseSource,
            refId = t.exerciseId,
            primaryMuscle = t.primaryMuscle,
            restSec = (t.restSec ?: DraftExercise.DEFAULT_REST_SEC).coerceIn(0, WorkoutLimits.REST_SEC_MAX),
            sets = sets
        )
    }

    /** Hệ số nhân tối thiểu giữa hai số để tránh chia cho 0 trong thống kê hiển thị. */
    fun safeRatio(value: Float, max: Float): Float = if (max <= 0f) 0f else min(1f, value / max)
}
