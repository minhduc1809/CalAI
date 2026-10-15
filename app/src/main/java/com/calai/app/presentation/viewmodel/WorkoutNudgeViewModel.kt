package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Nhắc "Bạn vừa tập xong?": mở Trang chủ thì hỏi máy chủ một lần. Nếu người tập đều có dấu hiệu quên ghi buổi tập,
 * máy chủ tự đẩy một thông báo vào hộp thư (tối đa 1 lần/ngày, tôn trọng công tắc trong Cài đặt).
 */
@HiltViewModel
class WorkoutNudgeViewModel @Inject constructor(private val repository: CalAIRepository) : ViewModel() {
    init {
        viewModelScope.launch { repository.fetchWorkoutLogNudge() } // lỗi mạng: bỏ qua, đây chỉ là gợi ý
    }
}
