package com.basset.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.basset.R

val IBMPlexSansArabic = FontFamily(
    Font(
        resId = R.font.ibmplexsansarabic_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        resId = R.font.ibmplexsansarabic_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.ibmplexsansarabic_semibold,
        weight = FontWeight.SemiBold,
    ),
    Font(
        resId = R.font.ibmplexsansarabic_bold,
        weight = FontWeight.Bold,
    ),
)

val baseline = Typography()

val Typography = Typography(
    bodyLarge = baseline.bodyLarge.copy(fontFamily = IBMPlexSansArabic),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = IBMPlexSansArabic),
    bodySmall = baseline.bodySmall.copy(fontFamily = IBMPlexSansArabic),
    labelLarge = baseline.labelLarge.copy(fontFamily = IBMPlexSansArabic),
    labelMedium = baseline.labelMedium.copy(fontFamily = IBMPlexSansArabic),
    labelSmall = baseline.labelSmall.copy(fontFamily = IBMPlexSansArabic),
    titleLarge = baseline.titleLarge.copy(fontFamily = IBMPlexSansArabic),
    titleMedium = baseline.titleMedium.copy(fontFamily = IBMPlexSansArabic),
    titleSmall = baseline.titleSmall.copy(fontFamily = IBMPlexSansArabic),
)

