package com.calai.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddMealScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Thêm Bữa Ăn", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { /* Chụp ảnh */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Chụp ảnh món ăn (AI)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { /* Nhập tay */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Nhập thủ công")
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onBack) {
            Text("Quay lại")
        }
    }
}
