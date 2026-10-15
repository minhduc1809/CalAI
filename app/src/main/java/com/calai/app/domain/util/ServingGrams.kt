package com.calai.app.domain.util

import kotlin.math.round

/**
 * Quy đổi khẩu phần ↔ gram (BR-08.2). Số liệu của món tính cho MỘT khẩu phần chuẩn ("1 chén (130g)"); `quantity` là số
 * phần. Khi biết số gram của khẩu phần, người dùng có thể nhập theo gram và app đổi sang số phần. Không đọc được gram
 * thì trả null, không đoán.
 */
object ServingGrams {
    private val IN_PARENS = Regex("""\(([^)]*?)(\d+(?:[.,]\d+)?)\s*(g|gram|ml)\b[^)]*\)""", RegexOption.IGNORE_CASE)
    private val LEADING = Regex("""^(\d+(?:[.,]\d+)?)\s*(g|gram|ml)\b""", RegexOption.IGNORE_CASE)

    const val MIN_GRAMS = 0.5f
    const val MAX_GRAMS = 5000f

    /** Số gram (hoặc ml) của một khẩu phần chuẩn, hoặc null. Khớp cách backend đọc (`parseServing`). */
    fun parse(servingSize: String?): Float? {
        val text = servingSize?.trim().orEmpty()
        if (text.isEmpty()) return null
        val raw = IN_PARENS.find(text)?.groupValues?.get(2) ?: LEADING.find(text)?.groupValues?.get(1)
        val grams = raw?.replace(',', '.')?.toFloatOrNull() ?: return null
        return grams.takeIf { it in MIN_GRAMS..MAX_GRAMS }
    }

    /** Số phần ứng với `grams` gram; null nếu không đổi được. Làm tròn 3 chữ số, giới hạn 0,1–50 như backend. */
    fun portionsForGrams(servingGrams: Float?, grams: Float): Float? {
        if (servingGrams == null || servingGrams <= 0f || grams <= 0f) return null
        val portions = round(grams / servingGrams * 1000f) / 1000f
        return portions.coerceIn(0.1f, 50f)
    }

    fun gramsForPortions(servingGrams: Float?, portions: Float): Float? =
        if (servingGrams == null) null else round(servingGrams * portions * 10f) / 10f
}
