package com.calai.app.presentation.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.data.remote.dto.ExerciseGuideDto
import com.calai.app.presentation.theme.OnPrimary
import com.calai.app.presentation.theme.PrimaryStrong
import com.calai.app.presentation.theme.VividOrange

/**
 * Link video của bài tập: dùng link quản trị viên đã gắn (chỉ nhận https); chưa có thì mở trang TÌM KIẾM YouTube theo
 * tên bài. Tên bỏ phần trong ngoặc ("Chống đẩy quỳ gối (Knee Push-up)" → "Chống đẩy quỳ gối") để kết quả sát hơn.
 */
fun exerciseVideoUrl(ex: ExerciseGuideDto): String {
    ex.videoUrl?.trim()?.takeIf { it.startsWith("https://") }?.let { return it }
    val name = ex.name.substringBefore("(").trim().ifEmpty { ex.name }
    return "https://www.youtube.com/results?search_query=" + Uri.encode("cách tập $name đúng kỹ thuật")
}

fun hasCuratedVideo(ex: ExerciseGuideDto): Boolean = ex.videoUrl?.trim()?.startsWith("https://") == true

/** Tách đoạn "Thực hiện" thành từng bước (theo câu) để người mới dễ làm theo từng bước. */
private fun splitSteps(text: String): List<String> =
    text.split(Regex("(?<=[.!?])\\s+(?=[A-ZÀ-Ỹ0-9])")).map { it.trim() }.filter { it.isNotEmpty() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailSheet(exercise: ExerciseGuideDto, onDismiss: () -> Unit, onLog: () -> Unit) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(exercise.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Text("Nhóm cơ: ${exercise.targetMuscle}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Thông số buổi tập
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Stat("Số hiệp", "${exercise.sets}", Modifier.weight(1f))
                Stat("Mỗi hiệp", exercise.repsOrDuration, Modifier.weight(1.4f))
                Stat("Nghỉ", "${exercise.restSeconds}s", Modifier.weight(1f))
            }
            Text(
                "Mức ${levelLabel(exercise.level)} · ${equipmentLabel(exercise.equipment)} · ước tính ${exercise.caloriesBurnedEstimate.toInt()} kcal mỗi hiệp",
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Video
            val curated = hasCuratedVideo(exercise)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .clickableNoRipple {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(exerciseVideoUrl(exercise))))
                        } catch (_: Exception) {
                            Toast.makeText(context, "Không mở được video, hãy kiểm tra kết nối", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.PlayCircle, null, tint = PrimaryStrong, modifier = Modifier.size(34.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (curated) "Xem video hướng dẫn" else "Tìm video hướng dẫn trên YouTube",
                        fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (curated) "Mở bằng YouTube hoặc trình duyệt" else "Mở kết quả tìm kiếm theo tên bài",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Section("1. Chuẩn bị") { Body(exercise.instructions.preparation) }
            Section("2. Thực hiện") {
                val steps = splitSteps(exercise.instructions.execution)
                if (steps.size <= 1) Body(exercise.instructions.execution)
                else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.forEachIndexed { i, s ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                Modifier.size(22.dp).clip(CircleShape).background(VividOrange),
                                contentAlignment = Alignment.Center
                            ) { Text("${i + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnPrimary) }
                            Text(s, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Section("3. Lỗi sai cần tránh") { Body(exercise.instructions.commonMistakes) }
            Section("4. Cách hít thở") { Body(exercise.instructions.breathing) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(VividOrange)
                    .clickableNoRipple { onDismiss(); onLog() },
                contentAlignment = Alignment.Center
            ) { Text("Ghi buổi tập với bài này", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnPrimary) }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryStrong)
        content()
    }
}

@Composable
private fun Body(text: String) {
    Text(text, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
}

private fun levelLabel(level: String) = when (level) {
    "BEGINNER" -> "Dễ"
    "INTERMEDIATE" -> "Vừa"
    "ADVANCED" -> "Nâng cao"
    else -> level
}

private fun equipmentLabel(e: String) = when (e) {
    "NO_EQUIPMENT" -> "không dụng cụ"
    "DUMBBELL" -> "tạ đơn"
    "GYM_MACHINE" -> "máy tập"
    "RESISTANCE_BAND" -> "dây kháng lực"
    else -> e
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))
