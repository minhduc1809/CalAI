package com.calai.app.presentation.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.calai.app.data.billing.PremiumOffer
import com.calai.app.data.remote.dto.PaymentOrderDto
import com.calai.app.data.billing.PurchaseUiEvent
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.viewmodel.PremiumViewModel

/** Bật lại khi app được đăng lên Google Play và đã cấu hình Play Billing (BR-16). Hiện chỉ dùng QR ngân hàng. */
private const val SHOW_GOOGLE_PLAY = false

private fun periodLabel(iso: String) = when (iso) {
    "P1M" -> "/ tháng"
    "P1Y" -> "/ năm"
    else -> ""
}

/**
 * BR-16.9: bảng Free / Premium, giá lấy từ Google Play, nút dùng thử, khôi phục giao dịch và liên kết
 * "Quản lý gói" mở thẳng trang subscription trên Play Store. Không có popup lúc mở app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val offers by viewModel.offers.collectAsState()
    val entitlement by viewModel.entitlement.collectAsState()
    val event by viewModel.events.collectAsState()
    val qrPlans by viewModel.qrPlans.collectAsState()
    val order by viewModel.order.collectAsState()
    val qrMessage by viewModel.qrMessage.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(event) {
        val message = when (val e = event) {
            PurchaseUiEvent.Verified -> "Đã kích hoạt Premium. Cảm ơn bạn!"
            PurchaseUiEvent.Pending -> "Đang chờ xác nhận thanh toán. Quyền Premium sẽ mở khi Google xác nhận."
            PurchaseUiEvent.Canceled -> null
            is PurchaseUiEvent.Error -> e.message
            null -> null
        }
        if (message != null) snackbar.showSnackbar(message)
        if (event != null) viewModel.consumeEvent()
    }

    LaunchedEffect(qrMessage) {
        qrMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeQrMessage()
        }
    }

    order?.let { QrDialog(it, onClose = { viewModel.closeQr() }) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Gói Premium", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val ent = entitlement
            if (ent?.isPremium == true) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Bạn đang dùng Premium", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        ent.expiryTime?.let {
                            Text(
                                (if (ent.autoRenewing) "Gia hạn tự động: " else "Hết hạn: ") + it.take(10),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            ent?.banner?.let {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        if (it == "GRACE_PERIOD") "Google Play chưa thu được tiền gia hạn. Hãy cập nhật phương thức thanh toán để giữ Premium."
                        else "Gói của bạn đang bị tạm dừng do chưa thanh toán được. Cập nhật phương thức thanh toán để dùng lại.",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.5.sp
                    )
                }
            }

            Text("So sánh gói", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Miễn phí vẫn đủ để theo dõi ăn uống, mục tiêu, cân nặng, tập luyện và dùng thử AI. Premium cho nhiều hơn, sâu hơn và lâu hơn.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ComparisonRow("Tính năng", "Miễn phí", "Premium", header = true)
                    ComparisonRow("Nhận diện món bằng ảnh", "5 lượt/ngày", "30 lượt/ngày")
                    ComparisonRow("Quét thực đơn quán ăn", "1 lần/ngày", "5 lần/ngày")
                    ComparisonRow("AI gợi ý món", "2 lượt/ngày", "Nhiều hơn")
                    ComparisonRow("Trò chuyện AI Coach", "10 tin/ngày", "50.000 token/ngày")
                    ComparisonRow("Lịch sử chat", "3 ngày", "7 ngày")
                    ComparisonRow("Kế hoạch dinh dưỡng", "7 ngày + xem trước 30 ngày", "Đầy đủ 30 ngày")
                    ComparisonRow("Check-in mục tiêu", "1 lần/tháng", "Hằng tuần")
                    ComparisonRow("Lịch sử Expenditure", "7 ngày", "Đầy đủ")
                    ComparisonRow("Insight", "1 nổi bật", "Đầy đủ")
                    ComparisonRow("Tổng kết tuần", "Xem một phần", "Đầy đủ bằng AI")
                    ComparisonRow("Tra mã vạch", "Giới hạn", "Nhiều hơn")
                    Text(
                        "Dữ liệu của bạn luôn xem, xuất và xoá được dù ở gói nào.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.5.sp
                    )
                }
            }

            if (SHOW_GOOGLE_PLAY && ent?.isPremium != true) {
                if (offers.isEmpty()) {
                    Text(
                        "Chưa tải được giá từ Google Play. Hãy kiểm tra kết nối rồi thử lại sau.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                offers.forEach { offer -> OfferCard(offer) { (context as? Activity)?.let { a -> viewModel.buy(a, offer) } } }
            }

            if (ent?.isPremium != true && qrPlans.isNotEmpty()) {
                Text("Thanh toán bằng QR ngân hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                qrPlans.forEach { plan ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("%,d đ cho %d ngày".format(plan.amount, plan.days).replace(',', '.'), fontSize = 15.sp)
                            AppButton(text = "Chuyển khoản bằng QR", onClick = { viewModel.startQr(plan.productId) })
                        }
                    }
                }
            }

            if (SHOW_GOOGLE_PLAY) {
                TextButton(onClick = { viewModel.restore() }) { Text("Khôi phục giao dịch") }
                TextButton(onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions"))
                    )
                }) { Text("Quản lý gói trên Google Play") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OfferCard(offer: PremiumOffer, onBuy: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (offer.productId == "premium_yearly") "Premium năm" else "Premium tháng",
                fontWeight = FontWeight.Bold, fontSize = 16.sp
            )
            Text("${offer.formattedPrice} ${periodLabel(offer.billingPeriod)}", fontSize = 15.sp)
            AppButton(
                text = if (offer.hasFreeTrial) "Dùng thử 7 ngày" else "Đăng ký",
                onClick = onBuy
            )
        }
    }
}


@Composable
private fun QrDialog(order: PaymentOrderDto, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Quét mã để thanh toán", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = order.qrUrl,
                    contentDescription = "Mã QR chuyển khoản",
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                )
                Text("Số tiền: %,d đ".format(order.amount).replace(',', '.'), fontWeight = FontWeight.Bold)
                Text("${order.bankName} · ${order.accountNo}")
                Text("Chủ tài khoản: ${order.accountName}")
                Text("Nội dung chuyển khoản: ${order.transferContent}", fontWeight = FontWeight.Bold)
                Text(
                    "Giữ nguyên nội dung chuyển khoản. Premium được mở tự động ngay khi hệ thống nhận được tiền; màn này sẽ tự cập nhật.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Đóng") } }
    )
}


@Composable
private fun ComparisonRow(feature: String, free: String, premium: String, header: Boolean = false) {
    val weight = if (header) FontWeight.Bold else FontWeight.Normal
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(feature, Modifier.weight(1.3f), fontSize = 12.5.sp, fontWeight = if (header) FontWeight.Bold else FontWeight.Medium)
        Text(free, Modifier.weight(1f), fontSize = 12.sp, fontWeight = weight, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(premium, Modifier.weight(1f), fontSize = 12.sp, fontWeight = weight)
    }
}
