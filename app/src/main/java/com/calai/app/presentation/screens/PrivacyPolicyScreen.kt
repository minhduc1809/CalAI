package com.calai.app.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.calai.app.presentation.theme.OnPrimary
import com.calai.app.presentation.theme.PrimaryStrong
import com.calai.app.presentation.theme.VividOrange
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
                title = { Text("Chính sách quyền riêng tư", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại") }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = PrimaryStrong)
            }
            state.error != null -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(16.dp))
                Text("Không tải được chính sách", fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Hãy kiểm tra kết nối mạng và đảm bảo máy chủ đang chạy, rồi thử lại.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(VividOrange)
                        .clickable { viewModel.load() }
                        .padding(horizontal = 28.dp, vertical = 12.dp)
                ) { Text("Thử lại", fontWeight = FontWeight.Bold, color = OnPrimary) }
            }
            else -> state.policy?.let { p ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Đầu trang: tên, phiên bản, tóm tắt
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(p.title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Phiên bản ${p.version} · Hiệu lực từ ${p.lastUpdated}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (p.summary.isNotBlank()) Text(p.summary, fontSize = 13.5.sp, lineHeight = 20.sp)
                    }
                    p.sections.forEach { s ->
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(s.heading, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryStrong)
                            Text(plain(s.content), fontSize = 13.5.sp, lineHeight = 21.sp)
                        }
                    }
                }
            }
        }
    }
}
