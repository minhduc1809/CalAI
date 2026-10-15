package com.calai.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// ==========================================
// CALAI DARK LUXURY CANVAS (THEME CHÍNH) - SPEC 9.3
// ==========================================

// 1. Nền & Bề mặt — 5 lớp độ sâu (Spec 10.5 - Tinh chỉnh chống bệt đen)
val ObsidianBackground = Color(0xFF050505)       // Nền gốc toàn app (Final v3 - 3.1)
val CharcoalSurface = Color(0xFF1C1C1E)          // Surface — nền section (thoáng hơn #171920)
val CharcoalCard = Color(0xFF282829)             // Nền card chuẩn (tách lớp rõ ràng)
val CharcoalCardElevated = Color(0xFF323234)     // Card nổi cao hơn (hero card, overlay, toggle track)
val CharcoalBorder = Color(0xFF3E3E42)           // Viền 1px sắc nét tách bạch

// 2. Bento Pastel — Gradient 2 tông Đã Hiệu Chỉnh Hài Hòa (Spec 10.4)
// Độ sáng 68-80%, Độ bão hòa 45-60%, êm dịu như bộ macaron cao cấp
val CalorieCardGradientStart = Color(0xFFFFB8B0)
val CalorieCardGradientEnd = Color(0xFFFFD8D0)

val ProteinGradientStart = Color(0xFF8FDCC8)     // Bạc hà dịu
val ProteinGradientEnd = Color(0xFFC3EEE2)

val CarbGradientStart = Color(0xFFF5D58A)        // Vàng mật ong nhạt
val CarbGradientEnd = Color(0xFFFAE8BC)

val FatGradientStart = Color(0xFFB3BAF7)         // Hồng phấn dịu
val FatGradientEnd = Color(0xFFD8DCFB)

val LavenderGradientStart = Color(0xFFC9B5F0)    // Tím nhạt dịu cho selection
val LavenderGradientEnd = Color(0xFFE3D8F8)

// Brush helpers cho Bento Macro
val ProteinBrush = Brush.verticalGradient(listOf(ProteinGradientStart, ProteinGradientEnd))
val CarbBrush = Brush.verticalGradient(listOf(CarbGradientStart, CarbGradientEnd))
val FatBrush = Brush.horizontalGradient(listOf(FatGradientStart, FatGradientEnd))
val LavenderBrush = Brush.verticalGradient(listOf(LavenderGradientStart, LavenderGradientEnd))

// Màu đơn tương thích (Legacy support)
val PastelLavender: Color get() = if (NwTheme.isDark) LavenderGradientStart else PastelLavenderLight   // sáng: bản đậm để chữ/biểu tượng đủ tương phản
val PastelMint: Color get() = if (NwTheme.isDark) ProteinGradientStart else PastelProteinLight   // sáng: bản đậm để chữ/biểu tượng đủ tương phản
val PastelButtercup: Color get() = if (NwTheme.isDark) CarbGradientStart else PastelCarbLight   // sáng: bản đậm để chữ/biểu tượng đủ tương phản
val PastelRose: Color get() = if (NwTheme.isDark) FatGradientStart else PastelFatLight   // sáng: bản đậm để chữ/biểu tượng đủ tương phản

// 3. Điểm nhấn hành động (Accent) — Final v3 Part 3.4
val VividOrange = Color(0xFFFF9B91)              // CTA chính, icon Quét AI, tab active, glow sau số hero (was #FF6433)
val VividOrangeDark = Color(0xFFD96D68)          // Điểm cuối gradient/pressed — ngả đỏ cam rực, KHÔNG pha đen/nâu xỉn như bản cũ (0xFFE07936)
val VividOrangeLight = Color(0xFFFFADA5)         // Hover/disabled, viền nhấn nhẹ, tip của progress arc (derived ~25% lighter)
val VividOrangeSoft = Color(0x33FF9B91)          // 20% alpha - Nền mờ sau icon/badge
val VividOrangeGlow = Color(0x1AFF9B91)          // 13% alpha - Halo glow sau số liệu
val CtaSolidOrange = Color(0xFFFF9B91)           // Màu ĐẶC duy nhất cho nền nút CTA (AppButton) — không gradient chuyển màu dần

// Final v3 Part 3.4 — new solid semantic accents (NOT gradients; distinct from Bento macro card gradients above)
val VividMint = Color(0xFF4FBFA5)                // Protein/Positive, chart: Protein series
val VividAmber = Color(0xFFF2C265)               // Carbs/Attention, chart: Carbs series
val VividCoral = Color(0xFF8F9CF2)               // Fat/Warning, chart: Fat series
val VividCyan = Color(0xFF6FB1F0)                // Hydration/Secondary, chart: Water series

// 4. Chữ trên nền tối — Final v3 Part 3.3
val TextWhite = Color(0xFFFFFFFF)                // Tiêu đề, số liệu chính
val TextLightGrey = Color(0xFFC6C0C0)            // Nội dung phụ
val TextDeepInk = Color(0xFF302B2A)              // Chữ chính trên nền thẻ Pastel

// 5. Trạng thái
val EmeraldSuccess = Color(0xFF2E9E6B)
val CoralWarning = Color(0xFFE09A2B)
val CrimsonError = Color(0xFFE5484D)

// ==========================================
// IVORY LUXURY CANVAS (LIGHT MODE) - SPEC 9.4 & 10.6
// ==========================================

// Final v3 Part 3.2 — Light depth layers
val IvoryBackground = Color(0xFFFFFFFF)          // LightBackground — nền gốc (was #F5F1E8)
val PearlSurface = Color(0xFFFFFDFC)             // LightSurface — trắng tinh khiết
val PearlCard = Color(0xFFFFF5F2)                // LightSurfaceSecondary — nền section phụ (was #FFFFFF)
val PearlCardElevated = Color(0xFFFFFDFC)        // LightSurfaceElevated — card nổi cao hơn
val PearlBorder = Color(0xFFF6E7E2)              // LightBorder — viền card 1px (was #E8E2D6)
val LightDivider = Color(0xFFF8EAE5)             // LightDivider — Final v3 Part 3.2 (new)

// ==========================================
// ELEVATION SHADOW TOKEN SCALE (Graduated L1–L4)
// Base shadow color: #3B2A22 (deep navy, neutral, no harsh black)
// ==========================================
//
// L1 — Inputs / small controls:   very subtle, just lifts from background
// L2 — Standard cards:            soft visible, clear card separation
// L3 — Primary buttons / CTA:     stronger, colored per button brand
// L4 — Modals / floating sheets:  strongest but still diffused, not harsh
//
// Light Mode (IvoryBackground nền) — slightly stronger needed to separate from white
val ShadowLightL1Ambient  = Color(0x143B2A22)    // #3B2A22 @  8% — input fields unfocused
val ShadowLightL1Spot     = Color(0x1A3B2A22)    // #3B2A22 @ 10%
val ShadowLightL2Ambient  = Color(0x2A4A2E28)    // #3B2A22 @ 12% — standard cards
val ShadowLightL2Spot     = Color(0x244A2E28)    // #3B2A22 @ 14%
val ShadowLightL3Ambient  = Color(0x304A2E28)    // #3B2A22 @ 15% — neutral use; buttons use colored shadow
val ShadowLightL3Spot     = Color(0x2A4A2E28)    // #3B2A22 @ 18%
val ShadowLightL4Ambient  = Color(0x2E3B2A22)    // #3B2A22 @ 18% — modals / bottom sheets
val ShadowLightL4Spot     = Color(0x383B2A22)    // #3B2A22 @ 22%

// Dark Mode (ObsidianBackground nền) — slightly less needed; depth comes from surface contrast
val ShadowDarkL1Ambient   = Color(0x0F3B2A22)    // #3B2A22 @  6%
val ShadowDarkL1Spot      = Color(0x143B2A22)    // #3B2A22 @  8%
val ShadowDarkL2Ambient   = Color(0x1A3B2A22)    // #3B2A22 @ 10%
val ShadowDarkL2Spot      = Color(0x1E3B2A22)    // #3B2A22 @ 12%
val ShadowDarkL3Ambient   = Color(0x203B2A22)    // #3B2A22 @ 13%
val ShadowDarkL3Spot      = Color(0x263B2A22)    // #3B2A22 @ 15%
val ShadowDarkL4Ambient   = Color(0x263B2A22)    // #3B2A22 @ 15%
val ShadowDarkL4Spot      = Color(0x2E3B2A22)    // #3B2A22 @ 18%

// Colored button shadows — tinted with brand color so shadow "belongs" to the button
// Primary CTA (VividOrange / CtaSolidOrange)
val ShadowCtaAmbient      = Color(0x40FF9B91)    // CtaSolidOrange @ 25% — ambient (spread)
val ShadowCtaSpot         = Color(0x55FF9B91)    // CtaSolidOrange @ 33% — spot (directional)
val ShadowCtaDisabled     = Color(0x1AFF9B91)    // CtaSolidOrange @ 10% — disabled state

// Secondary / Google button — neutral light shadow

// Deprecated aliases — kept for callers not yet migrated; do NOT use in new code
// TODO: remove once all callsites migrate to graduated scale above
// Nâng từ L2→L3: card trước đó bóng quá nhẹ (12%/10%) nên "chìm" vào nền sáng, khó phân biệt
// ranh giới/bo góc. L3 (15%/13%) vẫn mềm (không phải shadow đen nặng) nhưng tách bạch rõ hơn hẳn.
val WarmShadow = ShadowLightL3Ambient
val DarkShadow = ShadowDarkL3Ambient

// Bento Pastel trên nền sáng — Độ bão hòa tươi 55-70% (Spec 10.6 - Không xỉn màu)
val PastelProteinLight = Color(0xFF2FA58C)       // Xanh ngọc lục bảo tươi
val PastelCarbLight = Color(0xFFD9A441)          // Vàng hổ phách mật ong tươi
val PastelFatLight = Color(0xFF6F7DE0)           // Hồng san hô đậm tươi
val PastelLavenderLight = Color(0xFF9C7FDB)      // Tím oải hương tươi

// Gradient 2 tông Bento Macro Light Mode — dùng làm nguồn DUY NHẤT cho brush lẫn màu đơn
val ProteinGradientStartLight = Color(0xFF9BE0CF)
val ProteinGradientEndLight = Color(0xFF4FBFA5)
val CarbGradientStartLight = Color(0xFFF5D58A)
val CarbGradientEndLight = Color(0xFFF2C265)
val FatGradientStartLight = Color(0xFFC3C9F9)
val FatGradientEndLight = Color(0xFF8F9CF2)
val LavenderGradientStartLight = Color(0xFFDCCFF5)
val LavenderGradientEndLight = Color(0xFFB69AE8)

// Brushes cho Bento Macro Light Mode (Tươi tắn, sắc nét)
val ProteinBrushLight = Brush.verticalGradient(listOf(ProteinGradientStartLight, ProteinGradientEndLight))
val CarbBrushLight = Brush.verticalGradient(listOf(CarbGradientStartLight, CarbGradientEndLight))
val FatBrushLight = Brush.horizontalGradient(listOf(FatGradientStartLight, FatGradientEndLight))

// Final v3 Part 3.3
val InkLightPrimary = Color(0xFF302B2A)
val InkDarkPrimary = Color(0xFFFFFFFF)
val TextInkPrimary: Color get() = if (NwTheme.isDark) InkDarkPrimary else InkLightPrimary   // tự đảo theo giao diện
val InkLightSecondary = Color(0xFF817674)
val InkDarkSecondary = Color(0xFFC6C0C0)
val TextInkSecondary: Color get() = if (NwTheme.isDark) InkDarkSecondary else InkLightSecondary
val InkLightMuted = Color(0xFF9C918E)
val InkDarkMuted = Color(0xFF969090)

// Semantic text-on-light
val ErrorTextLight = Color(0xFFC23B3B)

// Bí danh tương thích
val MintJade: Color get() = PastelMint



// ==========================================
// NUTRIWISE — BẢNG MÀU CAM ĐÀO (Light: Bright Peach · Dark: Midnight Peach)
// ==========================================
/** Accent sáng dùng cho chữ/vòng/biểu tượng nhấn trên nền TỐI (đạt 9:1 trên #151515). Nút bấm vẫn dùng VividOrange cho đồng nhất. */
val PeachOnDark = Color(0xFFFFADA5)
/** Chữ/biểu tượng đặt trên mảng PeachOnDark. */
/** Nền peach rất nhạt (vùng nhấn trên sáng) và nền peach tối (vùng nhấn trên tối). */
val PeachMist = Color(0xFFFFF5F2)
val PeachSoft = Color(0xFFFFD8D0)
val PeachDarkTint = Color(0xFF35221F)

/**
 * Cờ giao diện hiện tại cho các token cần đảo theo sáng/tối (chữ ink). Là snapshot state nên mọi chỗ đọc
 * (kể cả trong khối vẽ Canvas) tự cập nhật khi đổi giao diện. Do CalAITheme đặt; không ghi từ nơi khác.
 */
object NwTheme {
    var isDark by androidx.compose.runtime.mutableStateOf(false)
}

/** Chữ/biểu tượng đặt TRÊN nền nút Peach Pink (#FF9B91): mực tối, tương phản ~8:1. Không dùng chữ trắng trên nút. */
val OnPrimary = Color(0xFF2B1B19)

/**
 * Peach Pink dùng làm CHỮ/biểu tượng nhấn trên nền: sáng = Primary Strong (#D96D68, đọc được trên trắng),
 * tối = Primary Bright (#FFADA5, đọc được trên nền đen). Màu nút (VividOrange) quá nhạt để làm chữ trên nền sáng.
 */
val PrimaryStrong: Color get() = if (NwTheme.isDark) Color(0xFFFFADA5) else Color(0xFFD96D68)
