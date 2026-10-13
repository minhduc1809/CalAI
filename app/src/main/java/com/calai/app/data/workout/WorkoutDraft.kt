package com.calai.app.data.workout

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Một set trong phiên tập đang soạn. `hint` là gợi ý "Lần trước: 60 kg × 8" lấy từ buổi gần nhất của bài. */
data class DraftSet(
    val id: String = UUID.randomUUID().toString(),
    val reps: Int = 10,
    val weightKg: Float = 0f,
    val rpe: Float? = null,
    val isWarmup: Boolean = false,
    val isCompleted: Boolean = false,
    val hint: String? = null
)

data class DraftExercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    /** CATALOG | CUSTOM; null khi chỉ có tên. */
    val source: String? = null,
    val refId: String? = null,
    val primaryMuscle: String? = null,
    val restSec: Int = DEFAULT_REST_SEC,
    /** Tạ cao nhất (không khởi động) của lần tập trước, để cảnh báo mềm khi tạ nhập gấp hơn 2 lần. */
    val lastMaxKg: Float? = null,
    val sets: List<DraftSet> = emptyList()
) {
    companion object {
        const val DEFAULT_REST_SEC = 90
    }
}

/**
 * Phiên tập đang soạn (BR-10.4). Lưu bền trên máy: thoát app giữa buổi thì mở lại vẫn tiếp tục được, không mất set.
 * `clientRequestId` được tạo MỘT lần lúc bắt đầu và dùng cho mọi lần lưu/gửi lại, nên dù gửi nhiều lần máy chủ vẫn chỉ
 * ghi một buổi.
 */
data class WorkoutDraft(
    val clientRequestId: String = UUID.randomUUID().toString(),
    val startedAtMs: Long = System.currentTimeMillis(),
    val category: String = "STRENGTH",
    val name: String = "",
    val durationMinutes: Int = 45,
    /** true khi người dùng tự chỉnh thời lượng; chưa chỉnh thì thời lượng lấy theo đồng hồ từ lúc bắt đầu. */
    val durationManual: Boolean = false,
    val rpe: Int = 8,
    val note: String = "",
    /** Calo do người dùng tự nhập; null thì máy chủ tính theo MET (BR-10.5). */
    val caloriesOverride: Float? = null,
    val exercises: List<DraftExercise> = emptyList(),
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    /** Phiên có nội dung đáng giữ: ít nhất một bài tập có set đã làm, hoặc đã nhập tên/ghi chú. */
    val hasContent: Boolean
        get() = exercises.any { ex -> ex.sets.any { it.isCompleted } } || name.isNotBlank() || note.isNotBlank()
}

/** Phiên dở quá số giờ này thì hỏi lưu hay huỷ (workout.abandonedSessionHours). */
const val ABANDONED_SESSION_HOURS = 12

fun WorkoutDraft.isAbandoned(nowMs: Long = System.currentTimeMillis()): Boolean =
    nowMs - updatedAtMs > ABANDONED_SESSION_HOURS * 3_600_000L

/** Kho phiên tập đang soạn dùng chung cho mọi màn hình (Hub, Ghi tập) và lưu bền qua lần mở app sau. */
@Singleton
class WorkoutDraftStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("workout_draft", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _draft = MutableStateFlow(load())
    val draft: StateFlow<WorkoutDraft?> = _draft.asStateFlow()

    private fun load(): WorkoutDraft? {
        val json = prefs.getString(KEY, null) ?: return null
        return try {
            gson.fromJson(json, WorkoutDraft::class.java)
        } catch (_: Exception) {
            // Bản nháp hỏng thì bỏ, không để app lỗi khi mở lại
            prefs.edit().remove(KEY).apply()
            null
        }
    }

    private fun persist(value: WorkoutDraft?) {
        if (value == null) prefs.edit().remove(KEY).apply()
        else prefs.edit().putString(KEY, gson.toJson(value)).apply()
    }

    /** Bắt đầu phiên mới (thay thế phiên cũ nếu có). */
    fun start(draft: WorkoutDraft) {
        _draft.value = draft
        persist(draft)
    }

    /** Sửa phiên hiện tại; không có phiên thì bỏ qua. */
    fun update(transform: (WorkoutDraft) -> WorkoutDraft) {
        val current = _draft.value ?: return
        val next = transform(current).copy(updatedAtMs = System.currentTimeMillis())
        _draft.value = next
        persist(next)
    }

    fun clear() {
        _draft.value = null
        persist(null)
    }

    private companion object {
        const val KEY = "draft_json"
    }
}
