package com.calai.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// CALAI DARK LUXURY CANVAS (THEME CHÍNH) - SPEC 9.3
// ==========================================

// 1. Nền & Bề mặt — 5 lớp độ sâu (Spec 10.5 - Tinh chỉnh chống bệt đen)
val ObsidianBackground = Color(0xFF0B0D12)       // Nền gốc toàn app (Final v3 - 3.1)
val CharcoalSurface = Color(0xFF1B1E26)          // Surface — nền section (thoáng hơn #171920)
val CharcoalCard = Color(0xFF242833)             // Nền card chuẩn (tách lớp rõ ràng)
val CharcoalCardElevated = Color(0xFF2D3240)     // Card nổi cao hơn (hero card, overlay, toggle track)
val CharcoalBorder = Color(0xFF383D4D)           // Viền 1px sắc nét tách bạch
val CharcoalDock = Color(0xFF2D3240)             // Floating bottom dock (Final v3 - align to CharcoalCardElevated)

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

// 3. Điểm nhấn hành động (Accent) — Final v3 Part 3.4
val VividOrange = Color(0xFFFF8A3D)              // CTA chính, icon Quét AI, tab active, glow sau số hero (was #FF6433)
val VividOrangeDark = Color(0xFFE07936)          // Trạng thái pressed (derived ~12% darker of new base)
val VividOrangeLight = Color(0xFFFFA76E)         // Hover/disabled, viền nhấn nhẹ, tip của progress arc (derived ~25% lighter)
val VividOrangeSoft = Color(0x33FF8A3D)          // 20% alpha - Nền mờ sau icon/badge
val VividOrangeGlow = Color(0x22FF8A3D)          // 13% alpha - Halo glow sau số liệu

// Final v3 Part 3.4 — new solid semantic accents (NOT gradients; distinct from Bento macro card gradients above)
val BrandBlue = Color(0xFF4F7CFF)                // Navigation/Action, chart: Calories series
val VividMint = Color(0xFF20D6A3)                // Protein/Positive, chart: Protein series
val VividAmber = Color(0xFFFFB84D)               // Carbs/Attention, chart: Carbs series
val VividCoral = Color(0xFFFF6B6B)               // Fat/Warning, chart: Fat series
val VividPurple = Color(0xFF9B7CFF)              // AI/Premium, chart: AI series
val VividCyan = Color(0xFF39C8FF)                // Hydration/Secondary, chart: Water series

// 4. Chữ trên nền tối — Final v3 Part 3.3
val TextWhite = Color(0xFFF8FAFF)                // Tiêu đề, số liệu chính
val TextLightGrey = Color(0xFFC2CAD9)            // Nội dung phụ
val TextMuted = Color(0xFF8792A7)                // Nhãn, placeholder
val TextDeepInk = Color(0xFF14151C)              // Chữ chính trên nền thẻ Pastel

// 5. Trạng thái
val EmeraldSuccess = Color(0xFF10B981)
val CoralWarning = Color(0xFFF59E0B)
val CrimsonError = Color(0xFFEF4444)

// ==========================================
// IVORY LUXURY CANVAS (LIGHT MODE) - SPEC 9.4 & 10.6
// ==========================================

// Final v3 Part 3.2 — Light depth layers
val IvoryBackground = Color(0xFFF6F8FC)          // LightBackground — nền gốc (was #F5F1E8)
val PearlSurface = Color(0xFFFFFFFF)             // LightSurface — trắng tinh khiết
val PearlCard = Color(0xFFF0F3F8)                // LightSurfaceSecondary — nền section phụ (was #FFFFFF)
val PearlCardElevated = Color(0xFFFFFFFF)        // LightSurfaceElevated — card nổi cao hơn
val PearlBorder = Color(0xFFE3E8F1)              // LightBorder — viền card 1px (was #E8E2D6)
val PearlDock = Color(0xFFFFFFFF)                // Floating bottom dock trắng sáng
val LightDivider = Color(0xFFEDF0F5)             // LightDivider — Final v3 Part 3.2 (new)

// Màu bóng đổ (Elevation Shadow) — Final v3 Part 3.2: LightShadow #1A2540 @ 8% alpha
val WarmShadow = Color(0x141A2540)               // Bóng cho Light Mode (8% alpha, was 14% #423320)
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

// Gradient 2 tông Bento Macro Light Mode — dùng làm nguồn DUY NHẤT cho brush lẫn màu đơn
val ProteinGradientStartLight = Color(0xFF6EE7B7)
val ProteinGradientEndLight = Color(0xFF34D399)
val CarbGradientStartLight = Color(0xFFFDE68A)
val CarbGradientEndLight = Color(0xFFFBBF24)
val FatGradientStartLight = Color(0xFFFDA4AF)
val FatGradientEndLight = Color(0xFFFB7185)
val LavenderGradientStartLight = Color(0xFFDDD6FE)
val LavenderGradientEndLight = Color(0xFFA78BFA)

// Brushes cho Bento Macro Light Mode (Tươi tắn, sắc nét)
val ProteinBrushLight = Brush.verticalGradient(listOf(ProteinGradientStartLight, ProteinGradientEndLight))
val CarbBrushLight = Brush.verticalGradient(listOf(CarbGradientStartLight, CarbGradientEndLight))
val FatBrushLight = Brush.horizontalGradient(listOf(FatGradientStartLight, FatGradientEndLight))
val LavenderBrushLight = Brush.verticalGradient(listOf(LavenderGradientStartLight, LavenderGradientEndLight))

// Final v3 Part 3.3
val TextInkPrimary = Color(0xFF111827)           // Chữ chính đậm rõ nét (was #14151C)
val TextInkSecondary = Color(0xFF596579)         // Chữ phụ (was #5B5D6B)
val TextInkMuted = Color(0xFF8B95A7)             // Chữ mờ, placeholder (was #888A98)

// Semantic text-on-light
val SuccessTextLight = Color(0xFF047857)
val WarningTextLight = Color(0xFFB45309)
val ErrorTextLight = Color(0xFFDC2626)

// Bí danh tương thích
val MintJade = PastelMint
val ButtercupYellow = PastelButtercup
val RoseBlush = PastelRose
val TextLightGray = TextLightGrey

