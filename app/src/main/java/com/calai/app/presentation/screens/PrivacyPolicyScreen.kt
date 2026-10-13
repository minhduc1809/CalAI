package com.calai.app.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.PrivacyPolicyDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrivacyPolicyUiState(
    val loading: Boolean = true,
    val policy: PrivacyPolicyDto? = null,
    val error: String? = null
)

@HiltViewModel
class PrivacyPolicyViewModel @Inject constructor(private val repository: CalAIRepository) : ViewModel() {
    private val _state = MutableStateFlow(PrivacyPolicyUiState())
    val state: StateFlow<PrivacyPolicyUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = PrivacyPolicyUiState(loading = true)
        viewModelScope.launch {
            repository.getPrivacyPolicy()
                .onSuccess { _state.value = PrivacyPolicyUiState(loading = false, policy = it) }
                .onFailure { _state.value = PrivacyPolicyUiState(loading = false, error = "Không tải được chính sách. Kiểm tra kết nối rồi thử lại.") }
        }
    }
}

/** Bỏ ký hiệu markdown đơn giản (đậm, bảng) để đọc được trong Text thường. */
private fun plain(md: String): String =
    md.lines()
        .filterNot { it.trim().matches(Regex("^\\|?[\\s:|-]+\\|?$")) && it.contains("---") }
        .joinToString("\n") { line ->
            var l = line.replace("**", "").replace("`", "")
            if (l.trim().startsWith("|")) l = l.trim().trim('|').split("|").joinToString(": ") { it.trim() }
            if (l.trim() == "---") "" else l
        }
        .trim()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit, viewModel: PrivacyPolicyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Quyền riêng tư", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                state.loading -> Text("Đang tải…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.error != null -> {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::load) { Text("Thử lại") }
                }
                else -> state.policy?.let { p ->
                    Text(p.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Phiên bản ${p.version} · Hiệu lực từ ${p.lastUpdated}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    p.sections.forEach { s ->
                        Text(s.heading, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(plain(s.content), fontSize = 13.5.sp, lineHeight = 20.sp)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
