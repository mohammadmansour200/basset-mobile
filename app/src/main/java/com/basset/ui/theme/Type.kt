package com.basset.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.basset.R

val theYearOfTheCamel = FontFamily(
    Font(
        resId = R.font.theyearofhandicrafts_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        resId = R.font.theyearofhandicrafts_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.theyearofhandicrafts_semibold,
        weight = FontWeight.SemiBold,
    ),
    Font(
        resId = R.font.theyearofhandicrafts_bold,
        weight = FontWeight.Bold,
    ),
)

val baseline = Typography()

val Typography = Typography(
    bodyLarge = baseline.bodyLarge.copy(fontFamily = theYearOfTheCamel),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = theYearOfTheCamel),
    bodySmall = baseline.bodySmall.copy(fontFamily = theYearOfTheCamel),
    labelLarge = baseline.labelLarge.copy(fontFamily = theYearOfTheCamel),
    labelMedium = baseline.labelMedium.copy(fontFamily = theYearOfTheCamel),
    labelSmall = baseline.labelSmall.copy(fontFamily = theYearOfTheCamel),
    titleLarge = baseline.titleLarge.copy(fontFamily = theYearOfTheCamel),
    titleMedium = baseline.titleMedium.copy(fontFamily = theYearOfTheCamel),
    titleSmall = baseline.titleSmall.copy(fontFamily = theYearOfTheCamel),
)

