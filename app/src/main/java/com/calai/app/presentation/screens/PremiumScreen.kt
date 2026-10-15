package com.calai.app.presentation.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.calai.app.presentation.theme.*
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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Gói Premium", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val ent = entitlement
            PremiumHero(isPremium = ent?.isPremium == true, expiry = ent?.expiryTime?.take(10), autoRenewing = ent?.autoRenewing == true)

            ent?.banner?.let {
                Text(
                    if (it == "GRACE_PERIOD") "Google Play chưa thu được tiền gia hạn. Hãy cập nhật phương thức thanh toán để giữ Premium."
                    else "Gói của bạn đang bị tạm dừng do chưa thanh toán được. Cập nhật phương thức thanh toán để dùng lại.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CoralWarning.copy(alpha = 0.16f))
                        .padding(14.dp)
                )
            }

            if (ent?.isPremium != true && qrPlans.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    qrPlans.forEach { plan -> PlanCard(plan.name, plan.amount, plan.days, plan.productId == "premium_yearly") { viewModel.startQr(plan.productId) } }
                    Text(
                        "Thanh toán bằng QR ngân hàng. Premium mở ngay khi nhận được tiền.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            ComparisonTable()

            if (SHOW_GOOGLE_PLAY && ent?.isPremium != true) {
                if (offers.isEmpty()) {
                    Text(
                        "Chưa tải được giá từ Google Play. Hãy kiểm tra kết nối rồi thử lại sau.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                offers.forEach { offer -> OfferCard(offer) { (context as? Activity)?.let { a -> viewModel.buy(a, offer) } } }
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

/** Mảng peach đầu trang: tên gói và trạng thái hiện tại, không có đoạn mô tả dài. */
@Composable
private fun PremiumHero(isPremium: Boolean, expiry: String?, autoRenewing: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                if (NwTheme.isDark) Brush.linearGradient(listOf(PeachDarkTint, Color(0xFF1B1210)))
                else Brush.linearGradient(listOf(PeachSoft, PeachMist))
            )
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(VividOrangeLight.copy(alpha = if (NwTheme.isDark) 0.16f else 0.30f), size.width * 0.34f, Offset(size.width * 0.95f, 0f))
            drawCircle(VividOrangeLight.copy(alpha = if (NwTheme.isDark) 0.10f else 0.18f), size.width * 0.18f, Offset(size.width * 1.0f, size.height * 0.6f))
        }
        Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(VividOrange),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.WorkspacePremium, null, tint = OnPrimary, modifier = Modifier.size(30.dp)) }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (isPremium) "Bạn đang dùng Premium" else "NutriWise Premium",
                    fontSize = 21.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    when {
                        isPremium && expiry != null -> (if (autoRenewing) "Gia hạn tự động: " else "Hết hạn: ") + expiry
                        isPremium -> "Đang hoạt động"
                        else -> "Nhiều lượt AI hơn, kế hoạch đầy đủ"
                    },
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlanCard(name: String, amount: Int, days: Int, recommended: Boolean, onChoose: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(if (recommended) 2.dp else 1.dp, if (recommended) VividOrange else MaterialTheme.colorScheme.outline, shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    if (recommended) {
                        Text(
                            "Tiết kiệm",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnPrimary,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(VividOrange).padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text("$days ngày", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "%,d đ".format(amount).replace(',', '.'),
                fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface
            )
        }
        AppButton(text = "Thanh toán bằng QR", onClick = onChoose, height = 48.dp, shape = RoundedCornerShape(14.dp))
    }
}

private data class FeatureRow(val feature: String, val free: String, val premium: String)

private val FEATURE_ROWS = listOf(
    FeatureRow("Nhận diện món bằng ảnh", "5 lượt/ngày", "30 lượt/ngày"),
    FeatureRow("Quét thực đơn quán ăn", "1 lần/ngày", "5 lần/ngày"),
    FeatureRow("AI gợi ý món", "2 lượt/ngày", "Nhiều hơn"),
    FeatureRow("Trò chuyện AI Coach", "10 tin/ngày", "50.000 token/ngày"),
    FeatureRow("Lịch sử chat", "3 ngày", "7 ngày"),
    FeatureRow("Kế hoạch dinh dưỡng", "7 ngày", "Đầy đủ 30 ngày"),
    FeatureRow("Check-in mục tiêu", "1 lần/tháng", "Hằng tuần"),
    FeatureRow("Lịch sử Expenditure", "7 ngày", "Đầy đủ"),
    FeatureRow("Insight", "1 nổi bật", "Đầy đủ"),
    FeatureRow("Tổng kết tuần", "Một phần", "Đầy đủ bằng AI"),
    FeatureRow("Tra mã vạch", "Giới hạn", "Nhiều hơn"),
)

/** Bảng so sánh: cột Premium nổi bằng nền peach liền mạch từ đầu đến cuối bảng. */
@Composable
private fun ComparisonTable() {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Text(
                "So sánh gói", Modifier.weight(1.25f).padding(16.dp),
                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Miễn phí", Modifier.weight(1f).padding(vertical = 16.dp, horizontal = 8.dp),
                fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(Modifier.weight(1f).fillMaxHeight().background(VividOrange), contentAlignment = Alignment.CenterStart) {
                Text("Premium", Modifier.padding(horizontal = 8.dp), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = OnPrimary)
            }
        }
        FEATURE_ROWS.forEachIndexed { i, row ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Text(
                    row.feature, Modifier.weight(1.25f).padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    row.free, Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 12.dp),
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(Modifier.weight(1f).fillMaxHeight().background(VividOrangeSoft)) {
                    Text(
                        row.premium, Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (i < FEATURE_ROWS.lastIndex) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }
        }
        Text(
            "Dữ liệu của bạn luôn xem, xuất và xoá được ở mọi gói.",
            Modifier.padding(16.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    com.calai.app.presentation.components.NwAlertDialog(
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
