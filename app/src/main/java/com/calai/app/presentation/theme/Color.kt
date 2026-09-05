package com.calai.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// CALAI DARK LUXURY CANVAS (THEME CHÍNH) - SPEC 9.3
// ==========================================

// 1. Nền & Bề mặt — 5 lớp độ sâu
val ObsidianBackground = Color(0xFF0D0E12)       // Nền gốc toàn app
val CharcoalSurface = Color(0xFF171920)          // Surface — nền màn hình/section
val CharcoalCard = Color(0xFF20232B)             // Nền card thường
val CharcoalCardElevated = Color(0xFF262A34)     // Card/modal nổi cao hơn (bottom sheet, hero card, overlay)
val CharcoalBorder = Color(0xFF2B2E3A)           // Viền 1px, divider
val CharcoalDock = Color(0xFF242630)             // Floating bottom dock

// 2. Bento Pastel — Gradient 2 tông (Đá quý)
val ProteinGradientStart = Color(0xFF34D399)
val ProteinGradientEnd = Color(0xFF76CBA3)

val CarbGradientStart = Color(0xFFE0B93A)
val CarbGradientEnd = Color(0xFFE5D266)

val FatGradientStart = Color(0xFFEC7FA6)
val FatGradientEnd = Color(0xFFE89BB6)

val LavenderGradientStart = Color(0xFFA78BFA)    // Chỉ dùng cho trạng thái "đang chọn" (lịch tuần...)
val LavenderGradientEnd = Color(0xFFB8A7EA)

// Brush helpers cho Bento Macro
val ProteinBrush = Brush.verticalGradient(listOf(ProteinGradientStart, ProteinGradientEnd))
val CarbBrush = Brush.verticalGradient(listOf(CarbGradientStart, CarbGradientEnd))
val FatBrush = Brush.horizontalGradient(listOf(FatGradientStart, FatGradientEnd))
val LavenderBrush = Brush.verticalGradient(listOf(LavenderGradientStart, LavenderGradientEnd))

// Màu đơn tương thích (Legacy support)
val PastelLavender = Color(0xFFDCD3F6)
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
// IVORY LUXURY CANVAS (LIGHT MODE) - SPEC 9.4
// ==========================================

val IvoryBackground = Color(0xFFFAF8F5)          // Nền gốc — trắng ngà ấm
val PearlSurface = Color(0xFFFFFFFF)             // Surface
val PearlCard = Color(0xFFF3F1EC)                // Nền card
val PearlCardElevated = Color(0xFFFFFFFF)        // Card nổi
val PearlBorder = Color(0xFFE4E1D9)              // Viền
val PearlDock = Color(0xFFFFFFFF)                // Dock

// Bento Pastel trên nền sáng (Đậm hơn để không bị chìm)
val PastelProteinLight = Color(0xFF34D399)
val PastelProteinTrackLight = Color(0xFFA9EAC9)
val PastelCarbLight = Color(0xFFE0B93A)
val PastelCarbTrackLight = Color(0xFFF5DD6B)
val PastelFatLight = Color(0xFFEC7FA6)
val PastelFatTrackLight = Color(0xFFF5B8CE)
val PastelLavenderLight = Color(0xFFA78BFA)
val PastelLavenderTrackLight = Color(0xFFD9CBFA)

val TextInkPrimary = Color(0xFF14151C)
val TextInkSecondary = Color(0xFF5B5D6B)
val TextInkMuted = Color(0xFF9596A2)

// Semantic text-on-light
val SuccessTextLight = Color(0xFF047857)
val WarningTextLight = Color(0xFFB45309)
val ErrorTextLight = Color(0xFFDC2626)

// Bí danh tương thích
val MintJade = PastelMint
val ButtercupYellow = PastelButtercup
val RoseBlush = PastelRose
val TextLightGray = TextLightGrey

