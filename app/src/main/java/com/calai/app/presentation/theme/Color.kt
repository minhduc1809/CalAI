package com.calai.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// CALAI DARK LUXURY CANVAS (THEME CHÍNH) - SPEC 9.3
// ==========================================

// 1. Nền & Bề mặt — 5 lớp độ sâu (Spec 10.5 - Tinh chỉnh chống bệt đen)
val ObsidianBackground = Color(0xFF0D0E12)       // Nền gốc toàn app
val CharcoalSurface = Color(0xFF1B1E26)          // Surface — nền section (thoáng hơn #171920)
val CharcoalCard = Color(0xFF242833)             // Nền card chuẩn (tách lớp rõ ràng)
val CharcoalCardElevated = Color(0xFF2D3240)     // Card nổi cao hơn (hero card, overlay, toggle track)
val CharcoalBorder = Color(0xFF383D4D)           // Viền 1px sắc nét tách bạch
val CharcoalDock = Color(0xFF242833)             // Floating bottom dock (đồng bộ layer)

// 2. Bento Pastel — Gradient 2 tông Đã Hiệu Chỉnh Hài Hòa (Spec 10.4)
// Độ sáng 68-80%, Độ bão hòa 45-60%, êm dịu như bộ macaron cao cấp
val CalorieCardGradientStart = Color(0xFFF2A585)
val CalorieCardGradientEnd = Color(0xFFF7C4AC)

val ProteinGradientStart = Color(0xFF8FE0BE)     // Bạc hà dịu
val ProteinGradientEnd = Color(0xFFB9EFD8)

val CarbGradientStart = Color(0xFFF4D486)        // Vàng mật ong nhạt
val CarbGradientEnd = Color(0xFFFAE7B8)

val FatGradientStart = Color(0xFFF3B8CE)         // Hồng phấn dịu
val FatGradientEnd = Color(0xFFF9D6E3)

val LavenderGradientStart = Color(0xFFC9BBF5)    // Tím nhạt dịu cho selection
val LavenderGradientEnd = Color(0xFFE2D9FA)

// Brush helpers cho Bento Macro
val ProteinBrush = Brush.verticalGradient(listOf(ProteinGradientStart, ProteinGradientEnd))
val CarbBrush = Brush.verticalGradient(listOf(CarbGradientStart, CarbGradientEnd))
val FatBrush = Brush.horizontalGradient(listOf(FatGradientStart, FatGradientEnd))
val LavenderBrush = Brush.verticalGradient(listOf(LavenderGradientStart, LavenderGradientEnd))
val CalorieCardBrush = Brush.verticalGradient(listOf(CalorieCardGradientStart, CalorieCardGradientEnd))

// Màu đơn tương thích (Legacy support)
val PastelLavender = LavenderGradientStart
val PastelLavenderDark = LavenderGradientEnd
val PastelMint = ProteinGradientStart
val PastelMintDark = ProteinGradientEnd
val PastelButtercup = CarbGradientStart
val PastelButtercupDark = CarbGradientEnd
val PastelRose = FatGradientStart
val PastelRoseDark = FatGradientEnd

// 3. Điểm nhấn hành động (Accent)
val VividOrange = Color(0xFFFF6433)              // CTA chính, icon Quét AI, tab active, glow sau số hero
val VividOrangeDark = Color(0xFFE55122)          // Trạng thái pressed
val VividOrangeLight = Color(0xFFFF8F6B)         // Hover/disabled, viền nhấn nhẹ, tip của progress arc
val VividOrangeSoft = Color(0x33FF6433)          // 20% alpha - Nền mờ sau icon/badge
val VividOrangeGlow = Color(0x22FF6433)          // 13% alpha - Halo glow sau số liệu

// 4. Chữ trên nền tối
val TextWhite = Color(0xFFFFFFFF)                // Tiêu đề, số liệu chính
val TextLightGrey = Color(0xFFE1E2E8)            // Nội dung phụ
val TextMuted = Color(0xFF9596A2)                // Nhãn, placeholder
val TextDeepInk = Color(0xFF14151C)              // Chữ chính trên nền thẻ Pastel

// 5. Trạng thái
val EmeraldSuccess = Color(0xFF10B981)
val CoralWarning = Color(0xFFF59E0B)
val CrimsonError = Color(0xFFEF4444)

// ==========================================
// IVORY LUXURY CANVAS (LIGHT MODE) - SPEC 9.4 & 10.6
// ==========================================

val IvoryBackground = Color(0xFFF5F1E8)          // Nền gốc — trắng ngà ấm tạo độ tương phản rõ với card #FFFFFF
val PearlSurface = Color(0xFFFFFFFF)             // Surface — trắng tinh khiết
val PearlCard = Color(0xFFFFFFFF)                // Nền card trắng tinh nổi bật trên nền Ivory
val PearlCardElevated = Color(0xFFFFFFFF)        // Card nổi cao hơn
val PearlBorder = Color(0xFFE8E2D6)              // Viền card ấm 1px
val PearlDock = Color(0xFFFFFFFF)                // Floating bottom dock trắng sáng

// Màu bóng đổ (Elevation Shadow) chuẩn Spec 10.6
val WarmShadow = Color(0x22423320)               // Bóng nâu xám ấm mềm cho Light Mode (14% alpha)
val DarkShadow = Color(0x75000000)               // Bóng đen sâu cho Dark Mode (46% alpha)

// Bento Pastel trên nền sáng — Độ bão hòa tươi 55-70% (Spec 10.6 - Không xỉn màu)
val PastelProteinLight = Color(0xFF10B981)       // Xanh ngọc lục bảo tươi
val PastelProteinTrackLight = Color(0xFFD1FAE5)
val PastelCarbLight = Color(0xFFF59E0B)          // Vàng hổ phách mật ong tươi
val PastelCarbTrackLight = Color(0xFFFEF3C7)
val PastelFatLight = Color(0xFFF43F5E)           // Hồng san hô đậm tươi
val PastelFatTrackLight = Color(0xFFFFE4E6)
val PastelLavenderLight = Color(0xFF8B5CF6)      // Tím oải hương tươi
val PastelLavenderTrackLight = Color(0xFFEDE9FE)

// Brushes cho Bento Macro Light Mode (Tươi tắn, sắc nét)
val ProteinBrushLight = Brush.verticalGradient(listOf(Color(0xFF6EE7B7), Color(0xFF34D399)))
val CarbBrushLight = Brush.verticalGradient(listOf(Color(0xFFFDE68A), Color(0xFFFBBF24)))
val FatBrushLight = Brush.horizontalGradient(listOf(Color(0xFFFDA4AF), Color(0xFFFB7185)))
val LavenderBrushLight = Brush.verticalGradient(listOf(Color(0xFFDDD6FE), Color(0xFFA78BFA)))

val TextInkPrimary = Color(0xFF14151C)           // Chữ chính đậm rõ nét
val TextInkSecondary = Color(0xFF5B5D6B)         // Chữ phụ
val TextInkMuted = Color(0xFF888A98)             // Chữ mờ, placeholder

// Semantic text-on-light
val SuccessTextLight = Color(0xFF047857)
val WarningTextLight = Color(0xFFB45309)
val ErrorTextLight = Color(0xFFDC2626)

// Bí danh tương thích
val MintJade = PastelMint
val ButtercupYellow = PastelButtercup
val RoseBlush = PastelRose
val TextLightGray = TextLightGrey

