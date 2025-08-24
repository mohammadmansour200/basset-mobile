package com.basset.home.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basset.R

@Composable
fun HintCard(
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .widthIn(max = 500.dp)
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\uD83D\uDCA1",
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(end = 12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            val hints = listOf(R.string.hint_1, R.string.hint_2, R.string.hint_3, R.string.hint_4)
            Text(
                text = stringResource(hints[(0..hints.size - 1).random()]),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        }
    }
}